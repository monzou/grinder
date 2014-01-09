package com.github.monzou.grinder.annotation.processor;

import java.nio.charset.Charset;

import org.junit.Before;
import org.seasar.aptina.unit.AptinaTestCase;

import com.github.monzou.grinder.annotation.processor.Options;
import com.github.monzou.grinder.annotation.processor.Grinder;

/**
 * GrinderTest
 */
public class GrinderTest extends AptinaTestCase {

    @Before
    public void setUp() throws Exception {
        addSourcePath("src/test/java");
        setCharset(Charset.forName("UTF-8"));
    }

    public void testGrinder1() throws Exception {
        addProcessor(new Grinder());
        addCompilationUnit(TradeBean.class);
        compile();
        assertEqualsGeneratedSourceWithResource("com/github/monzou/grinder/annotation/processor/Grinder1.expected", "com.github.monzou.grinder.annotation.processor.meta.TradeBeanMeta");
    }

    public void testGrinder2() throws Exception {
        addProcessor(new Grinder());
        addCompilationUnit(TradeBean.class);
        addOption(String.format("-A%s=test.meta", Options.PACKAGE));
        compile();
        assertEqualsGeneratedSourceWithResource("com/github/monzou/grinder/annotation/processor/Grinder2.expected", "test.meta.TradeBeanMeta");
    }

    public void testGrinder3() throws Exception {
        addProcessor(new Grinder());
        addCompilationUnit(TradeBean.class);
        addOption(String.format("-A%s=foo", Options.PACKAGE_SUFFIX), String.format("-A%s=Foo", Options.CLASS_SUFFIX));
        compile();
        assertEqualsGeneratedSourceWithResource("com/github/monzou/grinder/annotation/processor/Grinder3.expected", "com.github.monzou.grinder.annotation.processor.foo.TradeBeanFoo");
    }

}
