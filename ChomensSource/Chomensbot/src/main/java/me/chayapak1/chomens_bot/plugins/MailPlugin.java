/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.plugins;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.Main;
import me.chayapak1.chomens_bot.command.CommandException;
import me.chayapak1.chomens_bot.data.listener.Listener;
import me.chayapak1.chomens_bot.data.mail.Mail;
import me.chayapak1.chomens_bot.data.player.PlayerEntry;
import me.chayapak1.chomens_bot.plugins.DatabasePlugin;
import me.chayapak1.chomens_bot.util.LoggerUtilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class MailPlugin
implements Listener {
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS mails (sentBy VARCHAR(255), sentTo VARCHAR(255), timeSent BIGINT, server VARCHAR(255), contents TEXT);";
    private static final String INSERT_MAIL = "INSERT INTO mails (sentBy, sentTo, timeSent, server, contents) VALUES (?, ?, ?, ?, ?);";
    private static final String LIST_MAILS = "SELECT * FROM mails;";
    private static final String REMOVE_MAIL = "DELETE FROM mails WHERE sentTo = ?;";
    private final Bot bot;

    public MailPlugin(Bot bot) {
        this.bot = bot;
        if (Main.database == null) {
            return;
        }
        bot.listener.addListener(this);
    }

    @Override
    public void onPlayerJoined(PlayerEntry target) {
        DatabasePlugin.EXECUTOR_SERVICE.execute(() -> {
            String name = target.profile.getName();
            int sendToTargetSize = 0;
            List<Mail> mails = this.list();
            for (Mail mail : mails) {
                if (!mail.sentTo().equals(name)) continue;
                ++sendToTargetSize;
            }
            if (sendToTargetSize > 0) {
                TranslatableComponent component = Component.translatable("You have %s new mail%s!\nRun %s or %s to read", (TextColor)NamedTextColor.GOLD, Component.text(sendToTargetSize, (TextColor)NamedTextColor.GREEN), Component.text(sendToTargetSize > 1 ? "s" : ""), Component.text(this.bot.config.commandSpyPrefixes.getFirst() + "mail read", this.bot.colorPalette.primary), Component.text(this.bot.config.prefixes.getFirst() + "mail read", this.bot.colorPalette.primary));
                this.bot.chat.tellraw((Component)component, target.profile.getId());
            }
        });
    }

    public void send(Mail mail) throws CommandException {
        List<Mail> mails = this.list();
        int count = 0;
        for (Mail eachMail : mails) {
            if (!eachMail.sentBy().equals(mail.sentBy())) continue;
            if (count > 50) {
                throw new CommandException(Component.translatable("commands.mail.error.spam"));
            }
            ++count;
        }
        try {
            PreparedStatement statement = Main.database.connection.prepareStatement(INSERT_MAIL);
            statement.setString(1, mail.sentBy());
            statement.setString(2, mail.sentTo());
            statement.setLong(3, mail.timeSent());
            statement.setString(4, mail.server());
            statement.setString(5, mail.contents());
            statement.executeUpdate();
        }
        catch (SQLException e) {
            this.bot.logger.error(e);
        }
    }

    public void clear(String sentTo) {
        try {
            PreparedStatement statement = Main.database.connection.prepareStatement(REMOVE_MAIL);
            statement.setString(1, sentTo);
            statement.executeUpdate();
        }
        catch (SQLException e) {
            this.bot.logger.error(e);
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public List<Mail> list() {
        ArrayList<Mail> output = new ArrayList<Mail>();
        try (ResultSet result = Main.database.query(LIST_MAILS);){
            if (result == null) {
                ArrayList<Mail> arrayList = output;
                return arrayList;
            }
            while (result.next()) {
                Mail mail = new Mail(result.getString("sentBy"), result.getString("sentTo"), result.getLong("timeSent"), result.getString("server"), result.getString("contents"));
                output.add(mail);
            }
            return output;
        }
        catch (SQLException e) {
            this.bot.logger.error(e);
        }
        return output;
    }

    static {
        if (Main.database != null) {
            DatabasePlugin.EXECUTOR_SERVICE.execute(() -> {
                try {
                    Main.database.execute(CREATE_TABLE);
                }
                catch (SQLException e) {
                    LoggerUtilities.error(e);
                }
            });
        }
    }
}

