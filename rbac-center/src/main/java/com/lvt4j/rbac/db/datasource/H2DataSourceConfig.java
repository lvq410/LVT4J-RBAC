package com.lvt4j.rbac.db.datasource;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;

import com.zaxxer.hikari.HikariDataSource;

/**
 *
 * @author LV on 2020年8月5日
 */
abstract class H2DataSourceConfig extends EmbeddedDataSourceConfig {

    protected final DataSource h2DataSource(String url) {
        url += ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE";
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setUrl(url);
        HikariDataSource hikari = new HikariDataSource();
        hikari.setDataSource(dataSource);
        return hikari;
    }

}