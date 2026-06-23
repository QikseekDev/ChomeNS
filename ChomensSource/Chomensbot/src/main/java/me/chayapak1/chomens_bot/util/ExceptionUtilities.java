/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class ExceptionUtilities {
    public static String getStacktrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter((Writer)sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }
}

