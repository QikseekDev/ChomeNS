/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.plugins;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.listener.Listener;
import me.chayapak1.chomens_bot.data.team.Team;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.session.DisconnectedEvent;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.scoreboard.ClientboundSetPlayerTeamPacket;

public class TeamPlugin
implements Listener {
    public final List<Team> teams = Collections.synchronizedList(new ObjectArrayList());

    public TeamPlugin(Bot bot) {
        bot.listener.addListener(this);
    }

    @Override
    public void disconnected(DisconnectedEvent event) {
        this.teams.clear();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Team findTeamByName(String name) {
        List<Team> list = this.teams;
        synchronized (list) {
            for (Team team : new ArrayList<Team>(this.teams)) {
                if (!team.teamName.equals(name)) continue;
                return team;
            }
            return null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Team findTeamByMember(String member) {
        List<Team> list = this.teams;
        synchronized (list) {
            for (Team team : new ArrayList<Team>(this.teams)) {
                if (!team.players.contains(member)) continue;
                return team;
            }
            return null;
        }
    }

    @Override
    public void packetReceived(Session session, Packet packet) {
        if (packet instanceof ClientboundSetPlayerTeamPacket) {
            ClientboundSetPlayerTeamPacket t_packet = (ClientboundSetPlayerTeamPacket)packet;
            this.packetReceived(t_packet);
        }
    }

    private void packetReceived(ClientboundSetPlayerTeamPacket packet) {
        switch (packet.getAction()) {
            case CREATE: {
                Team team = new Team(packet.getTeamName(), new ArrayList<String>(), packet.getDisplayName(), packet.isFriendlyFire(), packet.isSeeFriendlyInvisibles(), packet.getNameTagVisibility(), packet.getCollisionRule(), packet.getColor(), packet.getPrefix(), packet.getSuffix());
                this.teams.add(team);
                break;
            }
            case REMOVE: {
                Team team = this.findTeamByName(packet.getTeamName());
                if (team == null) {
                    return;
                }
                this.teams.remove(team);
                break;
            }
            case UPDATE: {
                Team team = this.findTeamByName(packet.getTeamName());
                if (team == null) {
                    return;
                }
                team.teamName = packet.getTeamName();
                team.displayName = packet.getDisplayName();
                team.friendlyFire = packet.isFriendlyFire();
                team.seeFriendlyInvisibles = packet.isSeeFriendlyInvisibles();
                team.nametagVisibility = packet.getNameTagVisibility();
                team.collisionRule = packet.getCollisionRule();
                team.color = packet.getColor();
                team.prefix = packet.getPrefix();
                team.suffix = packet.getSuffix();
                break;
            }
            case ADD_PLAYER: {
                Team team = this.findTeamByName(packet.getTeamName());
                if (team == null) {
                    return;
                }
                team.players.addAll(Arrays.asList(packet.getPlayers()));
                break;
            }
            case REMOVE_PLAYER: {
                Team team = this.findTeamByName(packet.getTeamName());
                if (team == null) {
                    return;
                }
                team.players.removeAll(Arrays.asList(packet.getPlayers()));
            }
        }
    }
}

