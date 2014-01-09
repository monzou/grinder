# Grinder - Grind Java Beans by APT

Grinder provides bean property accessors by APT.

### Build Status

[![Build Status](https://travis-ci.org/monzou/grinder.png)](https://travis-ci.org/monzou/grinder)

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

## How to use

Add ```@Grind``` annotation to your bean class.

```java
@Grind
public class Trade implements Serializable {

  private String tradeNo;

  private String remarks;

  // ...

}
```

Grinder generates bean meta classes:

```java
public class TradeBeanMeta {

    /** tradeNo */
    public static final BeanProperty<TradeBean, java.lang.String> tradeNo = new BeanProperty<TradeBean, java.lang.String>() {

        /** {@inheritDoc} */
        @Override
        public java.lang.String getName() {
            return "tradeNo";
        }

        /** {@inheritDoc} */
        @Override
        public java.lang.String apply(TradeBean bean) {
            return bean.getTradeNo();
        }

        /** {@inheritDoc} */
        @Override
        public TradeBean apply(TradeBean bean, java.lang.String tradeNo) {
            bean.setTradeNo(tradeNo);
            return bean;
        }

    };

    // ...

```

```BeanProperty``` extends ```com.google.common.base.Function```. So you can use ```BeanProperty``` like this:

```java
List<Trade> trades = Trade.findAll();
List<String> tradeNos = FluentIterable.from(trades).transform(TradeMeta.tradeNo).toList();
```

## Configuration

Just configure ```grinder``` as an annotation processor.
For example, if you use Eclipse and Gradle, configure ```build.gradle``` like following:

```groovy
import groovy.xml.MarkupBuilder
apply plugin: "eclipse"

ext.eclipseProcessorDir = "apt/lib"

configurations {
    apt
}

sourceSets {
    apt
}

dependencies {
    apt "com.github.monzou:grinder:${grinderVersion}"
}

task compileAptJava(overwrite: true, dependsOn: clean)  {
    doLast {
        sourceSets.apt.output.resourcesDir.mkdirs()
        ant.javac(
            includeAntRuntime: false,
            classpath: configurations.compile.asPath,
            srcdir: "src/main/java",
            encoding: "UTF-8"
        ) {
            compilerarg(line: "-proc:only")
            compilerarg(line: "-processorpath ${configurations.apt.asPath}")
            compilerarg(line: "-s ${sourceSets.apt.output.resourcesDir}")
        }
        compileJava.source sourceSets.apt.output.resourcesDir
    }
    compileJava.dependsOn compileAptJava
}

eclipse {
    jdt {
        file {
            withProperties { properties ->
                properties.setProperty("org.eclipse.jdt.core.compiler.processAnnotations", "enabled")
            }
        }
    }
}

task copyProcessorLibsToEclipse(type: Copy) {
    eclipseJdt.dependsOn copyProcessorLibsToEclipse
    into eclipseProcessorDir
    from configurations.apt
}

def writeFactorySettings() {
    def file = file(".factorypath")
    def writer = new FileWriter(file)
    def xml = new MarkupBuilder(writer)
    xml.setDoubleQuotes(true)
    xml."factorypath"() {
        "factorypathentry"(kind: "PLUGIN", id: "org.eclipse.jst.ws.annotations.core", enabled: true, runInBatchMode: false)
        "factorypathentry"(kind: "WKSPJAR", id: "/${project.name}/${eclipseProcessorDir}/grinder-${grinderVersion}.jar", enabled: true, runInBatchMode: false)
    }
    writer.close()
}

def writeEclipseSettings() {
    def file = file(".settings/org.eclipse.jdt.apt.core.prefs")
    file.getParentFile().mkdirs()
    def writer = new FileWriter(file)
    writer.write("""
eclipse.preferences.version=1
org.eclipse.jdt.apt.aptEnabled=true
org.eclipse.jdt.apt.genSrcDir=src/main/java
org.eclipse.jdt.apt.reconcileEnabled=true
""")
    writer.close()
}

eclipseJdt {
    doFirst {
        writeFactorySettings()
        writeEclipseSettings()
    }
}

task cleanEclipseSettings {
    cleanEclipse.dependsOn cleanEclipseSettings
    doLast {
        delete ".settings/org.eclipse.jdt.apt.core.prefs"
        delete ".factorypath"
        delete eclipseProcessorDir
    }
}
```

## Requirements

* JDK 7 +

## Dependencies

* [Google Guava](https://code.google.com/p/guava-libraries/)

## License

(The MIT License)

Copyright (c) 2014 Takuro Monji @monzou
