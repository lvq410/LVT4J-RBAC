package com.lvt4j.rbac;

import java.io.File;

import org.springframework.boot.autoconfigure.velocity.VelocityAutoConfiguration;
import org.springframework.boot.autoconfigure.velocity.VelocityProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

/**
 * velocity的配置
 * @author lichenxi
 */
@Configuration
@SuppressWarnings("deprecation")
public class VelocityConfig extends VelocityAutoConfiguration {

    public VelocityConfig(ApplicationContext applicationContext,
            VelocityProperties properties) {
        super(applicationContext, properties);
        properties.setResourceLoaderPath(new File("web/vm").toURI().toString());
    }

}
