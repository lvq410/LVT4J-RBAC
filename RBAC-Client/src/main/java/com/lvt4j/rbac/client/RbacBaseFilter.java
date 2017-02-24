package com.lvt4j.rbac.client;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lvt4j.rbac.data.AbstractProductAuth;

abstract class RbacBaseFilter {

    protected static final int HttpStatus_OK = 200;
    protected static final int HttpStatus_Forbidden = 403;
    
    protected static final String ContentType_Html = "text/html;charset=UTF-8";
    
    protected String proId;
    
    protected AbstractProductAuth productAuth;
    
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
    
    /** 用户未登陆,向response写入未登录提示 */
    protected void onNotLogin(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        String content = "用户未登陆!";
        responeWriteFobiddenContent(response, content);
    }
    /** 用户无权访问时,向response写入提示信息 */
    protected void onNotAllowAccess(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        String userId = getUserId(request, response);
        String uri = request.getRequestURI();
        String content = "用户["+userId+"]无权访问URI["+uri+"]";
        responeWriteFobiddenContent(response, content);
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
    
    public void setProId(String proId){
        this.proId = proId;
        productAuth = new ProductAuthImp(proId);
    }
}
