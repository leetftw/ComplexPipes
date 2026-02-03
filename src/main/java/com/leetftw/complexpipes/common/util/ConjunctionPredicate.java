package com.leetftw.complexpipes.common.util;

import java.util.function.Predicate;

// Predicate.and() generates a new object every time it is used
// This prevents allocations in routing strategy hot paths by reusing the same object
// As long as it is used on a single thread, it is safe to call from multiple call sites
public class ConjunctionPredicate implements Predicate<Object> {
    Predicate<Object> a;
    Predicate<Object> b;

    public void setPredicates(Predicate<Object> first, Predicate<Object> second) {
        this.a = first; this.b = second;
    }

    @Override
    public boolean test(Object obj) {
        return a.test(obj) && b.test(obj);
    }
}
