package com.lvt4j.rbac.web;

import java.util.Base64;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.lvt4j.rbac.Config;

/**
 * @author LV
 */
public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        String adminUser = (String) session.getAttribute("adminUser");
        String adminPwd = (String) session.getAttribute("adminPwd");
        if(Config.adminUser().equals(adminUser)
                && Config.adminPwd().equals(adminPwd)) return true;
        String[] userAndPwd = null;
        String authorization = request.getHeader("authorization");
        if(StringUtils.isEmpty(authorization)) return onForbidden(response);
        try {
            userAndPwd = new String(Base64.getDecoder().decode(authorization.split(" ")[1])).split(":");
        } catch (Exception e) {
            return onForbidden(response);
        }
        if(userAndPwd==null) return onForbidden(response);
        if(StringUtils.isEmpty(userAndPwd[0])) return onForbidden(response);
        if(StringUtils.isEmpty(userAndPwd[1])) return onForbidden(response);
        if(!Config.adminUser().equals(userAndPwd[0])) return onForbidden(response);
        if(!Config.adminPwd().equals(userAndPwd[1])) return onForbidden(response);
        session.setAttribute("adminUser", userAndPwd[0]);
        session.setAttribute("adminPwd", userAndPwd[1]);
        return true;
    }
    private boolean onForbidden(HttpServletResponse response) throws Exception {
        response.setStatus(401);
        response.setHeader("WWW-authenticate","Basic realm=\"请输入管理员密码\"");
        response.getWriter().write("需要管理员信息!");
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
