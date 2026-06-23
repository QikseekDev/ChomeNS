/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.command.contexts;

import java.awt.Color;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.command.CommandContext;
import me.chayapak1.chomens_bot.data.player.PlayerEntry;
import me.chayapak1.chomens_bot.util.CodeBlockUtilities;
import me.chayapak1.chomens_bot.util.ComponentUtilities;
import me.chayapak1.chomens_bot.util.I18nUtilities;
import me.chayapak1.chomens_bot.util.UUIDUtilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.FileUpload;
import net.kyori.adventure.text.Component;
import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;

public class DiscordCommandContext
extends CommandContext {
    public final Member member;
    public final String name;
    public final Consumer<FileUpload> replyFiles;
    public final Consumer<MessageEmbed> replyEmbed;
    private final Bot bot;

    public DiscordCommandContext(Bot bot, String prefix, Member member, String name, Consumer<FileUpload> replyFiles, Consumer<MessageEmbed> replyEmbed) {
        super(bot, prefix, new PlayerEntry(new GameProfile(UUIDUtilities.getOfflineUUID(name), name), GameMode.SURVIVAL, -69420, Component.text(name), 0L, null, new byte[0], true), false);
        this.bot = bot;
        this.member = member;
        this.name = name;
        this.replyFiles = replyFiles;
        this.replyEmbed = replyEmbed;
    }

    @Override
    public void sendOutput(Component component) {
        Component rendered = I18nUtilities.render(component);
        String output = ComponentUtilities.stringifyDiscordAnsi(rendered);
        if (output.length() > 2048) {
            output = ComponentUtilities.stringify(rendered);
            this.replyFiles.accept(FileUpload.fromData(output.getBytes(StandardCharsets.UTF_8), String.format("output-%d.txt", System.currentTimeMillis())));
        } else {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("Output");
            builder.setColor(Color.decode(this.bot.config.discord.embedColors.normal));
            builder.setDescription("```ansi\n" + CodeBlockUtilities.escape(output) + "\n```");
            MessageEmbed embed = builder.build();
            this.replyEmbed.accept(embed);
        }
    }

    @Override
    public Component displayName() {
        return Component.text(this.name);
    }
}

