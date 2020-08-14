package com.lvt4j.rbac.web.controller.json.edit;

import java.beans.PropertyEditorSupport;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lvt4j.rbac.dto.ListInt;
import com.lvt4j.rbac.dto.ListStr;
import com.lvt4j.rbac.dto.MapIntStr;
import com.lvt4j.rbac.dto.Pager;

import lombok.SneakyThrows;

/**
 *
 * @author LV on 2020年8月5日
 */
@ControllerAdvice(basePackageClasses=PropertyEditors.class)
class PropertyEditors implements WebMvcConfigurer {

    @Autowired
    private ObjectMapper objectMapper;
    
    private List<JsonBasedPropertyEditor> editors = Arrays.asList(
        new JsonBasedPropertyEditor(MapIntStr.class),
        new JsonBasedPropertyEditor(ListInt.class),
        new JsonBasedPropertyEditor(ListStr.class),
        new JsonBasedPropertyEditor(Pager.class),
        new JsonBasedPropertyEditor(Date.class));
    
    @InitBinder
    public void bind(DataBinder binder) {
        editors.forEach(e->binder.registerCustomEditor(e.cls, e));
    }
    
    class JsonBasedPropertyEditor extends PropertyEditorSupport {
        private Class<?> cls;
        private JsonBasedPropertyEditor(Class<?> cls) {
            this.cls = cls;
        }
        @SneakyThrows
        public void setAsText(String text) throws IllegalArgumentException {
            setValue(objectMapper.readValue(text, cls));
        }
    }
    
}