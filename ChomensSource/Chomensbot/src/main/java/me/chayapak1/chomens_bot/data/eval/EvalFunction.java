/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.data.eval;

import me.chayapak1.chomens_bot.Bot;

public class EvalFunction {
    public final String name;

    public EvalFunction(String name) {
        this.name = name;
    }

    public Output execute(Bot bot, Object ... args2) throws Exception {
        return null;
    }

    public record Output(String message, boolean parseJSON) {
    }
}

