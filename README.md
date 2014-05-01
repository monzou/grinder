# Grinder - Grind Java Beans by APT

Grinder provides bean property accessors by APT

[![Build Status](https://travis-ci.org/monzou/grinder.png)](https://travis-ci.org/monzou/grinder)

## Motivation

Sometimes Java programmers have to write ```com.google.common.base.Function``` to access bean property. This is necessary but it's boring. So we made Grinder to generate bean meta classes automatically. Grinder provides following functions:

* [BeanPropertyAccessor](https://github.com/monzou/grinder/blob/master/src/main/java/com/github/monzou/grinder/BeanPropertyAccessor.java) to read-only property
* [BeanProperty](https://github.com/monzou/grinder/blob/master/src/main/java/com/github/monzou/grinder/BeanProperty.java) to writable property

## How to use

Add ```@Grind``` annotation to your bean class.

### Bean

```java
@Grind
public class Trade implements Serializable {

    private final Long id;

    private String tradeNo;

    private long version;

    private boolean deleted;

    public Trade(Long id) {
        this.id = id;
    }

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Long getId() {
        return id;
    }

}
```

### Generated meta class

```java
public class TradeMeta {

    /** tradeNo */
    public static final BeanProperty<Trade, java.lang.String> tradeNo = new BeanProperty<Trade, java.lang.String>() {

        /** {@inheritDoc} */
        @Override
        public java.lang.String getName() {
            return "tradeNo";
        }

        /** {@inheritDoc} */
        @Override
        public java.lang.String apply(Trade bean) {
            return bean.getTradeNo();
        }

        /** {@inheritDoc} */
        @Override
        public Trade apply(Trade bean, java.lang.String tradeNo) {
            bean.setTradeNo(tradeNo);
            return bean;
        }

    };

    /** version */
    public static final BeanProperty<Trade, java.lang.Long> version = new BeanProperty<Trade, java.lang.Long>() {

        /** {@inheritDoc} */
        @Override
        public java.lang.String getName() {
            return "version";
        }

        /** {@inheritDoc} */
        @Override
        public java.lang.Long apply(Trade bean) {
            return bean.getVersion();
        }

        /** {@inheritDoc} */
        @Override
        public Trade apply(Trade bean, java.lang.Long version) {
            bean.setVersion(version);
            return bean;
        }

    };

    /** deleted */
    public static final BeanProperty<Trade, java.lang.Boolean> deleted = new BeanProperty<Trade, java.lang.Boolean>() {

        /** {@inheritDoc} */
        @Override
        public java.lang.String getName() {
            return "deleted";
        }

        /** {@inheritDoc} */
        @Override
        public java.lang.Boolean apply(Trade bean) {
            return bean.isDeleted();
        }

        /** {@inheritDoc} */
        @Override
        public Trade apply(Trade bean, java.lang.Boolean deleted) {
            bean.setDeleted(deleted);
            return bean;
        }

    };


    /** id */
    public static final BeanPropertyAccessor<Trade, java.lang.Long> id = new BeanPropertyAccessor<Trade, java.lang.Long>() {

        /** {@inheritDoc} */
        @Override
        public java.lang.String getName() {
            return "id";
        }

        /** {@inheritDoc} */
        @Override
        public java.lang.Long apply(Trade bean) {
            return bean.getId();
        }

    };

    private static final Map<String, BeanProperty<? super Trade, ?>> properties;

    private static final Map<String, BeanPropertyAccessor<? super Trade, ?>> propertyAccessors;

    static {

        Builder<String, BeanProperty<? super Trade, ?>> propertiesBuilder = ImmutableMap.builder();
        propertiesBuilder.put("tradeNo", tradeNo);
        propertiesBuilder.put("version", version);
        propertiesBuilder.put("deleted", deleted);
        properties = propertiesBuilder.build();

        Builder<String, BeanPropertyAccessor<? super Trade, ?>> propertyAccessorsBuilder = ImmutableMap.builder();
        propertyAccessorsBuilder.put("id", id);
        propertyAccessors = propertyAccessorsBuilder.build();

    }

    /**
     * Get all properties
     *
     * @return properties
     */
    public static List<BeanProperty<? super Trade, ?>> getProperties() {
        return ImmutableList.copyOf(properties.values());
    }

    /**
     * Get all property accessors
     *
     * @return property accessors
     */
    public static List<BeanPropertyAccessor<? super Trade, ?>> getPropertyAccessors() {
        ImmutableList.Builder<BeanPropertyAccessor<? super Trade, ?>> builder = ImmutableList.builder();
        builder.addAll(properties.values());
        builder.addAll(propertyAccessors.values());
        return builder.build();
    }

    /**
     * Get the bean property by name
     *
     * @param propertyName property name
     * @return {@link BeanProperty}
     */
    public static BeanProperty<? super Trade, ?> getProperty(String propertyName) {
        return properties.get(propertyName);
    }

    /**
     * Get the bean property accessor by name
     *
     * @param propertyName property name
     * @return {@link BeanPropertyAccessor}
     */
    public static BeanPropertyAccessor<? super Trade, ?> getPropertyAccessor(String propertyName) {
        BeanPropertyAccessor<? super Trade,?> property = getProperty(propertyName);
        if (property == null) {
            property = propertyAccessors.get(propertyName);
        }
        return property;
    }

}
```

```BeanProperty``` and ```BeanPropertyAccessor``` extends ```com.google.common.base.Function```. So you can use it like this:

```java
List<Trade> trades = Trade.findAll();
List<String> tradeNos = FluentIterable.from(trades).filter(Predicates.not(Predicates2.fromFunction(TradeMeta.deleted))).transform(TradeMeta.tradeNo).toList();
```

## Installation

1. Add Maven repository: http://monzou.github.com/maven-repository/
2. Add dependency: com.github.monzou:grinder:${version}

Configuration example for Gradle:

```groovy
repositories {
    maven {
        url "http://monzou.github.com/maven-repository/"
    }
}
dependencies {
    compile "com.github.monzou:grinder:${version}"
}
```

## Requirements

* JDK 7 +

## Dependencies

* [Google Guava](https://code.google.com/p/guava-libraries/)

## License

(The MIT License)

Copyright (c) 2014 Takuro Monji @monzou
