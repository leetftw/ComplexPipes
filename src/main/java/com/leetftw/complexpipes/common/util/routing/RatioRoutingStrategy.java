package com.leetftw.complexpipes.common.util.routing;

import com.leetftw.complexpipes.common.util.PipeHandlerWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class RatioRoutingStrategy extends BaseRoutingStrategy {
    public enum RatioPolicy {
        STRICT, // Only transfer when exactly maxItems - (maxItems mod ratioSum) resources are transferred to all handlers with their respective ratios == O(n)
        RELAXED_ITERATIVE, // Accumulate the amount of resources not successfully transferred and perform more iterations over remaining targets (which did accept) until no items are left over == O(n^2) worst case, in practice O(n log n) or even O(n)
        BEST_EFFORT // Attempt to do a ratio transfer, but do not enforce it (breaks ratio promise, maximizes throughput) == O(n)
    }

    private RatioPolicy policy = RatioPolicy.STRICT;

    @Override
    protected <T> int route(Transaction transaction, PipeHandlerWrapper<T> handlerWrapper, T base, Predicate<Object> baseFilter, TargetBatch<T> targets, int priorityLevel, int minTransfer, int maxTransfer, TransferFunction transferFunction) {
        // Expand target batch
        List<T> targetHandlers = targets.handlers();
        // If there are no targets, escape early
        if (targetHandlers.isEmpty())
            return 0;

        List<Predicate<Object>> targetFilters = targets.filters();
        List<Integer> targetRatios = targets.ratios();

        // Sum up all ratios
        int ratioSum = 0;
        for (int ratio : targetRatios)
            ratioSum += ratio;

        // If sum of ratios is 0, escape early
        if (ratioSum == 0)
            return 0;

        // Calculate desired total: min(maxTransfer, (possible poll base for count))
        int desiredTotal = Integer.min(maxTransfer, handlerWrapper.getCount(base, baseFilter));

        // If STRICT, desiredTotal % sumRatios MUST be zero, otherwise try clamping it down, otherwise escape early
        int totalRemainder = desiredTotal % ratioSum;
        if (policy == RatioPolicy.STRICT && totalRemainder != 0) {
            desiredTotal = desiredTotal - (totalRemainder);
            if (desiredTotal == 0)
                return 0;
        }

        // All targets start out as active
        int[] activeList = new int[targetHandlers.size()];
        int totalCount = targetHandlers.size();
        AtomicInteger activeCount = new AtomicInteger(totalCount);
        for (int i = 0; i < activeCount.get(); i++) activeList[i] = i;

        // Java still has no nested functions as of 2026
        Consumer<Integer> deactivate = (Integer index) -> {
            for (int i = 0; i < activeCount.get(); i++) {
                if (activeList[i] == index) {
                    activeList[i] = activeList[activeCount.get() - 1];
                    activeCount.getAndDecrement();
                    return;
                }
            }
        };

        //   Push a transaction context
        Transaction subTransaction = Transaction.open(transaction);

        //   Variables used in loop
        int[] actual = new int[totalCount];
        int[] quotas = new int[totalCount];
        int totalTransferred = 0;

        // Main loop (while true)
        while (true) {
            //   Recompute ratio sum
            ratioSum = 0;
            for (int ratio : targetRatios)
                ratioSum += ratio;

            // If sum of ratios is 0, escape early
            if (ratioSum == 0) {
                subTransaction.close();
                return 0;
            }

            //   Compute quotas (quota = distribution of transfer to active targets)
            int quotaSum = 0;
            for (int i = 0; i < activeCount.get(); i++) {
                int index = activeList[i];
                quotas[index] = desiredTotal * targetRatios.get(index) / ratioSum;
                quotaSum += quotas[index];
            }

            //   Note that at this point, if policy is strict, quotaSum % ratioSum = 0
            assert !(policy == RatioPolicy.STRICT && quotaSum % ratioSum != 0);
            //   Also, the remainder at this point should be equivalent to modular difference between the quota and ratios
            //assert quotaSum % ratioSum == desiredTotal - quotaSum;

            //   Redistribute remainder
            int quotaRemainder = desiredTotal - quotaSum;
            if (quotaRemainder > 0 && policy == RatioPolicy.BEST_EFFORT) {
                // First distribute evenly
                int perTarget = quotaRemainder / activeCount.get();
                if (perTarget > 0) {
                    for (int i = 0; i < activeCount.get(); i++) {
                        int index = activeList[i];
                        quotas[index] += perTarget;
                        quotaSum += perTarget;
                    }
                }
                // Then add 1 to the first few targets
                int remainderOfRemainder = quotaRemainder % activeCount.get();
                if (remainderOfRemainder > 0) {
                    for (int i = 0; i < remainderOfRemainder; i++) {
                        int index = activeList[i];
                        quotas[index]++;
                        quotaSum++;
                    }
                }
            }

            // For STRICT: we should have left earlier if a distribution was not possible
            // For BEST_EFFOT: we just finished distributing the remainder so we should not have any left
            assert desiredTotal == quotaSum || policy == RatioPolicy.RELAXED_ITERATIVE;

            //   Attempt to transfer the quota amounts to the targets (keep track of how much actually got transferred)
            int actualSum = 0;
            for (int i = 0; i < activeCount.get(); i++) {
                int index = activeList[i];
                int q = quotas[index];
                if (q <= 0) continue;

                CONJUNCTION.setPredicates(baseFilter, targetFilters.get(index));
                actual[index] = transferFunction.transfer(handlerWrapper, base, targetHandlers.get(index), q, CONJUNCTION, subTransaction);
                actualSum += actual[index];
            }

            totalTransferred += actualSum;

            //   If policy == BEST_EFFORT, commit transaction and escape early
            //   If all quotas were satisfied, commit transaction and escape early
            if (policy == RatioPolicy.BEST_EFFORT || actualSum == desiredTotal) {
                subTransaction.commit();
                subTransaction.close();
                return totalTransferred;
            }

            //   If not, and policy == STRICT:
            else if (policy == RatioPolicy.STRICT) {
                //     First try lowering desiredTotal to next lowest multiple of sumRatios
                desiredTotal -= ratioSum;
                //     If no such multiple exists, discard all transfers and escape early
                if (desiredTotal == 0) {
                    subTransaction.close();
                    return 0;
                }
                //     If it does, go back to start of loop (pop and push new transaction context first)
                subTransaction.close();
                subTransaction = Transaction.open(transaction);
                continue;
            }

            //  Note to self: rest of code path is for RELAXED_ITERATIVE
            assert policy == RatioPolicy.RELAXED_ITERATIVE;

            //  Deactivate all connections which did not accept their full quota (take note of the sum)
            for (int i = 0; i < activeCount.get(); i++) {
                int index = activeList[i];
                if (actual[index] < quotas[index]) {
                    deactivate.accept(i--);
                }
            }

            //  If all connections did not accept full quota, commit transaction and escape early
            if (activeCount.get() == 0) {
                subTransaction.commit();
                subTransaction.close();
                return totalTransferred;
            }

            //  Set desiredTotal = remaining
            desiredTotal -= actualSum;

            //  Pop transaction context and go back to start of loop
            //  Actually: don't. We need to redistribute the remainder, so we have to be in same context
        }

        // If at this point in time we are still in the function, throw an exception
        //throw new IllegalStateException("[RatioRoutingStrategy] Escaped main routing loop, this is a bug.");
    }

    @Override
    public String getId() {
        return "ratio";
    }
}
