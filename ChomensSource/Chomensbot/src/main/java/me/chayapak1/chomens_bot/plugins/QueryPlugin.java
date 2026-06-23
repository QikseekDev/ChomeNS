/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.plugins;

import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.chat.ChatPacketType;
import me.chayapak1.chomens_bot.data.listener.Listener;
import me.chayapak1.chomens_bot.util.ComponentUtilities;
import me.chayapak1.chomens_bot.util.RandomStringUtilities;
import net.kyori.adventure.text.BlockNBTComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.mcprotocollib.network.event.session.DisconnectedEvent;

public class QueryPlugin
implements Listener {
    private static final String ID = "chomens_bot_query";
    private final Bot bot;
    public final Map<String, CompletableFuture<String>> requests = new ConcurrentHashMap<String, CompletableFuture<String>>();
    public final Queue<Component> cargosQueue = new ConcurrentLinkedQueue<Component>();

    public QueryPlugin(Bot bot) {
        this.bot = bot;
        bot.listener.addListener(this);
    }

    @Override
    public void onSecondTick() {
        if (this.cargosQueue.isEmpty()) {
            return;
        }
        if (this.cargosQueue.size() > 1000) {
            this.cargosQueue.clear();
            return;
        }
        Set<Component> set = this.cargosQueue.stream().limit(150L).collect(Collectors.toUnmodifiableSet());
        this.sendQueueComponent(set);
        for (int i = 0; i < set.size(); ++i) {
            this.cargosQueue.poll();
        }
    }

    @Override
    public boolean onSystemMessageReceived(Component component, ChatPacketType packetType, String string, String ansi) {
        TranslatableComponent rootTranslatable;
        if (packetType != ChatPacketType.SYSTEM || !(component instanceof TranslatableComponent) || !(rootTranslatable = (TranslatableComponent)component).key().equals(ID)) {
            return true;
        }
        List<TranslationArgument> arguments = rootTranslatable.arguments();
        if (arguments.size() != 1) {
            return false;
        }
        Component cargosComponent = arguments.getFirst().asComponent();
        if (!(cargosComponent instanceof TextComponent)) {
            return false;
        }
        List<Component> cargos = cargosComponent.children();
        for (Component cargo : cargos) {
            this.processCargo(cargo);
        }
        return false;
    }

    private void sendQueueComponent(Component component) {
        HashSet<Component> queue = new HashSet<Component>();
        queue.add(component);
        queue.add(Component.empty());
        this.sendQueueComponent(queue);
    }

    private void sendQueueComponent(Set<Component> queue) {
        this.bot.chat.tellraw((Component)Component.translatable(ID, Component.join(JoinConfiguration.noSeparators(), queue)), this.bot.profile.getId());
    }

    private void processCargo(Component cargo) {
        String id = cargo.insertion();
        if (!(cargo instanceof TextComponent) || id == null || id.length() != 5) {
            return;
        }
        CompletableFuture<String> future = this.requests.get(id);
        if (future == null) {
            return;
        }
        this.requests.remove(id);
        boolean interpret = id.endsWith("1");
        Component result = cargo.insertion(null);
        if (result.equals(Component.empty())) {
            future.complete(null);
        } else {
            String stringOutput = interpret ? (String)GsonComponentSerializer.gson().serialize(result) : ComponentUtilities.stringify(result);
            future.complete(stringOutput);
        }
    }

    private ObjectObjectImmutablePair<String, CompletableFuture<String>> getFutureAndId(boolean interpret) {
        String id = String.format("%s%s", RandomStringUtilities.generate(4, RandomStringUtilities.ALPHABETS_ONLY), interpret ? "1" : "0");
        CompletableFuture future = new CompletableFuture();
        this.requests.put(id, future);
        return ObjectObjectImmutablePair.of(id, future);
    }

    private void addComponent(Component component, boolean useCargo) {
        if (useCargo || !this.bot.core.hasEnoughPermissions()) {
            this.cargosQueue.add(component);
        } else {
            this.sendQueueComponent(component);
        }
    }

    public CompletableFuture<String> block(Vector3i location, String path) {
        return this.block(false, location, path, false);
    }

    public CompletableFuture<String> block(boolean useCargo, Vector3i location, String path, boolean interpret) {
        ObjectObjectImmutablePair<String, CompletableFuture<String>> pair = this.getFutureAndId(interpret);
        String id = (String)pair.left();
        Component component = Component.blockNBT(path, interpret, BlockNBTComponent.WorldPos.worldPos(BlockNBTComponent.WorldPos.Coordinate.absolute(location.getX()), BlockNBTComponent.WorldPos.Coordinate.absolute(location.getY()), BlockNBTComponent.WorldPos.Coordinate.absolute(location.getZ()))).insertion(id);
        this.addComponent(component, useCargo);
        return (CompletableFuture)pair.right();
    }

    public CompletableFuture<String> entity(String selector, String path) {
        return this.entity(false, selector, path);
    }

    public CompletableFuture<String> entity(boolean useCargo, String selector, String path) {
        ObjectObjectImmutablePair<String, CompletableFuture<String>> pair = this.getFutureAndId(false);
        String id = (String)pair.left();
        Component component = Component.entityNBT(path, selector).insertion(id);
        this.addComponent(component, useCargo);
        return (CompletableFuture)pair.right();
    }

    @Override
    public void disconnected(DisconnectedEvent event) {
        this.requests.clear();
        this.cargosQueue.clear();
    }
}

