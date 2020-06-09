package com.lvt4j.rbac.test.h2.slave;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.lvt4j.rbac.RbacCenter;

/**
 *
 * @author LV on 2020年5月26日
 */
@SpringBootApplication
public class H2SlaveApp {

    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "h2slave");
        SpringApplication.run(RbacCenter.class, args);
    }
    
}
