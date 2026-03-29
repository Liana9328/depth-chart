package com.fanduel.depthchart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class DepthChartApplication {

    public static void main(String[] args) {
        SpringApplication.run(DepthChartApplication.class, args);
    }
}
