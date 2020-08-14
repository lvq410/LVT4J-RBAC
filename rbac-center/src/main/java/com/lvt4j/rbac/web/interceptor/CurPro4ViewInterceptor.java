package com.lvt4j.rbac.web.interceptor;

import static com.lvt4j.rbac.Consts.CookieName_CurProAutoId;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.lvt4j.rbac.dao.ProductMapper;
import com.lvt4j.rbac.po.Product;

/**
 *
 * @author LV
 */
@Order(InterceptorOrder.CurPro4View)
@Configuration("CurPro4ViewInterceptor")
class CurPro4ViewInterceptor implements WebMvcConfigurer,HandlerInterceptor {

    @Autowired
    private ProductMapper mapper;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this).addPathPatterns("", "/", "/view/**");
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {
        request.setAttribute("curPro", getCurProFromCookie(request.getCookies()));
        return true;
    }
    private Product getCurProFromCookie(Cookie[] cookies) {
        Integer proAutoId = getCurProAutoIdFromCookie(cookies);
        if(proAutoId==null) return null;
        return mapper.selectById(proAutoId);
    }
    private Integer getCurProAutoIdFromCookie(Cookie[] cookies) {
        if(ArrayUtils.isEmpty(cookies)) return null;
        for(Cookie cookie : cookies){
            if(!CookieName_CurProAutoId.equals(cookie.getName())) continue;
            try{
                String value = cookie.getValue();
                return Integer.valueOf(value);
            }catch(Exception ig){
                return null;
            }
        }
        return null;
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