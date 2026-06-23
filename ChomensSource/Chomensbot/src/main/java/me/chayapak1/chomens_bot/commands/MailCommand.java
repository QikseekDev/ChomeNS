/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.commands;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.Main;
import me.chayapak1.chomens_bot.command.Command;
import me.chayapak1.chomens_bot.command.CommandContext;
import me.chayapak1.chomens_bot.command.CommandException;
import me.chayapak1.chomens_bot.command.TrustLevel;
import me.chayapak1.chomens_bot.data.chat.ChatPacketType;
import me.chayapak1.chomens_bot.data.mail.Mail;
import me.chayapak1.chomens_bot.data.player.PlayerEntry;
import me.chayapak1.chomens_bot.plugins.DatabasePlugin;
import me.chayapak1.chomens_bot.util.I18nUtilities;
import me.chayapak1.chomens_bot.util.TimeUtilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class MailCommand
extends Command {
    public MailCommand() {
        super("mail", new String[]{"send <player> <message>", "sendselecteditem <player>", "read"}, new String[0], TrustLevel.PUBLIC, false, new ChatPacketType[]{ChatPacketType.DISGUISED});
    }

    @Override
    public Component execute(CommandContext context) throws CommandException {
        String action;
        Bot bot = context.bot;
        if (Main.database == null) {
            throw new CommandException(Component.translatable("commands.generic.error.database_disabled"));
        }
        Main.database.checkOverloaded();
        PlayerEntry sender = context.sender;
        switch (action = context.getAction()) {
            case "send": {
                DatabasePlugin.EXECUTOR_SERVICE.execute(() -> {
                    try {
                        bot.mail.send(new Mail(sender.profile.getName(), context.getString(false, true), Instant.now().toEpochMilli(), bot.getServerString(), context.getString(true, true)));
                        context.sendOutput(Component.translatable("commands.mail.sent", bot.colorPalette.defaultColor));
                    }
                    catch (CommandException e) {
                        context.sendOutput(e.message.colorIfAbsent(NamedTextColor.RED));
                    }
                });
                break;
            }
            case "sendselecteditem": {
                context.checkOverloadArgs(2);
                CompletableFuture<String> future = bot.query.entity(context.sender.profile.getIdAsString(), "SelectedItem.components.minecraft:custom_data.message");
                future.thenApply(output -> {
                    try {
                        if (output == null) {
                            throw new CommandException(Component.translatable("commands.mail.sendselecteditem.error.no_item_nbt"));
                        }
                        DatabasePlugin.EXECUTOR_SERVICE.execute(() -> {
                            try {
                                bot.mail.send(new Mail(sender.profile.getName(), context.getString(true, true), Instant.now().toEpochMilli(), bot.getServerString(), (String)output));
                                context.sendOutput(Component.translatable("commands.mail.sent", bot.colorPalette.defaultColor));
                            }
                            catch (CommandException e) {
                                context.sendOutput(e.message.colorIfAbsent(NamedTextColor.RED));
                            }
                        });
                    }
                    catch (CommandException e) {
                        context.sendOutput(e.message.colorIfAbsent(NamedTextColor.RED));
                        return null;
                    }
                    return output;
                });
                break;
            }
            case "read": {
                context.checkOverloadArgs(1);
                DatabasePlugin.EXECUTOR_SERVICE.execute(() -> {
                    List<Mail> mails = bot.mail.list();
                    int senderMailSize = 0;
                    for (Mail mail : mails) {
                        if (!mail.sentTo().equals(sender.profile.getName())) continue;
                        ++senderMailSize;
                    }
                    if (senderMailSize == 0) {
                        context.sendOutput(Component.translatable("commands.mail.read.no_new_mails", (TextColor)NamedTextColor.RED));
                        return;
                    }
                    int tempFinalSenderMailSize = senderMailSize;
                    ArrayList<TranslatableComponent> mailsComponent = new ArrayList<TranslatableComponent>();
                    int count = 1;
                    for (Mail mail : mails) {
                        if (!mail.sentTo().equals(sender.profile.getName())) continue;
                        String formattedTime = TimeUtilities.formatTime(mail.timeSent(), "MMMM d, yyyy, hh:mm:ss a Z", ZoneId.of("UTC"));
                        mailsComponent.add(Component.translatable("commands.mail.read.mail_contents", (TextColor)NamedTextColor.GREEN, new ComponentLike[]{Component.text(count, bot.colorPalette.number), Component.text("-", (TextColor)NamedTextColor.DARK_GRAY), Component.text(mail.sentBy(), bot.colorPalette.username), Component.translatable("commands.mail.read.hover_more_info", (TextColor)NamedTextColor.GREEN).hoverEvent(HoverEvent.showText(Component.translatable("commands.mail.read.hover_info", (TextColor)NamedTextColor.GREEN, Component.text(formattedTime, bot.colorPalette.string), Component.text(mail.server(), bot.colorPalette.string)))), Component.text(mail.contents(), (TextColor)NamedTextColor.WHITE)}));
                        ++count;
                    }
                    Component component = ((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)Component.empty().append(Component.translatable("commands.mail.read.mails_text", (TextColor)NamedTextColor.GREEN))).append(Component.text("(", (TextColor)NamedTextColor.DARK_GRAY))).append(Component.text(tempFinalSenderMailSize, (TextColor)NamedTextColor.GRAY))).append(Component.text(")", (TextColor)NamedTextColor.DARK_GRAY))).append(Component.newline())).append(Component.join(JoinConfiguration.newlines(), mailsComponent));
                    if (context.inGame) {
                        Component renderedComponent = I18nUtilities.render(component);
                        bot.chat.tellraw(renderedComponent, context.sender.profile.getId());
                    } else {
                        context.sendOutput(component);
                    }
                    bot.mail.clear(sender.profile.getName());
                });
                break;
            }
            default: {
                throw new CommandException(Component.translatable("commands.generic.error.invalid_action"));
            }
        }
        return null;
    }
}

