package com.lvt4j.rbac.web.controller;

import static com.lvt4j.rbac.Utils.parseIPFromReq;

import java.io.ObjectOutputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.lvt4j.rbac.BroadcastMsg4Center;
import com.lvt4j.rbac.ProductAuthCaches;
import com.lvt4j.rbac.ProductAuthCaches.ProductAuth4Center;
import com.lvt4j.rbac.cluster.Cluster;
import com.lvt4j.rbac.UserAuth;
import com.lvt4j.rbac.db.lock.Read;
import com.lvt4j.rbac.service.ClientService;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author LV on 2020年7月24日
 */
@Slf4j
@RestController
@RequestMapping("/inner/v2")
public class InnerV2Controller {

    @Autowired
    private ProductAuthCaches productAuthCaches;
    
    @Autowired
    private ClientService clientSevice;
    
    @Autowired
    private Cluster cluster;
    
    @RequestMapping("subscribe")
    public SseEmitter subscribe(HttpServletRequest request,
            @RequestParam String id,
            @RequestParam String host,
            @RequestParam String proId,
            @RequestParam String version) {
        if(log.isTraceEnabled()) log.trace("客户端[{},{},{},{}]请求接入", id, host, proId, version);
        return clientSevice.onClientSub(id, host, parseIPFromReq(request), request.getRemotePort(), proId, version);
    }
    
    @RequestMapping("heartbeat")
    public void heartbeat(@RequestParam String id) {
        if(log.isTraceEnabled()) log.trace("收到客户端心跳[{}]", id);
        boolean heartbeated = clientSevice.onHeartbeat(id);
        if(heartbeated) return;
        //如果未在本机注册，需要广播出去
        if(log.isTraceEnabled()) log.trace("客户端心跳[{}]未在本机注册，广播", id);
        cluster.publish(new BroadcastMsg4Center.ClientHeartbeat(id));
    }
    
    @Read
    @RequestMapping("userAuth")
    public void userAuth(HttpServletResponse res,
            @RequestParam String proId,
            @RequestParam(required=false) String userId) throws Exception {
        if(log.isTraceEnabled()) log.trace("客户端请求产品[{}]用户[{}]权限", proId, userId);
        UserAuth userAuth = null;
        ProductAuth4Center productAuth = productAuthCaches.get(proId);
        if(productAuth==null){ //产品为空返回空的用户权限
            userAuth = new UserAuth();
            userAuth.userId = userId;
        }else{
            userAuth = productAuth.getUserAuth(userId);
            //用户未在授权中心注册,因为直接就是游客权限,不用传输,序列化前克隆一遍后移除权限信息
            if(userId!=null && !userId.isEmpty() //查询指定用户的权限
                    && !userAuth.exist){ //但该用户未在授权中心注册，因为直接就是游客权限，因此不用传输具体权限信息，减少传输数据量
                UserAuth origUserAuth = userAuth;
                userAuth = new UserAuth(); //序列化前克隆一遍后移除权限信息
                userAuth.userId = origUserAuth.userId;
            }
        }
        GZIPOutputStream zipOut = new GZIPOutputStream(res.getOutputStream());
        ObjectOutputStream oos = new ObjectOutputStream(zipOut);
        oos.writeObject(userAuth);
        oos.close();
    }
    
}