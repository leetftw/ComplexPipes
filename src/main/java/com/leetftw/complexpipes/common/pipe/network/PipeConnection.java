package com.leetftw.complexpipes.common.pipe.network;

import com.leetftw.complexpipes.common.PipeMod;
import com.leetftw.complexpipes.common.pipe.types.PipeType;
import com.leetftw.complexpipes.common.pipe.upgrade.PipeUpgrade;
import com.leetftw.complexpipes.common.util.routing.BaseRoutingStrategy;
import com.leetftw.complexpipes.common.util.routing.DefaultRoutingStrategy;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class PipeConnection {
    public static final int MAX_UPGRADES = 6;

    public static final Codec<PipeConnection> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BlockPos.CODEC.fieldOf("position").forGetter(PipeConnection::getPipePos),
                    Direction.CODEC.fieldOf("direction").forGetter(PipeConnection::getSide),
                    Codec.STRING.fieldOf("mode").forGetter(a -> a.getMode().name()),
                    Codec.INT.fieldOf("priority").forGetter(PipeConnection::getPriority),
                    Codec.INT.fieldOf("ratio").forGetter(PipeConnection::getRatio),
                    Codec.STRING.fieldOf("routingStrategy").forGetter(a -> a.getRoutingStrategy().getId()),
                    Codec.INT.fieldOf("tickCount").forGetter(a -> a.tickCount),
                    Codec.list(Codec.pair(Codec.intRange(0, MAX_UPGRADES - 1).fieldOf("slot").codec(), PipeUpgrade.CODEC.fieldOf("upgrade").codec())).fieldOf("upgrades").forGetter(a -> {
                        List<Pair<Integer, PipeUpgrade>> pairs = new ArrayList<>();
                        for (int i = 0; i < MAX_UPGRADES; i++)
                            if (a.pipeUpgrades[i] != null)
                                pairs.add(new Pair<>(i, a.pipeUpgrades[i]));
                        return pairs;
                    })
            ).apply(instance, PipeConnection::new)
    );

    private final BlockPos pipePos;
    private final Direction side;

    private PipeConnectionMode mode = PipeConnectionMode.PASSIVE;
    private int priority = 0;
    private int ratio = 1;
    private BaseRoutingStrategy routingStrategy = new DefaultRoutingStrategy();
    private final PipeUpgrade[] pipeUpgrades = new PipeUpgrade[MAX_UPGRADES];
    private int tickCount = 0;

    private boolean dirty = false;

    public PipeConnection(BlockPos pos, Direction side) {
        this.pipePos = pos;
        this.side = side;
    }

    private PipeConnection(BlockPos pos, Direction side, String mode, int priority, int ratio, String routingStrategy, int tickCount, List<Pair<Integer, PipeUpgrade>> upgrades) {
        this.pipePos = pos;
        this.side = side;
        this.mode = PipeConnectionMode.valueOf(mode);
        this.priority = priority;
        this.ratio = ratio;
        this.routingStrategy = BaseRoutingStrategy.create(routingStrategy);
        this.tickCount = tickCount;
        for (Pair<Integer, PipeUpgrade> storedUpgrade : upgrades)
            this.pipeUpgrades[storedUpgrade.getFirst()] = storedUpgrade.getSecond();
        this.dirty = true;
    }

    public BlockPos getPipePos() {
        return pipePos;
    }

    public Direction getSide() {
        return side;
    }

    public PipeConnectionMode getMode() {
        return mode;
    }

    public int getPriority() {
        return priority;
    }

    public int getRatio() {
        return ratio;
    }

    public BaseRoutingStrategy getRoutingStrategy() {
        return routingStrategy;
    }

    public PipeConnection setMode(PipeConnectionMode mode) {
        this.mode = mode;
        setDirty();
        return this;
    }

    public PipeConnection setPriority(int priority) {
        this.priority = priority;
        setDirty();
        return this;
    }

    public PipeConnection setRatio(int ratio) {
        this.ratio = ratio;
        setDirty();
        return this;
    }

    public PipeConnection setRoutingStrategy(BaseRoutingStrategy routingStrategy) {
        this.routingStrategy = routingStrategy;
        setDirty();
        return this;
    }

    public Stream<PipeUpgrade> getUpgradeStream() {
        return Arrays.stream(pipeUpgrades);
    }

    public PipeUpgrade getUpgradeInSlot(int slot) {
        return pipeUpgrades[slot];
    }

    public void setUpgradeInSlot(int slot, PipeUpgrade upgrade) {
        pipeUpgrades[slot] = upgrade;
        setDirty();
    }

    public PipeUpgrade removeUpgradeInSlot(int slot) {
        PipeUpgrade upgrade = pipeUpgrades[slot];
        pipeUpgrades[slot] = null;
        setDirty();
        return upgrade;
    }

    public boolean tryAddUpgrade(PipeUpgrade upgrade) {
        // Max 6 upgrades
        if (Arrays.stream(pipeUpgrades).filter(Objects::nonNull).count() == MAX_UPGRADES) return false;
        // Adding this upgrade should not exceed max upgrades for this type
        if (upgrade.getMaxInstalledCount() == Arrays.stream(pipeUpgrades).filter(Objects::nonNull)
                .filter(existingUpgrade -> existingUpgrade.getType() == upgrade.getType()).count()) return false;

        // TODO: add compatibility check
        int firstEmptyIndex = -1;
        for (int i = 0; i < MAX_UPGRADES; i++) {
            if (pipeUpgrades[i] == null) {
                firstEmptyIndex = i;
                break;
            }
        }
        if (firstEmptyIndex < 0) return false;

        pipeUpgrades[firstEmptyIndex] = upgrade;
        setDirty();
        return true;
    }

    public boolean isDirty() {
        return dirty;
    }

    private void setDirty() {
        PipeMod.LOGGER.info("[PipeConnection] Dirty set to TRUE");
        dirty = true;
    }

    public void clearDirty() {
        PipeMod.LOGGER.info("[PipeConnection] Dirty set to FALSE");
        dirty = false;
    }

    public int calculateTransferRate(PipeType<?> type) {
        return Arrays.stream(pipeUpgrades).filter(Objects::nonNull)
                .map(PipeUpgrade::getTransferAmountMultiplier)
                .reduce((double) type.getDefaultTransferAmount(), (a, b) -> a * b)
                .intValue();
    }

    public long calculateOperationTime(PipeType<?> type) {
        return Long.max(1, Math.round(Arrays.stream(pipeUpgrades).filter(Objects::nonNull)
                .map(PipeUpgrade::getTransferIntervalMultiplier)
                .reduce((double) type.getDefaultTransferSpeed(), (a, b) -> a * b)));
    }

    public double calculateResourcesPerTick(PipeType<?> type) {
        return (double) calculateTransferRate(type) / (double) calculateOperationTime(type);
    }

    public <T> void tick(Level level, BlockPos pos, PipeNetworkView networkView, PipeType<T> type) {
        long operationTime = calculateOperationTime(type); // 1 operation per second

        if (++tickCount < operationTime)
            return;
        tickCount = 0;

        // TEMP: only do an operation if the current connection is extract mode
        if (mode != PipeConnectionMode.EXTRACT)
            return;

        int transferRate = calculateTransferRate(type);

        // Get resource handler of this connection
        T base = level.getCapability(type.getBlockCapability(), pos.relative(side), side.getOpposite());

        // Get resource handlers of other connections
        // Stream hackery, other solution maybe better?
        List<PipeConnection> targets = networkView.connections.stream()
                .filter(otherConnection -> otherConnection != this)
                .filter(otherConnection -> otherConnection.mode == PipeConnectionMode.PASSIVE)
                .sorted(Comparator.comparing(PipeConnection::getPriority))
                .toList();

        if (targets.isEmpty())
            return;

        List<T> targetHandlers = targets.stream()
                .map(otherConnection -> level.getCapability(type.getBlockCapability(), otherConnection.pipePos.relative(otherConnection.side), otherConnection.side.getOpposite()))
                .toList();

        List<Predicate<Object>> targetFilters = targets.stream()
                .map(otherConnection -> Arrays.stream(otherConnection.pipeUpgrades).filter(Objects::nonNull).map(PipeUpgrade::getFilter).reduce(a -> true, Predicate::and))
                .toList();

        Transaction transaction = Transaction.openRoot();
        routingStrategy.routeExtract(
                type.getHandlerWrapper(), transaction,
                base, Arrays.stream(pipeUpgrades).filter(Objects::nonNull).map(PipeUpgrade::getFilter).reduce(a -> true, Predicate::and),
                targetHandlers, targetFilters,
                0, transferRate);
    }
}
