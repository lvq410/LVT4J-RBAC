package com.lvt4j.rbac.client;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class RbacFilter extends RbacBaseFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException{
        if(!(request instanceof HttpServletRequest) //非标准请求,忽略权限验证
                || !(response instanceof HttpServletResponse)){
            chain.doFilter(request, response);
            return;
        }
        if(!handle((HttpServletRequest)request, (HttpServletResponse)response)) return;
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        
    }

}
