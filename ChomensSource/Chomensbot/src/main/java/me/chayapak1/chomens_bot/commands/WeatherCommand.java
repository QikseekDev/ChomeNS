/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.commands;

import com.google.gson.Gson;
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
import me.chayapak1.chomens_bot.util.HttpUtilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class WeatherCommand
extends Command {
    public WeatherCommand() {
        super("weather", new String[]{"<location>"}, new String[0], TrustLevel.PUBLIC);
    }

    @Override
    public Component execute(CommandContext context) throws CommandException {
        Bot bot = context.bot;
        String location = context.getString(true, true);
        Gson gson = new Gson();
        try {
            URL url = new URI("https://api.weatherapi.com/v1/current.json?key=" + bot.config.weatherApiKey + "&q=" + URLEncoder.encode(location, StandardCharsets.UTF_8)).toURL();
            String jsonOutput = HttpUtilities.getRequest(url);
            JsonObject jsonObject = gson.fromJson(jsonOutput, JsonObject.class);
            return Component.translatable("commands.weather.info", bot.colorPalette.defaultColor, Component.text(jsonObject.get("location").getAsJsonObject().get("name").getAsString(), bot.colorPalette.string), Component.text(jsonObject.get("location").getAsJsonObject().get("country").getAsString(), bot.colorPalette.string), Component.empty().append(Component.text(jsonObject.get("current").getAsJsonObject().get("temp_c").getAsString() + "\u00b0C").color(bot.colorPalette.secondary)), Component.text(jsonObject.get("current").getAsJsonObject().get("temp_f").getAsString() + "\u00b0F").color(bot.colorPalette.secondary), Component.text(jsonObject.get("current").getAsJsonObject().get("feelslike_c").getAsString() + "\u00b0C").color(NamedTextColor.GREEN), Component.text(jsonObject.get("current").getAsJsonObject().get("feelslike_f").getAsString() + "\u00b0F").color(NamedTextColor.GREEN), Component.text(jsonObject.get("current").getAsJsonObject().get("condition").getAsJsonObject().get("text").getAsString()).color(bot.colorPalette.string), ((TextComponent)Component.text(jsonObject.get("current").getAsJsonObject().get("cloud").getAsInt()).append(Component.text("%"))).color(bot.colorPalette.number), ((TextComponent)Component.text(jsonObject.get("current").getAsJsonObject().get("humidity").getAsInt()).append(Component.text("%"))).color(bot.colorPalette.number), Component.text(jsonObject.get("location").getAsJsonObject().get("localtime").getAsString()).color(bot.colorPalette.string));
        }
        catch (Exception e) {
            throw new CommandException(Component.translatable("commands.weather.error.not_found", Component.text(location)));
        }
    }
}

