package com.lvt4j.rbac;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author LV on 2020年8月3日
 */
@Slf4j
public class Utils {
    
    /**
     * 打印异常堆栈到字符串
     * @param e
     * @return null if e==null
     * @see #stack(Throwable)
     */
    public static String printStack(Throwable e) {
        if(e==null) return null;
        return StringUtils.join(stack(e), "\n");
    }
    /**
     * 收集异常堆栈信息<br>
     * 如果想要对特定异常增加额外信息在堆栈中，注册额外信息提取{@link #exceptionExtraMsgerRegister}
     * @param e
     * @return
     */
    public static List<String> stack(Throwable e) {
        return stack(new LinkedList<String>(), e);
    }
    private static List<String> stack(List<String> stacks, Throwable e) {
        if(e==null) return stacks;
        String exClsName = e.getClass().getName();
        String msg = exClsName+" : "+defaultIfNull(e.getMessage(), EMPTY);
        stacks.add(msg);
        StackTraceElement[] stackTraceElements = e.getStackTrace();
        if(stackTraceElements==null) return stack(stacks, e.getCause());
        for(StackTraceElement stackTraceElement : stackTraceElements) stacks.add("\tat "+stackTraceElement.toString());
        return stack(stacks, e.getCause());
    }
    
    public static String parseIPFromReq(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        if(StringUtils.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) return ip.split(",")[0];
        ip = req.getHeader("Proxy-Client-IP");
        if(StringUtils.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) return ip;
        ip = req.getHeader("WL-Proxy-Client-IP");
        if(StringUtils.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) return ip;
        ip = req.getHeader("X-Real-IP");
        if(StringUtils.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) return ip;
        return req.getRemoteAddr();
    }
    
    /** 将对象序列化为base64编码 */
    public static String seriaBase64(Serializable obj) {
        try{
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        }catch(Throwable ig){
            log.warn("序列化对象{}异常", obj, ig);
            return null;
        }
    }
    
    public static void sse(SseEmitter emitter, Serializable msg, Consumer<SseEmitter> onException) {
        sseRaw(emitter, seriaBase64(msg), onException);
    }
    public static void sses(Collection<SseEmitter> emitters, Serializable msg, Consumer<SseEmitter> onException) {
        ssesRaw(emitters, seriaBase64(msg), onException);
    }
    public static void ssesRaw(Collection<SseEmitter> emitters, Object msg, Consumer<SseEmitter> onException) {
        emitters.parallelStream().forEach(emitter->sseRaw(emitter, msg, onException));
    }
    public static void sseRaw(SseEmitter emitter, Object msg, Consumer<SseEmitter> onException) {
        try{
            emitter.send(msg);
        }catch(Exception e){
            log.warn("sse推送消息{}失败", msg, e);
            emitter.complete();
            if(onException!=null) onException.accept(emitter);
        }
    }
    
}