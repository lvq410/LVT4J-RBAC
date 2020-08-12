package com.lvt4j.rbac;

import java.io.Serializable;

import lombok.RequiredArgsConstructor;

/**
 *
 * @author LV on 2020年8月12日
 */
@RequiredArgsConstructor
public abstract class BroadcastMsg4Center implements Serializable {
    private static final long serialVersionUID = -8762029843910613773L;

    /** master节点任期 */
    public final long masterTerm;
    /** 消息编号（master节点任期变化后会重置） */
    public final long msgIdx;
    
    /** 设置消息编号 */
    public abstract BroadcastMsg4Center msgIdx(long masterTerm, long msgIdx);
    
    public abstract BroadcastMsg toClient();
    
    /** 从节点/客户端 建立连接成功后，服务端返回握手信号 */
    public static class Handshake extends BroadcastMsg4Center{
        private static final long serialVersionUID = 1619469277716503431L;
        
        public Handshake(long masterTerm, long msgIdx) {
            super(masterTerm, msgIdx);
        }
        
        @Override
        public String toString() {
            return String.format("Handshake at:%s-%s", masterTerm, msgIdx);
        }
        
        @Override
        public BroadcastMsg4Center msgIdx(long masterTerm, long msgIdx) {
            return new Handshake(masterTerm, msgIdx);
        }
        
        @Override
        public BroadcastMsg toClient() {
            return new BroadcastMsg.Handshake(masterTerm, msgIdx);
        }
    }
    
    /** 数据变更后需要清理缓存 */
    public static class CacheClean extends BroadcastMsg4Center {
        private static final long serialVersionUID = -6480976047704888050L;
        
        /** 清理指定产品的缓存，可为null */
        public final String proId;
        /** 清理指定用户的缓存，可为null */
        public final String userId;
        
        public CacheClean(long masterTerm, long msgIdx, String proId, String userId) {
            super(masterTerm, msgIdx);
            this.proId = proId;
            this.userId = userId;
        }
        
        @Override
        public BroadcastMsg4Center msgIdx(long masterTerm, long msgIdx) {
            return new CacheClean(masterTerm, msgIdx, proId, userId);
        }
        
        @Override
        public BroadcastMsg toClient() {
            return new BroadcastMsg.CacheClean(masterTerm, msgIdx, proId, userId);
        }
        
    }
    
}