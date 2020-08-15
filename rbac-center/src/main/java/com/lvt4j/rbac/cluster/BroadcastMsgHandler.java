package com.lvt4j.rbac.cluster;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvt4j.rbac.BroadcastMsg4Center;
import com.lvt4j.rbac.BroadcastMsg4Center.CacheClean;
import com.lvt4j.rbac.BroadcastMsg4Center.ClientHeartbeat;
import com.lvt4j.rbac.BroadcastMsg4Center.ForceCacheClean;
import com.lvt4j.rbac.ProductAuthCaches;
import com.lvt4j.rbac.service.ClientService;

import lombok.extern.slf4j.Slf4j;

/**
 * 收到事件总线上的消息后的处理
 * @author LV on 2020年8月3日
 * @param <T>
 * @param <T>
 */
@Slf4j
@Service
public class BroadcastMsgHandler {
    
    @Autowired
    private ProductAuthCaches productAuthCaches;
    @Autowired
    private ClientService clientService;
    
    private Map<Object, Object> handlers = new HashMap<>();
    
    @PostConstruct
    private void init() {
        addHandler(ClientHeartbeat.class, this::clientHeartbeat);
        addHandler(CacheClean.class, this::cacheClean);
        addHandler(ForceCacheClean.class, this::forceCacheClean);
    }
    
    private <T extends BroadcastMsg4Center> void addHandler(Class<T> cls, Consumer<T> consumer) {
        handlers.put(cls, consumer);
    }
    @SuppressWarnings("unchecked")
    private Consumer<BroadcastMsg4Center> getHandler(Class<?> cls) {
        return (Consumer<BroadcastMsg4Center>) handlers.get(cls);
    }
    
    public void onBroadcastMsg(BroadcastMsg4Center msg) {
        if(log.isTraceEnabled()) log.trace("onBroadcastMsg {}", msg);
        
        Consumer<BroadcastMsg4Center> consumer = getHandler(msg.getClass());
        if(consumer==null) return;
        consumer.accept(msg);
    }
    
    /**
     * 未注册指定客户端的节点收到了节点的心跳后广播了出去
     * @param heartbeat
     */
    private void clientHeartbeat(ClientHeartbeat heartbeat) {
        clientService.onHeartbeat(heartbeat.id);
    }
    
    /** 缓存清理 */
    private void cacheClean(CacheClean msg) {
        if(log.isTraceEnabled()) log.trace("处理清理缓存消息:{}", msg);
        productAuthCaches.invalidate(msg.proId, msg.userId);
        clientService.clientsPublish(msg);
    }
    
    /** 强制缓存清理 */
    private void forceCacheClean(ForceCacheClean msg) {
        if(log.isTraceEnabled()) log.trace("处理强制清理缓存消息:{}", msg);
        productAuthCaches.invalidate(msg.proId, msg.userId);
        clientService.clientsPublish(msg);
    }
    
}