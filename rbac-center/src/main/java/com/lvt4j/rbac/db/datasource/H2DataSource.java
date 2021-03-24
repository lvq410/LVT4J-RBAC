package com.lvt4j.rbac.db.datasource;

import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import com.lvt4j.rbac.cluster.Cluster;
import com.lvt4j.rbac.cluster.OnMasterChangedListener;
import com.lvt4j.rbac.condition.DbIsH2;
import com.lvt4j.rbac.dto.NodeInfo;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author LV on 2021年3月3日
 */
@Slf4j
@Component
@Conditional({DbIsH2.class})
public class H2DataSource extends EmbeddedDataSourceConfig implements DataSource, OnMasterChangedListener {

    @Value("${db.h2.filelock}")
    private String filelock;

    @Autowired
    private Cluster cluster;
    
    private HikariDataSource wrapped;
    
    @PostConstruct
    private void init() throws Throwable {
        Cluster.addMasterChangeListener(this);
        initDataSource(cluster.isLocalMaster(), cluster.getMasterInfo());
    }
    private void initDataSource(boolean isLocalMaster, NodeInfo masterInfo) throws Throwable {
        if(isLocalMaster){
            initAsMaster();
        }else{
            initAsSlave(masterInfo);
        }
    }
    private void initAsMaster() throws Throwable {
        File db = new File(folder, "rbac.mv.db");
        if(!db.exists()) initDbFile("h2.mv.db", db);
        log.info("数据库H2-master:{}",db.getPath());
        HikariDataSource dataSource = h2DataSource("jdbc:h2:"+folder+"rbac;FILE_LOCK="+filelock);
        try{
            dataSource.getConnection().close();
        }catch(Exception e){
            throw new IllegalStateException("无法建立数据库连接，可能原因：H2 master 节点只能部署一个", e);
        }
        wrapped = dataSource;
    }
    private void initAsSlave(NodeInfo masterNodeInfo) throws Throwable {
        String masterHost = masterNodeInfo.getHost();
        int masterTcpPort = masterNodeInfo.getH2TcpPort();
        log.info("数据库H2-slave:{}",masterHost+":"+masterTcpPort+"/"+folder+"rbac");
        String url = "jdbc:h2:tcp://"+masterHost+":"+masterTcpPort+"/"+folder+"rbac";
        HikariDataSource dataSource = h2DataSource(url);
        for(int i=3; i>0; i--){
            try{
                dataSource.getConnection().close();
                break;
            }catch(Exception e){
                log.error("与主节点建立数据库连接失败,重试剩余次数:{}", i-1);
                if(i==1) throw e;
                else Thread.sleep(5000);
            }
        }
        wrapped = dataSource;
    }
    private final HikariDataSource h2DataSource(String url) {
        url += ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE";
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setUrl(url);
        HikariDataSource hikari = new HikariDataSource();
        hikari.setDataSource(dataSource);
        return hikari;
    }
    
    @PreDestroy
    private void destory() {
        if(wrapped==null) return;
        try{
            if(log.isTraceEnabled()) log.trace("销毁DataSource");
            wrapped.close();
            wrapped = null;
        }catch(Throwable e){
            log.error("销毁DataSource异常", e);
        }
    }
    
    @Override public int getOrder() { return Order_H2DataSource; }
    @Override
    public void beforeMasterChange() throws Throwable {
        destory();
    }
    @Override
    public void afterMasterChanged(boolean isLocalMaster, NodeInfo masterInfo) throws Throwable {
        initDataSource(isLocalMaster, masterInfo);
    }
    
    
    @Override public PrintWriter getLogWriter() throws SQLException { return wrapped.getLogWriter(); }
    @Override public void setLogWriter(PrintWriter out) throws SQLException { wrapped.setLogWriter(out); }
    @Override public void setLoginTimeout(int seconds) throws SQLException { wrapped.setLoginTimeout(seconds); }
    @Override public int getLoginTimeout() throws SQLException { return wrapped.getLoginTimeout(); }
    @Override public Logger getParentLogger() throws SQLFeatureNotSupportedException { return wrapped.getParentLogger(); }
    @Override public <T> T unwrap(Class<T> iface) throws SQLException { return wrapped.unwrap(iface); }
    @Override public boolean isWrapperFor(Class<?> iface) throws SQLException { return wrapped.isWrapperFor(iface); }
    @Override public Connection getConnection() throws SQLException { return wrapped.getConnection(); }
    @Override public Connection getConnection(String username, String password) throws SQLException { return wrapped.getConnection(username, password); }
}