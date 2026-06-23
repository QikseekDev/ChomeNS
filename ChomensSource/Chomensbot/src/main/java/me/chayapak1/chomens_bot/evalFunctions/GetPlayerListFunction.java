/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.evalFunctions;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.eval.EvalFunction;
import me.chayapak1.chomens_bot.data.player.PlayerEntry;
import me.chayapak1.chomens_bot.util.SNBTUtilities;

public class GetPlayerListFunction
extends EvalFunction {
    public GetPlayerListFunction() {
        super("getPlayerList");
    }

    @Override
    public EvalFunction.Output execute(Bot bot, Object ... args2) {
        List<PlayerEntry> list = bot.players.list;
        JsonArray array = new JsonArray();
        for (PlayerEntry entry : list) {
            JsonObject object = new JsonObject();
            object.addProperty("uuid", entry.profile.getIdAsString());
            object.addProperty("username", entry.profile.getName());
            if (entry.displayName != null) {
                object.addProperty("displayName", SNBTUtilities.fromComponent(false, entry.displayName));
            }
            array.add(object);
        }
        return new EvalFunction.Output(array.toString(), true);
    }
}

