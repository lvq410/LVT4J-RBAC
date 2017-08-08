package com.lvt4j.rbac;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 *
 * @author LV
 */
@SpringBootApplication
public class Main{

    public static void main(String[] args) {
        System.out.println("App路径:"+Consts.AppFolder.getAbsolutePath());
        SpringApplication.run(Main.class, args);
    }
    
}
