package com.lvt4j.rbac.cluster;

import com.lvt4j.rbac.dto.NodeInfo;

/**
 *
 * @author LV on 2021年3月4日
 */
public interface OnMasterChangedListener {
    
    public static final int Order_OpLogCleaner = 0;
    public static final int Order_H2Backuper = Order_OpLogCleaner+1;
    public static final int Order_H2WebServer = Order_H2Backuper+1;
    public static final int Order_H2TcpServer = Order_H2WebServer+1;
    public static final int Order_H2DataSource = Order_H2TcpServer+1;
    
    public int getOrder();
    public void beforeMasterChange() throws Throwable;
    public void afterMasterChanged(boolean isLocalMaster, NodeInfo masterInfo) throws Throwable;
}