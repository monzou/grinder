package com.github.monzou.grinder.annotation.processor.util;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.beans.PropertyDescriptor;
import java.math.BigDecimal;
import java.util.Map;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import com.github.monzou.grinder.annotation.processor.exception.PropertyAccessRuntimeException;
import com.github.monzou.grinder.annotation.processor.util.Beans;

/**
 * BeansTest
 */
public class BeansTest {

    private N bean;

    @Before
    public void setUp() {
        bean = createBean();
    }

    @Test
    public void testGetProperty() {

        C2Impl value = (C2Impl) bean.getValue();

        assertThat((C2Impl) Beans.getProperty(bean, "value"), is(value));
        assertThat((BigDecimal) Beans.getProperty(bean, "value.value1"), is(new BigDecimal("123.45")));
        assertThat((Integer) Beans.getProperty(bean, "value.value2"), is(999));
        assertThat((String) Beans.getProperty(bean, "value.value3"), is("foo"));
        assertThat((N) Beans.getProperty(bean, "child"), is(bean.getChild()));
        assertThat((C2Impl) Beans.getProperty(bean, "child.value"), is(value));
        assertThat((BigDecimal) Beans.getProperty(bean, "child.value.value1"), is(new BigDecimal("123.45")));
        assertThat((Integer) Beans.getProperty(bean, "child.value.value2"), is(999));
        assertThat((String) Beans.getProperty(bean, "child.value.value3"), is("foo"));

        value.setValue2(100);

        assertThat((Integer) Beans.getProperty(bean, "value.value2"), is(100));
        assertThat((Integer) Beans.getProperty(bean, "child.value.value2"), is(100));

    }

    @Test(expected = PropertyAccessRuntimeException.class)
    public void testGetUnknownProperty() {
        Beans.getProperty(bean, "foo.bar");
    }

    @Test
    public void testSetProperty() {

        C2Impl value = (C2Impl) bean.getValue();

        Beans.setProperty(bean, "value.value1", new BigDecimal("999.99"));
        assertThat(value.getValue1(), is(new BigDecimal("999.99")));
        Beans.setProperty(bean, "value.value2", 111);
        assertThat(value.getValue2(), is(111));
        Beans.setProperty(bean, "value.value3", "bar");
        assertThat(value.getValue3(), is("bar"));

        Beans.setProperty(bean, "child.value.value1", new BigDecimal("123.45"));
        assertThat(value.getValue1(), is(new BigDecimal("123.45")));
        Beans.setProperty(bean, "child.value.value2", 999);
        assertThat(value.getValue2(), is(999));
        Beans.setProperty(bean, "child.value.value3", "foo");
        assertThat(value.getValue3(), is("foo"));

    }

    @Test(expected = PropertyAccessRuntimeException.class)
    public void testSetUnknownProperty() {
        Beans.getProperty(bean, "foo.bar");
    }

    @Test
    public void testGetPropertyDescriptors() {

        Map<String, PropertyDescriptor> descriptors = Beans.getPropertyDescriptors(C2.class);
        assertThat(descriptors.size(), is(3));
        assertThat(descriptors.get("value1").getPropertyType(), isSameClass(BigDecimal.class));
        assertThat(descriptors.get("value2").getPropertyType(), isSameClass(int.class));
        assertThat(descriptors.get("value3").getPropertyType(), isSameClass(String.class));

    }

    @SuppressWarnings("unchecked")
    private Matcher<Class<?>> isSameClass(Class<?> clazz) {
        return (Matcher<Class<?>>) (Matcher<?>) is((Object) clazz);
    }

    private N createBean() {

        N bean = new N();
        C2Impl value = new C2Impl();
        value.setValue1(new BigDecimal("123.45"));
        value.setValue2(999);
        value.setValue3("foo");
        bean.setValue(value);

        N child = new N();
        child.setValue(value);

        bean.setChild(child);

        return bean;

    }

    public static class N {

        private P value;

        private N child;

        public P getValue() {
            return value;
        }

        public void setValue(P value) {
            this.value = value;
        }

        public N getChild() {
            return child;
        }

        public void setChild(N child) {
            this.child = child;
        }

    }

    public interface P {

        Object getValue1();

    }

    public interface C1 extends P {

        Number getValue1();

        int getValue2();

    }

    public interface C2 extends C1 {

        BigDecimal getValue1();

        String getValue3();

    }

    public static class C2Impl implements C2 {

        private BigDecimal value1;

        private int value2;

        private String value3;

        public BigDecimal getValue1() {
            return value1;
        }

        public void setValue1(BigDecimal value1) {
            this.value1 = value1;
        }

        public int getValue2() {
            return value2;
        }

        public void setValue2(int value2) {
            this.value2 = value2;
        }

        public String getValue3() {
            return value3;
        }

        public void setValue3(String value3) {
            this.value3 = value3;
        }

    }

}
