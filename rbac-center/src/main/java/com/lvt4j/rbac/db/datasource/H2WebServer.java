package com.lvt4j.rbac.db.datasource;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import com.lvt4j.rbac.cluster.Cluster;
import com.lvt4j.rbac.cluster.OnMasterChangedListener;
import com.lvt4j.rbac.condition.DbIsH2;
import com.lvt4j.rbac.dto.NodeInfo;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author LV on 2021年3月3日
 */
@Slf4j
@Component
@Conditional({DbIsH2.class})
public class H2WebServer implements OnMasterChangedListener {

    @Value("${db.h2.web.port}")
    private String webPort;
    
    @Autowired
    private Cluster cluster;
    
    private Server wrapped;
    
    @PostConstruct
    private void init() throws Throwable {
        Cluster.addMasterChangeListener(this);
        initWebServer(cluster.isLocalMaster(), cluster.getMasterInfo());
    }
    private void initWebServer(boolean isLocalMaster, NodeInfo masterInfo) throws Throwable {
        if(isLocalMaster) initAsMaster();
    }
    private void initAsMaster() throws Throwable {
        log.info("H2开启web控制台端口：{}", webPort);
        Server webServer = Server.createWebServer("-webAllowOthers","-webPort", webPort);
        wrapped = webServer.start();
    }
    
    @PreDestroy
    private void destory() {
        if(wrapped==null) return;
        try{
            if(log.isTraceEnabled()) log.trace("销毁WebServer");
            wrapped.stop();
            wrapped = null;
        }catch(Throwable e){
            log.error("销毁WebServer异常", e);
        }
    }
    
    @Override public int getOrder() { return Order_H2WebServer; }
    @Override
    public void beforeMasterChange() throws Throwable {
        destory();
    }
    @Override
    public void afterMasterChanged(boolean isLocalMaster, NodeInfo masterInfo) throws Throwable {
        initWebServer(isLocalMaster, masterInfo);
    }
    
}