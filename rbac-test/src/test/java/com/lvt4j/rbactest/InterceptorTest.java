package com.lvt4j.rbactest;

/**
 *
 * @author LV on 2020年8月14日
 */
public class InterceptorTest {

    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "interceptor");
        TestApp.main(args);
    }
    
}
