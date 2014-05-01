package com.github.monzou.grinder.util;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.github.monzou.grinder.util.Strings2;

/**
 * Strings2Test
 */
public class Strings2Test {

    @Test
    public void testCapitalize() {
        assertThat(Strings2.capitalize(null), is(""));
        assertThat(Strings2.capitalize(""), is(""));
        assertThat(Strings2.capitalize("abc"), is("Abc"));
        assertThat(Strings2.capitalize("ABC"), is("ABC"));
        assertThat(Strings2.capitalize("Abc"), is("Abc"));
    }

    @Test
    public void testUncapitalize() {
        assertThat(Strings2.uncapitalize(null), is(""));
        assertThat(Strings2.uncapitalize(""), is(""));
        assertThat(Strings2.uncapitalize("abc"), is("abc"));
        assertThat(Strings2.uncapitalize("ABC"), is("aBC"));
        assertThat(Strings2.uncapitalize("Abc"), is("abc"));
    }

}
