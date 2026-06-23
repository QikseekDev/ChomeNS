/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.KeybindComponent;
import net.kyori.adventure.text.SelectorComponent;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.ComponentEncoder;
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.ansi.ColorLevel;

public class ComponentUtilities {
    private static final List<String> LANGUAGES = List.of("minecraftLanguage.json", "voiceChatLanguage.json");
    public static final Map<String, String> LANGUAGE = new Object2ObjectOpenHashMap<String, String>();
    public static final Map<String, String> KEYBINDINGS = ComponentUtilities.loadJsonStringMap("keybinds.json");
    private static final Pattern ARG_PATTERN;
    private static final Pattern DISCORD_ANSI_PATTERN;
    private static final ThreadLocal<Integer> TOTAL_PLACEHOLDERS;
    private static final int PLACEHOLDER_THRESHOLD = 4;
    private static final long MAX_PARSE_TIME = 200L;
    private static final ThreadLocal<Long> START_PARSE_TIME;
    private static final ANSIComponentSerializer TRUE_COLOR_ANSI_SERIALIZER;
    private static final ANSIComponentSerializer DISCORD_ANSI_SERIALIZER;
    private static final LegacyComponentSerializer LEGACY_COMPONENT_SERIALIZER;
    private static final PlainTextComponentSerializer PLAIN_TEXT_COMPONENT_SERIALIZER;

    private static Map<String, String> loadJsonStringMap(String name) {
        Object2ObjectOpenHashMap<String, String> map = new Object2ObjectOpenHashMap<String, String>();
        InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(name);
        assert (is != null);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            map.put(entry.getKey(), json.get(entry.getKey()).getAsString());
        }
        return map;
    }

    private static ComponentFlattener getFlattener(boolean shouldReplaceSectionSignsWithANSI, boolean isDiscord) {
        return (ComponentFlattener)ComponentFlattener.builder().mapper(TextComponent.class, component -> ComponentUtilities.mapText(component, shouldReplaceSectionSignsWithANSI, isDiscord)).complexMapper(KeybindComponent.class, ComponentUtilities::mapKeybind).mapper(SelectorComponent.class, SelectorComponent::pattern).complexMapper(TranslatableComponent.class, ComponentUtilities::mapTranslatable).unknownMapper(component -> "<Unhandled component type: " + component.getClass().getSimpleName() + ">").build();
    }

    public static String getOrReturnFallback(TranslatableComponent component) {
        String key = component.key();
        String fallback = component.fallback();
        return LANGUAGE.getOrDefault(key, fallback != null ? fallback : key);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static String guardedStringify(ComponentEncoder<Component, String> serializer, Component message) {
        try {
            START_PARSE_TIME.set(System.currentTimeMillis());
            String string = serializer.serialize(message);
            return string;
        }
        catch (Throwable throwable) {
            String string = ComponentUtilities.guardedStringify(serializer, Component.translatable("<Failed to parse component: %s>", (TextColor)NamedTextColor.RED, Component.text(throwable.toString())));
            return string;
        }
        finally {
            TOTAL_PLACEHOLDERS.set(0);
        }
    }

    public static String stringify(Component message) {
        return ComponentUtilities.guardedStringify(PLAIN_TEXT_COMPONENT_SERIALIZER, message);
    }

    public static String stringifyLegacy(Component message) {
        return ComponentUtilities.guardedStringify(LEGACY_COMPONENT_SERIALIZER, message);
    }

    public static String stringifyAnsi(Component message) {
        return ComponentUtilities.guardedStringify(TRUE_COLOR_ANSI_SERIALIZER, message);
    }

    public static String stringifyDiscordAnsi(Component message) {
        return ComponentUtilities.guardedStringify(DISCORD_ANSI_SERIALIZER, message).replace("\u001b[9", "\u001b[3");
    }

    public static String deserializeFromDiscordAnsi(String original) {
        Matcher matcher = DISCORD_ANSI_PATTERN.matcher(original);
        StringBuilder builder = new StringBuilder();
        while (matcher.find()) {
            ANSIStyle[] values2;
            String match = matcher.group();
            boolean replaced = false;
            for (ANSIStyle value : values2 = ANSIStyle.values()) {
                if (!value.discordAnsiCode.equals(match)) continue;
                matcher.appendReplacement(builder, "\u00a7" + value.legacyCode);
                replaced = true;
                break;
            }
            if (replaced) continue;
            matcher.appendReplacement(builder, match);
        }
        matcher.appendTail(builder);
        return builder.toString();
    }

    private static String mapText(TextComponent component, boolean shouldReplaceSectionSignsWithANSI, boolean isDiscord) {
        String content = component.content();
        if (!shouldReplaceSectionSignsWithANSI || !content.contains("\u00a7")) {
            return component.content();
        }
        String formatting = LEGACY_COMPONENT_SERIALIZER.serialize(component.content(" ").children(List.of())).trim();
        TextComponent deserialized = LEGACY_COMPONENT_SERIALIZER.deserialize(content.replaceAll("\u00a7[^a-f0-9rlonmk]", "").replace("\u00a7r", formatting).replaceFirst("\u00a7+$", ""));
        return (isDiscord ? ComponentUtilities.stringifyDiscordAnsi(deserialized) : ComponentUtilities.stringifyAnsi(deserialized)) + ComponentUtilities.mapText(Component.text(formatting), true, isDiscord);
    }

    private static void mapKeybind(KeybindComponent component, Consumer<Component> consumer) {
        consumer.accept(Component.translatable(KEYBINDINGS.getOrDefault(component.keybind(), component.keybind())));
    }

    private static void mapTranslatable(TranslatableComponent component, Consumer<Component> consumer) {
        String format = ComponentUtilities.getOrReturnFallback(component);
        Matcher matcher = ARG_PATTERN.matcher(format);
        ArrayList<Component> result = new ArrayList<Component>();
        try {
            int i = 0;
            int lastIndex = 0;
            while (matcher.find(lastIndex)) {
                int start = matcher.start();
                int end = matcher.end();
                if (start > lastIndex) {
                    String formatSegment = format.substring(lastIndex, start);
                    if (formatSegment.indexOf(37) != -1) {
                        throw new IllegalArgumentException();
                    }
                    result.add(Component.text(formatSegment));
                }
                String full = format.substring(start, end);
                if (matcher.group().equals("%") && full.equals("%%")) {
                    result.add(Component.text('%'));
                } else if (matcher.group(2).equals("s")) {
                    int idx;
                    String idxStr = matcher.group(1);
                    int n = idx = idxStr == null ? i++ : Integer.parseInt(idxStr) - 1;
                    if (idx < 0 || idx > component.arguments().size()) {
                        throw new IllegalArgumentException();
                    }
                    int currentTotalPlaceholders = TOTAL_PLACEHOLDERS.get();
                    if (currentTotalPlaceholders > 4 && System.currentTimeMillis() - START_PARSE_TIME.get() > 200L) {
                        return;
                    }
                    TOTAL_PLACEHOLDERS.set(currentTotalPlaceholders + 1);
                    result.add(component.arguments().get(idx).asComponent());
                } else {
                    throw new IllegalArgumentException();
                }
                lastIndex = end;
            }
            if (lastIndex < format.length()) {
                String remaining = format.substring(lastIndex);
                if (remaining.indexOf(37) != -1) {
                    throw new IllegalArgumentException();
                }
                result.add(Component.text(remaining));
            }
        }
        catch (Throwable throwable) {
            result.clear();
            result.add(Component.text(format));
        }
        consumer.accept(Component.join(JoinConfiguration.noSeparators(), result));
    }

    static {
        for (String language : LANGUAGES) {
            LANGUAGE.putAll(ComponentUtilities.loadJsonStringMap(language));
        }
        ARG_PATTERN = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)");
        DISCORD_ANSI_PATTERN = Pattern.compile("(\\u001b\\[\\d+m)");
        TOTAL_PLACEHOLDERS = ThreadLocal.withInitial(() -> 0);
        START_PARSE_TIME = ThreadLocal.withInitial(() -> 0L);
        TRUE_COLOR_ANSI_SERIALIZER = ANSIComponentSerializer.builder().flattener(ComponentUtilities.getFlattener(true, false)).colorLevel(ColorLevel.TRUE_COLOR).build();
        DISCORD_ANSI_SERIALIZER = ANSIComponentSerializer.builder().flattener(ComponentUtilities.getFlattener(true, true)).colorLevel(ColorLevel.INDEXED_8).build();
        LEGACY_COMPONENT_SERIALIZER = LegacyComponentSerializer.builder().flattener(ComponentUtilities.getFlattener(false, false)).build();
        PLAIN_TEXT_COMPONENT_SERIALIZER = (PlainTextComponentSerializer)PlainTextComponentSerializer.builder().flattener(ComponentUtilities.getFlattener(false, false)).build();
    }

    public static enum ANSIStyle {
        GREEN("a", "\u001b[32m"),
        AQUA("b", "\u001b[36m"),
        RED("c", "\u001b[31m"),
        LIGHT_PURPLE("d", "\u001b[35m"),
        YELLOW("e", "\u001b[33m"),
        WHITE("f", "\u001b[37m"),
        BLACK("0", "\u001b[30m"),
        DARK_RED("1", "\u001b[34m"),
        DARK_GREEN("2", "\u001b[32m"),
        GOLD("3", "\u001b[36m"),
        DARK_BLUE("4", "\u001b[31m"),
        DARK_PURPLE("5", "\u001b[35m"),
        DARK_AQUA("6", "\u001b[33m"),
        GRAY("7", "\u001b[37m"),
        DARK_GRAY("8", "\u001b[30m"),
        BLUE("9", "\u001b[34m"),
        BOLD("l", "\u001b[1m"),
        ITALIC("o", "\u001b[3m"),
        UNDERLINED("n", "\u001b[4m"),
        STRIKETHROUGH("m", "\u001b[9m"),
        OBFUSCATED("k", "\u001b[6m"),
        RESET("r", "\u001b[0m");

        private final String legacyCode;
        private final String discordAnsiCode;

        private ANSIStyle(String legacyCode, String discordAnsiCode) {
            this.legacyCode = legacyCode;
            this.discordAnsiCode = discordAnsiCode;
        }
    }
}

