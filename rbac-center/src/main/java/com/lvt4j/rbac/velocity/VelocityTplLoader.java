package com.lvt4j.rbac.velocity;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.springframework.web.server.ResponseStatusException;

import com.lvt4j.rbac.Consts;

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
        if(!source.isEmpty() && source.charAt(0)=='/') source = source.substring(1);
        File vmFile = new File(Consts.VMFolder, source);
        try{
            return new FileInputStream(vmFile);
        }catch(FileNotFoundException e){
            throw new ResponseStatusException(NOT_FOUND, "找不到vm资源["+source+"]");
        }
    }

    @Override
    public boolean isSourceModified(Resource resource) {
        return false;
    }

    @Override
    public long getLastModified(Resource resource) {
        return 0L;
    }

}
