package com.lvt4j.rbac;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 权限拦截器，配置参数:<br>
 * 1.proId(必须):在授权中心注册的产品ID<br>
 * 2.cacheCapacity(非必须):最大为多少用户缓存权限,默认{@link com.lvt4j.rbac.RbacBaseFilter#CacheCapacityDef 1000个}<br>
 * 3.rbacCenterProtocol(非必须):与授权中心同步的协议,http/https,默认{@link com.lvt4j.rbac.RbacBaseFilter#RbacCenterProtocolDef http}<br>
 * 4.rbacCenterAddr(非必须):授权中心服务地址,[host](:[port])形式,默认{@link com.lvt4j.rbac.RbacBaseFilter#RbacCenterAddrDef 127.0.0.1:80}<br>
 * 5.rbacCenterSyncInterval(非必须):与授权中心服务同步时间间隔,单位分钟,默认{@link com.lvt4j.rbac.RbacBaseFilter.RbacCenterSyncIntervalDef 5分钟}<br>
 * 6.rbacCenterSyncTimeout(非必须):与授权中心同步超时时间,单位毫秒,默认{@link com.lvt4j.rbac.RbacBaseFilter.RbacCenterSyncTimeoutDef 200ms}
 * @author LV
 */
public abstract class RbacInterceptor extends RbacBaseFilter implements HandlerInterceptor {

    protected String proId;
    protected int cacehCapacity = CacheCapacityDef;
    protected String rbacCenterProtocol = RbacCenterProtocolDef;
    protected String rbacCenterAddr = RbacCenterAddrDef;
    protected int rbacCenterSyncInterval = RbacCenterSyncIntervalDef;
    protected int rbacCenterSyncTimeout = RbacCenterSyncTimeoutDef;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
            Object handleObject) throws Exception{
        init();
        String userId = getUserId(request, response);
        if(strIsEmpty(userId) && !onNotLogin(request, response)) return false;
        String uri = request.getRequestURI();
        if(!productAuth.allowAccess(userId, uri)
                && !onNotAllowAccess(request, response)) return false;
        request.setAttribute(UserAuth.ReqAttr, productAuth.getUserAuth(userId));
        return true;
    }
    private void init() {
        if(productAuth!=null) return;
        synchronized (this) {
            if(productAuth!=null) return;
            if(strIsEmpty(proId)) throw new IllegalArgumentException("产品ID必须配置!");
            productAuth = new ProductAuth4Client(proId, cacehCapacity,
                    rbacCenterProtocol, rbacCenterAddr, rbacCenterSyncInterval, rbacCenterSyncTimeout);
        }
    }
    
    @Override
    protected void finalize() throws Throwable {
        if(productAuth!=null) productAuth.destory();
        super.finalize();
    }
    
    public void setProId(String proId){this.proId=proId;}
    public void setCacehCapacity(int cacehCapacity){this.cacehCapacity = cacehCapacity;}
    public void setRbacCenterProtocol(String rbacCenterProtocol){this.rbacCenterProtocol=rbacCenterProtocol;}
    public void setRbacCenterAddr(String rbacCenterAddr){this.rbacCenterAddr=rbacCenterAddr;}
    public void setRbacCenterSyncInterval(int rbacCenterSyncInterval){this.rbacCenterSyncInterval=rbacCenterSyncInterval;}
    public void setRbacCenterSyncTimeout(int rbacCenterSyncTimeout){this.rbacCenterSyncTimeout=rbacCenterSyncTimeout;}
}
