package com.lvt4j.rbac.service;

import static com.lvt4j.rbac.Utils.sse;
import static com.lvt4j.rbac.Utils.sses;
import static com.lvt4j.rbac.Utils.ssesRaw;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.lvt4j.rbac.BroadcastMsg4Center;
import com.lvt4j.rbac.cluster.BroadcastMsgHandler;

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

    @Autowired
    private SingleThreader singleThreader;
    
    @Autowired
    private BroadcastMsgHandler broadcastMsgHandler;
    
    /** 所有客户端长链 */
    private final Map<SseEmitter, ClientInfo> clients = new ConcurrentHashMap<>();
    
    public SseEmitter onClientSub(String host, int port, String proId, String clientId, String version) {
        ClientInfo clientInfo = new ClientInfo();
        clientInfo.host = host;
        clientInfo.port = port;
        clientInfo.clientId = clientId;
        clientInfo.proId = proId;
        clientInfo.version = version;
        
        SseEmitter emitter = new SseEmitter(0L);
        
        emitter.onTimeout(()->clients.remove(emitter));
        emitter.onError(e->clients.remove(emitter));
        emitter.onCompletion(()->clients.remove(emitter));
        
        singleThreader.enqueue(()->{
            sse(emitter, broadcastMsgHandler.handshake().toClient(), this::onSendException);
            clients.put(emitter, clientInfo);
        });
        return emitter;
    }
    
    public void clientsPublish(BroadcastMsg4Center msg) {
        Serializable msg4Client = msg.toClient();
        singleThreader.enqueue(()->{
            sses(clients.keySet(), msg4Client, this::onSendException);
        });
    }
    
    @SneakyThrows
    @Scheduled(cron="0/10 * * * * ?")
    public void heartbeat() {
        singleThreader.enqueue(()->{
            ssesRaw(clients.keySet(), EMPTY, this::onSendException);
        });
    }
    
    private void onSendException(SseEmitter emitter) {
        ClientInfo clientInfo = clients.get(emitter);
        if(clientInfo==null) return;
        log.info("断开客户端[{},{}:{}]的连接", clientInfo.proId, clientInfo.host, clientInfo.port);
        clients.remove(emitter);
    }
    
    @ManagedOperation
    public Collection<ClientInfo> getClients() {
        return clients.values();
    }
    
    @Data
    public static class ClientInfo {
        public String host;
        public int port;
        public String clientId;
        public String proId;
        public String version;
    }
}