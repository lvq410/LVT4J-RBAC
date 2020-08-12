package com.lvt4j.rbac;

import java.io.Serializable;

import lombok.RequiredArgsConstructor;

/**
 * 广播消息
 * @author LV
 */
@RequiredArgsConstructor
abstract class BroadcastMsg implements Serializable {
    private static final long serialVersionUID = 1228877889494521208L;
    
    /** master节点任期 */
    public final long masterTerm;
    /** 消息编号（master节点任期变化后会重置） */
    public final long msgIdx;
    
    /** 客户端建立连接成功后，服务端返回握手信号 */
    static class Handshake extends BroadcastMsg{
        private static final long serialVersionUID = -8487697961651208356L;
        
        public Handshake(long masterTerm, long msgIdx) {
            super(masterTerm, msgIdx);
        }

        @Override
        public String toString() {
            return String.format("Handshake at:%s-%s", masterTerm, msgIdx);
        }
    }
    
    /** 数据变更后需要清理缓存 */
    static class CacheClean extends BroadcastMsg {
        private static final long serialVersionUID = 5244839815950898357L;
        
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
        public String toString() {
            return String.format("CacheClean pro:%s user:%s at:%s-%s", proId, userId, masterTerm, msgIdx);
        }
    }
    
}