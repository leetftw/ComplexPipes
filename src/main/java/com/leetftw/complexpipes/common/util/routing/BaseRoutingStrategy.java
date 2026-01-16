package com.leetftw.complexpipes.common.util.routing;

import com.leetftw.complexpipes.common.util.PipeHandlerWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class BaseRoutingStrategy {
    protected class Transfer<T> {
        PipeHandlerWrapper<T> handlerWrapper;
        Transaction transaction;
        T base;
        T target;
        Predicate<Object> filter;
        int desiredAmount;

        int performInsert() {
            return handlerWrapper.move(target, base, desiredAmount, filter, transaction);
        }

        int performExtract() {
            return handlerWrapper.move(base, target, desiredAmount, filter, transaction);
        }
    }

    protected abstract <T> void route(
            PipeHandlerWrapper<T> handlerWrapper,
            Transaction transaction,
            T base,
            Predicate<Object> baseFilter,
            List<T> targets,
            List<Predicate<Object>> targetFilters,
            int minTransfer,
            int maxTransfer,
            Function<Transfer<T>, Integer> transferFunction);

    public final <T> void routeExtract(
            PipeHandlerWrapper<T> handlerWrapper,
            Transaction transaction,
            T base,
            Predicate<Object> baseFilter,
            List<T> targets,
            List<Predicate<Object>> targetFilters,
            int minTransfer,
            int maxTransfer) {
        route(handlerWrapper, transaction, base, baseFilter, targets, targetFilters, minTransfer, maxTransfer, Transfer::performExtract);
    }

    public final <T> void routeInsert(
            PipeHandlerWrapper<T> handlerWrapper,
            Transaction transaction,
            T base,
            Predicate<Object> baseFilter,
            List<T> targets,
            List<Predicate<Object>> targetFilters,
            int minTransfer,
            int maxTransfer) {
        route(handlerWrapper, transaction, base, baseFilter, targets, targetFilters, minTransfer, maxTransfer, Transfer::performInsert);
    }

    public abstract String getId();

    public static BaseRoutingStrategy create(String id) {
        switch (id) {
            case "default":
            default:
                return new DefaultRoutingStrategy();
        }
    }
}
