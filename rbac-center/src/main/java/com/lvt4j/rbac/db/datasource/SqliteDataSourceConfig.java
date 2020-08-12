package com.lvt4j.rbac.db.datasource;

import java.io.File;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.sqlite.SQLiteDataSource;

import com.lvt4j.rbac.condition.DbIsSqlite;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author LV on 2020年8月5日
 */
@Slf4j
@Configuration
@Conditional(DbIsSqlite.class)
class SqliteDataSourceConfig extends EmbeddedDataSourceConfig {
    
    @Bean
    public DataSource sqliteDataSource() throws Exception {
        File db = new File(folder, "rbac.db");
        if(!db.exists()) initDbFile("sqlite.db", db);
        log.info("数据库SQLite:{}",db.getPath());
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:"+folder+"rbac.db");
        dataSource.setIncrementalVacuum(1000);
        dataSource.setCacheSize(-200000);
        HikariDataSource hikari = new HikariDataSource();
        hikari.setDataSource(dataSource);
        hikari.setMaximumPoolSize(1);
        return hikari;
    }
    
}
