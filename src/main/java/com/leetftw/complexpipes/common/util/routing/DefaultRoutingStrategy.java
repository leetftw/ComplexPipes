package com.leetftw.complexpipes.common.util.routing;

import com.leetftw.complexpipes.common.util.PipeHandlerWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.List;
import java.util.function.Predicate;

/// Default: choose an inventory, fill it, go to next inventory, etc.
public class DefaultRoutingStrategy extends BaseRoutingStrategy {

    @Override
    protected <T> int route(Transaction transaction, PipeHandlerWrapper<T> handlerWrapper, T base, Predicate<Object> baseFilter, TargetBatch<T> targets, int minTransfer, int maxTransfer, TransferFunction transferFunction) {
        List<T> handlers = targets.handlers();
        if (maxTransfer <= 0 || handlers.isEmpty()) {
            transaction.close();
            return 0;
        }

        int amountTransferred = 0;
        List<Predicate<Object>> filters = targets.filters();
        for (int i = 0; i < handlers.size(); i++) {
            T targetHandler = handlers.get(i);
            CONJUNCTION.setPredicates(baseFilter, filters.get(i));
            amountTransferred += transferFunction.transfer(
                    handlerWrapper,
                    base, targetHandler,
                    maxTransfer - amountTransferred,
                    CONJUNCTION,
                    transaction
            );

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
