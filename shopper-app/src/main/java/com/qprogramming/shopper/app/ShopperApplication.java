package com.qprogramming.shopper.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Created by Jakub Romaniszyn  on 19.07.2018.
 */
@SpringBootApplication
public class ShopperApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(ShopperApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ShopperApplication.class);
    }
}
