package com.lvt4j.rbac.dto;

import java.io.Serializable;

import lombok.Data;

/**
 *
 * @author LV on 2020年8月15日
 */
@Data
public class ClientInfo implements Serializable {
    private static final long serialVersionUID = 3585080369261730299L;

    private final String id;
    
    private final long regTime;
    
    private final String host;
    private final String fromHost;
    private final int fromPort;
    
    private final String proId;
    private final String version;
    
    public String txt() {
        return String.format("%s(%s,%s,%s,%s)", id, host, fromHost, proId, version);
    }
}