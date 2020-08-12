package com.lvt4j.rbac.web.controller.view;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import com.lvt4j.rbac.Utils;
import com.lvt4j.rbac.dao.ProductMapper;
import com.lvt4j.rbac.db.lock.Read;
import com.lvt4j.rbac.po.Product;

@Controller
class IndexController{

    @Autowired
    private ProductMapper mapper;
    
    @Read
    @RequestMapping("")
    public String index(
            HttpSession session){
        refreshCurPro(session);
        return "index";
    }
    
    @Read
    @RequestMapping("view/**")
    public String view(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpSession session){
        refreshCurPro(session);
        response.setHeader("Cache-control", "no-cache");
        response.setHeader("Expires", "0");
        String uri = request.getRequestURI().replaceFirst("/view", "");
        uri = FilenameUtils.removeExtension(uri);
        if(StringUtils.isBlank(uri)) uri = "index";
        return uri;
    }
    
    @GetMapping("error")
    public String error(
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) throws Exception {
        String uri = firstNonNull((String)request.getAttribute("javax.servlet.forward.request_uri"), request.getRequestURI());
        uri = StringEscapeUtils.escapeHtml4(URLDecoder.decode(uri, "utf8"));
        int statusCode = defaultIfNull((Integer)request.getAttribute("javax.servlet.error.status_code"), HttpStatus.OK.value());
        Throwable e = firstNonNull(
            (Throwable) request.getAttribute("org.springframework.web.servlet.DispatcherServlet.EXCEPTION"),
            (Throwable) request.getAttribute("javax.servlet.error.exception"));
        if(e!=null){
            Throwable detectingErr = e;
            while(detectingErr!=null && !(detectingErr instanceof ResponseStatusException)){
                detectingErr = detectingErr.getCause();
            }
            if(detectingErr!=null){
                ResponseStatusException detectedErr = (ResponseStatusException) detectingErr;
                statusCode = detectedErr.getStatus().value();
                e = detectedErr;
            }
        }
        String msg = e!=null?e.getMessage():null;
        String stack = Utils.printStack(e);
        if(statusCode==HttpStatus.NOT_FOUND.value()) msg="请求的地址"+uri+"无效<br>尝试从侧边栏进入试试？";
        else if(StringUtils.isNotEmpty(msg)) msg = StringEscapeUtils.escapeHtml4(msg);
        if(StringUtils.isNotEmpty(stack)) stack = StringEscapeUtils.escapeHtml4(stack);
        response.setStatus(statusCode);
        model.addAttribute("uri", uri);
        model.addAttribute("statusCode", statusCode);
        model.addAttribute("msg", msg);
        model.addAttribute("stack", stack);
        return "err";
    }
    
    private void refreshCurPro(HttpSession session){
        Product curPro = (Product)session.getAttribute("curPro");
        if(curPro!=null) session.setAttribute("curPro", mapper.selectById(curPro.autoId));
    }
    
}