/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.Map;
import java.util.Set;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.json.JSONOptions;

public class SNBTUtilities {
    private static final GsonComponentSerializer SERIALIZER_1_21_4 = GsonComponentSerializer.builder().options(JSONOptions.byDataVersion().at(4174)).build();
    private static final GsonComponentSerializer SERIALIZER_1_21_6 = GsonComponentSerializer.builder().options(JSONOptions.byDataVersion().at(4422)).build();
    private static final String QUOTE = "'";
    private static final String COMMA = ",";
    private static final String BEGIN_OBJECT = "{";
    private static final String COLON = ":";
    private static final String END_OBJECT = "}";
    private static final String BEGIN_ARRAY = "[";
    private static final String END_ARRAY = "]";

    public static String fromComponent(boolean useSNBTComponents, Component component) {
        if (!useSNBTComponents) {
            return (String)SERIALIZER_1_21_4.serialize(component);
        }
        return SNBTUtilities.fromJson(SERIALIZER_1_21_6.serializeToTree(component));
    }

    private static String fromJson(JsonElement json) {
        if (json.isJsonPrimitive()) {
            JsonPrimitive primitive = json.getAsJsonPrimitive();
            if (primitive.isString()) {
                return SNBTUtilities.needQuotes(primitive.getAsString()) ? QUOTE + SNBTUtilities.escapeString(primitive.getAsString()) + QUOTE : primitive.getAsString();
            }
            if (primitive.isBoolean()) {
                return primitive.getAsBoolean() ? "1b" : "0b";
            }
            if (primitive.isNumber()) {
                return String.valueOf(primitive.getAsNumber());
            }
        } else {
            if (json.isJsonArray()) {
                StringBuilder stringBuilder = new StringBuilder(BEGIN_ARRAY);
                JsonArray array = json.getAsJsonArray();
                int i = 1;
                for (JsonElement element : array) {
                    stringBuilder.append(SNBTUtilities.fromJson(element));
                    if (i++ == array.size()) continue;
                    stringBuilder.append(COMMA);
                }
                stringBuilder.append(END_ARRAY);
                return stringBuilder.toString();
            }
            if (json.isJsonObject()) {
                StringBuilder stringBuilder = new StringBuilder(BEGIN_OBJECT);
                Set<Map.Entry<String, JsonElement>> entries = json.getAsJsonObject().entrySet();
                int i = 1;
                for (Map.Entry<String, JsonElement> entry : entries) {
                    stringBuilder.append(entry.getKey()).append(COLON).append(SNBTUtilities.fromJson(entry.getValue()));
                    if (i++ == entries.size()) continue;
                    stringBuilder.append(COMMA);
                }
                stringBuilder.append(END_OBJECT);
                return stringBuilder.toString();
            }
        }
        return "''";
    }

    private static String escapeString(String string) {
        return string.replace("\\", "\\\\").replace(QUOTE, "\\'").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t").replace("\b", "\\b").replace("\f", "\\f");
    }

    public static boolean needQuotes(String string) {
        if (string == null || string.isBlank() || string.equalsIgnoreCase("true") || string.equalsIgnoreCase("false")) {
            return true;
        }
        char firstChar = string.charAt(0);
        if (Character.isDigit(firstChar) || firstChar == '.' || firstChar == '-' || firstChar == '+') {
            return true;
        }
        for (int i = 0; i < string.length(); ++i) {
            if (SNBTUtilities.isAllowedChar(string.charAt(i))) continue;
            return true;
        }
        return false;
    }

    private static boolean isAllowedChar(char c) {
        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '_' || c == '-' || c == '.';
    }
}

