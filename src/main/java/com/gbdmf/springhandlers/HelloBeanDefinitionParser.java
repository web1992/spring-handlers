package com.gbdmf.springhandlers;

import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

public class HelloBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    @Override
    protected Class<?> getBeanClass(Element element) {
        return HelloBean.class;
    }

    @Override
    protected boolean shouldGenerateId() {
        // 允许没有 id 的情况，Spring 会自动生成一个唯一 id
        return true;
    }
    @Override
    protected void doParse(Element element, BeanDefinitionBuilder builder) {
        String name = element.getAttribute("name");
        builder.addPropertyValue("name", name);
    }
}
