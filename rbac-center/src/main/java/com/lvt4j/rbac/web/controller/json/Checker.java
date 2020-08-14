package com.lvt4j.rbac.web.controller.json;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import lombok.NonNull;

/**
 * 数据校验，不符合预期抛{@link ResponseStatusException}
 * @author LV on 2019年5月22日
 */
public class Checker {

    /**
     * 期望expression=true，否则抛{@link HttpStatus#BAD_REQUEST}
     * @param expression
     * @param messageFormat
     * @param args
     */
    public static void isTrue(boolean expression, @NonNull String messageFormat, Object... args) {
        isTrue(expression, BAD_REQUEST, messageFormat, args);
    }
    
    /**
     * 期望expression=true，否则抛{@link HttpStatus#FORBIDDEN}
     * @param expression
     * @param messageFormat
     * @param args
     */
    public static final void permit(boolean expression, @NonNull String messageFormat, Object... args) {
        isTrue(expression, FORBIDDEN, messageFormat, args);
    }
    
    /**
     * 期望expression=true
     * @param expression
     * @param messageFormat
     * @param args
     */
    public static void isTrue(boolean expression, @NonNull HttpStatus status, @NonNull String messageFormat, Object... args) {
        if(expression) return;
        throw new ResponseStatusException(status, String.format(messageFormat, args));
    }
    
    public static void isNotBlank(CharSequence cs, @NonNull String messageFormat, Object... args) {
        isTrue(StringUtils.isNotBlank(cs), messageFormat, args);
    }
    
    public static void isNotBlank(CharSequence cs, @NonNull HttpStatus status, @NonNull String messageFormat, Object... args) {
        isTrue(StringUtils.isNotBlank(cs), status, messageFormat, args);
    }
    
    public static <T extends Comparable<T>> void inclusiveBetween(@NonNull T start, @NonNull T end, @NonNull T value,
            @NonNull String messageFormat, Object... args) {
        inclusiveBetween(start, end, value, BAD_REQUEST, messageFormat, args);
    }
    
    public static <T extends Comparable<T>> void inclusiveBetween(@NonNull T start, @NonNull T end, @NonNull T value,
            @NonNull HttpStatus status, @NonNull String messageFormat, Object... args) {
        isTrue(start.compareTo(value)<=0 && end.compareTo(value)>=0, status, messageFormat, args);
    }
    
}