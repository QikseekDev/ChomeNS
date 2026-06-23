/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.commands;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.Main;
import me.chayapak1.chomens_bot.command.Command;
import me.chayapak1.chomens_bot.command.CommandContext;
import me.chayapak1.chomens_bot.command.CommandException;
import me.chayapak1.chomens_bot.command.TrustLevel;
import me.chayapak1.chomens_bot.command.contexts.DiscordCommandContext;
import me.chayapak1.chomens_bot.data.chat.ChatPacketType;
import me.chayapak1.chomens_bot.util.HttpUtilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class UrbanCommand
extends Command {
    public final AtomicInteger requestsPerSecond = new AtomicInteger();

    public UrbanCommand() {
        super("urban", new String[]{"<term>"}, new String[0], TrustLevel.PUBLIC, false, new ChatPacketType[]{ChatPacketType.DISGUISED});
        Main.EXECUTOR.scheduleAtFixedRate(() -> this.requestsPerSecond.set(0), 0L, 1L, TimeUnit.SECONDS);
    }

    @Override
    public Component execute(CommandContext context) throws CommandException {
        if (this.requestsPerSecond.get() > 3) {
            throw new CommandException(Component.translatable("commands.urban.error.too_many_requests"));
        }
        Bot bot = context.bot;
        boolean discord = context instanceof DiscordCommandContext;
        String term = context.getString(true, true);
        Gson gson = new Gson();
        bot.executorService.execute(() -> {
            try {
                URL url = new URI("https://api.urbandictionary.com/v0/define?term=" + URLEncoder.encode(term, StandardCharsets.UTF_8)).toURL();
                String jsonOutput = HttpUtilities.getRequest(url);
                JsonObject jsonObject = gson.fromJson(jsonOutput, JsonObject.class);
                JsonArray list = jsonObject.getAsJsonArray("list");
                if (list.isEmpty()) {
                    context.sendOutput(Component.translatable("commands.urban.error.no_results", (TextColor)NamedTextColor.RED));
                }
                Component discordComponent = Component.translatable("commands.urban.discord_warning").append(Component.newline());
                int count = 0;
                int index = 1;
                for (JsonElement element : list) {
                    if (count >= 3) break;
                    JsonObject definitionObject = element.getAsJsonObject();
                    String word = definitionObject.get("word").getAsString();
                    String originalDefinition = definitionObject.get("definition").getAsString();
                    DecimalFormat formatter = new DecimalFormat("#,###");
                    String author = definitionObject.get("author").getAsString();
                    String thumbsUp = formatter.format(definitionObject.get("thumbs_up").getAsInt());
                    String thumbsDown = formatter.format(definitionObject.get("thumbs_down").getAsInt());
                    String example = definitionObject.get("example").getAsString();
                    Component definitionComponent = Component.empty();
                    String definition = originalDefinition.replaceAll("\r\n?", "\n");
                    String[] splitDefinition = definition.split("[\\[\\]]");
                    for (int i = 0; i < splitDefinition.length; ++i) {
                        boolean even = i % 2 == 0;
                        String wordWithDefinition = word + " - " + definition;
                        TranslatableComponent globalHoverEvent = Component.translatable("commands.urban.hover.info", Component.text(author, bot.colorPalette.string), Component.text(thumbsUp, (TextColor)NamedTextColor.GREEN), Component.text(thumbsDown, (TextColor)NamedTextColor.RED), Component.text(example.replaceAll("\r\n?", "\n"), bot.colorPalette.string));
                        if (even) {
                            definitionComponent = definitionComponent.append(((TextComponent)Component.text(splitDefinition[i], (TextColor)NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(globalHoverEvent.append(Component.newline()).append(Component.translatable("commands.urban.hover.copy", (TextColor)NamedTextColor.GREEN))))).clickEvent(ClickEvent.copyToClipboard(wordWithDefinition)));
                            continue;
                        }
                        String command = context.prefix + this.name + " " + splitDefinition[i];
                        definitionComponent = definitionComponent.append(((TextComponent)((TextComponent)((TextComponent)Component.text(splitDefinition[i]).style(Style.style(TextDecoration.UNDERLINED))).hoverEvent(HoverEvent.showText(globalHoverEvent.append(Component.newline()).append(Component.translatable("commands.urban.hover.run", (TextColor)NamedTextColor.GREEN, Component.text(command)))))).clickEvent(ClickEvent.suggestCommand(command))).color(NamedTextColor.AQUA));
                    }
                    if (discord) {
                        discordComponent = discordComponent.append(Component.translatable("%s - %s", (TextColor)NamedTextColor.DARK_GRAY, Component.text(word, (TextColor)NamedTextColor.GRAY), definitionComponent)).append(Component.newline());
                        ++count;
                    } else {
                        TranslatableComponent component = Component.translatable("[%s] %s - %s", (TextColor)NamedTextColor.DARK_GRAY, Component.text(index, (TextColor)NamedTextColor.GREEN), Component.text(word, (TextColor)NamedTextColor.GRAY), definitionComponent);
                        context.sendOutput(component);
                    }
                    ++index;
                }
                if (discord && !list.isEmpty()) {
                    context.sendOutput(discordComponent);
                }
            }
            catch (Exception e) {
                bot.logger.error(e);
                context.sendOutput(Component.text(e.toString(), (TextColor)NamedTextColor.RED));
            }
        });
        this.requestsPerSecond.getAndIncrement();
        return null;
    }
}

