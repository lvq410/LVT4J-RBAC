package com.lvt4j.rbac.cluster;

import com.lvt4j.rbac.BroadcastMsg4Center;

/**
 * 事件总线对象
 * 调用该对象的pub方法会将消息广播到整个集群上
 * @author LV on 2020年8月3日
 */
public interface EventBusPublisher {

    /** 向事件总线上发布消息 */
    public void publish(BroadcastMsg4Center msg);
    
}