package com.mybilibili.common.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;


@Aspect
@Component
public class GlobalOperationAspect {

    /**
     * 方法上的配置优先，类上的配置兜底，后面加别的拦截注解复用这一套逻辑。
     */
    public <T extends Annotation> T getAnnotation(JoinPoint point, Class<T> annotationClass) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method interfaceMethod = signature.getMethod();
        Class<?> targetClass = point.getTarget().getClass();

        Method targetMethod;
        try {
            targetMethod = targetClass.getMethod(signature.getName(), signature.getParameterTypes());
        } catch (NoSuchMethodException e) {
            targetMethod = interfaceMethod;
        }

        T methodAnnotation = AnnotationUtils.findAnnotation(targetMethod, annotationClass);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }

        return AnnotationUtils.findAnnotation(targetClass, annotationClass);
    }
}
