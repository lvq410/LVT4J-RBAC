package com.lvt4j.rbac.db;

import java.io.File;
import java.io.InputStream;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.io.FileUtils;
import org.h2.Driver;
import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.sqlite.SQLiteDataSource;

import com.lvt4j.rbac.RbacCenter;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author LV
 */
@Slf4j
@Configuration("DataSourceConfig")
public class DataSourceConfig {

    @Value("${db.type}")
    private String dbType;
    @Value("${db.folder}")
    private String dbFolder;
    
    @Value("${db.h2.master}")
    private boolean h2Master;
    @Value("${db.h2.web.port}")
    private String h2WebPort;
    @Value("${db.h2.tcp.port}")
    private String h2TcpPort;
    @Value("${db.h2.master.host}")
    private String h2MasterHost;
    @Value("${db.h2.master.tcp.port}")
    private int h2MasterTcpPort;
    
    @PostConstruct
    private void init() {
        if(!dbFolder.endsWith("/")) dbFolder += "/";
    }
    
    @Bean
    public DataSource dataSource() throws Exception {
        switch(dbType){
        case "sqlite": return sqliteDataSource();
        case "h2": return h2DataSource();
        default: throw new IllegalArgumentException("未知的数据库类型:"+dbType);
        }
    }
    
    public boolean isDistributedDatabase() {
        return "h2".equals(dbType);
    }
    
    //====================================================================SQLiteDbLock
    private SQLiteDataSource sqliteDataSource() throws Exception {
        File db = new File(dbFolder, "rbac.db");
        if(!db.exists()) initDbFile("sqlite.db", db);
        log.info("数据库SQLite:{}",db.getPath());
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:"+dbFolder+"rbac.db");
        dataSource.setEnforceForeignKeys(true);
        dataSource.setIncrementalVacuum(1000);
        dataSource.setCacheSize(-200000);
        return dataSource;
    }
    //========================================================================H2
    private BasicDataSource h2DataSource() throws Exception {
        return h2Master?h2MasterDataSource():h2SlaveDataSource();
    }
    private BasicDataSource h2MasterDataSource() throws Exception {
        File db = new File(dbFolder, "rbac.mv.db");
        if(!db.exists()) initDbFile("h2.mv.db", db);
        log.info("数据库H2-master:{}",db.getPath());
        BasicDataSource dataSource = h2DataSource("jdbc:h2:"+dbFolder+"rbac");
        try{
            dataSource.getConnection().close();
        }catch(Exception e){
            throw new IllegalStateException("无法建立数据库连接，可能原因：H2 master 节点只能部署一个", e);
        }
        return dataSource;
    }
    private BasicDataSource h2SlaveDataSource() {
        log.info("数据库H2-slave:{}",h2MasterHost+":"+h2MasterTcpPort+"/"+dbFolder+"rbac");
        String url = "jdbc:h2:tcp://"+h2MasterHost+":"+h2MasterTcpPort+"/"+dbFolder+"rbac";
        return h2DataSource(url);
    }
    private BasicDataSource h2DataSource(String url) {
        url += ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE";
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriver(new Driver());
        dataSource.setUrl(url);
        return dataSource;
    }
    private void initDbFile(String src, File db) throws Exception {
        InputStream is = RbacCenter.class.getResourceAsStream(src);
        FileUtils.copyInputStreamToFile(is, db);
    }
    @Bean(name="h2WebServer",destroyMethod="stop")
    public Object h2WebServer(@Autowired DataSource dataSource) throws Exception {
        if(!"h2".equals(dbType)) return new FalseH2Server();
        if(!h2Master) return new FalseH2Server();
        log.info("H2开启web控制台端口：{}", h2WebPort);
        Server webServer = Server.createWebServer("-webAllowOthers","-webPort",h2WebPort);
        return webServer.start();
    }
    @Bean(name="h2TcpServer",destroyMethod="stop")
    public Object h2TcpServer(@Autowired DataSource dataSource) throws Exception {
        if(!"h2".equals(dbType)) return new FalseH2Server();
        if(!h2Master) return new FalseH2Server();
        log.info("H2开启tcp服务端口：{}", h2TcpPort);
        Server tcpServer = Server.createTcpServer("-tcpPort", h2TcpPort, "-tcpAllowOthers");
        return tcpServer.start();
    }
    class FalseH2Server {
        public void stop(){}
    }
    
}