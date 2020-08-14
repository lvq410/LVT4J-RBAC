package com.lvt4j.rbac.db.datasource;

import java.io.File;
import java.io.InputStream;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;

import com.lvt4j.rbac.RbacCenterApp;

/**
 *
 * @author LV on 2020年8月5日
 */
abstract class EmbeddedDataSourceConfig {

    @Value("${db.folder}")
    protected String folder;
    
    @PostConstruct
    private void init() {
        if(!folder.endsWith("/")) folder += "/";
    }
    
    protected final void initDbFile(String src, File db) throws Exception {
        InputStream is = RbacCenterApp.class.getResourceAsStream(src);
        FileUtils.copyInputStreamToFile(is, db);
    }
    
}