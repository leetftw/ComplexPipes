package com.leetftw.complexpipes.common.util;

import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.function.Predicate;

public interface PipeHandlerWrapper<T> {
    int move(T from, T to, int amount, Predicate<Object> filter, TransactionContext transaction);
}
