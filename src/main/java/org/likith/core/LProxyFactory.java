package org.likith.core;

import org.likith.annotations.LLoggable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class LProxyFactory {
    public static Object createProxy(Object original){
        InvocationHandler handler=((proxy, method, args) -> {
            Method targetMethod=original.getClass().getMethod(method.getName(),method.getParameterTypes());
            if (targetMethod.isAnnotationPresent(LLoggable.class)){
                System.out.println("Before "+targetMethod);
                Object result= targetMethod.invoke(original,args);
                System.out.println("After "+targetMethod);
                return result;
            }
            return targetMethod.invoke(original,args);
        });
        return Proxy.newProxyInstance(original.getClass().getClassLoader(),original.getClass().getInterfaces(),handler);
    }
}
