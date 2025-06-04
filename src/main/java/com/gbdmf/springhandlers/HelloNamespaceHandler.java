package com.gbdmf.springhandlers;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class HelloNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("hello", new HelloBeanDefinitionParser());
    }
}
