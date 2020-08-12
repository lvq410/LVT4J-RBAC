package com.lvt4j.rbac.web.controller.view;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

import com.lvt4j.rbac.Utils;

/**
 *
 * @author LV on 2020年8月5日
 */
@Configuration("viewControllerConfig")
@ControllerAdvice(basePackageClasses=ControllerConfig.class)
class ControllerConfig implements ErrorController{

    @ExceptionHandler(ResponseStatusException.class)
    public String exhandler(HttpServletRequest req, HttpServletResponse res, Model model, ResponseStatusException e) {
        res.setStatus(e.getStatus().value());
        model.addAttribute("uri", req.getRequestURI());
        model.addAttribute("statusCode", e.getStatus().value());
        model.addAttribute("msg", e.getMessage());
        model.addAttribute("stack", Utils.printStack(e));
        return "err";
    }
    @ExceptionHandler
    public String exhandler(HttpServletRequest req, HttpServletResponse res, Model model, Exception e) {
        HttpStatus status = INTERNAL_SERVER_ERROR;
        ResponseStatus statusAnn = AnnotatedElementUtils.findMergedAnnotation(e.getClass(), ResponseStatus.class);
        if(statusAnn!=null) status = statusAnn.code();
        res.setStatus(status.value());
        model.addAttribute("uri", req.getRequestURI());
        model.addAttribute("statusCode", status.value());
        model.addAttribute("msg", e.getMessage());
        model.addAttribute("stack", Utils.printStack(e));
        return "err";
    }
    
    @Override
    public String getErrorPath() {
        return "error";
    }
    
}