package com.github.monzou.grinder.annotation.processor;

import java.nio.charset.Charset;

import org.junit.Before;
import org.seasar.aptina.unit.AptinaTestCase;

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
        assertEqualsGeneratedSourceWithResource("com/github/monzou/grinder/annotation/processor/Grinder1.Trade.expected", "com.github.monzou.grinder.annotation.processor.TradeBeanMeta");
        assertEqualsGeneratedSourceWithResource("com/github/monzou/grinder/annotation/processor/Grinder1.CashFlow.expected", "com.github.monzou.grinder.annotation.processor.CashFlowBeanMeta");
    }

    public void testGrinder2() throws Exception {
        addProcessor(new Grinder());
        addCompilationUnit(TradeBean.class);
        addOption(String.format("-A%s=test.meta", Options.PACKAGE), String.format("-A%s=M", Options.CLASS_PREFIX));
        compile();
        assertEqualsGeneratedSourceWithResource("com/github/monzou/grinder/annotation/processor/Grinder2.Trade.expected", "test.meta.MTradeBean");
        assertEqualsGeneratedSourceWithResource("com/github/monzou/grinder/annotation/processor/Grinder2.CashFlow.expected", "test.meta.MCashFlowBean");
    }

    public void testGrinder3() throws Exception {
        addProcessor(new Grinder());
        addCompilationUnit(TradeBean.class);
        addOption(String.format("-A%s=foo", Options.PACKAGE_SUFFIX), String.format("-A%s=Foo", Options.CLASS_SUFFIX));
        compile();
        assertEqualsGeneratedSourceWithResource("com/github/monzou/grinder/annotation/processor/Grinder3.Trade.expected", "com.github.monzou.grinder.annotation.processor.foo.TradeBeanFoo");
        assertEqualsGeneratedSourceWithResource("com/github/monzou/grinder/annotation/processor/Grinder3.CashFlow.expected", "com.github.monzou.grinder.annotation.processor.foo.CashFlowBeanFoo");
    }

}
