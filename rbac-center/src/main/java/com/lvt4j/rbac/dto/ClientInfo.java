package com.lvt4j.rbac.dto;

/**
 *
 * @author LV on 2020年8月15日
 */
public class ClientInfo {
    public String id;
    
    public long regTime;
    
    public String host;
    public String fromHost;
    public int fromPort;
    
    public String proId;
    public String version;
    
    public String txt() {
        return String.format("%s(%s,%s,%s,%s)", id, host, fromHost, proId, version);
    }
}