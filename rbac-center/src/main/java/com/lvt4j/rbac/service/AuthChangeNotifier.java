package com.lvt4j.rbac.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.lvt4j.rbac.BroadcastMsg4Center.CacheClean;
import com.lvt4j.rbac.cluster.Cluster;

/**
 * 权限变更后需要走事件总线将变更信息
 * 广播到所有服务节点和客户端上
 * @author LV on 2020年8月3日
 */
@Service
public class AuthChangeNotifier {

    @Autowired
    private Cluster cluster;
    
    @Async
    public void notify(String proId, String userId){
        try{ Thread.sleep(1000L); }catch(Throwable ig){}
        cluster.publish(new CacheClean(proId, userId));
    }
    
}
