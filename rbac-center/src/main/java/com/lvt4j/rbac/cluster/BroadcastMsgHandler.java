package com.lvt4j.rbac.cluster;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvt4j.rbac.BroadcastMsg4Center;
import com.lvt4j.rbac.BroadcastMsg4Center.CacheClean;
import com.lvt4j.rbac.BroadcastMsg4Center.ClientHeartbeat;
import com.lvt4j.rbac.BroadcastMsg4Center.Handshake;
import com.lvt4j.rbac.ProductAuthCaches;
import com.lvt4j.rbac.service.ClientService;

import lombok.extern.slf4j.Slf4j;

/**
 * 收到事件总线上的消息后的处理
 * @author LV on 2020年8月3日
 */
@Slf4j
@Service
public class BroadcastMsgHandler {

    private long masterTerm;
    private long broadMsgIdx;
    
    @Autowired
    private ProductAuthCaches productAuthCaches;
    @Autowired
    private ClientService clientService;
    
    /**
     * 客户端接入时，需要向客户端传递一个握手消息
     * 这个握手消息从系统当前已消费的消息编号开始
     * 本方法生成一个系统当前已消费的消息编号的握手消息
     * @return
     */
    public Handshake handshake() {
        return new BroadcastMsg4Center.Handshake(masterTerm, broadMsgIdx);
    }
    
    public void onBroadcastMsg(BroadcastMsg4Center msg) {
        if(log.isTraceEnabled()) log.trace("onBroadcastMsg {}", msg);
        
        if(msg instanceof Handshake){
            handshake((Handshake) msg);
            return;
        }
        
        if(msg instanceof ClientHeartbeat){
            clientHeartbeat((ClientHeartbeat) msg);
            return;
        }
        
        if(msg instanceof CacheClean){
            cacheClean((CacheClean) msg);
            clientService.clientsPublish(msg);
            return;
        }
    }
    
    /** 握手信号，初次建立连接 */
    private void handshake(Handshake msg) {
        masterTerm = msg.masterTerm;
        broadMsgIdx = msg.msgIdx;
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
        if(!Objects.equals(masterTerm, msg.masterTerm)){ //master变更
            log.info("主节点变更");
            masterTerm = msg.masterTerm;
            broadMsgIdx = msg.msgIdx;
            productAuthCaches.invalidate(null, null);
            return;
        }
        
        long msgIdxDiff = msg.msgIdx-broadMsgIdx;
        if(msgIdxDiff>1 || msgIdxDiff<0){ //漏消息
            log.info("漏消息");
            masterTerm = msg.masterTerm;
            broadMsgIdx = msg.msgIdx;
            productAuthCaches.invalidate(null, null);
            return;
        }
        
        if(log.isTraceEnabled()) log.trace("处理清理缓存消息:{}", msg);
        productAuthCaches.invalidate(msg.proId, msg.userId);
        masterTerm = msg.masterTerm;
        broadMsgIdx = msg.msgIdx;
    }
    
}