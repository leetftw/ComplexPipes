package com.leetftw.complexpipes.common.util.routing;

import com.leetftw.complexpipes.common.util.PipeHandlerWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/// Default: choose an inventory, fill it, go to next inventory, etc.
public class DefaultRoutingStrategy extends BaseRoutingStrategy {

    @Override
    protected <T> int route(PipeHandlerWrapper<T> handlerWrapper, Transaction transaction, T base, Predicate<Object> baseFilter, List<T> targets, List<Predicate<Object>> targetFilters, int minTransfer, int maxTransfer, Function<Transfer<T>, Integer> transferFunction) {
        if (maxTransfer <= 0 || targets.isEmpty()) {
            transaction.close();
            return 0;
        }

        int amountTransferred = 0;
        for (int i = 0; i < targets.size(); i++) {
            T handler = targets.get(i);
            Transfer<T> t = new Transfer<>();
            t.handlerWrapper = handlerWrapper;
            t.transaction = transaction;
            t.base = base;
            t.target = handler;
            t.filter = baseFilter.and(targetFilters.get(i));
            t.desiredAmount = maxTransfer - amountTransferred;
            amountTransferred += transferFunction.apply(t);

            if (amountTransferred >= maxTransfer)
                break;
        }

        assert amountTransferred <= maxTransfer;
        if (amountTransferred >= minTransfer) {
            transaction.commit();
            transaction.close();
            return amountTransferred;
        }

        transaction.close();
        return 0;
    }

    @Override
    public String getId() {
        return "default";
    }
}
