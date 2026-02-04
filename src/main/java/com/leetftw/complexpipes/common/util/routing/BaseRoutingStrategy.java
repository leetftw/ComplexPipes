package com.leetftw.complexpipes.common.util.routing;

import com.leetftw.complexpipes.common.util.ConjunctionPredicate;
import com.leetftw.complexpipes.common.util.PipeHandlerWrapper;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public abstract class BaseRoutingStrategy {
    public record TargetBatch<T>(List<T> handlers, List<Predicate<Object>> filters, List<Integer> ratios) { }

    @FunctionalInterface
    protected interface TransferFunction {
        <T> int transfer(
                PipeHandlerWrapper<T> handlerWrapper,
                T base, T target,
                int desiredAmount,
                Predicate<Object> filter,
                Transaction transaction);
    }

    private static <T> int insertFunction(@NonNull PipeHandlerWrapper<T> handlerWrapper,
                                          T base, T target,
                                          int desiredAmount,
                                          Predicate<Object> filter,
                                          Transaction transaction) {
        return handlerWrapper.move(target, base, desiredAmount, filter, transaction);
    }

    private static final TransferFunction EXTRACT = PipeHandlerWrapper::move;
    private static final TransferFunction INSERT = BaseRoutingStrategy::insertFunction;

    protected static final ConjunctionPredicate CONJUNCTION = new ConjunctionPredicate();

    // TODO: Remove minTransfer as it is handled by PipeConnection::tick already
    protected abstract <T> int route(
            Transaction transaction,
            PipeHandlerWrapper<T> handlerWrapper,
            T base, Predicate<Object> baseFilter,
            TargetBatch<T> targets, int priorityLevel,
            int minTransfer, int maxTransfer,
            TransferFunction transferFunction);

    public final <T> int routeExtract(
            Transaction transaction,
            PipeHandlerWrapper<T> handlerWrapper,
            T base, Predicate<Object> baseFilter,
            TargetBatch<T> targets, int priorityLevel,
            int minTransfer, int maxTransfer) {
        return route(transaction, handlerWrapper, base, baseFilter, targets, priorityLevel,  minTransfer, maxTransfer, EXTRACT);
    }

    public final <T> int routeInsert(
            Transaction transaction,
            PipeHandlerWrapper<T> handlerWrapper,
            T base, Predicate<Object> baseFilter,
            TargetBatch<T> targets, int priorityLevel,
            int minTransfer, int maxTransfer) {
        return route(transaction, handlerWrapper, base, baseFilter, targets, priorityLevel, minTransfer, maxTransfer, INSERT);
    }

    protected void saveAdditional(CompoundTag data) { }
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", getId());
        CompoundTag dataTag = new CompoundTag();
        saveAdditional(dataTag);
        tag.put("data", dataTag);
        return tag;
    }
    public abstract String getId();

    public static BaseRoutingStrategy create(CompoundTag tag) {
        String id = tag.getStringOr("id", "default");
        return switch (id) {
            case "round_robin" -> {
                Optional<CompoundTag> data = tag.getCompound("data");
                yield data.map(RoundRobinRoutingStrategy::create).orElseGet(RoundRobinRoutingStrategy::new);
            }
            case "ratio" -> new RatioRoutingStrategy();
            default -> new DefaultRoutingStrategy();
        };
    }
}
