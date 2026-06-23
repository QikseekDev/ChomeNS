/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.evalFunctions;

import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.eval.EvalFunction;

public class ChatFunction
extends EvalFunction {
    public ChatFunction() {
        super("chat");
    }

    @Override
    public EvalFunction.Output execute(Bot bot, Object ... args2) {
        if (args2.length == 0) {
            return null;
        }
        String message = (String)args2[0];
        bot.chat.send(message);
        return null;
    }
}

