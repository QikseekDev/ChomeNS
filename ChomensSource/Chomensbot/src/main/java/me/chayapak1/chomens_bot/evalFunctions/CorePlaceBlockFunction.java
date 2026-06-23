/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.evalFunctions;

import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.eval.EvalFunction;

public class CorePlaceBlockFunction
extends EvalFunction {
    public CorePlaceBlockFunction() {
        super("corePlaceBlock");
    }

    @Override
    public EvalFunction.Output execute(Bot bot, Object ... args2) {
        if (args2.length == 0) {
            return null;
        }
        String command = (String)args2[0];
        bot.core.runPlaceBlock(command);
        return null;
    }
}

