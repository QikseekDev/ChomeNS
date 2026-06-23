/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.plugins;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.longs.LongObjectImmutablePair;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.Main;
import me.chayapak1.chomens_bot.data.listener.Listener;
import me.chayapak1.chomens_bot.data.player.PlayerEntry;
import me.chayapak1.chomens_bot.plugins.DatabasePlugin;
import me.chayapak1.chomens_bot.util.LoggerUtilities;
import org.geysermc.mcprotocollib.network.event.session.DisconnectedEvent;

public class PlayersDatabasePlugin
implements Listener {
    private static final String CREATE_MAIN_TABLE = "CREATE TABLE IF NOT EXISTS players (uuid CHAR(36) PRIMARY KEY, username VARCHAR(32) NOT NULL, lastSeenTime TIMESTAMP,lastSeenServer VARCHAR(100));";
    private static final String CREATE_IPS_TABLE = "CREATE TABLE IF NOT EXISTS playerIPs (uuid CHAR(36) NOT NULL, server VARCHAR(100) NOT NULL, ip VARCHAR(45) NOT NULL, PRIMARY KEY (uuid, server), INDEX idx_ip_server (ip, server), FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE);";
    private static final String INSERT_PLAYER = "INSERT INTO players (uuid, username, lastSeenTime, lastSeenServer) VALUES (?, ?, NOW(), ?) ON DUPLICATE KEY UPDATE username = VALUES(username), lastSeenTime = VALUES(lastSeenTime), lastSeenServer = VALUES(lastSeenServer);";
    private static final String UPDATE_PLAYER_IP = "INSERT INTO playerIPs (uuid, server, ip) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE ip = VALUES(ip);";
    private static final String UPDATE_LAST_SEEN = "UPDATE players SET lastSeenTime = NOW(), lastSeenServer = ? WHERE uuid = ?;";
    private static final String GET_LAST_SEEN = "SELECT lastSeenTime AS time, lastSeenServer AS server FROM players WHERE username = ?;";
    private static final String GET_IP = "SELECT ipInfo.ip FROM playerIPs ipInfo JOIN players player ON ipInfo.uuid = player.uuid WHERE player.username = ? AND ipInfo.server = ?;";
    private static final String FIND_ALTS_SINGLE_SERVER = "SELECT player.username, player.lastSeenTime, player.lastSeenServer FROM playerIPs ipInfo JOIN players player ON ipInfo.uuid = player.uuid WHERE ipInfo.ip = ? AND ipInfo.server = ? LIMIT ?;";
    private static final String FIND_ALTS_ALL_SERVERS = "SELECT player.username, player.lastSeenTime, player.lastSeenServer FROM playerIPs ipInfo JOIN players player ON ipInfo.uuid = player.uuid WHERE ipInfo.ip = ? LIMIT ?;";
    private final Bot bot;

    public PlayersDatabasePlugin(Bot bot) {
        this.bot = bot;
        if (Main.database == null) {
            return;
        }
        bot.listener.addListener(this);
    }

    public Pair<Long, String> getPlayerLastSeen(String username) {
        if (Main.database == null || Main.database.connection == null) {
            return null;
        }
        try {
            PreparedStatement statement = Main.database.connection.prepareStatement(GET_LAST_SEEN);
            statement.setString(1, username);
            ResultSet result = statement.executeQuery();
            if (!result.next()) {
                return null;
            }
            long time = result.getTimestamp("time").getTime();
            String server = result.getString("server");
            return LongObjectImmutablePair.of(time, server);
        }
        catch (SQLException e) {
            this.bot.logger.error(e);
            return null;
        }
    }

    public String getPlayerIP(String username) {
        if (Main.database == null || Main.database.connection == null) {
            return null;
        }
        try {
            PreparedStatement statement = Main.database.connection.prepareStatement(GET_IP);
            statement.setString(1, username);
            statement.setString(2, this.bot.getServerString(true));
            ResultSet result = statement.executeQuery();
            if (!result.next()) {
                return null;
            }
            return result.getString("ip");
        }
        catch (SQLException e) {
            this.bot.logger.error(e);
            return null;
        }
    }

    public Map<String, Pair<Long, String>> findPlayerAlts(String ip, boolean allServer, int limit) {
        try {
            PreparedStatement statement;
            HashMap<String, Pair<Long, String>> output = new HashMap<String, Pair<Long, String>>();
            if (allServer) {
                statement = Main.database.connection.prepareStatement(FIND_ALTS_ALL_SERVERS);
                statement.setString(1, ip);
                statement.setInt(2, limit);
            } else {
                statement = Main.database.connection.prepareStatement(FIND_ALTS_SINGLE_SERVER);
                statement.setString(1, ip);
                statement.setString(2, this.bot.getServerString(true));
                statement.setInt(3, limit);
            }
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                output.put(result.getString("username"), LongObjectImmutablePair.of(result.getTimestamp("lastSeenTime").getTime(), result.getString("lastSeenServer")));
            }
            return output;
        }
        catch (SQLException e) {
            this.bot.logger.error(e);
            return null;
        }
    }

    @Override
    public void onPlayerJoined(PlayerEntry target) {
        DatabasePlugin.EXECUTOR_SERVICE.execute(() -> {
            try {
                PreparedStatement insertPlayerStatement = Main.database.connection.prepareStatement(INSERT_PLAYER);
                insertPlayerStatement.setString(1, target.profile.getIdAsString());
                insertPlayerStatement.setString(2, target.profile.getName());
                insertPlayerStatement.setString(3, this.bot.getServerString(true));
                insertPlayerStatement.executeUpdate();
                this.updateLastSeenEntry(target);
            }
            catch (SQLException e) {
                this.bot.logger.error(e);
            }
        });
    }

    @Override
    public void onQueriedPlayerIP(PlayerEntry target, String ip) {
        DatabasePlugin.EXECUTOR_SERVICE.execute(() -> {
            try {
                PreparedStatement updatePlayerStatement = Main.database.connection.prepareStatement(UPDATE_PLAYER_IP);
                updatePlayerStatement.setString(1, target.profile.getIdAsString());
                updatePlayerStatement.setString(2, this.bot.getServerString(true));
                updatePlayerStatement.setString(3, ip);
                updatePlayerStatement.executeUpdate();
            }
            catch (SQLException e) {
                this.bot.logger.error(e);
            }
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void disconnected(DisconnectedEvent event) {
        if (Main.stopping) {
            return;
        }
        List<PlayerEntry> list = this.bot.players.list;
        synchronized (list) {
            if (this.bot.players.list.isEmpty()) {
                return;
            }
            ArrayList<PlayerEntry> clonedList = new ArrayList<PlayerEntry>(this.bot.players.list);
            DatabasePlugin.EXECUTOR_SERVICE.execute(() -> {
                for (PlayerEntry target : clonedList) {
                    this.updateLastSeenEntry(target);
                }
            });
        }
    }

    @Override
    public void onPlayerLeft(PlayerEntry target) {
        DatabasePlugin.EXECUTOR_SERVICE.execute(() -> this.updateLastSeenEntry(target));
    }

    private void updateLastSeenEntry(PlayerEntry target) {
        try {
            PreparedStatement updateLastSeenStatement = Main.database.connection.prepareStatement(UPDATE_LAST_SEEN);
            updateLastSeenStatement.setString(1, this.bot.getServerString(true));
            updateLastSeenStatement.setString(2, target.profile.getIdAsString());
            updateLastSeenStatement.executeUpdate();
        }
        catch (SQLException e) {
            this.bot.logger.error(e);
        }
    }

    static {
        if (Main.database != null) {
            DatabasePlugin.EXECUTOR_SERVICE.execute(() -> {
                try {
                    Main.database.execute(CREATE_MAIN_TABLE);
                    Main.database.execute(CREATE_IPS_TABLE);
                }
                catch (SQLException e) {
                    LoggerUtilities.error(e);
                }
            });
        }
    }
}

