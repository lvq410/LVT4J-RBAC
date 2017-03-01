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
 * 配置参数:<br>
 * 1.proId(必须):在授权中心注册的产品ID<br>
 * 2.cacheCapacity(非必须):最大为多少用户缓存权限<br>
 * 3.rbacCenterAddress(必须):授权中心服务地址,[host](:[port])形式<br>
 * @author lichenxi
 *
 */
public abstract class RbacFilter extends RbacBaseFilter implements Filter {

    @Override
    public void init(FilterConfig config) throws ServletException {
        String proId = config.getInitParameter("proId");
        if(strIsEmpty(proId)) throw new IllegalArgumentException("产品ID必须配置!");
        String cacheCapacityStr = config.getInitParameter("cacheCapacity");
        int cacehCapacity = strIsEmpty(cacheCapacityStr)?CacheCapacityDef:Integer.parseInt(cacheCapacityStr);
        String rbacCenterAddress = config.getInitParameter("rbacCenterAddress");
        if(strIsEmpty(rbacCenterAddress)) throw new IllegalArgumentException("授权中心服务地址必须配置!");
        productAuth = new ProductAuthImp(proId, cacehCapacity, rbacCenterAddress);
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

    private boolean strIsEmpty(String str) {
        return str==null || str.isEmpty();
    }
    
}
