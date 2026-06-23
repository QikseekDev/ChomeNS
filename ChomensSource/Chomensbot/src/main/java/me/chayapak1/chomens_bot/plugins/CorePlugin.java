/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.plugins;

import com.google.gson.JsonSyntaxException;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.listener.Listener;
import me.chayapak1.chomens_bot.data.selfCare.SelfData;
import me.chayapak1.chomens_bot.util.MathUtilities;
import me.chayapak1.chomens_bot.util.StringUtilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.cloudburstmc.math.vector.Vector3d;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtMapBuilder;
import org.geysermc.mcprotocollib.network.ClientSession;
import org.geysermc.mcprotocollib.network.event.session.ConnectedEvent;
import org.geysermc.mcprotocollib.network.event.session.DisconnectedEvent;
import org.geysermc.mcprotocollib.protocol.data.game.entity.object.Direction;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.Hand;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.PlayerAction;
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponentTypes;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponents;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.CommandBlockMode;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.inventory.ServerboundSetCommandBlockPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.inventory.ServerboundSetCreativeModeSlotPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundPlayerActionPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundUseItemOnPacket;

public class CorePlugin
implements Listener {
    private static final int MAX_PENDING_COMMANDS = 768;
    public static final int COMMAND_BLOCK_ID = 425;
    private final Bot bot;
    public volatile boolean ready = false;
    public final Vector3i fromSize;
    public Vector3i toSize;
    public Vector3i from;
    public Vector3i to;
    public volatile Vector3i block = null;
    public final AtomicInteger index = new AtomicInteger();
    public final Queue<String> placeBlockQueue = new ConcurrentLinkedQueue<String>();
    public final Queue<String> pendingCommands = new ConcurrentLinkedQueue<String>();
    public final AtomicInteger commandsPerTick = new AtomicInteger();
    public final AtomicInteger commandsPerSecond = new AtomicInteger();
    private final AtomicInteger positionChangesPerSecond = new AtomicInteger(0);
    private boolean exists = false;
    private boolean shouldRefill = false;

    public CorePlugin(Bot bot) {
        this.bot = bot;
        this.fromSize = Vector3i.from(0, -64, 0);
        this.toSize = Vector3i.from(15, -64, 15);
        if (this.hasRateLimit() && this.hasReset()) {
            bot.executor.scheduleAtFixedRate(() -> this.commandsPerSecond.set(0), 0L, bot.options.coreRateLimit.reset, TimeUnit.MILLISECONDS);
        }
        bot.listener.addListener(this);
    }

    @Override
    public void onTick() {
        if (!this.pendingCommands.isEmpty() && this.exists && this.hasEnoughPermissions()) {
            if (this.pendingCommands.size() > 768) {
                this.pendingCommands.clear();
            } else {
                for (String pendingCommand : this.pendingCommands) {
                    this.forceRun(pendingCommand);
                }
                this.pendingCommands.clear();
            }
        }
        if (this.placeBlockQueue.size() > 300) {
            this.placeBlockQueue.clear();
            return;
        }
        String command = this.placeBlockQueue.poll();
        if (command == null) {
            return;
        }
        this.forceRunPlaceBlock(command);
    }

    @Override
    public void onLocalTick() {
        if (this.commandsPerTick.get() > 0) {
            this.commandsPerTick.decrementAndGet();
        }
    }

    @Override
    public void onLocalSecondTick() {
        this.resizeTick();
    }

    @Override
    public void onSecondTick() {
        this.checkCoreTick();
        this.exists = this.isCoreExists();
        if (this.shouldRefill) {
            this.refill(false);
            this.shouldRefill = false;
        }
        this.positionChangesPerSecond.set(0);
    }

    public boolean hasRateLimit() {
        return this.bot.options.coreRateLimit.limit > 0;
    }

    public boolean hasReset() {
        return this.bot.options.coreRateLimit.reset > 0;
    }

    public boolean isRateLimited() {
        return this.commandsPerSecond.get() > this.bot.options.coreRateLimit.limit;
    }

    private void forceRun(String command) {
        if (!this.ready || command.length() > Short.MAX_VALUE) {
            return;
        }
        this.commandsPerTick.incrementAndGet();
        if (!this.bot.serverFeatures.hasNamespaces) {
            command = StringUtilities.removeNamespace(command);
        }
        if (this.bot.serverFeatures.hasExtras && !this.bot.options.forceImpulseCore) {
            this.bot.session.send(new ServerboundSetCommandBlockPacket(this.block, command, CommandBlockMode.AUTO, true, false, true));
        } else {
            this.bot.session.send(new ServerboundSetCommandBlockPacket(this.block, "", CommandBlockMode.REDSTONE, false, false, false));
            this.bot.session.send(new ServerboundSetCommandBlockPacket(this.block, command, CommandBlockMode.REDSTONE, true, false, true));
        }
        this.incrementBlock(0);
    }

    public void run(String command) {
        if (!(this.pendingCommands.size() > 768 || this.exists && this.hasEnoughPermissions())) {
            this.pendingCommands.add(command);
            return;
        }
        if (!this.ready || command.length() > Short.MAX_VALUE) {
            return;
        }
        if (this.bot.options.useCore) {
            if (this.bot.options.useCorePlaceBlock) {
                this.runPlaceBlock(command);
                return;
            }
            if (this.isRateLimited() && this.hasRateLimit()) {
                return;
            }
            this.forceRun(command);
            if (this.hasRateLimit()) {
                this.commandsPerSecond.incrementAndGet();
            }
        } else if (command.length() < 256) {
            this.bot.chat.send("/" + command);
        }
    }

    public CompletableFuture<Component> runTracked(String command) {
        if (!this.ready || command.length() > Short.MAX_VALUE) {
            return null;
        }
        if (!this.bot.options.useCore) {
            return null;
        }
        if (this.bot.options.useCorePlaceBlock) {
            this.runPlaceBlock(command);
            return null;
        }
        Vector3i coreBlock = this.block.clone();
        this.run(command);
        CompletableFuture<Component> trackedFuture = new CompletableFuture<Component>();
        CompletableFuture<String> future = this.bot.query.block(false, coreBlock, "LastOutput", true);
        future.thenApply(output -> {
            if (output == null) {
                return null;
            }
            trackedFuture.complete(Component.join(JoinConfiguration.separator(Component.empty()), GsonComponentSerializer.gson().deserialize(output).children()));
            return output;
        });
        return trackedFuture;
    }

    public void runPlaceBlock(String command) {
        try {
            this.placeBlockQueue.add(command);
        }
        catch (Exception e) {
            this.bot.logger.error(e);
        }
    }

    public void forceRunPlaceBlock(String command) {
        if (!this.ready || !this.bot.options.useCore) {
            return;
        }
        if (!this.bot.serverFeatures.hasNamespaces) {
            command = StringUtilities.removeNamespace(command);
        }
        NbtMapBuilder blockEntityTagBuilder = NbtMap.builder();
        blockEntityTagBuilder.putString("id", "minecraft:command_block");
        blockEntityTagBuilder.putString("Command", command);
        blockEntityTagBuilder.putByte("auto", (byte)1);
        blockEntityTagBuilder.putByte("TrackOutput", (byte)1);
        NbtMap blockEntityTag = blockEntityTagBuilder.build();
        HashMap map = new HashMap();
        map.put(DataComponentTypes.BLOCK_ENTITY_DATA, DataComponentTypes.BLOCK_ENTITY_DATA.getDataComponentFactory().create(DataComponentTypes.BLOCK_ENTITY_DATA, blockEntityTag));
        try {
            Object customName = GsonComponentSerializer.gson().deserialize(this.bot.config.core.customName);
            map.put(DataComponentTypes.CUSTOM_NAME, DataComponentTypes.CUSTOM_NAME.getDataComponentFactory().create(DataComponentTypes.CUSTOM_NAME, (Component)customName));
        }
        catch (JsonSyntaxException e) {
            this.bot.logger.error("Error while parsing the core's custom name into Component! You might have an invalid syntax in the custom name.");
            this.bot.logger.error(e);
        }
        DataComponents dataComponents = new DataComponents(map);
        Vector3i temporaryBlockPosition = Vector3i.from(this.bot.position.position.getX(), this.bot.position.position.getY() - 1.0, this.bot.position.position.getZ());
        ClientSession session = this.bot.session;
        session.send(new ServerboundSetCreativeModeSlotPacket(36, new ItemStack(425, 64, dataComponents)));
        session.send(new ServerboundPlayerActionPacket(PlayerAction.START_DIGGING, temporaryBlockPosition, Direction.NORTH, 0));
        session.send(new ServerboundUseItemOnPacket(temporaryBlockPosition, Direction.UP, Hand.MAIN_HAND, 0.5f, 0.5f, 0.5f, false, false, 1));
        if (!this.bot.options.useCorePlaceBlock) {
            this.bot.executor.schedule(() -> session.send(new ServerboundSetCreativeModeSlotPacket(36, null)), 100L, TimeUnit.MILLISECONDS);
        }
    }

    private void resizeTick() {
        if (!this.ready) {
            return;
        }
        if (!this.isCore(this.block)) {
            this.recalculateRelativePositions();
        }
        Vector3i oldSize = this.toSize;
        int x = this.toSize.getX();
        int y = this.bot.world.minY;
        int z = this.toSize.getZ();
        while (this.commandsPerTick.get() > 256) {
            ++y;
            this.commandsPerTick.getAndAdd(-256);
        }
        y = Math.min(y, this.bot.world.maxY);
        this.toSize = Vector3i.from(x, y, z);
        if (oldSize.getY() != this.toSize.getY()) {
            this.recalculateRelativePositions();
            this.refill(false);
        }
    }

    public boolean hasEnoughPermissions() {
        SelfData data = this.bot.selfCare.data;
        if (data == null) {
            return false;
        }
        return data.permissionLevel >= 2 && data.gameMode == GameMode.CREATIVE;
    }

    public boolean isCoreComplete() {
        if (!this.ready) {
            return false;
        }
        for (int y = this.from.getY(); y <= this.to.getY(); ++y) {
            for (int z = this.from.getZ(); z <= this.to.getZ(); ++z) {
                for (int x = this.from.getX(); x <= this.to.getX(); ++x) {
                    int block = this.bot.world.getBlock(x, y, z);
                    if (this.isCommandBlockState(block)) continue;
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isCoreExists() {
        if (!this.ready) {
            return false;
        }
        for (int y = this.from.getY(); y <= this.to.getY(); ++y) {
            for (int z = this.from.getZ(); z <= this.to.getZ(); ++z) {
                for (int x = this.from.getX(); x <= this.to.getX(); ++x) {
                    int block = this.bot.world.getBlock(x, y, z);
                    if (!this.isCommandBlockState(block)) continue;
                    return true;
                }
            }
        }
        return false;
    }

    private void checkCoreTick() {
        if (!this.isCoreComplete()) {
            this.shouldRefill = true;
        }
    }

    private boolean isCommandBlockState(int blockState) {
        return blockState >= 8690 && blockState <= 8701 || blockState >= 13550 && blockState <= 13561 || blockState >= 13538 && blockState <= 13549;
    }

    private boolean isCore(Vector3i position) {
        return position.getX() >= this.from.getX() && position.getX() <= this.to.getX() && position.getY() >= this.from.getY() && position.getY() <= this.to.getY() && position.getZ() >= this.from.getZ() && position.getZ() <= this.to.getZ();
    }

    private void incrementBlock(int times) {
        int currentIndex = this.index.get();
        int x = this.from.getX() + (currentIndex & 0xF);
        int z = this.from.getZ() + (currentIndex >> 4 & 0xF);
        int y = (currentIndex >> 8) + this.bot.world.minY;
        this.block = Vector3i.from(x, y, z);
        this.index.set((currentIndex + 1) % (256 * Math.max(1, this.to.getY() - this.from.getY())));
        if (times <= 256 && !this.isCommandBlockState(this.bot.world.getBlock(x, y, z))) {
            this.incrementBlock(times + 1);
        }
    }

    @Override
    public void onPositionChange(Vector3d position) {
        if (this.bot.position.isGoingDownFromHeightLimit) {
            return;
        }
        this.positionChangesPerSecond.incrementAndGet();
        int coreChunkPosX = this.from == null ? -1 : (int)Math.floor((double)this.from.getX() / 16.0);
        int coreChunkPosZ = this.from == null ? -1 : (int)Math.floor((double)this.from.getZ() / 16.0);
        int botChunkPosX = (int)Math.floor(this.bot.position.position.getX() / 16.0);
        int botChunkPosZ = (int)Math.floor(this.bot.position.position.getZ() / 16.0);
        if (this.from == null || this.to == null || Math.abs(botChunkPosX - coreChunkPosX) >= this.bot.world.simulationDistance || Math.abs(botChunkPosZ - coreChunkPosZ) >= this.bot.world.simulationDistance) {
            String deleteCommand = this.from != null && this.to != null ? String.format("minecraft:fill %d %d %d %d %d %d air", this.from.getX(), this.from.getY(), this.from.getZ(), this.to.getX(), this.to.getY(), this.to.getZ()) : null;
            this.reset();
            this.refill(false);
            this.runPlaceBlock(deleteCommand);
        }
        if (!this.ready) {
            this.ready = true;
            this.reset();
            this.refill();
            this.bot.listener.dispatch(Listener::onCoreReady);
        }
    }

    @Override
    public void onWorldChanged(String dimension) {
        this.reset();
        this.refill();
    }

    @Override
    public void connected(ConnectedEvent event) {
        this.pendingCommands.add("minecraft:gamerule commandBlockOutput false");
    }

    @Override
    public void disconnected(DisconnectedEvent event) {
        this.ready = false;
        this.exists = false;
    }

    public void recalculateRelativePositions() {
        int botChunkPosX = (int)Math.floor(this.bot.position.position.getX() / 16.0);
        int botChunkPosZ = (int)Math.floor(this.bot.position.position.getZ() / 16.0);
        this.from = Vector3i.from(this.fromSize.getX() + botChunkPosX * 16, MathUtilities.clamp(this.fromSize.getY(), this.bot.world.minY, this.bot.world.maxY), this.fromSize.getZ() + botChunkPosZ * 16);
        this.to = Vector3i.from(this.toSize.getX() + botChunkPosX * 16, MathUtilities.clamp(this.toSize.getY(), this.bot.world.minY, this.bot.world.maxY), this.toSize.getZ() + botChunkPosZ * 16);
    }

    public void reset() {
        this.recalculateRelativePositions();
        this.block = Vector3i.from(this.from);
        this.index.set(0);
    }

    public void refill() {
        this.refill(true);
    }

    public void refill(boolean force) {
        if (!this.ready) {
            return;
        }
        HashMap<Integer, Boolean> refilledMap = new HashMap<Integer, Boolean>();
        String customName = this.bot.options.useSNBTComponents ? String.format("{CustomName:%s}", this.bot.config.core.customName) : String.format("{CustomName:'%s'}", this.bot.config.core.customName.replace("\\", "\\\\").replace("'", "\\'"));
        for (int y = this.from.getY(); y <= this.to.getY(); ++y) {
            for (int z = this.from.getZ(); z <= this.to.getZ(); ++z) {
                for (int x = this.from.getX(); x <= this.to.getX(); ++x) {
                    int block = this.bot.world.getBlock(x, y, z);
                    Boolean refilled = (Boolean)refilledMap.get(y);
                    if (!force && this.isCommandBlockState(block) || refilled != null && refilled.booleanValue()) continue;
                    boolean useChat = this.positionChangesPerSecond.get() > 10;
                    String command = String.format("%sfill %d %d %d %d %d %d command_block%s", useChat ? "" : "minecraft:", this.from.getX(), y, this.from.getZ(), this.to.getX(), y, this.to.getZ(), useChat ? "" : customName);
                    if (useChat) {
                        this.bot.chat.sendCommandInstantly(command);
                    } else if (this.isCoreExists()) {
                        this.run(command);
                    } else {
                        this.runPlaceBlock(command);
                    }
                    refilledMap.put(y, true);
                }
            }
        }
        if (refilledMap.containsValue(true)) {
            this.bot.listener.dispatch(Listener::onCoreRefilled);
        }
    }
}

