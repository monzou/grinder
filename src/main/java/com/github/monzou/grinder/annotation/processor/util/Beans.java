package com.github.monzou.grinder.annotation.processor.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import com.github.monzou.grinder.annotation.processor.exception.PropertyAccessRuntimeException;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * Bean utilities
 */
public final class Beans {

    private static Cache<Class<?>, Map<String, PropertyDescriptor>> CACHE;
    static {
        CACHE = CacheBuilder.newBuilder().concurrencyLevel(Runtime.getRuntime().availableProcessors()).weakKeys().build();
    }

    private static final char PATH_SEPARATOR = '.';

    public static Object getProperty(Object bean, String propertyName) {

        int p = propertyName.indexOf(PATH_SEPARATOR);
        if (p > 0) {
            Object property = getProperty(bean, propertyName.substring(0, p));
            if (property == null) {
                return null;
            }
            return getProperty(property, propertyName.substring(p + 1));
        }

        Class<?> clazz = bean.getClass();
        PropertyDescriptor descriptor = getPropertyDescriptor(clazz, propertyName);
        Method method = descriptor.getReadMethod();
        if (method == null) {
            String message = String.format("ReadMethod not found: class=%s, property=%s", clazz.getName(), propertyName);
            throw new PropertyAccessRuntimeException(message);
        }
        return invoke(bean, method);

    }

    public static void setProperty(Object bean, String propertyName, Object value) {

        int p = propertyName.indexOf(PATH_SEPARATOR);
        if (p > 0) {
            Object property = getProperty(bean, propertyName.substring(0, p));
            setProperty(property, propertyName.substring(p + 1), value);
            return;
        }

        Class<?> clazz = bean.getClass();
        PropertyDescriptor descriptor = getPropertyDescriptor(clazz, propertyName);
        Method method = descriptor.getWriteMethod();
        if (method == null) {
            String message = String.format("WriteMethod not found: class=%s, property=%s", clazz.getName(), propertyName);
            throw new PropertyAccessRuntimeException(message);
        }
        invoke(bean, method, new Object[] { value });

    }

    @VisibleForTesting
    static PropertyDescriptor getPropertyDescriptor(Class<?> clazz, String propertyName) {
        PropertyDescriptor propertyDescriptor = null;
        int p = propertyName.indexOf(PATH_SEPARATOR);
        if (p > 0) {
            String property = propertyName.substring(0, p);
            propertyDescriptor = getPropertyDescriptor(clazz, property);
            if (propertyDescriptor != null) {
                return getPropertyDescriptor(propertyDescriptor.getPropertyType(), propertyName.substring(p + 1));
            }
        } else {
            propertyDescriptor = getPropertyDescriptors(clazz).get(propertyName);
        }
        if (propertyDescriptor == null) {
            String message = String.format("Property not found: class=%s, property=%s", clazz.getName(), propertyName);
            throw new PropertyAccessRuntimeException(message);
        }
        return propertyDescriptor;
    }

    @VisibleForTesting
    static Map<String, PropertyDescriptor> getPropertyDescriptors(final Class<?> clazz) {
        try {
            return CACHE.get(clazz, new Callable<Map<String, PropertyDescriptor>>() {
                @Override
                public Map<String, PropertyDescriptor> call() throws Exception { // SUPPRESS CHECKSTYLE
                    Map<String, PropertyDescriptor> descriptors = Maps.newHashMap();
                    for (BeanInfo info : collectBeanInfo(clazz)) {
                        for (PropertyDescriptor propertyDescriptor : info.getPropertyDescriptors()) {
                            PropertyDescriptor existence = descriptors.get(propertyDescriptor.getName());
                            Class<?> existenceType = existence == null ? null : existence.getPropertyType();
                            if (existenceType == null || !propertyDescriptor.getPropertyType().isAssignableFrom(existenceType)) {
                                descriptors.put(propertyDescriptor.getName(), propertyDescriptor);
                            }
                        }
                    }
                    return ImmutableMap.copyOf(descriptors);
                }
            });
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<BeanInfo> collectBeanInfo(Class<?> clazz) {
        Builder<BeanInfo> builder = ImmutableList.builder();
        BeanInfo info = getBeanInfo(clazz);
        if (info != null) {
            builder.add(info);
        }
        for (Class<?> i : clazz.getInterfaces()) {
            if (Modifier.isPublic(i.getModifiers())) {
                builder.addAll(collectBeanInfo(i));
            }
        }
        return builder.build();
    }

    private static BeanInfo getBeanInfo(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        try {
            return Modifier.isPublic(clazz.getModifiers()) ? Introspector.getBeanInfo(clazz) : getBeanInfo(clazz.getSuperclass());
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }
    }

    private static Object invoke(Object o, Method method, Object... args) {
        try {
            return method.invoke(o, args);
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private Beans() {
    }

}
