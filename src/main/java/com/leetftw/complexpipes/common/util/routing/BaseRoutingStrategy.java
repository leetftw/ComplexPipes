package com.leetftw.complexpipes.common.util.routing;

import com.leetftw.complexpipes.common.pipe.network.PipeConnection;
import com.leetftw.complexpipes.common.util.ConjunctionPredicate;
import com.leetftw.complexpipes.common.util.PipeHandlerWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.Function;
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

    protected abstract <T> int route(
            Transaction transaction,
            PipeHandlerWrapper<T> handlerWrapper,
            T base, Predicate<Object> baseFilter,
            TargetBatch<T> targets,
            int minTransfer, int maxTransfer,
            TransferFunction transferFunction);

    public final <T> int routeExtract(
            Transaction transaction,
            PipeHandlerWrapper<T> handlerWrapper,
            T base, Predicate<Object> baseFilter,
            TargetBatch<T> targets,
            int minTransfer, int maxTransfer) {
        return route(transaction, handlerWrapper, base, baseFilter, targets, minTransfer, maxTransfer, EXTRACT);
    }

    public final <T> int routeInsert(
            Transaction transaction,
            PipeHandlerWrapper<T> handlerWrapper,
            T base, Predicate<Object> baseFilter,
            TargetBatch<T> targets,
            int minTransfer, int maxTransfer) {
        return route(transaction, handlerWrapper, base, baseFilter, targets, minTransfer, maxTransfer, INSERT);
    }

    public abstract String getId();

    public static BaseRoutingStrategy create(String id) {
        switch (id) {
            case "round_robin":
                return new RoundRobinRoutingStrategy();
            case "default":
            default:
                return new DefaultRoutingStrategy();
        }
    }
}
