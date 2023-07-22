package com.sinozo.config.custom;

import org.springframework.cache.interceptor.KeyGenerator;

import java.lang.reflect.Method;

import static org.springframework.cache.interceptor.SimpleKeyGenerator.generateKey;

public class CustomKeyGenerate implements KeyGenerator {


    @Override
    public Object generate(Object target, Method method, Object... params) {

        String className = target.getClass().getCanonicalName();

        String methodName = method.getName();

        if (params.length == 0) {

            return className+"_"+methodName;

        }

        Object[] newParams = new Object[params.length+1];

        System.arraycopy(params, 0, newParams, 0, params.length);

        //把类名+方法名作为最后一个参数加入到参数列表中

        newParams[params.length]=className+"_"+methodName;

        return generateKey(newParams);

    }
}
