/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.command.CommandException;
import me.chayapak1.chomens_bot.command.TrustLevel;
import me.chayapak1.chomens_bot.data.player.PlayerEntry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class CommandContext {
    public static final Component UNKNOWN_ARGUMENT_COMPONENT = Component.text("???").style(Style.style(TextDecoration.UNDERLINED));
    private static final Pattern FLAGS_PATTERN = Pattern.compile("^\\s*(?:--|-)([^\\s0-9]\\S*)");
    public final Bot bot;
    public final String prefix;
    public final PlayerEntry sender;
    public final boolean inGame;
    public TrustLevel trustLevel = TrustLevel.PUBLIC;
    public String commandName = null;
    public String userInputCommandName = null;
    public String[] fullArgs;
    public String[] args;
    private int argsPosition = 0;

    public CommandContext(Bot bot, String prefix, PlayerEntry sender, boolean inGame) {
        this.bot = bot;
        this.prefix = prefix;
        this.sender = sender;
        this.inGame = inGame;
    }

    public Component displayName() {
        return Component.empty();
    }

    public void sendOutput(Component component) {
    }

    public String getString(boolean greedy, boolean required) throws CommandException {
        return this.getString(greedy, required, "string");
    }

    public String getString(boolean greedy, boolean required, boolean returnLowerCase) throws CommandException {
        return this.getString(greedy, returnLowerCase, required, "string");
    }

    private String getString(boolean greedy, boolean required, String type) throws CommandException {
        return this.getString(greedy, false, required, type);
    }

    private String getString(boolean greedy, boolean returnLowerCase, boolean required, String type) throws CommandException {
        if (this.argsPosition >= this.args.length || this.args[this.argsPosition] == null) {
            if (required) {
                throw new CommandException(Component.translatable("arguments_parsing.error.expected_string", Component.text(type), Component.text(this.argsPosition), Component.text(this.prefix + this.userInputCommandName), this.argsPosition == 0 ? UNKNOWN_ARGUMENT_COMPONENT : ((TextComponent)Component.text(String.join((CharSequence)" ", this.args)).append(Component.space())).append(UNKNOWN_ARGUMENT_COMPONENT), this.inGame ? Component.space().append(Component.translatable("[%s]").arguments(Component.translatable("arguments_parsing.hover.usages").clickEvent(ClickEvent.suggestCommand(this.prefix + "help " + this.commandName)))) : Component.empty()));
            }
            return "";
        }
        String greedyString = String.join((CharSequence)" ", Arrays.copyOfRange(this.args, this.argsPosition, this.args.length));
        StringBuilder string = new StringBuilder();
        if (greedy) {
            string.append(greedyString);
        } else if (greedyString.length() > 1 && (greedyString.startsWith("'") || greedyString.startsWith("\""))) {
            char quote = greedyString.charAt(0);
            int pointer = 1;
            while (true) {
                if (pointer >= greedyString.length()) {
                    if (greedyString.charAt(pointer - 1) != quote) {
                        throw new CommandException(Component.translatable("arguments_parsing.error.unterminated_quote").arguments(Component.text(greedyString, this.bot.colorPalette.string), Component.text(quote, (TextColor)NamedTextColor.YELLOW)));
                    }
                } else {
                    char character = greedyString.charAt(pointer);
                    ++pointer;
                    if (character == ' ') {
                        ++this.argsPosition;
                    }
                    if (character == '\\') {
                        if (pointer >= greedyString.length()) {
                            throw new CommandException(Component.translatable("arguments_parsing.error.unterminated_escape").arguments(Component.text(greedyString).color(this.bot.colorPalette.string)));
                        }
                        char nextCharacter = greedyString.charAt(pointer);
                        char toAdd = switch (nextCharacter) {
                            case 'n' -> '\n';
                            case 't' -> '\t';
                            case 'r' -> '\r';
                            default -> nextCharacter;
                        };
                        string.append(toAdd);
                        ++pointer;
                        continue;
                    }
                    if (character != quote) {
                        string.append(character);
                        continue;
                    }
                }
                break;
            }
        } else {
            string.append(this.args[this.argsPosition]);
        }
        ++this.argsPosition;
        String result = string.toString();
        return returnLowerCase ? result.toLowerCase() : result;
    }

    public String getAction() throws CommandException {
        return this.getString(false, true, true, "action");
    }

    public List<String> getFlags(String ... allowedFlags) throws CommandException {
        return this.getFlags(false, allowedFlags);
    }

    public List<String> getFlags(boolean returnLowerCase, String ... allowedFlags) throws CommandException {
        ArrayList<String> flags = new ArrayList<String>();
        String flag = this.getFlag(returnLowerCase, allowedFlags);
        while (flag != null) {
            flags.add(flag);
            flag = this.getFlag(returnLowerCase, allowedFlags);
        }
        return flags;
    }

    private String getFlag(boolean returnLowerCase, String[] allowedFlagsArray) throws CommandException {
        String match;
        List<String> allowedFlags = Arrays.asList(allowedFlagsArray);
        String string = this.getString(false, false, returnLowerCase);
        if (string.isBlank()) {
            return null;
        }
        Matcher matcher = FLAGS_PATTERN.matcher(string);
        if (matcher.find() && allowedFlags.contains(match = matcher.group(1))) {
            return match;
        }
        --this.argsPosition;
        return null;
    }

    public Integer getInteger(boolean required) throws CommandException {
        String string = this.getString(false, required, "integer");
        if (string.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(string);
        }
        catch (NumberFormatException e) {
            throw new CommandException(Component.translatable("arguments_parsing.error.invalid_type", Component.text("integer")));
        }
    }

    public Long getLong(boolean required) throws CommandException {
        String string = this.getString(false, required, "long");
        if (string.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(string);
        }
        catch (NumberFormatException e) {
            throw new CommandException(Component.translatable("arguments_parsing.error.invalid_type", Component.text("long")));
        }
    }

    public Double getDouble(boolean required, boolean allowInfinite) throws CommandException {
        String string = this.getString(false, required, "double");
        if (string.isEmpty()) {
            return null;
        }
        try {
            double parsedDouble = Double.parseDouble(string);
            if (!Double.isFinite(parsedDouble) && !allowInfinite) {
                throw new NumberFormatException();
            }
            return parsedDouble;
        }
        catch (NumberFormatException e) {
            throw new CommandException(Component.translatable("arguments_parsing.error.invalid_type", Component.text("double")));
        }
    }

    public Float getFloat(boolean required, boolean allowInfinite) throws CommandException {
        String string = this.getString(false, required, "float");
        if (string.isEmpty()) {
            return null;
        }
        try {
            float parsedFloat = Float.parseFloat(string);
            if (!Float.isFinite(parsedFloat) && !allowInfinite) {
                throw new NumberFormatException();
            }
            return Float.valueOf(parsedFloat);
        }
        catch (NumberFormatException e) {
            throw new CommandException(Component.translatable("arguments_parsing.error.invalid_type", Component.text("float")));
        }
    }

    public Boolean getBoolean(boolean required) throws CommandException {
        String string = this.getString(false, required, "boolean");
        if (string.isEmpty()) {
            return null;
        }
        return switch (string) {
            case "true" -> true;
            case "false" -> false;
            default -> throw new CommandException(Component.translatable("arguments_parsing.error.invalid_type", Component.text("boolean")));
        };
    }

    public <T extends Enum<T>> T getEnum(boolean required, Class<T> enumClass) throws CommandException {
        String string = this.getString(false, required, enumClass.getSimpleName());
        if (string.isEmpty()) {
            return null;
        }
        try {
            return Enum.valueOf(enumClass, string.toUpperCase());
        }
        catch (IllegalArgumentException | NullPointerException e) {
            Object[] values2 = (Enum[])enumClass.getEnumConstants();
            throw new CommandException(Component.translatable("arguments_parsing.error.invalid_enum", Component.text(enumClass.getSimpleName()), Component.text(Arrays.toString(values2))));
        }
    }

    public void checkOverloadArgs(int maximumArgs) throws CommandException {
        int count;
        String joined = String.join((CharSequence)" ", this.args);
        String quotesReplaced = joined.replaceAll("([\"'])(?:\\.|(?!\u0001).)*\u0001", "i");
        int n = count = quotesReplaced.isBlank() ? 0 : quotesReplaced.split("\\s+").length;
        if (count > maximumArgs) {
            throw new CommandException(Component.translatable("arguments_parsing.error.too_many_arguments", Component.text(maximumArgs)));
        }
    }
}

