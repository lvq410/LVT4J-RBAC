package com.lvt4j.rbac.web.controller.json.edit;

import static com.lvt4j.rbac.Consts.CookieName_CurProAutoId;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lvt4j.rbac.dao.ProductMapper;
import com.lvt4j.rbac.db.lock.Read;
import com.lvt4j.rbac.dto.JsonResult;
import com.lvt4j.rbac.po.Product;

/**
 *
 * @author LV on 2020年8月5日
 */
@RestController
@RequestMapping("edit")
public class CurProController {

    @Autowired
    private ProductMapper mapper;
    
    @Read
    @RequestMapping("curProSet")
    public JsonResult curProSet(
            HttpServletResponse res,
            @RequestParam int proAutoId) throws Exception {
        Product pro = mapper.selectById(proAutoId);
        String proAutoIdStr = pro==null?EMPTY:String.valueOf(pro.autoId);
        Cookie cookie = new Cookie(CookieName_CurProAutoId, proAutoIdStr);
        cookie.setMaxAge(Integer.MAX_VALUE);
        cookie.setPath("/");
        res.addCookie(cookie);
        return JsonResult.success();
    }
    
}
