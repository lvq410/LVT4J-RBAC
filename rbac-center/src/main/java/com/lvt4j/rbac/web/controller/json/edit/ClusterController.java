package com.lvt4j.rbac.web.controller.json.edit;

import static java.util.stream.Collectors.joining;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info.Builder;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lvt4j.rbac.cluster.Cluster;
import com.lvt4j.rbac.dto.JsonResult;

/**
 *
 * @author LV on 2020年8月11日
 */
@RestController
@RequestMapping("edit/cluster")
public class ClusterController implements InfoContributor {

    @Autowired
    private Cluster cluster;
    
    @RequestMapping("stats")
    public JsonResult stats() throws Throwable {
        return JsonResult.success(cluster.getMemberStats());
    }

    @Override
    public void contribute(Builder builder) {
        builder.withDetail("cluster", cluster.getMemberShortStats().stream().map(m->m.id+" "+m.status).collect(joining("\n")));
    }
    
}