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

import com.lvt4j.rbac.ProductAuth4Center;
import com.lvt4j.rbac.UserAuth;
import com.lvt4j.rbac.db.Read;
import com.lvt4j.rbac.service.ProductAuthCache;

@RestController
@RequestMapping("/inner")
public class InnerController{

    @Autowired
    private ProductAuthCache productAuthCache;
    
    @Read
    @RequestMapping("/proLastModify")
    public void proLastModify(
            HttpServletResponse response,
            @RequestParam String proId) throws Exception {
        ProductAuth4Center productAuth = productAuthCache.get(proId);
        long lastModify = productAuth==null?0L:productAuth.product.lastModify;
        Map<String, Object> rst = new HashMap<String, Object>();
        rst.put("lastModify", lastModify);
        mapCompress2Stream(rst, response.getOutputStream());
    }
    
    @Read
    @RequestMapping("/userAuth")
    public void userAuth(
            HttpServletResponse response,
            @RequestParam String proId,
            @RequestParam(required=false) String userId) throws Exception {
        UserAuth userAuth = null;
        ProductAuth4Center productAuth = productAuthCache.get(proId);
        if(productAuth==null){ //产品为空返回空的用户权限
            userAuth = new UserAuth();
            userAuth.userId = userId;
            userAuth.exist = false;
        } else {
            userAuth = productAuth.getUserAuth(userId);
            //用户未在授权中心注册,因为直接就是游客权限,不用传输,序列化前克隆一遍后移除权限信息
            if(userId!=null && !userId.isEmpty() && !userAuth.exist){
                UserAuth origUserAuth = userAuth;
                userAuth = new UserAuth();
                userAuth.userId = origUserAuth.userId;
                userAuth.exist = origUserAuth.exist;
            }
        }
        Map<String, Object> rst = new HashMap<String, Object>();
        rst.put("lastModify", productAuth==null?0L:productAuth.product.lastModify);
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
