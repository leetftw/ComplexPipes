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
    protected <T> int route(PipeHandlerWrapper<T> handlerWrapper, Transaction transaction, T base, Predicate<Object> baseFilter, List<T> targets, List<Predicate<Object>> targetFilters, int minTransfer, int maxTransfer, Function<Transfer<T>, Integer> transferFunction) {
        // Reset when size of list changes
        if (previousConnectionCount != targets.size()) {
            insertionIndex = 0;
        }

        // Reset when coming to end
        if (insertionIndex >= previousConnectionCount) {
            insertionIndex = 0;
        }

        if (maxTransfer <= 0 || targets.isEmpty()) {
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
        int amountTransferred = 0;
        int currentIndex = insertionIndex;
        int targetCount = targets.size();
        while (amountTransferred == 0 && (currentIndex + 1) % targetCount != insertionIndex) {
            T handler = targets.get(insertionIndex);
            Transfer<T> t = new Transfer<>();
            t.handlerWrapper = handlerWrapper;
            t.transaction = transaction;
            t.base = base;
            t.target = handler;
            t.filter = baseFilter.and(targetFilters.get(insertionIndex));
            t.desiredAmount = maxTransfer;
            amountTransferred = transferFunction.apply(t);
            currentIndex = (currentIndex + 1) % targetCount;
        }

        assert amountTransferred <= maxTransfer;
        if (amountTransferred >= minTransfer) {
            previousConnectionCount = targets.size();
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
