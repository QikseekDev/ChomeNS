/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.plugins;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.Main;
import me.chayapak1.chomens_bot.data.filter.PlayerFilter;
import me.chayapak1.chomens_bot.data.listener.Listener;
import me.chayapak1.chomens_bot.data.player.PlayerEntry;
import me.chayapak1.chomens_bot.plugins.DatabasePlugin;
import me.chayapak1.chomens_bot.util.LoggerUtilities;

public class PlayerFilterPlugin
implements Listener {
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS filters (name VARCHAR(255) PRIMARY KEY, reason VARCHAR(2048), regex BOOLEAN, ignoreCase BOOLEAN);";
    private static final String LIST_FILTERS = "SELECT * FROM filters;";
    private static final String INSERT_FILTER = "INSERT INTO filters (name, reason, regex, ignoreCase) VALUES (?, ?, ?, ?);";
    private static final String REMOVE_FILTER = "DELETE FROM filters WHERE name = ?;";
    private static final String CLEAR_FILTER = "DELETE FROM filters;";
    public static List<PlayerFilter> localList = new ObjectArrayList<PlayerFilter>();
    private final Bot bot;

    public PlayerFilterPlugin(Bot bot) {
        this.bot = bot;
        if (Main.database == null) {
            return;
        }
        bot.listener.addListener(this);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static List<PlayerFilter> list() {
        ArrayList<PlayerFilter> output = new ArrayList<PlayerFilter>();
        try (ResultSet result = Main.database.query(LIST_FILTERS);){
            if (result == null) {
                ArrayList<PlayerFilter> arrayList = output;
                return arrayList;
            }
            while (result.next()) {
                PlayerFilter playerFilter = new PlayerFilter(result.getString("name"), result.getString("reason"), result.getBoolean("regex"), result.getBoolean("ignoreCase"));
                output.add(playerFilter);
            }
        }
        catch (SQLException e) {
            LoggerUtilities.error(e);
        }
        localList = output;
        return output;
    }

    private PlayerFilter getPlayer(String name) {
        for (PlayerFilter playerFilter : localList) {
            if (!this.matchesPlayer(name, playerFilter)) continue;
            return playerFilter;
        }
        return null;
    }

    private boolean matchesPlayer(String name, PlayerFilter player) {
        if (player.regex()) {
            Pattern pattern = this.compilePattern(player);
            return pattern != null && pattern.matcher(name).find();
        }
        return this.compareNames(name, player);
    }

    private Pattern compilePattern(PlayerFilter player) {
        try {
            int flags = player.ignoreCase() ? 2 : 0;
            return Pattern.compile(player.playerName(), flags);
        }
        catch (Exception e) {
            this.bot.logger.error("Error compiling player filter regex " + player.playerName() + " (this shouldn't happen):");
            this.bot.logger.error(e);
            return null;
        }
    }

    private boolean compareNames(String name, PlayerFilter player) {
        String playerName = player.ignoreCase() ? player.playerName().toLowerCase() : player.playerName();
        String targetName = player.ignoreCase() ? name.toLowerCase() : name;
        return playerName.equals(targetName);
    }

    @Override
    public void onPlayerJoined(PlayerEntry target) {
        this.bot.executorService.execute(() -> {
            PlayerFilter player = this.getPlayer(target.profile.getName());
            if (player == null) {
                return;
            }
            this.bot.filterManager.add(target, player.reason());
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void add(String playerName, String reason, boolean regex, boolean ignoreCase) {
        try {
            PreparedStatement statement = Main.database.connection.prepareStatement(INSERT_FILTER);
            statement.setString(1, playerName);
            statement.setString(2, reason);
            statement.setBoolean(3, regex);
            statement.setBoolean(4, ignoreCase);
            statement.executeUpdate();
            PlayerFilterPlugin.list();
        }
        catch (SQLException e) {
            this.bot.logger.error(e);
        }
        for (Bot bot : this.bot.bots) {
            List<PlayerEntry> list = bot.players.list;
            synchronized (list) {
                for (PlayerEntry entry : bot.players.list) {
                    PlayerFilter player = this.getPlayer(entry.profile.getName());
                    if (player == null) continue;
                    bot.filterManager.add(entry, player.reason());
                }
            }
        }
    }

    public void remove(String playerName) {
        this.bot.filterManager.remove(playerName);
        try {
            PreparedStatement statement = Main.database.connection.prepareStatement(REMOVE_FILTER);
            statement.setString(1, playerName);
            statement.executeUpdate();
            PlayerFilterPlugin.list();
        }
        catch (SQLException e) {
            this.bot.logger.error(e);
        }
    }

    public void clear() {
        for (PlayerFilter player : localList) {
            this.bot.filterManager.remove(player.playerName());
        }
        try {
            Main.database.update(CLEAR_FILTER);
            PlayerFilterPlugin.list();
        }
        catch (SQLException e) {
            this.bot.logger.error(e);
        }
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
            Main.EXECUTOR.scheduleAtFixedRate(PlayerFilterPlugin::list, 5L, 30L, TimeUnit.SECONDS);
        }
    }
}

