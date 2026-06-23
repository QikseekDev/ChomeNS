/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.plugins;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.Main;
import me.chayapak1.chomens_bot.data.listener.Listener;
import me.chayapak1.chomens_bot.data.player.PlayerEntry;
import me.chayapak1.chomens_bot.plugins.DatabasePlugin;
import me.chayapak1.chomens_bot.util.LoggerUtilities;

public class IPFilterPlugin
implements Listener {
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS ipFilters (ip VARCHAR(255) PRIMARY KEY, reason VARCHAR(2048));";
    private static final String LIST_FILTERS = "SELECT * FROM ipFilters;";
    private static final String INSERT_FILTER = "INSERT INTO ipFilters (ip, reason) VALUES (?, ?);";
    private static final String REMOVE_FILTER = "DELETE FROM ipFilters WHERE ip = ?;";
    private static final String CLEAR_FILTER = "DELETE FROM ipFilters;";
    public static Map<String, String> localList = new Object2ObjectOpenHashMap<String, String>();
    private final Bot bot;

    public IPFilterPlugin(Bot bot) {
        this.bot = bot;
        if (Main.database == null) {
            return;
        }
        bot.listener.addListener(this);
        bot.executor.scheduleAtFixedRate(this::checkAllPlayers, 5L, 5L, TimeUnit.SECONDS);
    }

    @Override
    public void onCoreReady() {
        this.checkAllPlayers();
    }

    @Override
    public void onQueriedPlayerIP(PlayerEntry target, String ip) {
        if (localList.isEmpty()) {
            return;
        }
        this.check(target);
    }

    private void check(PlayerEntry target) {
        if (this.bot.options.useCorePlaceBlock) {
            return;
        }
        String ip = target.persistingData.ip;
        if (ip == null) {
            return;
        }
        this.handleFilterManager(ip, target);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static Map<String, String> list() {
        Object2ObjectOpenHashMap<String, String> output = new Object2ObjectOpenHashMap<String, String>();
        try (ResultSet result = Main.database.query(LIST_FILTERS);){
            if (result == null) {
                Object2ObjectOpenHashMap<String, String> object2ObjectOpenHashMap = output;
                return object2ObjectOpenHashMap;
            }
            while (result.next()) {
                output.put(result.getString("ip"), result.getString("reason"));
            }
        }
        catch (SQLException e) {
            LoggerUtilities.error(e);
        }
        localList = output;
        return output;
    }

    public void add(String ip, String reason) {
        try {
            PreparedStatement statement = Main.database.connection.prepareStatement(INSERT_FILTER);
            statement.setString(1, ip);
            statement.setString(2, reason);
            statement.executeUpdate();
            IPFilterPlugin.list();
        }
        catch (SQLException e) {
            this.bot.logger.error(e);
        }
        this.checkAllPlayers();
    }

    private void checkAllPlayers() {
        if (localList.isEmpty()) {
            return;
        }
        this.bot.executorService.execute(() -> {
            List<PlayerEntry> list = this.bot.players.list;
            synchronized (list) {
                for (PlayerEntry entry : this.bot.players.list) {
                    this.check(entry);
                }
            }
        });
    }

    public void remove(String ip) {
        try {
            PreparedStatement statement = Main.database.connection.prepareStatement(REMOVE_FILTER);
            statement.setString(1, ip);
            statement.executeUpdate();
            IPFilterPlugin.list();
        }
        catch (SQLException e) {
            this.bot.logger.error(e);
        }
    }

    public void clear() {
        try {
            Main.database.update(CLEAR_FILTER);
            IPFilterPlugin.list();
        }
        catch (SQLException e) {
            this.bot.logger.error(e);
        }
    }

    private void handleFilterManager(String ip, PlayerEntry entry) {
        for (Map.Entry<String, String> ipEntry : localList.entrySet()) {
            String eachIP = ipEntry.getKey();
            String reason = ipEntry.getValue();
            if (entry.profile.equals(this.bot.profile) || !eachIP.equals(ip)) continue;
            this.bot.filterManager.add(entry, reason);
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
            Main.EXECUTOR.scheduleAtFixedRate(IPFilterPlugin::list, 5L, 30L, TimeUnit.SECONDS);
        }
    }
}

