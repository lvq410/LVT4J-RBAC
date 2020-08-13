package com.lvt4j.rbac.velocity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.springframework.util.StringUtils;

/**
 *
 * @author LV on 2019年6月10日
 */
public class SpringResourceLoader extends ResourceLoader {

    public static final String NAME = "spring";

    public static final String SPRING_RESOURCE_LOADER_CLASS = "spring.resource.loader.class";

    public static final String SPRING_RESOURCE_LOADER_CACHE = "spring.resource.loader.cache";

    public static final String SPRING_RESOURCE_LOADER = "spring.resource.loader";

    public static final String SPRING_RESOURCE_LOADER_PATH = "spring.resource.loader.path";


    protected final Log logger = LogFactory.getLog(getClass());

    private org.springframework.core.io.ResourceLoader resourceLoader;

    private String[] resourceLoaderPaths;


    @Override
    public void init(ExtendedProperties configuration) {
            this.resourceLoader = (org.springframework.core.io.ResourceLoader)
                            this.rsvc.getApplicationAttribute(SPRING_RESOURCE_LOADER);
            String resourceLoaderPath = (String) this.rsvc.getApplicationAttribute(SPRING_RESOURCE_LOADER_PATH);
            if (this.resourceLoader == null) {
                    throw new IllegalArgumentException(
                                    "'resourceLoader' application attribute must be present for SpringResourceLoader");
            }
            if (resourceLoaderPath == null) {
                    throw new IllegalArgumentException(
                                    "'resourceLoaderPath' application attribute must be present for SpringResourceLoader");
            }
            this.resourceLoaderPaths = StringUtils.commaDelimitedListToStringArray(resourceLoaderPath);
            for (int i = 0; i < this.resourceLoaderPaths.length; i++) {
                    String path = this.resourceLoaderPaths[i];
                    if (!path.endsWith("/")) {
                            this.resourceLoaderPaths[i] = path + "/";
                    }
            }
            if (logger.isInfoEnabled()) {
                    logger.info("SpringResourceLoader for Velocity: using resource loader [" + this.resourceLoader +
                                    "] and resource loader paths " + Arrays.asList(this.resourceLoaderPaths));
            }
    }

    @Override
    public InputStream getResourceStream(String source) throws ResourceNotFoundException {
            if (logger.isDebugEnabled()) {
                    logger.debug("Looking for Velocity resource with name [" + source + "]");
            }
            for (String resourceLoaderPath : this.resourceLoaderPaths) {
                    org.springframework.core.io.Resource resource =
                                    this.resourceLoader.getResource(resourceLoaderPath + source);
                    try {
                            return resource.getInputStream();
                    }
                    catch (IOException ex) {
                            if (logger.isDebugEnabled()) {
                                    logger.debug("Could not find Velocity resource: " + resource);
                            }
                    }
            }
            throw new ResourceNotFoundException(
                            "Could not find resource [" + source + "] in Spring resource loader path");
    }

    @Override
    public boolean isSourceModified(Resource resource) {
            return false;
    }

    @Override
    public long getLastModified(Resource resource) {
            return 0;
    }

}