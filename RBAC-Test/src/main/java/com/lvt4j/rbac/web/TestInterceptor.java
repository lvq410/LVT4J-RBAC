package com.lvt4j.rbac.web;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lvt4j.rbac.RbacInterceptor;
import com.lvt4j.rbac.SpringConfig;

/**
 * @author LV
 */
public class TestInterceptor extends RbacInterceptor {

    @Override
    protected String getUserId(HttpServletRequest request,
            HttpServletResponse response) {
        return request.getParameter("userId");
    }
    
    @Override
    protected boolean onNotLogin(HttpServletRequest request, HttpServletResponse response) throws IOException{
        if(SpringConfig.props.getBoolean("onNotLogin")) return true;
        responeWriteForbiddenContent(response, "onNotLogin Forbidden");
        return false;
    }
    
    @Override
    protected boolean onNotRegister(HttpServletRequest request, HttpServletResponse response) throws IOException{
        if(SpringConfig.props.getBoolean("onNotRegister")) return true;
        responeWriteForbiddenContent(response, "onNotRegister Forbidden");
        return false;
    }
    
    @Override
    protected boolean onNotAllowAccess(HttpServletRequest request, HttpServletResponse response, String userId,
            String uri) throws IOException{
        if(SpringConfig.props.getBoolean("onNotAllowAccess")) return true;
        responeWriteForbiddenContent(response, "onNotAllowAccess Forbidden");
        return false;
    }
    
    @Override
    protected boolean onForbidden(HttpServletRequest request, HttpServletResponse response, String userId, String uri)
            throws IOException{
        if(SpringConfig.props.getBoolean("onForbidden")) return true;
        responeWriteForbiddenContent(response, "onForbidden Forbidden");
        return false;
    }
}
