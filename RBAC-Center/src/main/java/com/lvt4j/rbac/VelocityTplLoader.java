package com.lvt4j.rbac;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;

/**
 * @author LV
 */
@Slf4j
public class VelocityTplLoader extends ResourceLoader{

    private static final ClassLoader ClassLoader = VelocityTplLoader.class.getClassLoader();
    
    @Override public void init(ExtendedProperties configuration) {}
    
    @Override
    public InputStream getResourceStream(String source)
            throws ResourceNotFoundException {
        InputStream is = ClassLoader.getResourceAsStream(source);
        if(is!=null) return is;
        if(!source.isEmpty() && source.charAt(0)=='/') source = source.substring(1);
        File vmFile = new File(Consts.VMFolder, source);
        try{
            return new FileInputStream(vmFile);
        }catch(FileNotFoundException e){
            log.error("无法加载vm资源:{}", source, e);
            return load404();
        }
    }

    private InputStream load404() {
        File vmFile = new File(Consts.VMFolder, "404.vm");
        try{
            return new FileInputStream(vmFile);
        }catch(FileNotFoundException e){
            log.error("无法加载404.vm", e);
            return new ByteArrayInputStream("404".getBytes());
        }
    }
    
    @Override
    public boolean isSourceModified(Resource resource) {
        return true;
    }

    @Override
    public long getLastModified(Resource resource) {
        return System.currentTimeMillis();
    }

}
