/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.plugins;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.socket.client.IO;
import io.socket.client.Socket;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.Main;
import me.chayapak1.chomens_bot.data.eval.EvalFunction;
import me.chayapak1.chomens_bot.data.eval.EvalOutput;
import me.chayapak1.chomens_bot.evalFunctions.ChatFunction;
import me.chayapak1.chomens_bot.evalFunctions.CoreFunction;
import me.chayapak1.chomens_bot.evalFunctions.CorePlaceBlockFunction;
import me.chayapak1.chomens_bot.evalFunctions.GetBotInfoFunction;
import me.chayapak1.chomens_bot.evalFunctions.GetPlayerListFunction;
import me.chayapak1.chomens_bot.util.LoggerUtilities;

public class EvalPlugin {
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(2, new ThreadFactoryBuilder().setNameFormat("ExecutorService (eval)").build());
    private static final String BRIDGE_PREFIX = "function:";
    public static final List<EvalFunction> FUNCTIONS = ObjectList.of(new EvalFunction[]{new CoreFunction(), new CorePlaceBlockFunction(), new ChatFunction(), new GetPlayerListFunction(), new GetBotInfoFunction()});
    private static final Gson GSON = new Gson();
    private static Socket socket = null;
    public static boolean connected = false;
    private static boolean initialized = false;
    private static final AtomicInteger transactionId = new AtomicInteger();
    private static final Map<Integer, CompletableFuture<EvalOutput>> futures = new Object2ObjectOpenHashMap<Integer, CompletableFuture<EvalOutput>>();
    private final Bot bot;

    public static void connect(String address) {
        try {
            socket = IO.socket(address);
        }
        catch (Exception e) {
            LoggerUtilities.error(e);
            return;
        }
        JsonArray functionsArray = new JsonArray();
        for (Bot bot : Main.bots) {
            String server = bot.getServerString(true);
            for (EvalFunction function : FUNCTIONS) {
                JsonObject object = new JsonObject();
                object.addProperty("name", function.name);
                object.addProperty("server", server);
                functionsArray.add(object);
            }
        }
        socket.on("codeOutput", outputArgs -> {
            if (outputArgs.length < 3) {
                return;
            }
            try {
                int id = (Integer)outputArgs[0];
                boolean isError = (Boolean)outputArgs[1];
                String output = (String)outputArgs[2];
                if (!futures.containsKey(id)) {
                    return;
                }
                CompletableFuture<EvalOutput> future = futures.remove(id);
                future.complete(new EvalOutput(isError, output));
            }
            catch (ClassCastException | NumberFormatException runtimeException) {
                // empty catch block
            }
        });
        socket.on("connect", args2 -> {
            connected = true;
            socket.emit("setFunctions", GSON.toJson(functionsArray));
            if (initialized) {
                return;
            }
            initialized = true;
            for (Bot bot : Main.bots) {
                for (EvalFunction function : FUNCTIONS) {
                    socket.on(BRIDGE_PREFIX + function.name + ":" + bot.getServerString(true), functionArgs -> {
                        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor)EXECUTOR_SERVICE;
                        if (threadPoolExecutor.getQueue().size() > 75) {
                            return;
                        }
                        EXECUTOR_SERVICE.execute(() -> {
                            try {
                                EvalFunction.Output output = function.execute(bot, functionArgs);
                                if (output == null) {
                                    return;
                                }
                                socket.emit("functionOutput:" + function.name, output.message(), output.parseJSON());
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                        });
                    });
                }
            }
        });
        socket.on("disconnect", args2 -> {
            connected = false;
        });
        socket.on("connect_error", args2 -> {
            connected = false;
        });
        socket.connect();
    }

    public EvalPlugin(Bot bot) {
        this.bot = bot;
    }

    public CompletableFuture<EvalOutput> run(String code) {
        CompletableFuture<EvalOutput> future = new CompletableFuture<EvalOutput>();
        if (!connected) {
            return null;
        }
        socket.emit("runCode", this.bot.getServerString(true), transactionId.get(), code);
        futures.put(transactionId.getAndIncrement(), future);
        return future;
    }

    public static void reset() {
        if (!connected) {
            return;
        }
        socket.emit("reset", new Object[0]);
    }
}

