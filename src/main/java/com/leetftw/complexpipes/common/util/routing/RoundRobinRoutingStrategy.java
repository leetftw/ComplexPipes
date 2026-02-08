package com.leetftw.complexpipes.common.util.routing;

import com.leetftw.complexpipes.common.util.PipeHandlerWrapper;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class RoundRobinRoutingStrategy extends BaseRoutingStrategy {
    int previousConnectionCount = -1;
    int previousPriority = Integer.MAX_VALUE;
    int insertionIndex = 0;

    @Override
    protected <T> int route(Transaction transaction, PipeHandlerWrapper<T> handlerWrapper, T base, Predicate<Object> baseFilter, TargetBatch<T> targets, int priorityLevel, int minTransfer, int maxTransfer, TransferFunction transferFunction) {
        List<T> handlers = targets.handlers();

        // Reset when size of list changes
        if (previousConnectionCount != handlers.size()) {
            insertionIndex = 0;
        }

        // Reset when priority changes
        if (previousPriority != priorityLevel) {
            insertionIndex = 0;
        }

        // Leave early if transfer is not valid
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
        List<Predicate<Object>> filters = targets.filters();
        int amountTransferred = 0;
        int targetCount = handlers.size();

        Transaction subTransaction = Transaction.open(transaction);
        for (int attempts = 0; attempts < targetCount; attempts++) {
            // Set index based on insertionIndex, wrap around targetCount
            int index = (insertionIndex + attempts) % targetCount;

            CONJUNCTION.setPredicates(baseFilter, filters.get(index));

            amountTransferred = transferFunction.transfer(
                    handlerWrapper,
                    base,
                    handlers.get(index),
                    maxTransfer,
                    CONJUNCTION,
                    subTransaction
            );

            if (amountTransferred > 0) {
                insertionIndex = (index + 1) % targetCount;
                break;
            }
        }

        assert amountTransferred <= maxTransfer;
        if (amountTransferred >= minTransfer) {
            previousConnectionCount = handlers.size();
            previousPriority = priorityLevel;
            subTransaction.commit();
            subTransaction.close();
            return amountTransferred;
        }

        subTransaction.close();
        return 0;
    }

    @Override
    public String getId() {
        return "round_robin";
    }

    @Override
    protected void saveAdditional(CompoundTag data) {
        data.putInt("previous_count", previousConnectionCount);
        data.putInt("previous_priority", previousPriority);
        data.putInt("insertion_index", insertionIndex);
    }

    @Override
    protected void loadAdditional(CompoundTag data) {
        Optional<Integer> previousCountOpt = data.getInt("previous_count");
        previousCountOpt.ifPresent(integer -> previousConnectionCount = integer);

        Optional<Integer> previousPriorityOpt = data.getInt("previous_priority");
        previousPriorityOpt.ifPresent(integer -> previousPriority = integer);

        Optional<Integer> insertIndexOpt = data.getInt("insertion_index");
        insertIndexOpt.ifPresent(integer -> insertionIndex = integer);
    }
}
