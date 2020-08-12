package com.lvt4j.rbac.db.datasource;

import java.io.File;

import javax.sql.DataSource;

import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import com.lvt4j.rbac.condition.DbIsH2;
import com.lvt4j.rbac.condition.IsMaster;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author LV on 2020年8月5日
 */
@Slf4j
@Configuration
@Conditional({DbIsH2.class, IsMaster.class})
class H2MasterDataSourceConfig extends H2DataSourceConfig {

    @Value("${db.h2.filelock}")
    private String filelock;
    @Value("${db.h2.web.port}")
    private String webPort;
    @Value("${db.h2.tcp.port}")
    private String tcpPort;
    
    @Bean
    public DataSource h2MasterDataSource() throws Exception {
        File db = new File(folder, "rbac.mv.db");
        if(!db.exists()) initDbFile("h2.mv.db", db);
        log.info("数据库H2-master:{}",db.getPath());
        DataSource dataSource = h2DataSource("jdbc:h2:"+folder+"rbac;FILE_LOCK="+filelock);
        try{
            dataSource.getConnection().close();
        }catch(Exception e){
            throw new IllegalStateException("无法建立数据库连接，可能原因：H2 master 节点只能部署一个", e);
        }
        return dataSource;
    }
    
    @Bean(name="h2WebServer",destroyMethod="stop")
    public Server h2WebServer(@Autowired DataSource dataSource) throws Exception {
        log.info("H2开启web控制台端口：{}", webPort);
        Server webServer = Server.createWebServer("-webAllowOthers","-webPort", webPort);
        return webServer.start();
    }
    @Bean(name="h2TcpServer",destroyMethod="stop")
    public Server h2TcpServer(@Autowired DataSource dataSource) throws Exception {
        log.info("H2开启tcp服务端口：{}", tcpPort);
        Server tcpServer = Server.createTcpServer("-tcpAllowOthers","-tcpPort", tcpPort);
        return tcpServer.start();
    }
    
}