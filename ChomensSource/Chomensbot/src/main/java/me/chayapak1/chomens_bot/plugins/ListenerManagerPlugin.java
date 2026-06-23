/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.plugins;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.listener.Listener;
import net.kyori.adventure.text.Component;

public class ListenerManagerPlugin {
    private final Bot bot;
    private final List<Listener> listeners = Collections.synchronizedList(new ObjectArrayList());

    public ListenerManagerPlugin(Bot bot) {
        this.bot = bot;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void dispatch(Consumer<Listener> consumer) {
        List<Listener> list = this.listeners;
        synchronized (list) {
            for (Listener listener : this.listeners) {
                try {
                    consumer.accept(listener);
                }
                catch (Throwable throwable) {
                    this.bot.logger.error(Component.translatable("Caught an error while trying to dispatch an event to %s!", Component.text(listener.getClass().getSimpleName())));
                    this.bot.logger.error(throwable);
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void dispatchWithCheck(Function<Listener, Boolean> function) {
        List<Listener> list = this.listeners;
        synchronized (list) {
            for (Listener listener : this.listeners) {
                try {
                    Boolean result = function.apply(listener);
                    if (result == null || result.booleanValue()) continue;
                    break;
                }
                catch (Throwable throwable) {
                    this.bot.logger.error(Component.translatable("Caught an error while trying to dispatch an event with a returning boolean to %s!", Component.text(listener.getClass().getSimpleName())));
                    this.bot.logger.error(throwable);
                }
            }
        }
    }

    public void addListener(Listener listener) {
        if (this.listeners.contains(listener)) {
            throw new IllegalArgumentException("This listener is already in the listeners list. Please call `removeListener(listener)` first.");
        }
        this.listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        this.listeners.remove(listener);
    }
}

