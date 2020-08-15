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
    
    /** 客户端建立连接成功后，服务端返回握手信号 */
    static class Handshake extends BroadcastMsg{
        private static final long serialVersionUID = -8487697961651208356L;
        
        static final Handshake Instance = new Handshake();
        
        @Override
        public String toString() {
            return "Handshake";
        }
    }
    
    /** 数据变更后需要清理缓存 */
    static class CacheClean extends BroadcastMsg {
        private static final long serialVersionUID = 5244839815950898357L;
        
        /** 清理指定产品的缓存，可为null，代表清理所有产品 */
        public final String proId;
        /** 清理指定用户的缓存，可为null，代表清理所有用户 */
        public final String userId;
        
        public CacheClean(String proId, String userId) {
            this.proId = proId;
            this.userId = userId;
        }

        @Override
        public String toString() {
            return String.format("CacheClean pro:%s user:%s", proId, userId);
        }
    }
    
    /**
     * 手动发起的缓存清理命令<br>
     * 这种命令优先级较高，应立刻清理缓存
     * @author LV on 2020年8月15日
     */
    static class ForceCacheClean extends BroadcastMsg {
        private static final long serialVersionUID = -6006201409078176424L;

        /** 清理指定产品的缓存，可为null，代表清理所有产品 */
        public final String proId;
        /** 清理指定用户的缓存，可为null，代表清理所有用户 */
        public final String userId;
        
        public ForceCacheClean(String proId, String userId) {
            this.proId = proId;
            this.userId = userId;
        }

        @Override
        public String toString() {
            return String.format("ForceCacheClean pro:%s user:%s", proId, userId);
        }
    }
    
}