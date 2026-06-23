/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.evalFunctions;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.eval.EvalFunction;
import me.chayapak1.chomens_bot.util.SNBTUtilities;
import net.kyori.adventure.text.Component;

public class CoreFunction
extends EvalFunction {
    private long lastExecutionTime = System.currentTimeMillis();

    public CoreFunction() {
        super("core");
    }

    @Override
    public EvalFunction.Output execute(Bot bot, Object ... args2) throws Exception {
        if (args2.length == 0) {
            return null;
        }
        if (System.currentTimeMillis() - this.lastExecutionTime < 50L) {
            return null;
        }
        this.lastExecutionTime = System.currentTimeMillis();
        String command = (String)args2[0];
        CompletableFuture<Component> future = bot.core.runTracked(command);
        return new EvalFunction.Output(SNBTUtilities.fromComponent(false, future.get(1L, TimeUnit.SECONDS)), true);
    }
}

