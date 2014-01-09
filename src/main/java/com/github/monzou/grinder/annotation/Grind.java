package com.github.monzou.grinder.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Grind Java Beans !
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface Grind {

}
