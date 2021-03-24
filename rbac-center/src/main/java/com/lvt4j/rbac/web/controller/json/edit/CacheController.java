package com.lvt4j.rbac.web.controller.json.edit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lvt4j.rbac.BroadcastMsg4Center.ForceCacheClean;
import com.lvt4j.rbac.cluster.Cluster;
import com.lvt4j.rbac.dto.JsonResult;

/**
 *
 * @author LV on 2020年8月11日
 */
@RestController
@RequestMapping("edit/cache")
public class CacheController {

    @Autowired
    private Cluster cluster;
    
    @RequestMapping("clean")
    public JsonResult clean(
            @RequestParam(required=false) String proId,
            @RequestParam(required=false) String userId) {
        cluster.publish(new ForceCacheClean(proId, userId));
        return JsonResult.success();
    }
    
}