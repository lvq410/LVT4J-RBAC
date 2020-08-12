package com.lvt4j.rbac.web.controller;

import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lvt4j.rbac.ProductAuthCaches;
import com.lvt4j.rbac.ProductAuthCaches.ProductAuth4Center;
import com.lvt4j.rbac.UserAuth;
import com.lvt4j.rbac.db.lock.Read;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("inner")
public class InnerController{

    @Autowired
    private ProductAuthCaches productAuthCaches;
    
    @Read
    @RequestMapping("proLastModify")
    public void proLastModify(
            HttpServletResponse response,
            @RequestParam String proId) throws Exception {
        if(log.isTraceEnabled()) log.trace("客户端检查产品[{}]最近修改时间", proId);
        ProductAuth4Center productAuth = productAuthCaches.get(proId);
        long lastModify = productAuth==null?0L:productAuth.getLastModify();
        Map<String, Object> rst = new HashMap<String, Object>();
        rst.put("lastModify", lastModify);
        mapCompress2Stream(rst, response.getOutputStream());
    }
    
    @Read
    @RequestMapping("userAuth")
    public void userAuth(
            HttpServletResponse response,
            @RequestParam String proId,
            @RequestParam(required=false) String userId) throws Exception {
        if(log.isTraceEnabled()) log.trace("客户端请求产品[{}]用户[{}]权限", proId, userId);
        UserAuth userAuth = null;
        ProductAuth4Center productAuth = productAuthCaches.get(proId);
        if(productAuth==null){ //产品为空返回空的用户权限
            userAuth = new UserAuth();
            userAuth.userId = userId;
        } else {
            userAuth = productAuth.getUserAuth(userId);
            //用户未在授权中心注册,因为直接就是游客权限,不用传输,序列化前克隆一遍后移除权限信息
            if(userId!=null && !userId.isEmpty() //查询指定用户的权限
                    && !userAuth.exist){ //但该用户未在授权中心注册，因为直接就是游客权限，因此不用传输具体权限信息，减少传输数据量
                UserAuth origUserAuth = userAuth;
                userAuth = new UserAuth(); //序列化前克隆一遍后移除权限信息
                userAuth.userId = origUserAuth.userId;
            }
        }
        Map<String, Object> rst = new HashMap<String, Object>();
        rst.put("lastModify", productAuth==null?0L:productAuth.getLastModify());
        rst.put("userAuth", userAuth);
        mapCompress2Stream(rst, response.getOutputStream());
    }
    
    private void mapCompress2Stream(Map<String, Object> map, OutputStream out) throws Exception {
        GZIPOutputStream zipOut = new GZIPOutputStream(out);
        ObjectOutputStream oos = new ObjectOutputStream(zipOut);
        oos.writeObject(map);
        oos.close();
    }
    
}