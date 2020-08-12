package com.lvt4j.rbac.web.controller.json;

import static com.lvt4j.rbac.Utils.printStack;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.lvt4j.rbac.dto.JsonResult;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author LV on 2020年8月5日
 */
@Slf4j
@Configuration("jsonControllerConfig")
@ControllerAdvice(basePackageClasses=ControllerConfig.class)
class ControllerConfig extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class) @ResponseBody
    public ResponseEntity<Object> constraintViolationException(HttpServletRequest request, ConstraintViolationException e) throws Exception {
        return exceptionResponse(BAD_REQUEST, e.getConstraintViolations().stream().findFirst().orElse(null).getMessage(), e);
    }
    @ExceptionHandler(ResponseStatusException.class) @ResponseBody
    public final ResponseEntity<Object> responseStatusException(HttpServletRequest request, ResponseStatusException e) throws Exception {
        return exceptionResponse(e.getStatus(), e.getReason(), e);
    }
    @Override
    protected ResponseEntity<Object> handleBindException(BindException ex,
            HttpHeaders headers, HttpStatus status, WebRequest request) {
        return exceptionResponse(BAD_REQUEST, ex.getBindingResult().getAllErrors().get(0).getDefaultMessage(), ex);
    }
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatus status, WebRequest request) {
        List<ObjectError> errors = ex.getBindingResult().getAllErrors();
        if(errors.isEmpty()) return exceptionResponse(status, ex.getMessage(), ex);
        ObjectError firstError = errors.get(0);
        return exceptionResponse(status, firstError.getDefaultMessage(), ex);
    }
    @ExceptionHandler(Throwable.class) @ResponseBody
    public final ResponseEntity<Object> throwable(Throwable e) {
        HttpStatus status = INTERNAL_SERVER_ERROR;
        ResponseStatus statusAnn = AnnotatedElementUtils.findMergedAnnotation(e.getClass(), ResponseStatus.class);
        if(statusAnn!=null) status = statusAnn.code();
        return exceptionResponse(status, e.getMessage(), e);
    }
    @Override
    protected final ResponseEntity<Object> handleExceptionInternal(Exception ex,
            Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return exceptionResponse(status, ex.getMessage(), ex);
    }
    protected ResponseEntity<Object> exceptionResponse(HttpStatus status, String message, Throwable e) {
        if(log.isErrorEnabled()) log.error("status {} {} {}", status, message, printStack(e));
        return new ResponseEntity<>(JsonResult.fail(status.value(), message, e), status);
    }
    
    /** 异常转换为JsonResult#fail，自动识别各种异常进行不同err和msg的提取 */
    public JsonResult throwable2FailResult(Throwable e) {
        if(e instanceof ConstraintViolationException){
            return JsonResult.fail(BAD_REQUEST.value(), ((ConstraintViolationException)e).getConstraintViolations().stream().findFirst().orElse(null).getMessage(), e);
        }else if(e instanceof ResponseStatusException){
            ResponseStatusException ee = (ResponseStatusException) e;
            return JsonResult.fail(ee.getStatus().value(), ee.getReason(), ee);
        }else if(e instanceof Exception){
            Exception ee = (Exception) e;
            try{
                return (JsonResult) handleException(ee, null).getBody();
            }catch(Exception unknown){}
        }
        HttpStatus status = INTERNAL_SERVER_ERROR;
        ResponseStatus statusAnn = AnnotatedElementUtils.findMergedAnnotation(e.getClass(), ResponseStatus.class);
        if(statusAnn!=null) status = statusAnn.code();
        return JsonResult.fail(status.value(), e.getMessage(), e);
    }
    
}