package com.lvt4j.rbac;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

abstract class RbacBaseFilter {

    /** 默认产品用户权限缓存容量:1000个用户的权限 */
    protected static final int CacheCapacityDef = 1000;
    /** 默认授权中心地址:127.0.0.1:80 */
    protected static final String RbacCenterAddrDef = "127.0.0.1:80";
    /** 默认与授权中心同步时间间隔:5分钟 */
    protected static final int RbacCenterSyncIntervalDef = 5;
    
    protected static final int HttpStatus_OK = 200;
    protected static final int HttpStatus_Forbidden = 403;
    protected static final String ContentType_Html = "text/html;charset=utf-8";
    
    protected AbstractProductAuth productAuth;
    
    /** 至少需要实现用户ID获取的方法,若用户未登陆,返回null */
    protected abstract String getUserId(HttpServletRequest request, HttpServletResponse response);
    
    /**
     * 处理是否登陆及访问项
     * @param request
     * @param response
     * @return 可继续执行返回true
     * @throws IOException
     */
    protected boolean handle(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        String userId = getUserId(request, response);
        if(userId==null){
            onNotLogin(request, response);
            return false;
        }
        String uri = request.getRequestURI();
        if(!productAuth.allowAccess(userId, uri)){
            onNotAllowAccess(request, response);
            return false;
        }
        return true;
    }
    
    /**
     * request内填充用户权限信息
     * @param request
     * @param response
     * @see RbacConst
     */
    protected void fillAuth(HttpServletRequest request,
            HttpServletResponse response) {
        String userId = getUserId(request, response);
        request.setAttribute("rbac", productAuth.getUserAuth(userId));
    }
    
    /**
     * 重写此方法以进行用户未登陆处理<br>
     * 默认向response写入未登录提示<br>
     * @return 可继续访问返回true,拦截返回false
     */
    protected boolean onNotLogin(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        String content = "用户未登陆!";
        responeWriteFobiddenContent(response, content);
        return false;
    }
    /**
     * 重写此方法以进行用户无权访问处理<br>
     * 默认向response写入提示信息<br>
     * @return 可继续访问返回true,拦截返回false
     */
    protected boolean onNotAllowAccess(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        String userId = getUserId(request, response);
        String uri = request.getRequestURI();
        String content = "用户["+userId+"]无权访问URI["+uri+"]";
        responeWriteFobiddenContent(response, content);
        return false;
     }
    /** 用户没有指定授权项时,向response写入提示信息 */
    protected void onNotPermitted(HttpServletRequest request,
            HttpServletResponse response, String permissionId) throws IOException {
        String userId = getUserId(request, response);
        String content = "用户["+userId+"]没有权限["+permissionId+"]";
        responeWriteFobiddenContent(response, content);
    }
    
    /** response内写入禁止访问的提示信息 */
    private void responeWriteFobiddenContent(HttpServletResponse response,
            String content) throws IOException {
        response.setStatus(HttpStatus_Forbidden);
        response.setContentType(ContentType_Html);
        response.setContentLength(content.length());
        response.getWriter().write(content);
        response.getWriter().close();
    }
    
}
