package com.github.monzou.grinder.util;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

/**
 * Predicates2
 */
public final class Predicates2 {

    /**
     * Converts {@link Function} to {@link Predicate}
     * 
     * @param function {@link Function}
     * @return {@link Predicate}
     */
    public static final <T> Predicate<T> fromFunction(final Function<T, Boolean> function) {
        return new Predicate<T>() {
            @Override
            public boolean apply(T input) {
                Boolean bool = function.apply(input);
                if (bool == null) {
                    return false;
                }
                return bool.booleanValue() ? true : false;
            }
        };
    }

    private Predicates2() {
    }

}
