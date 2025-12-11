package com.hohoedu.all_pass;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.web.servlet.support.ErrorPageFilter;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;


@ConfigurationPropertiesScan
@SpringBootApplication
public class AllPassApplication {


    public static void main(String[] args) {
        SpringApplication.run(AllPassApplication.class, args);
    }

}
