package com.leetftw.complexpipes.common.util.routing;

import com.leetftw.complexpipes.common.util.PipeHandlerWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class RoundRobinRoutingStrategy extends BaseRoutingStrategy {
    int previousConnectionCount = -1;
    int insertionIndex = 0;

    @Override
    protected <T> int route(Transaction transaction, PipeHandlerWrapper<T> handlerWrapper, T base, Predicate<Object> baseFilter, TargetBatch<T> targets, int minTransfer, int maxTransfer, TransferFunction transferFunction) {
        List<T> handlers = targets.handlers();

        // Reset when size of list changes
        if (previousConnectionCount != handlers.size()) {
            insertionIndex = 0;
        }

        // Reset when coming to end
        if (insertionIndex >= previousConnectionCount) {
            insertionIndex = 0;
        }

        if (maxTransfer <= 0 || handlers.isEmpty()) {
            transaction.close();
            return 0;
        }

        // Round-robin
        // Cycle between interfaces
        // If interface doesnt accept, skip it and go to next
        // Respects priority:
        //    Interface 1: Priority 1
        //    Interface 2: Priority 1
        //    Interface 3: Priority 2
        //    Interface 4: Priority 2
        //  Cycle: 3-4
        //  Alternative cycle when both 3 and 4 do not accept: 1-2

        // TODO: Check validity of this loop
        List<Predicate<Object>> filters = targets.filters();
        int amountTransferred = 0;
        int currentIndex = insertionIndex;
        int targetCount = handlers.size();
        while (amountTransferred == 0 && (currentIndex + 1) % targetCount != insertionIndex) {
            T targetHandler = handlers.get(insertionIndex);
            CONJUNCTION.setPredicates(baseFilter, filters.get(insertionIndex));
            amountTransferred = transferFunction.transfer(
                    handlerWrapper,
                    base, targetHandler,
                    maxTransfer,
                    CONJUNCTION,
                    transaction
            );
            currentIndex = (currentIndex + 1) % targetCount;
        }

        assert amountTransferred <= maxTransfer;
        if (amountTransferred >= minTransfer) {
            previousConnectionCount = handlers.size();
            insertionIndex = currentIndex;
            transaction.commit();
            transaction.close();
            return amountTransferred;
        }

        transaction.close();
        return 0;
    }

    @Override
    public String getId() {
        return "round_robin";
    }
}
