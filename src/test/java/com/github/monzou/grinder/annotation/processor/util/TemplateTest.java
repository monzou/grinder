package com.github.monzou.grinder.annotation.processor.util;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.junit.Test;

import com.github.monzou.grinder.annotation.processor.util.Template;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;

/**
 * TemplateTest
 */
public class TemplateTest {

    @Test
    public void testRender() throws IOException {

        Bean bean = new Bean();
        bean.setValue1("bean");
        bean.setValue2(0);
        List<Bean> children = Lists.newArrayList();
        for (int i = 1; i <= 3; i++) {
            Bean child = new Bean();
            child.setValue1(String.format("bean-%d", i));
            child.setValue2(i);
            children.add(child);
        }
        bean.setChildren(children);

        assertThat(new Template(read("template-test-source.template")).render(bean), is(read("template-test-expected.template")));

    }

    private String read(String resourceName) throws IOException {
        URL resource = Resources.getResource(getClass(), resourceName);
        return Resources.toString(resource, Charsets.UTF_8);
    }

    public static class Bean {

        private String value1;

        private int value2;

        private List<Bean> children;

        public String getValue1() {
            return value1;
        }

        public void setValue1(String value1) {
            this.value1 = value1;
        }

        public int getValue2() {
            return value2;
        }

        public void setValue2(int value2) {
            this.value2 = value2;
        }

        public List<Bean> getChildren() {
            return children;
        }

        public void setChildren(List<Bean> children) {
            this.children = children;
        }

    }

}
