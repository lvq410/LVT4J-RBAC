package com.lvt4j.rbac;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
public abstract class RbacFilter extends RbacBaseFilter implements Filter {

    @Override
    public void init(FilterConfig config) throws ServletException {
        String proId = config.getInitParameter("proId");
        if(strIsEmpty(proId)) throw new IllegalArgumentException("产品ID必须配置!");
        String cacheCapacityStr = config.getInitParameter("cacheCapacity");
        int cacehCapacity = strIsEmpty(cacheCapacityStr)?CacheCapacityDef:Integer.parseInt(cacheCapacityStr);
        String rbacCenterProtocol = config.getInitParameter("rbacCenterProtocol");
        rbacCenterProtocol = strIsEmpty(rbacCenterProtocol)?RbacCenterProtocolDef:rbacCenterProtocol;
        String rbacCenterAddr = config.getInitParameter("rbacCenterAddr");
        rbacCenterAddr = strIsEmpty(rbacCenterAddr)?RbacCenterAddrDef:rbacCenterAddr;
        String rbacCenterSyncIntervalStr = config.getInitParameter("rbacCenterSyncInterval");
        int rbacCenterSyncInterval = strIsEmpty(rbacCenterSyncIntervalStr)?RbacCenterSyncIntervalDef:Integer.parseInt(rbacCenterSyncIntervalStr);
        String rbacCenterSyncTimeoutStr = config.getInitParameter("rbacCenterSyncTimeout");
        int rbacCenterSyncTimeout = strIsEmpty(rbacCenterSyncTimeoutStr)?RbacCenterSyncTimeoutDef:Integer.parseInt(rbacCenterSyncTimeoutStr);
        productAuth = new ProductAuth4Client(proId, cacehCapacity,
                rbacCenterProtocol, rbacCenterAddr, rbacCenterSyncInterval, rbacCenterSyncTimeout);
    }

    @Override
    public void doFilter(ServletRequest rawRequest, ServletResponse rawResponse,
            FilterChain chain) throws IOException, ServletException{
        if(!(rawRequest instanceof HttpServletRequest) //非标准请求,忽略权限验证
                || !(rawResponse instanceof HttpServletResponse)){
            chain.doFilter(rawRequest, rawResponse);
            return;
        }
        HttpServletRequest request = (HttpServletRequest) rawRequest;
        HttpServletResponse response = (HttpServletResponse) rawResponse;
        String userId = getUserId(request, response);
        if(strIsEmpty(userId) && !onNotLogin(request, response)) return;
        String uri = request.getRequestURI();
        if(!productAuth.allowAccess(userId, uri)
                && !onNotAllowAccess(request, response)) return;
        request.setAttribute(UserAuth.ReqAttr, productAuth.getUserAuth(userId));
        chain.doFilter(rawRequest, rawResponse);
    }

    @Override
    public void destroy() {
        productAuth.destory();
    }

}
