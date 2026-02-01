package com.leetftw.complexpipes.common.pipe.network;

import com.leetftw.complexpipes.common.items.ItemComponentRegistry;
import com.leetftw.complexpipes.common.items.ItemRegistry;
import com.leetftw.complexpipes.common.pipe.types.PipeType;
import com.leetftw.complexpipes.common.pipe.types.PipeTypeRegistry;
import com.leetftw.complexpipes.common.pipe.upgrades.PipeUpgrade;
import com.leetftw.complexpipes.common.tests.GameRuleRegistry;
import com.leetftw.complexpipes.common.util.routing.BaseRoutingStrategy;
import com.leetftw.complexpipes.common.util.routing.DefaultRoutingStrategy;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.leetftw.complexpipes.common.ComplexPipes.LOGGER;

public class PipeConnection {
    public static final int MAX_UPGRADES = 6;

    public static final Codec<PipeConnection> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("type").forGetter(a -> a.TYPE.getRegisteredId()),
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

    private BlockPos pipePos;
    private final Direction side;

    private PipeConnectionMode mode = PipeConnectionMode.PASSIVE;
    private int priority = 0;
    private int ratio = 1;
    private BaseRoutingStrategy routingStrategy = new DefaultRoutingStrategy();
    private final PipeUpgrade[] pipeUpgrades = new PipeUpgrade[MAX_UPGRADES];
    private int tickCount = 0;
    private final PipeType<?> TYPE;

    private Predicate<Object> predicate;

    private boolean dirty = false;

    public PipeConnection(PipeType<?> type, BlockPos pos, Direction side) {
        this.pipePos = pos;
        this.side = side;
        this.TYPE = type;
    }

    private PipeConnection(String type, BlockPos pos, Direction side, String mode, int priority, int ratio, String routingStrategy, int tickCount, List<Pair<Integer, PipeUpgrade>> upgrades) {
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
        this.TYPE = PipeTypeRegistry.getType(type);
    }

    public BlockPos getPipePos() {
        return pipePos;
    }

    public PipeConnection overwritePipePos(BlockPos newPos) {
        this.pipePos = newPos;
        return this;
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
        if (Arrays.stream(pipeUpgrades).filter(Objects::nonNull).count() == MAX_UPGRADES)
            return false;
        // Adding this upgrade should not exceed max upgrades for this type
        if (upgrade.getMaxInstalledCount() == Arrays.stream(pipeUpgrades).filter(Objects::nonNull)
                .filter(existingUpgrade -> existingUpgrade.getType() == upgrade.getType()).count())
            return false;
        // The upgrade should be supported by the pipe
        if (!TYPE.supportsUpgrade(upgrade.getType()))
            return false;

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
        LOGGER.info("[PipeConnection] Dirty set to TRUE");
        dirty = true;
    }

    public void clearDirty() {
        LOGGER.info("[PipeConnection] Dirty set to FALSE");
        dirty = false;
    }

    public int calculateTransferRate() {
        return Arrays.stream(pipeUpgrades).filter(Objects::nonNull)
                .map(PipeUpgrade::getTransferAmountMultiplier)
                .reduce((double) TYPE.getDefaultTransferAmount(), (a, b) -> a * b)
                .intValue();
    }

    public long calculateOperationTime() {
        return Long.max(1, Math.round(Arrays.stream(pipeUpgrades).filter(Objects::nonNull)
                .map(PipeUpgrade::getTransferIntervalMultiplier)
                .reduce((double) TYPE.getDefaultTransferSpeed(), (a, b) -> a * b)));
    }

    public double calculateResourcesPerTick() {
        return (double) calculateTransferRate() / (double) calculateOperationTime();
    }

    public void appendItems(List<ItemStack> items) {
        for (PipeUpgrade upgrade : pipeUpgrades) {
            if (upgrade == null) continue;

            ItemStack stack = new ItemStack(upgrade.getType().getItem(), 1);
            stack.set(ItemComponentRegistry.PIPE_UPGRADE, upgrade);
            items.add(stack);
        }

        switch (mode) {
            case PipeConnectionMode.EXTRACT:
                ItemStack extractionCard = new ItemStack(ItemRegistry.EXTRACTION_CARD.get(), 1);
                items.add(extractionCard);
                break;
            case PipeConnectionMode.INSERT:
                ItemStack insertionCard = new ItemStack(ItemRegistry.INSERTION_CARD.get(), 1);
                items.add(insertionCard);
                break;
            case PipeConnectionMode.PASSIVE:
            default:
                break;
        }

        switch (routingStrategy.getId()) {
            case "round_robin":
                ItemStack roundRobinCard = new ItemStack(ItemRegistry.ROUND_ROBIN_CARD.get(), 1);
                items.add(roundRobinCard);
                break;
            case "default":
            default:
                break;
        }
    }

    public Predicate<Object> computePredicate() {
        if (predicate == null) {
            predicate = object -> {
                for (PipeUpgrade upgrade : pipeUpgrades) {
                    if (upgrade == null)
                        continue;
                    if (upgrade.isFilter() && !upgrade.allowResourceTransfer(object))
                        return false;
                }
                return true;
            };
        }
        return predicate;
    }

    public <T> void tick(ServerLevel level, BlockPos pos, PipeNetworkView networkView, PipeType<T> type) {
        long operationTime = calculateOperationTime(); // 1 operation per second

        if (++tickCount < operationTime)
            return;
        tickCount = 0;

        // TEMP: only do an operation if the current connection is extract mode
        if (mode != PipeConnectionMode.EXTRACT && mode != PipeConnectionMode.INSERT)
            return;

        int transferRate = calculateTransferRate();

        // Get resource handler of this connection
        T base = level.getCapability(type.getBlockCapability(), pos.relative(side), side.getOpposite());

        // Group targets by priority
        int largestList = 0;
        Map<Integer, List<Tuple<PipeConnection, T>>> prioritizedTargets = new HashMap<>(networkView.connections.size());
        for (PipeConnection target : networkView.connections) {
            if (target == this) continue;
            if (target.mode != PipeConnectionMode.PASSIVE) continue;

            T targetHandler = level.getCapability(type.getBlockCapability(), target.pipePos.relative(target.side), target.side.getOpposite());
            if (targetHandler == null) {
                LOGGER.warn("[PipeConnection] Found null connection while ticking!");
                continue;
            }

            List<Tuple<PipeConnection, T>> list = prioritizedTargets.computeIfAbsent(target.priority, ArrayList::new);
            list.add(new Tuple<>(target, targetHandler));
            int listSize = list.size();
            if (listSize > largestList)
                largestList = listSize;
        }

        // Only do stuff if needed
        if (prioritizedTargets.isEmpty()) return;

        // Enumerate and sort priority groups
        int[] priorities = new int[prioritizedTargets.size()];
        int i = 0;
        for (int priority : prioritizedTargets.keySet()) {
            priorities[i++] = priority;
        }
        Arrays.sort(priorities);

        // Compute base filter
        boolean useFilters = !level.getGameRules().get(GameRuleRegistry.NO_PIPE_FILTER.get());
        Predicate<Object> alwaysTrue = a -> true;
        Predicate<Object> baseFilter = useFilters ? computePredicate() : alwaysTrue;

        // Iterate from highest to lowest priority
        int totalTransferred = 0;
        List<T> targetHandlers = new ArrayList<>(largestList);
        List<Predicate<Object>> targetFilters = new ArrayList<>(largestList);
        for (int j = priorities.length - 1; j >= 0 && totalTransferred < transferRate; j--) {
            int priority = priorities[j];
            List<Tuple<PipeConnection, T>> targets = prioritizedTargets.get(priority);

            // Get the handlers and the filters for the pipe connections
            for (Tuple<PipeConnection, T> tuple : targets) {
                targetHandlers.add(tuple.getB());
                Predicate<Object> targetFilter = useFilters ? tuple.getA().computePredicate() : alwaysTrue;
                targetFilters.add(targetFilter);
            }

            // Transactionally move the items
            try (Transaction transaction = Transaction.openRoot()) {
                if (mode == PipeConnectionMode.EXTRACT) {
                    totalTransferred += routingStrategy.routeExtract(
                            type.getHandlerWrapper(), transaction,
                            base, baseFilter,
                            targetHandlers, targetFilters,
                            0, transferRate - totalTransferred);
                } else {
                    totalTransferred += routingStrategy.routeInsert(
                            type.getHandlerWrapper(), transaction,
                            base, baseFilter,
                            targetHandlers, targetFilters,
                            0, transferRate - totalTransferred);
                }
            } catch (IllegalStateException e) {
                LOGGER.error("[PipeConnection] Could not open transaction for transferring items!", e);
            }

            // Clear lists for next iteration
            targetHandlers.clear();
            targetFilters.clear();
        }
    }
}
