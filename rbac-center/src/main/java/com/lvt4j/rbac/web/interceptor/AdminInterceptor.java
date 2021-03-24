package com.lvt4j.rbac.web.interceptor;

import static com.lvt4j.rbac.Utils.md5;
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
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.lvt4j.rbac.Consts;
import com.lvt4j.rbac.RbacCenterApp;

import lombok.extern.slf4j.Slf4j;

/**
 * @author LV
 */
@Slf4j
@Order(InterceptorOrder.Admin)
@Configuration("AdminInterceptor")
class AdminInterceptor implements WebMvcConfigurer, HandlerInterceptor {
    
    @Autowired
    private Environment env;
    
    private PropertiesConfiguration props;
    
    @PostConstruct
    private void init() throws Exception {
        File propFile = new File(Consts.ConfFolder, "admin.properties");
        if(!propFile.exists()) initAdminFile(propFile);
        props = new PropertiesConfiguration();
        props.setEncoding("utf-8");
        props.setFile(propFile);
        props.setReloadingStrategy(new FileChangedReloadingStrategy());
        props.load();
    }
    private void initAdminFile(File propFile) throws Exception {
        InputStream is = RbacCenterApp.class.getResourceAsStream("admin.properties");
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
        request.setAttribute("operator", userAndPwd[0]);
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
        if(isBlank(userId) || isBlank(pwd)) return false;
        return isAdminInProp(userId, md5(pwd));
    }
    private boolean isAdminInProp(String userId, String pwdMd5) {
        if(!props.containsKey("admin."+userId)) return isAdminInSpring(userId, pwdMd5);
        String storedPwdMd5 = props.getString("admin."+userId);
        if(isBlank(storedPwdMd5)) return false;
        return storedPwdMd5.equalsIgnoreCase(pwdMd5);
    }
    private boolean isAdminInSpring(String userId, String pwdMd5) {
        String storedPwdMd5 = env.getProperty("admin."+userId);
        if(isBlank(storedPwdMd5)) return false;
        return storedPwdMd5.equalsIgnoreCase(pwdMd5);
    }
}