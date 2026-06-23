/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.util;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTMLUtilities {
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<\\s*(/)?\\s*(\\w+).*?>|<!--.*?-->|\n", 32);
    private static final String CODE_COLOR = "\u00a72";
    private static final String DEFAULT_COLOR = "\u00a7a";

    public static String toFormattingCodes(String html) {
        Matcher matcher = HTML_TAG_PATTERN.matcher(html);
        StringBuilder raw = new StringBuilder();
        boolean bold = false;
        boolean italic = false;
        boolean underline = false;
        boolean code = false;
        int listIndex = -1;
        while (matcher.find()) {
            matcher.appendReplacement(raw, "");
            boolean endTag = matcher.group(1) != null;
            String tagName = matcher.group(2);
            if (tagName == null) continue;
            tagName = tagName.toLowerCase(Locale.ENGLISH);
            if (!endTag) {
                switch (tagName) {
                    case "b": {
                        raw.append("\u00a7l");
                        bold = true;
                        break;
                    }
                    case "i": {
                        raw.append("\u00a7o");
                        italic = true;
                        break;
                    }
                    case "u": 
                    case "dt": {
                        raw.append("\u00a7n");
                        underline = true;
                        break;
                    }
                    case "code": {
                        raw.append(CODE_COLOR);
                        if (bold) {
                            raw.append("\u00a7l");
                        }
                        if (italic) {
                            raw.append("\u00a7o");
                        }
                        if (underline) {
                            raw.append("\u00a7n");
                        }
                        code = true;
                        break;
                    }
                    case "dd": {
                        raw.append("  ");
                        break;
                    }
                    case "ul": {
                        listIndex = 0;
                        break;
                    }
                    case "ol": {
                        listIndex = 1;
                        break;
                    }
                    case "li": {
                        if (listIndex >= 1) {
                            raw.append("  ").append(listIndex).append(". ");
                            ++listIndex;
                            break;
                        }
                        raw.append("  \u2022 ");
                        break;
                    }
                    case "br": {
                        raw.append("\n");
                    }
                }
                continue;
            }
            switch (tagName) {
                case "b": {
                    if (code) {
                        raw.append(CODE_COLOR);
                    } else {
                        raw.append(DEFAULT_COLOR);
                    }
                    if (italic) {
                        raw.append("\u00a7o");
                    }
                    if (underline) {
                        raw.append("\u00a7n");
                    }
                    bold = false;
                    break;
                }
                case "i": {
                    if (code) {
                        raw.append(CODE_COLOR);
                    } else {
                        raw.append(DEFAULT_COLOR);
                    }
                    if (bold) {
                        raw.append("\u00a7l");
                    }
                    if (underline) {
                        raw.append("\u00a7n");
                    }
                    italic = false;
                    break;
                }
                case "dt": {
                    raw.append("\n");
                }
                case "u": {
                    if (code) {
                        raw.append(CODE_COLOR);
                    } else {
                        raw.append(DEFAULT_COLOR);
                    }
                    if (bold) {
                        raw.append("\u00a7l");
                    }
                    if (italic) {
                        raw.append("\u00a7o");
                    }
                    underline = false;
                    break;
                }
                case "code": {
                    raw.append(DEFAULT_COLOR);
                    if (bold) {
                        raw.append("\u00a7l");
                    }
                    if (italic) {
                        raw.append("\u00a7o");
                    }
                    if (underline) {
                        raw.append("\u00a7n");
                    }
                    code = false;
                    break;
                }
                case "ul": 
                case "ol": {
                    listIndex = -1;
                    break;
                }
                case "dd": 
                case "li": 
                case "br": 
                case "p": {
                    raw.append("\n");
                }
            }
        }
        matcher.appendTail(raw);
        if (raw.isEmpty()) {
            return null;
        }
        String rawStr = raw.toString();
        rawStr = rawStr.replace("&quot;", "\"");
        rawStr = rawStr.replace("&#39;", "'");
        rawStr = rawStr.replace("&lt;", "<");
        rawStr = rawStr.replace("&gt;", ">");
        rawStr = rawStr.replace("&amp;", "&");
        return rawStr;
    }
}

