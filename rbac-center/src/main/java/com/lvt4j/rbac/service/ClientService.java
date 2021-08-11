package com.lvt4j.rbac.service;

import static com.lvt4j.rbac.Utils.dateFormat;
import static com.lvt4j.rbac.Utils.sse;
import static com.lvt4j.rbac.Utils.sses;
import static com.lvt4j.rbac.Utils.ssesRaw;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.lvt4j.rbac.BroadcastMsg4Center;
import com.lvt4j.rbac.BroadcastMsg4Center.ProIdBroadcastMsg;
import com.lvt4j.rbac.dto.ClientInfo;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * 所有连接的客户端管理
 * @author LV on 2020年8月3日
 */
@Slf4j
@Service
@ManagedResource(objectName="ClientService:type=ClientService")
public class ClientService {
    /** 过久未收到客户端心跳的客户端会被移除 */
    private static final long ClientKeepaliveThreshold = TimeUnit.SECONDS.toMillis(30);
    
    @Autowired
    private SingleThreader singleThreader;
    
    /** 所有客户端长链 */
    private final Map<String, ClientMeta> clients = new ConcurrentHashMap<>();
    
    public SseEmitter onClientSub(String id, String host, String fromHost, int fromPort, String proId, String version) {
        ClientMeta client = new ClientMeta();
        
        ClientInfo info = client.info = new ClientInfo(id, System.currentTimeMillis(), host, fromHost, fromPort, proId, version);
        
        client.lastHeartbeatTime = info.getRegTime();
        
        SseEmitter emitter = client.emitter = new SseEmitter(0L);
        
        singleThreader.enqueue(()->{
            sse(emitter, BroadcastMsg4Center.ClientHandshake, this::onSendException);
            clients.put(id, client);
            log.info("客户端[{}]接入", info.txt());
        });
        return emitter;
    }
    
    public void clientsPublish(BroadcastMsg4Center msg) {
        if(clients.isEmpty()) return;
        Serializable msg4Client = msg.toClient();
        if(msg4Client==null) return;
        singleThreader.enqueue(()->{
            String proId = null;
            if(msg instanceof ProIdBroadcastMsg){
                proId = ((ProIdBroadcastMsg)msg).getProId();
            }
            sses(emitters(proId), msg4Client, this::onSendException);
        });
    }
    
    @SneakyThrows
    @Scheduled(fixedRate=10000)
    public void heartbeat() {
        if(clients.isEmpty()) return;
        singleThreader.enqueue(()->{
            ssesRaw(emitters(null), EMPTY, this::onSendException);
        });
        long now = System.currentTimeMillis();
        clients.values().parallelStream().filter(c->now-c.lastHeartbeatTime>ClientKeepaliveThreshold).forEach(c->{
            log.info("客户端[{}]移除：因为过久未心跳(上次{})", c.info.txt(), dateFormat(c.lastHeartbeatTime));
            clients.remove(c.info.getId());
        });
    }
    
    /**
     * 收到客户端心跳
     * @param id
     * @return false如果本节点没有注册该客户端
     */
    public boolean onHeartbeat(String id) {
        if(log.isTraceEnabled()) log.trace("处理客户端[{}]心跳", id);
        ClientMeta client = clients.get(id);
        if(client==null){
            if(log.isTraceEnabled()) log.trace("心跳客户端[{}]，未在本机注册", id);
            return false;
        }
        client.lastHeartbeatTime = System.currentTimeMillis();
        if(log.isTraceEnabled()) log.trace("客户端[{}]刷新心跳时间{}", id, dateFormat(client.lastHeartbeatTime));
        return true;
    }
    
    private Collection<SseEmitter> emitters(String proId) {
        if(clients.isEmpty()) return Collections.emptyList();
        if(StringUtils.isBlank(proId)) return clients.values().stream().map(c->c.emitter).collect(toList());
        return clients.values().stream().filter(c->proId.equals(c.info.getProId())).map(c->c.emitter).collect(toList());
    }
    
    private void onSendException(SseEmitter emitter) {
        ClientMeta client = clients.values().stream().filter(c->c.emitter==emitter).findFirst().orElse(null);
        if(client==null) return;
        log.info("断开客户端[{}]的连接", client.info.txt());
        clients.remove(client.info.getId());
    }
    
    @ManagedOperation
    public List<ClientInfo> getClients() {
        if(clients.isEmpty()) return Collections.emptyList();
        return clients.values().stream().map(c->c.info).collect(toList());
    }
    
    @Data
    class ClientMeta {
        public ClientInfo info;
        public SseEmitter emitter;
        public long lastHeartbeatTime;
    }
}