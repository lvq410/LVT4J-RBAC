package com.lvt4j.rbac.web.controller.json.edit;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lvt4j.rbac.dto.JsonResult;

/**
 *
 * @author LV on 2020年8月13日
 */
@RestController
@RequestMapping("edit")
public class TestMatchController {

    @RequestMapping("testMatch")
    public JsonResult testMatch(
            @RequestParam String regex,
            @RequestParam String text){
        return JsonResult.success(text.matches(regex));
    }
    
}