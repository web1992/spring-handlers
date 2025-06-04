package com.gbdmf.springhandlers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@SpringBootApplication
public class SpringHandlersApplication {

    public static void main(String[] args) {
        //SpringApplication.run(SpringHandlersApplication.class, args);

        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
        HelloBean helloBean = ctx.getBean(HelloBean.class);
        helloBean.sayHello();  // 输出：Hello, Spring Custom Tag

    }

}
