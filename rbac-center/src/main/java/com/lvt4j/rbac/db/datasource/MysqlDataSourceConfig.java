package com.lvt4j.rbac.db.datasource;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import com.lvt4j.rbac.condition.DbIsMysql;
import com.mysql.cj.jdbc.Driver;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author LV on 2020年8月11日
 */
@Slf4j
@Configuration
@Conditional(DbIsMysql.class)
public class MysqlDataSourceConfig {

    @Value("${db.mysql.url}")
    private String jdbcUrl;
    @Value("${db.mysql.username}")
    private String username;
    @Value("${db.mysql.password}")
    private String password;
    
    @Bean
    public DataSource mysqlDataSource() {
        log.info("数据库Mysql:{}", jdbcUrl);
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(Driver.class.getName());
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }
    
}