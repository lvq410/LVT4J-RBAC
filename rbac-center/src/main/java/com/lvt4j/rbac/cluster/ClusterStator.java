package com.lvt4j.rbac.cluster;

import java.util.Collection;
import java.util.List;

import com.lvt4j.rbac.service.ClientService.ClientInfo;

import lombok.Data;

/**
 * 获取集群状态
 * @author LV on 2020年8月3日
 */
public interface ClusterStator {

    public List<MemberStatus> getMemberStats();
    
    @Data
    public static class MemberStatus {
        public String address;
        /** master/slave/unreachable */
        public String status;
        public Collection<ClientInfo> clients;
    }
    
}