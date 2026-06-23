/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.plugins;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.cloop.CommandLoop;
import me.chayapak1.chomens_bot.util.TimeUnitUtilities;
import org.apache.commons.lang3.tuple.Pair;

public class CloopPlugin {
    private final Bot bot;
    public final List<CommandLoop> loops = new ObjectArrayList<CommandLoop>();

    public CloopPlugin(Bot bot) {
        this.bot = bot;
    }

    public void add(ChronoUnit unit, long interval, String command) {
        Pair<Long, TimeUnit> converted = TimeUnitUtilities.fromChronoUnit(interval, unit);
        long convertedInterval = converted.getLeft();
        TimeUnit timeUnit = converted.getRight();
        this.loops.add(new CommandLoop(command, interval, unit, this.bot.executor.scheduleAtFixedRate(() -> this.bot.core.run(command), 0L, convertedInterval, timeUnit)));
    }

    public CommandLoop remove(int index) {
        CommandLoop removed = this.loops.remove(index);
        removed.task().cancel(false);
        return removed;
    }

    public void clear() {
        for (CommandLoop loop : this.loops) {
            loop.task().cancel(false);
        }
        this.loops.clear();
    }
}

