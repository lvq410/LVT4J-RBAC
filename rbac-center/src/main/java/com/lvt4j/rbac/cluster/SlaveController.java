package com.lvt4j.rbac.cluster;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lvt4j.rbac.condition.IsSlave;
import com.lvt4j.rbac.dto.ClientInfo;
import com.lvt4j.rbac.service.ClientService;

/**
 *
 * @author LV on 2020年8月11日
 */
@RestController
@RequestMapping("cluster")
@Conditional(IsSlave.class)
public class SlaveController {
    
    @Autowired
    private ClientService clientService;
    
    @GetMapping("clients")
    public Collection<ClientInfo> getClients() {
        return clientService.getClients();
    }

}