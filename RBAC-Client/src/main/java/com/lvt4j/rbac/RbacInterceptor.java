package com.lvt4j.rbac;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 权限拦截器，配置参数:<br>
 * 1.proId(必须):在授权中心注册的产品ID<br>
 * 2.cacheCapacity(非必须):最大为多少用户缓存权限,默认{@link com.lvt4j.rbac.RbacBaseFilter#CacheCapacityDef 1000个}<br>
 * 3.rbacCenterProtocol(非必须):与授权中心同步的协议,http/https,默认{@link com.lvt4j.rbac.RbacBaseFilter#RbacCenterProtocolDef http}<br>
 * 4.rbacCenterAddr(非必须):授权中心服务地址,[host](:[port])形式,默认{@link com.lvt4j.rbac.RbacBaseFilter#RbacCenterAddrDef 127.0.0.1:80}<br>
 * 5.rbacCenterSyncInterval(非必须):与授权中心服务同步时间间隔,单位分钟,默认{@link com.lvt4j.rbac.RbacBaseFilter.RbacCenterSyncIntervalDef 5分钟}<br>
 * 6.rbacCenterSyncTimeout(非必须):与授权中心同步超时时间,单位毫秒,默认{@link com.lvt4j.rbac.RbacBaseFilter.RbacCenterSyncTimeoutDef 200ms}
 * 权限验证处理逻辑:<br>
 * 1.读取并向request的attribute:{@link com.lvt4j.rbac.UserAuth.ReqAttr "rbac"}写入用户权限信息<br>
 * 2.判断是否是{@link com.lvt4j.rbac.RbacInterceptor.RbacIgnore @RbacIgnore}标注的处理方法,若是,则直接通过权限验证<br>
 * 3.判断用户是否登陆,若未登陆且{@link com.lvt4j.rbac.RbacBaseFilter#onNoLogin onNotLogin}为false,则不通过验证;否则进行下一步<br>
 * 4.根据用户权限信息中拥有的访问项判断当前请求uri用户是否可访问<br>
 *   　若不能访问并且{@link com.lvt4j.rbac.RbacBaseFilter#onNotAllowAccess onNotAllowAccess}为false,则不通过验证;否则进行下一步<br>
 * 5.判断是否是{@link com.lvt4j.rbac.RbacInterceptor.PermissionNeed @PermissionNeed}标注的处理方法<br>
 *   　若是,则根据用户权限信息中拥有的授权项判断用户是否拥有该注解标注的授权项之一<br>
 *   　　若无且{@link #onForbidden}为false,则不通过验证;否则,则通过验证<br>
 *   　若不是,则通过验证
 * @author LV
 */
public abstract class RbacInterceptor extends RbacBaseFilter implements HandlerInterceptor{

    protected String proId;
    protected int cacehCapacity = CacheCapacityDef;
    protected String rbacCenterProtocol = RbacCenterProtocolDef;
    protected String rbacCenterAddr = RbacCenterAddrDef;
    protected int rbacCenterSyncInterval = RbacCenterSyncIntervalDef;
    protected int rbacCenterSyncTimeout = RbacCenterSyncTimeoutDef;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
            Object handleObject)throws Exception{
        init();
        String userId = getUserId(request, response);
        UserAuth userAuth = productAuth.getUserAuth(userId);
        request.setAttribute(UserAuth.ReqAttr, userAuth);
        if(isIgnoreRbac(handleObject)) return true;
        if(strIsEmpty(userId) && !onNotLogin(request, response)) return false;
        String uri = request.getRequestURI();
        if(!productAuth.allowAccess(userId, uri)
                && !onNotAllowAccess(request, response, userId, uri)) return false;
        if(!isPermit(request, response, userId, uri, userAuth, handleObject)) ;
        return true;
    }
    private void init() {
        if(productAuth!=null) return;
        synchronized(this){
            if(productAuth!=null) return;
            if(strIsEmpty(proId)) throw new IllegalArgumentException("产品ID必须配置!");
            productAuth = new ProductAuth4Client(proId, cacehCapacity,
                    rbacCenterProtocol, rbacCenterAddr, rbacCenterSyncInterval, rbacCenterSyncTimeout);
        }
    }
    private boolean isIgnoreRbac(Object handleObject){
        if(handleObject==null) return false;
        Class<?> handlerCls = handleObject.getClass();
        if(!"org.springframework.web.method.HandlerMethod".equals(handlerCls.getName())) return false;
        try{
            Method method = handlerCls.getDeclaredMethod("hasMethodAnnotation", Class.class);
            return (Boolean)method.invoke(handleObject, RbacIgnore.class);
        }catch(Exception ingore){}
        return false;
    }
    private boolean isPermit(HttpServletRequest request, HttpServletResponse response,
            String userId, String uri,
            UserAuth userAuth, Object handleObject){
        if(handleObject==null) return false;
        Class<?> handlerCls = handleObject.getClass();
        if(!"org.springframework.web.method.HandlerMethod".equals(handlerCls.getName())) return false;
        try{
            Method method = handlerCls.getDeclaredMethod("getMethodAnnotation", Class.class);
            PermissionNeed permissionNeed = (PermissionNeed)method.invoke(handleObject, PermissionNeed.class);
            if(permissionNeed==null) return true;
            String[] permissionIds = permissionNeed.value();
            for(String permission : permissionIds){
                if(!userAuth.permit(permission)) continue;
                return true;
            }
            if(onForbidden(request, response, userId, uri, permissionIds)) return true;
        }catch(Exception ingore){}
        return false;
    }
    
    protected boolean onForbidden(HttpServletRequest request,
            HttpServletResponse response,
            String userId, String uri, String[] permissionIds)throws Exception{
        StringBuilder content = new StringBuilder();
        content.append(strIsEmpty(userId)?"<strong>游客</strong>":("用户<strong>"+userId+"</strong>"));
        content.append("无权访问<strong>").append(uri).append("</strong><br>");
        content.append("原因为没有以下权限之一<br>");
        for(String permissionId : permissionIds)
            content.append("<strong>").append(permissionId).append("</strong><br>");
        responeWriteFobiddenContent(response, content.toString());
        return false;
    }
    
    @Override
    protected void finalize()throws Throwable{
        if(productAuth!=null) productAuth.destory();
        super.finalize();
    }
    
    @Override public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView)throws Exception{}
    @Override public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)throws Exception{}
    
    public void setProId(String proId){this.proId=proId;}
    public void setCacehCapacity(int cacehCapacity){this.cacehCapacity = cacehCapacity;}
    public void setRbacCenterProtocol(String rbacCenterProtocol){this.rbacCenterProtocol=rbacCenterProtocol;}
    public void setRbacCenterAddr(String rbacCenterAddr){this.rbacCenterAddr=rbacCenterAddr;}
    public void setRbacCenterSyncInterval(int rbacCenterSyncInterval){this.rbacCenterSyncInterval=rbacCenterSyncInterval;}
    public void setRbacCenterSyncTimeout(int rbacCenterSyncTimeout){this.rbacCenterSyncTimeout=rbacCenterSyncTimeout;}

    /**
     * 标注在Spring-mvc的HandlerMethod上,<br>
     * 表示由该HandlerMethod处理的请求不需要权限控制
     * @author LV
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RbacIgnore{}
    
    /**
     * 标注在Spring-mvc的HandlerMethod上,<br>
     * 表示由该HandlerMethod处理的请求需要指定的授权项才能访问
     * @author LV
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface PermissionNeed{String[] value();}
}
