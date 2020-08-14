package com.lvt4j.rbactest;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author LV on 2020年8月14日
 */
@Getter@Setter
@Configuration
@ConfigurationProperties("rbac")
public class RbacConfig {

    private String centerAddr;
    private String proId;
    private int cacheCapacity;
    private int centerTimeout;
    
}
