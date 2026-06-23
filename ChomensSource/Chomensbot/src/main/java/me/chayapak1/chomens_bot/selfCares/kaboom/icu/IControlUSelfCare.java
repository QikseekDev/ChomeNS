/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.selfCares.kaboom.icu;

import java.util.UUID;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.player.PlayerEntry;
import me.chayapak1.chomens_bot.data.selfCare.SelfCare;

public class IControlUSelfCare
extends SelfCare {
    public IControlUSelfCare(Bot bot) {
        super(bot);
    }

    @Override
    public boolean shouldRun() {
        return false;
    }

    @Override
    public void onCommandSpyMessageReceived(PlayerEntry sender, String command) {
        if (!this.bot.serverFeatures.hasIControlU || !this.bot.config.selfCare.icu) {
            return;
        }
        String[] args2 = command.split("\\s+");
        if (args2.length < 3) {
            return;
        }
        if (!args2[0].equals("/icontrolu:icu") && !args2[0].equals("/icu") || !args2[1].equalsIgnoreCase("control")) {
            return;
        }
        String player = args2[2];
        PlayerEntry target = this.bot.players.getEntryTheBukkitWay(player);
        if (target == null && args2[2].matches("([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8})")) {
            target = this.bot.players.getEntry(UUID.fromString(args2[2]));
        }
        if (target == null || !target.profile.getId().equals(this.bot.profile.getId())) {
            return;
        }
        this.bot.core.run("essentials:sudo " + sender.profile.getIdAsString() + " icu stop");
    }
}

