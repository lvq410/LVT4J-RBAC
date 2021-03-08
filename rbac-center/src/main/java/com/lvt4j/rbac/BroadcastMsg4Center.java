package com.lvt4j.rbac;

import java.io.Serializable;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author LV on 2020年8月12日
 */
@RequiredArgsConstructor
public abstract class BroadcastMsg4Center implements Serializable {
    private static final long serialVersionUID = -8762029843910613773L;

    public abstract BroadcastMsg toClient();
    
    /** 客户端 建立连接成功后，服务端返回握手信号 */
    public static BroadcastMsg ClientHandshake = BroadcastMsg.Handshake.Instance;
    
    /**
     * 任意一个节点收到客户端心跳后，如果客户端未在该节点注册
     * 需要将这个心跳信息广播出去
     * @author LV on 2020年8月15日
     */
    public static class ClientHeartbeat extends BroadcastMsg4Center {
        private static final long serialVersionUID = 4610570391858746115L;
        
        public final String id;
        
        public ClientHeartbeat(String id) {
            this.id = id;
        }

        @Override
        public BroadcastMsg toClient() {
            return null;
        }
        
    }
    
    /** 数据变更后需要清理缓存 */
    public static class CacheClean extends BroadcastMsg4Center implements ProIdBroadcastMsg {
        private static final long serialVersionUID = -6480976047704888050L;
        
        /** 清理指定产品的缓存，可为null，代表清理所有产品 */
        @Getter
        public final String proId;
        /** 清理指定用户的缓存，可为null，代表清理所有用户 */
        public final String userId;
        
        public CacheClean(String proId, String userId) {
            this.proId = proId;
            this.userId = userId;
        }
        
        @Override
        public BroadcastMsg toClient() {
            return new BroadcastMsg.CacheClean(proId, userId);
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
    public static class ForceCacheClean extends BroadcastMsg4Center implements ProIdBroadcastMsg {
        private static final long serialVersionUID = 6696371108597482924L;

        /** 清理指定产品的缓存，可为null，代表清理所有产品 */
        @Getter
        public final String proId;
        /** 清理指定用户的缓存，可为null，代表清理所有用户 */
        public final String userId;
        
        public ForceCacheClean(String proId, String userId) {
            this.proId = proId;
            this.userId = userId;
        }
        
        @Override
        public BroadcastMsg toClient() {
            return new BroadcastMsg.ForceCacheClean(proId, userId);
        }
        
        @Override
        public String toString() {
            return String.format("ForceCacheClean pro:%s user:%s", proId, userId);
        }
    }
    
    public interface ProIdBroadcastMsg {
        public String getProId();
    }
    
}