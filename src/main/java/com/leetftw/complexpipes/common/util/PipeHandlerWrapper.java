package com.leetftw.complexpipes.common.util;

import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.function.Predicate;

public abstract class PipeHandlerWrapper<T> {
    public abstract int move(T from, T to, int amount, Predicate<Object> filter, TransactionContext transaction);
}
