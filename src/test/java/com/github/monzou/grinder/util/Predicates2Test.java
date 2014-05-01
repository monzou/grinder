package com.github.monzou.grinder.util;

import static org.junit.Assert.*;

import org.junit.Test;

import com.google.common.base.Function;

/**
 * Predicates2Test
 */
public class Predicates2Test {

    @Test
    public void testFromFunction() {

        Function<String, Boolean> function = new Function<String, Boolean>() {
            @Override
            public Boolean apply(String input) {
                return input != null && input.contains("foo");
            }
        };

        assertFalse(Predicates2.fromFunction(function).apply(null));
        assertFalse(Predicates2.fromFunction(function).apply(""));
        assertTrue(Predicates2.fromFunction(function).apply("foo"));
        assertFalse(Predicates2.fromFunction(function).apply("bar"));
        assertTrue(Predicates2.fromFunction(function).apply("^foo$"));

    }

}
