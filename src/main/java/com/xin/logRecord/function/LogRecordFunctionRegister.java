package com.xin.logRecord.function;

import com.xin.logRecord.annotation.LogRecordFunction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class LogRecordFunctionRegister implements BeanPostProcessor {
    private final static Map<String, Method> functionMap = new HashMap<>();

    /**
     * postProcessBeforeInitialization处理的是未被代理的对象
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        for (Method method : bean.getClass().getDeclaredMethods()) {
            if (method.getDeclaredAnnotation(LogRecordFunction.class) != null && isStaticMethod(method)) {
                String name = method.getDeclaredAnnotation(LogRecordFunction.class).value();
                name = name.length() == 0 ? method.getName() : name;
                functionMap.put(name, method);
                log.info("LogRecordFunctionRegister register custom function [{}] as name [{}]", method.getName(), name);
            }
        }
        return bean;
    }

    public static void registerFunction(StandardEvaluationContext context) {
        functionMap.forEach(context::registerFunction);
    }

    private static boolean isStaticMethod(Method method) {
        if (method == null) {
            return false;
        }
        int modifiers = method.getModifiers();
        return Modifier.isStatic(modifiers);
    }
}
