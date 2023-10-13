package com.erybobo.message.core;

import org.apache.ignite.IgniteSystemProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CoreApplication {

    public static void main(String[] args) {
        System.setProperty(IgniteSystemProperties.IGNITE_NO_SHUTDOWN_HOOK, "true");
        SpringApplication.run(CoreApplication.class, args);
    }

}
