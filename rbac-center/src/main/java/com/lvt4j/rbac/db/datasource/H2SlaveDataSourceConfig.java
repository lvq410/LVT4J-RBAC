package com.lvt4j.rbac.db.datasource;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import com.lvt4j.rbac.condition.DbIsH2;
import com.lvt4j.rbac.condition.IsSlave;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author LV on 2020年8月5日
 */
@Slf4j
@Configuration
@Conditional({DbIsH2.class, IsSlave.class})
class H2SlaveDataSourceConfig extends H2DataSourceConfig {

    @Value("${server.master_host}")
    private String masterHost;
    @Value("${db.h2.master.tcp.port}")
    private int masterTcpPort;
    
    @Bean
    @Conditional({DbIsH2.class, IsSlave.class})
    public DataSource h2SlaveDataSource() {
        log.info("数据库H2-slave:{}",masterHost+":"+masterTcpPort+"/"+folder+"rbac");
        String url = "jdbc:h2:tcp://"+masterHost+":"+masterTcpPort+"/"+folder+"rbac";
        return h2DataSource(url);
    }
    
}
