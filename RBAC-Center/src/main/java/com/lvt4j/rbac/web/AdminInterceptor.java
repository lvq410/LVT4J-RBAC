package com.lvt4j.rbac.web;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.File;
import java.io.InputStream;
import java.util.Base64;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.lvt4j.rbac.Consts;
import com.lvt4j.rbac.RbacCenter;

import lombok.extern.slf4j.Slf4j;

/**
 * @author LV
 */
@Slf4j
@Order(InterceptorOrder.Admin)
@Configuration("AdminInterceptor")
class AdminInterceptor extends WebMvcConfigurerAdapter implements HandlerInterceptor {
    
    @Autowired
    private Environment env;
    
    private PropertiesConfiguration props;
    
    @PostConstruct
    private void init() throws Exception {
        File propFile = new File(Consts.ConfFolder, "application.properties");
        if(!propFile.exists()) initAdminFile(propFile);
        props = new PropertiesConfiguration();
        props.setEncoding("utf-8");
        props.setFile(propFile);
        props.setReloadingStrategy(new FileChangedReloadingStrategy());
        props.load();
        
    }
    private void initAdminFile(File propFile) throws Exception {
        InputStream is = RbacCenter.class.getResourceAsStream("admin.properties");
        FileUtils.copyInputStreamToFile(is, propFile);
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this).addPathPatterns("", "/", "/edit/**", "/view/**");
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {
        String[] userAndPwd = null;
        String authorization = request.getHeader("authorization");
        if(StringUtils.isNotBlank(authorization)){
            try{
                userAndPwd = new String(Base64.getDecoder().decode(authorization.split(" ")[1])).split(":", 2);
                if(userAndPwd.length!=2) userAndPwd=null;
            }catch(Exception e){
                if(log.isDebugEnabled()) log.debug("请求Auth头{}解析异常", authorization, e);
            }
        }
        if(userAndPwd==null) return onForbidden(response);
        if(userAndPwd.length!=2) return onForbidden(response);
        if(!isAdmin(userAndPwd[0], userAndPwd[1])) return onForbidden(response);
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
    
    public boolean isAdmin(String userId, String pwd) {
        if(isBlank(userId) || isBlank(userId)) return false;
        if(isAdminInProp(userId, pwd)) return true;
        if(isAdminInSpring(userId, pwd)) return true;
        return false;
    }
    private boolean isAdminInProp(String userId, String pwd) {
        String storedPwd = props.getString("admin."+userId);
        if(isBlank(storedPwd)) return false;
        if(!storedPwd.equals(pwd)) return false;
        return true;
    }
    private boolean isAdminInSpring(String userId, String pwd) {
        String storedPwd = env.getProperty("admin."+userId);
        if(isBlank(storedPwd)) return false;
        if(!storedPwd.equals(pwd)) return false;
        return true;
    }
}