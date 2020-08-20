package com.lvt4j.rbac.db.datasource;

import javax.activation.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.core.HazelcastInstance;
import com.lvt4j.rbac.condition.DbIsH2;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author LV on 2020年8月20日
 */
@Slf4j
@Configuration
@Conditional(DbIsH2.class)
public class H2DataSourceConfigHazelcast {

    @Value("${localIp}")
    private String host;
    
    @Value("${db.h2.filelock}")
    private String filelock;
    @Value("${db.h2.web.port}")
    private String webPort;
    @Value("${db.h2.tcp.port}")
    private String tcpPort;
    
    
    @Autowired
    private HazelcastInstance hazelcast;
    
    
    
    public DataSource h2DataSource() {
        //返回一个代理的DataSource
        //当主节点有变动时，创建新的DataSource并替换旧的，然后销毁旧的
    }
    
}
