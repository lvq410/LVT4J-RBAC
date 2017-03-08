/**
 * @(#)VelocityTplLoader.java, 2017年3月4日. 
 * 
 * Copyright 2017 Yodao, Inc. All rights reserved.
 * YODAO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.lvt4j.rbac;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;

/**
 * @author LV
 */
public class VelocityTplLoader extends ResourceLoader{

    private static final ClassLoader ClassLoader = VelocityTplLoader.class.getClassLoader();
    
    @Override public void init(ExtendedProperties configuration) {}
    
    @Override
    public InputStream getResourceStream(String source)
            throws ResourceNotFoundException {
        InputStream is = ClassLoader.getResourceAsStream(source);
        if(is!=null) return is;
        if(source.charAt(0)=='/') source = source.substring(1);
        source = Consts.VelocityTplContextPath+source;
        is = ClassLoader.getResourceAsStream(source);
        if(is!=null) return is;
        File file = new File(Consts.ResFolder, source);
        if(!file.exists()) return null;
        try{
            return new FileInputStream(file);
        }catch(FileNotFoundException ignore){
            return null;
        }
    }

    @Override
    public boolean isSourceModified(Resource resource) {
        return Config.isDebug;
    }

    @Override
    public long getLastModified(Resource resource) {
        return Config.isDebug?System.currentTimeMillis():0;
    }

}
