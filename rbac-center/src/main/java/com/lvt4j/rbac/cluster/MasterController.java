package com.lvt4j.rbac.cluster;

import java.io.ObjectInputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.lvt4j.rbac.BroadcastMsg4Center;
import com.lvt4j.rbac.cluster.ClusterStator.MemberStatus;
import com.lvt4j.rbac.condition.IsMaster;

import lombok.extern.slf4j.Slf4j;

/**
 * 主节点的接口
 * @author LV on 2020年8月4日
 */
@Slf4j
@RestController
@RequestMapping("cluster")
@Conditional(IsMaster.class)
class MasterController {

    @Autowired
    private Master master;
    
    /** 从节点应通过该接口与主节点建立连接并订阅 */
    @RequestMapping("subscribe")
    public SseEmitter subscribe(HttpServletRequest request,
            @RequestParam String id,
            @RequestParam String host,
            @RequestParam int port) throws Exception {
        log.trace("从节点[{}({}:{})]请求接入", id, host, port);
        return master.subscribe(id, host, port);
    }
    
    /** 从节点应定时由本接口通知主节点心跳 */
    @RequestMapping("heartbeat/4slave")
    public void heartbeat(@RequestParam String id) {
        log.trace("从节点[{}]心跳", id);
        master.onHeartbeat(id);
    }
    
    /**
     * 发送广播<br>
     * 操作在从节点上执行时，从节点通过该接口发送给master，由master发送广播
     * @param proId
     * @param userId
     */
    @RequestMapping("publish/4slave")
    public void publish4Slave(HttpServletRequest req) throws Exception{
        ObjectInputStream ois = new ObjectInputStream(req.getInputStream());
        Object msg = ois.readObject();
        
        if(log.isTraceEnabled()) log.trace("从节点请求发送广播消息:{}", msg);
        master.publish((BroadcastMsg4Center) msg);
    }
    
    /** 从节点从这个接口，利用主节点获取集群状态 */
    @RequestMapping("stats/4slave")
    public List<MemberStatus> stats4Slave() {
        return master.getMemberStats();
    }
    
}