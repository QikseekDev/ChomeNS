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
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.command.Command;
import me.chayapak1.chomens_bot.command.CommandContext;
import me.chayapak1.chomens_bot.command.CommandException;
import me.chayapak1.chomens_bot.command.TrustLevel;
import me.chayapak1.chomens_bot.data.chat.ChatPacketType;
import me.chayapak1.chomens_bot.util.HTMLUtilities;
import me.chayapak1.chomens_bot.util.HttpUtilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class WikipediaCommand
extends Command {
    public static final String pageIDStringURL = "https://en.wikipedia.org/w/api.php?prop=info%%7Cpageprops&inprop=url&ppprop=disambiguation&titles=%s&format=json&redirects=&action=query&origin=*&";
    public static final String outputStringURL = "https://en.wikipedia.org/w/api.php?prop=extracts&exintro=&pageids=%d&format=json&redirects=&action=query&origin=*&";

    public WikipediaCommand() {
        super("wikipedia", new String[]{"<page>"}, new String[]{"wiki"}, TrustLevel.PUBLIC, false, new ChatPacketType[]{ChatPacketType.DISGUISED});
    }

    @Override
    public Component execute(CommandContext context) throws CommandException {
        Bot bot = context.bot;
        String page = context.getString(true, true);
        Gson gson = new Gson();
        bot.executorService.execute(() -> {
            try {
                JsonObject pages;
                int pageID;
                Component component = Component.empty();
                URL pageIDUrl = new URI(String.format(pageIDStringURL, URLEncoder.encode(page, StandardCharsets.UTF_8))).toURL();
                JsonObject pageIDJsonOutput = gson.fromJson(HttpUtilities.getRequest(pageIDUrl), JsonObject.class);
                JsonObject query = pageIDJsonOutput.getAsJsonObject("query");
                JsonElement redirectsElement = query.get("redirects");
                if (redirectsElement != null) {
                    JsonArray normalized = redirectsElement.getAsJsonArray();
                    for (JsonElement element : normalized) {
                        JsonObject redirect = element.getAsJsonObject();
                        String redirectedTo = redirect.get("to").getAsString();
                        component = component.append(Component.translatable("Redirected to %s").arguments(Component.text(redirectedTo)).style(Style.style().decorate(TextDecoration.ITALIC).color(NamedTextColor.GRAY))).append(Component.newline());
                    }
                }
                if ((pageID = Integer.parseInt((pages = query.getAsJsonObject("pages")).entrySet().iterator().next().getKey())) == -1) {
                    throw new CommandException(Component.translatable("commands.wikipedia.error.not_found", Component.text(page)));
                }
                URL outputUrl = new URI(String.format(outputStringURL, pageID)).toURL();
                JsonObject outputJsonOutput = gson.fromJson(HttpUtilities.getRequest(outputUrl), JsonObject.class);
                JsonObject pageOutput = outputJsonOutput.getAsJsonObject("query").getAsJsonObject("pages").getAsJsonObject(String.valueOf(pageID));
                String title = pageOutput.get("title").getAsString();
                String extracted = HTMLUtilities.toFormattingCodes(pageOutput.get("extract").getAsString());
                if (extracted == null) {
                    throw new CommandException(Component.translatable("commands.wikipedia.error.no_contents"));
                }
                component = component.append(Component.text(title).style(Style.style().decorate(TextDecoration.BOLD).color(bot.colorPalette.secondary))).append(Component.newline()).append(Component.text(extracted, (TextColor)NamedTextColor.GREEN));
                context.sendOutput(component);
            }
            catch (NumberFormatException e) {
                context.sendOutput(Component.translatable("commands.wikipedia.error.fail_page_id_parse", (TextColor)NamedTextColor.RED));
                bot.logger.error(e);
            }
            catch (CommandException e) {
                context.sendOutput(e.message.color(NamedTextColor.RED));
            }
            catch (Exception e) {
                context.sendOutput(Component.text(e.toString(), (TextColor)NamedTextColor.RED));
            }
        });
        return null;
    }
}

