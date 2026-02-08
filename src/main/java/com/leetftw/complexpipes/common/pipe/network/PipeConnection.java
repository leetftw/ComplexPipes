package com.leetftw.complexpipes.common.pipe.network;

import com.leetftw.complexpipes.common.cards.PipeCard;
import com.leetftw.complexpipes.common.items.ItemComponentRegistry;
import com.leetftw.complexpipes.common.items.ItemRegistry;
import com.leetftw.complexpipes.common.pipe.types.PipeType;
import com.leetftw.complexpipes.common.pipe.types.PipeTypeRegistry;
import com.leetftw.complexpipes.common.cards.PipeUpgrade;
import com.leetftw.complexpipes.common.tests.GameRuleRegistry;
import com.leetftw.complexpipes.common.util.routing.BaseRoutingStrategy;
import com.leetftw.complexpipes.common.util.routing.DefaultRoutingStrategy;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.leetftw.complexpipes.common.ComplexPipes.LOGGER;

public class PipeConnection {
    public static final Codec<PipeConnection> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("type").forGetter(a -> a.TYPE.getRegisteredId()),
                    BlockPos.CODEC.fieldOf("position").forGetter(PipeConnection::getPipePos),
                    Direction.CODEC.fieldOf("direction").forGetter(PipeConnection::getSide),
                    Codec.STRING.fieldOf("mode").forGetter(a -> a.getMode().name()),
                    Codec.INT.fieldOf("priority").forGetter(PipeConnection::getPriority),
                    Codec.INT.fieldOf("ratio").forGetter(PipeConnection::getRatio),
                    CompoundTag.CODEC.fieldOf("routingStrategy").forGetter(a -> a.getRoutingStrategy().serialize()),
                    Codec.INT.fieldOf("tickCount").forGetter(a -> a.tickCount),
                    Codec.list(Codec.pair(Codec.intRange(0, 255).fieldOf("slot").codec(), PipeUpgrade.CODEC.fieldOf("upgrade").codec())).fieldOf("upgrades").forGetter(a -> {
                        List<Pair<Integer, PipeCard>> pairs = new ArrayList<>();
                        for (int i = 0; i < a.pipeCards.length; i++)
                            if (a.pipeCards[i] != null)
                                pairs.add(new Pair<>(i, a.pipeCards[i]));
                        return pairs;
                    })
            ).apply(instance, PipeConnection::new)
    );

    private BlockPos pipePos;
    private Direction side;

    private PipeConnectionMode mode = PipeConnectionMode.PASSIVE;
    private int priority = 0;
    private int ratio = 1;
    private BaseRoutingStrategy routingStrategy = new DefaultRoutingStrategy();
    private int tickCount = 0;
    private final PipeType<?> TYPE;
    private final int MAX_CARDS;
    private final PipeCard[] pipeCards;

    private Predicate<Object> predicate;

    private boolean dirty = false;

    public PipeConnection(PipeType<?> type, BlockPos pos, Direction side) {
        this.pipePos = pos;
        this.side = side;
        this.TYPE = type;
        this.MAX_CARDS = TYPE.getMaxCards();
        this.pipeCards = new PipeCard[MAX_CARDS];
    }

    private PipeConnection(String type, BlockPos pos, Direction side, String mode, int priority, int ratio, CompoundTag routingStrategy, int tickCount, List<Pair<Integer, PipeCard>> cards) {
        this.pipePos = pos;
        this.side = side;
        this.mode = PipeConnectionMode.valueOf(mode);
        this.priority = priority;
        this.ratio = ratio;
        this.routingStrategy = BaseRoutingStrategy.create(routingStrategy);
        this.tickCount = tickCount;

        this.TYPE = PipeTypeRegistry.getType(type);
        this.MAX_CARDS = TYPE.getMaxCards();
        this.pipeCards = new PipeCard[MAX_CARDS];

        for (Pair<Integer, PipeCard> storedCard : cards) {
            if (storedCard.getFirst() >= MAX_CARDS) {
                // This can happen due to config changes
                // Void items for now, maybe drop into world in the future?
                LOGGER.warn("[PipeConnection] Found card with invalid slot index {} while decoding!.", storedCard.getFirst());
                continue;
            }
            this.pipeCards[storedCard.getFirst()] = storedCard.getSecond();
        }
        this.dirty = true;
    }

    public BlockPos getPipePos() {
        return pipePos;
    }

    public void overwritePipePos(BlockPos newPos) {
        this.pipePos = newPos;
    }

    public void overwriteSide(Direction value) {
        this.side = value;
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

    public void setMode(PipeConnectionMode mode) {
        this.mode = mode;
        setDirty();
    }

    public void setPriority(int priority) {
        this.priority = priority;
        setDirty();
    }

    public void setRatio(int ratio) {
        this.ratio = ratio;
        setDirty();
    }

    public Stream<PipeCard> getCardStream() {
        return Arrays.stream(pipeCards);
    }

    public PipeCard getCardInSlot(int slot) {
        return pipeCards[slot];
    }

    public void setCardInSlot(int slot, PipeCard card) {
        pipeCards[slot] = card;
        if (card.getRoutingStrategyId() != null) {
            findRoutingStrategy();
        }
        setDirty();
    }

    private void findRoutingStrategy() {
        String id = "default";
        for (PipeCard card : pipeCards) {
            if (card == null) continue;

            String cardStrategy = card.getRoutingStrategyId();
            if (cardStrategy != null) {
                id = cardStrategy;
                break;
            }
        }

        if (!id.equals(routingStrategy.getId())) {
            routingStrategy = BaseRoutingStrategy.create(id);
        }
    }

    public void removeCardInSlot(int slot) {
        PipeCard upgrade = pipeCards[slot];
        pipeCards[slot] = null;
        setDirty();
        if (upgrade.getRoutingStrategyId() != null) {
            findRoutingStrategy();
        }
    }

    public boolean mayAddCard(PipeCard card) {
        // Max 6 card
        if (Arrays.stream(pipeCards).filter(Objects::nonNull).count() == MAX_CARDS)
            return false;

        // Adding this card should not exceed max upgrades for this type
        if (card.getMaxInstalledCount() == Arrays.stream(pipeCards).filter(Objects::nonNull)
                .filter(existingUpgrade -> existingUpgrade.getType() == card.getType()).count())
            return false;

        // The card should be supported by the pipe
        if (!TYPE.supportsCard(card))
            return false;

        // TODO: The card should be compatible with the other cards

        return true;
    }

    public boolean tryAddCard(PipeCard card) {
        if (!mayAddCard(card)) return false;

        int firstEmptyIndex = -1;
        for (int i = 0; i < MAX_CARDS; i++) {
            if (pipeCards[i] == null) {
                firstEmptyIndex = i;
                break;
            }
        }
        if (firstEmptyIndex < 0) return false;

        pipeCards[firstEmptyIndex] = card;
        findRoutingStrategy();

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
        return Arrays.stream(pipeCards).filter(Objects::nonNull)
                .filter(a -> a instanceof PipeUpgrade)
                .map(a -> (PipeUpgrade) a)
                .map(PipeUpgrade::getTransferAmountMultiplier)
                .reduce((double) TYPE.getDefaultTransferAmount(), (a, b) -> a * b)
                .intValue();
    }

    public long calculateOperationTime() {
        return Long.max(1, Math.round(Arrays.stream(pipeCards).filter(Objects::nonNull)
                .filter(a -> a instanceof PipeUpgrade)
                .map(a -> (PipeUpgrade) a)
                .map(PipeUpgrade::getTransferIntervalMultiplier)
                .reduce((double) TYPE.getDefaultTransferSpeed(), (a, b) -> a * b)));
    }

    public double calculateResourcesPerTick() {
        return (double) calculateTransferRate() / (double) calculateOperationTime();
    }

    public void appendItems(List<ItemStack> items) {
        for (PipeCard card : pipeCards) {
            if (card == null) continue;

            ItemStack stack = new ItemStack(card.getType().getItem(), 1);
            stack.set(ItemComponentRegistry.PIPE_CARD_DATA, card);
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
    }

    public Predicate<Object> computePredicate() {
        if (predicate == null) {
            predicate = object -> {
                for (PipeCard card : pipeCards) {
                    if (card == null)
                        continue;
                    if (card.isFilter() && !card.allowResourceTransfer(object))
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

        // Cached lists
        List<T> targetHandlers = new ArrayList<>(largestList);
        List<Predicate<Object>> targetFilters = new ArrayList<>(largestList);
        List<Integer> targetRatios = new ArrayList<>(largestList);
        BaseRoutingStrategy.TargetBatch<T> targetBatch = new BaseRoutingStrategy.TargetBatch<>(
                targetHandlers,
                targetFilters,
                targetRatios
        );

        // Transactionally move the items
        try (Transaction transaction = Transaction.openRoot()) {
            for (int j = priorities.length - 1; j >= 0 && totalTransferred < transferRate; j--) {
                int priority = priorities[j];
                List<Tuple<PipeConnection, T>> targets = prioritizedTargets.get(priority);

                // Get the handlers and the filters for the pipe connections
                for (Tuple<PipeConnection, T> tuple : targets) {
                    targetHandlers.add(tuple.getB());
                    Predicate<Object> targetFilter = useFilters ? tuple.getA().computePredicate() : alwaysTrue;
                    targetFilters.add(targetFilter);
                    targetRatios.add(tuple.getA().getRatio());
                }

                if (mode == PipeConnectionMode.EXTRACT) {
                    totalTransferred += routingStrategy.routeExtract(
                            transaction, type.getHandlerWrapper(),
                            base, baseFilter,
                            targetBatch, priority,
                            0, transferRate - totalTransferred);
                } else {
                    totalTransferred += routingStrategy.routeInsert(
                            transaction, type.getHandlerWrapper(),
                            base, baseFilter,
                            targetBatch, priority,
                            0, transferRate - totalTransferred);
                }

                // Clear lists for next iteration
                targetHandlers.clear();
                targetFilters.clear();
                targetRatios.clear();
            }

            if (totalTransferred > transferRate || totalTransferred < 0) {
                LOGGER.error("[PipeConnection] Attempt to transfer amount of resources outside of range!");
            } else {
                transaction.commit();
            }
        } catch (IllegalStateException e) {
            LOGGER.error("[PipeConnection] Could not open transaction for transferring items!", e);
        }
    }

    public PipeType<?> getType() {
        return TYPE;
    }

    public int getMaxCards() {
        return MAX_CARDS;
    }
}
