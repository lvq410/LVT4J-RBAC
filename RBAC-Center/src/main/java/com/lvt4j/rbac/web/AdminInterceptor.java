package com.lvt4j.rbac.web;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.Base64;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.lvt4j.rbac.AdminConfig;

/**
 * @author LV
 */
public class AdminInterceptor implements HandlerInterceptor {

    @Autowired
    private AdminConfig adminConfig;
    
    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        String adminUser = (String) session.getAttribute("adminUser");
        String adminPwd = (String) session.getAttribute("adminPwd");
        if(adminConfig.isAdmin(adminUser, adminPwd)) return true;
        String[] userAndPwd = null;
        String authorization = request.getHeader("authorization");
        if(isBlank(authorization)) return onForbidden(response);
        try {
            userAndPwd = new String(Base64.getDecoder().decode(authorization.split(" ")[1])).split(":");
        } catch (Exception e) {
            return onForbidden(response);
        }
        if(userAndPwd==null) return onForbidden(response);
        if(isBlank(userAndPwd[0])) return onForbidden(response);
        if(isBlank(userAndPwd[1])) return onForbidden(response);
        if(!adminConfig.isAdmin(userAndPwd[0], userAndPwd[1])) return onForbidden(response);
        session.setAttribute("adminUser", userAndPwd[0]);
        session.setAttribute("adminPwd", userAndPwd[1]);
        return true;
    }
    
    private boolean onForbidden(HttpServletResponse response) throws Exception {
        response.setStatus(401);
        response.setHeader("WWW-authenticate","Basic realm=\"Need Authentication\"");
        response.getWriter().write("Need Admin Authentication");
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
            HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
    }

}
