package com.lvt4j.rbac;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

abstract class RbacBaseFilter {

    static final String Encoding = System.getProperty("file.encoding", "UTF-8");
    
    /** 默认产品用户权限缓存容量:1000个用户的权限 */
    protected static final int CacheCapacityDef = 1000;
    /** 默认与授权中心同步的协议:http */
    protected static final String RbacCenterProtocolDef = "http";
    /** 默认授权中心地址:127.0.0.1:80 */
    protected static final String RbacCenterAddrDef = "127.0.0.1:80";
    /** 默认与授权中心同步时间间隔:5分钟 */
    protected static final int RbacCenterSyncIntervalDef = 5;
    /** 默认与授权中心同步超时时间:200ms */
    protected static final int RbacCenterSyncTimeoutDef = 200;
    
    protected static final int HttpStatus_OK = 200;
    protected static final int HttpStatus_Forbidden = 403;
    protected static final String ContentType_Html = "text/html;charset="+Encoding;
    
    protected AbstractProductAuth productAuth;
    
    /** 至少需要实现用户ID获取的方法,若用户未登陆,返回null */
    protected abstract String getUserId(HttpServletRequest request, HttpServletResponse response);
    
    /** 获取用户权限信息 */
    public UserAuth getUserAuth(String userId){
        return productAuth.getUserAuth(userId);
    }
    
    /**
     * 用户未登陆(即游客)处理,默认游客可以继续访问<br>
     * 重写此方法以进行用户未登陆时处理以及向response返回自定义信息
     * @return 可继续访问返回true,拦截返回false
     */
    protected boolean onNotLogin(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        return true;
    }
    
    /**
     * 重写此方法以进行用户无权访问处理<br>
     * 默认拦截并向response写入提示信息<br>
     * @return 可继续访问返回true,拦截返回false
     */
    protected boolean onNotAllowAccess(HttpServletRequest request,
            HttpServletResponse response,
            String userId, String uri) throws IOException {
        StringBuilder content = new StringBuilder();
        content.append(strIsEmpty(userId)?"<strong>游客</strong>":("用户<strong>"+userId+"</strong>"));
        content.append("无权访问<strong>").append(uri).append("</strong>");
        responeWriteFobiddenContent(response, content.toString());
        return false;
     }
    
    /** response内写入禁止访问的提示信息 */
    protected void responeWriteFobiddenContent(HttpServletResponse response,
            String content) throws IOException {
        response.setStatus(HttpStatus_Forbidden);
        response.setContentType(ContentType_Html);
        byte[] data = content.getBytes();
        response.setContentLength(data.length);
        OutputStream out = response.getOutputStream();
        out.write(data);
        out.close();
    }
    boolean strIsEmpty(String str) {
        return str==null || str.isEmpty();
    }
}
