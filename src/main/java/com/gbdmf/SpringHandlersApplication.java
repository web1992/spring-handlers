package com.gbdmf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringHandlersApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringHandlersApplication.class, args);
        System.out.println("http://127.0.0.1:8081/client/index.html");
    }

}
