package org.likith.core;

import org.likith.annotations.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.cert.TrustAnchor;
import java.util.*;

public class LBootContext {
    private final Map<Class<?>,Object> beanMap=new HashMap<>();
    private final Map<Object, Object> proxyToOriginal = new HashMap<>(); // NEW!

    public void init(List<Class<?>> classes) throws Exception {
        LStartupLogger.logComponentScan("org.likith", classes.size());
        // STEP 1: Create all "Real" instances first (No Proxies yet!)
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(LComponent.class)) {
                Object instance = clazz.getDeclaredConstructor().newInstance();
                beanMap.put(clazz, instance);
                LStartupLogger.logBeanCreation(clazz.getSimpleName(), "Component");

            }
            if (clazz.isAnnotationPresent(LConfiguration.class)) {
                Object configInstance = clazz.getDeclaredConstructor().newInstance();
                beanMap.put(clazz, configInstance);  // Store the config itself
                LStartupLogger.logBeanCreation(clazz.getSimpleName(), "Configuration");

                for (Method method : clazz.getMethods()) {
                    if (method.isAnnotationPresent(LBean.class)) {
                        // Invoke the @Bean method
                        Object beanInstance = method.invoke(configInstance);

                        // Store with the RETURN TYPE as key
                        Class<?> beanType = method.getReturnType();
                        beanMap.put(beanType, beanInstance);

                        LStartupLogger.logBeanCreation(beanType.getSimpleName(), "Bean");

                    }
                }
            }
        }

        // STEP 3: Inject dependencies into the REAL objects
        for (Object instance : beanMap.values()) {
            for (Field field : instance.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(LAutowired.class)) {
                    Object dependency = resolveDependency(field);
                    field.setAccessible(true);
                    field.set(instance, dependency);
                }
                else if (field.isAnnotationPresent(LValue.class)) {
                    LValue annotation = field.getAnnotation(LValue.class);
                    String propertyKey = annotation.value();
                    String defaultValue = annotation.defaultValue();

                    field.setAccessible(true);

                    if (field.getType() == String.class) {
                        String value = LPropertyLoader.getProperty(propertyKey, defaultValue);
                        field.set(instance, value);
                    } else if (field.getType() == int.class || field.getType() == Integer.class) {
                        int value = LPropertyLoader.getPropertyAsInt(propertyKey,
                                defaultValue.isEmpty() ? 0 : Integer.parseInt(defaultValue));
                        field.set(instance, value);
                    } else if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                        boolean value = LPropertyLoader.getPropertyAsBoolean(propertyKey,
                                !defaultValue.isEmpty() && Boolean.parseBoolean(defaultValue));
                        field.set(instance, value);
                    }
                }
            }
        }

        // STEP 4: NOW wrap them in Proxies if they need it
        for (Map.Entry<Class<?>, Object> entry : beanMap.entrySet()) {
            Class<?> clazz = entry.getKey();
            Object instance = entry.getValue();

            boolean needsProxy = false;
            for (Method m : clazz.getDeclaredMethods()) {
                if (m.isAnnotationPresent(LLoggable.class)) {
                    needsProxy = true;
                    break;
                }
            }

            if (needsProxy && clazz.getInterfaces().length > 0) {
                Object proxy = LProxyFactory.createProxy(instance);
                proxyToOriginal.put(proxy, instance); // Track mapping!
                beanMap.put(clazz, proxy); // Replace the real one with the Proxy
            }
        }
    }
    // NEW METHOD: Get the original class for reflection/annotations
    public Class<?> getOriginalClass(Object bean) {
        Object original = proxyToOriginal.getOrDefault(bean, bean);
        return original.getClass();
    }

    // NEW METHOD: Get the original instance
    public Object getOriginalInstance(Object bean) {
        return proxyToOriginal.getOrDefault(bean, bean);
    }
    private Object resolveDependency(Field field){
        Class<?> typeToInject = field.getType();
        List<Object> childs= new ArrayList<>();
        for(Object bean :beanMap.values()){
            if(typeToInject.isAssignableFrom(bean.getClass())){
                childs.add(bean);
            }
        }
        if (childs.size()==0){
            throw new RuntimeException("No bean found for "+typeToInject.getName());
        }
        else if (childs.size()==1){
            return childs.get(0);
        }
        Object primaryCandidate = null;
        for(Object bean: childs){
            Class<?> originalclass = this.getOriginalClass(bean);
            if (originalclass.isAnnotationPresent(LPrimary.class)){
                if(primaryCandidate!=null){
                    throw new RuntimeException("Multiple @Lprimary beans found for " +typeToInject.getName());
                }
                primaryCandidate=bean;
            }
        }
        if(primaryCandidate==null){
            throw new RuntimeException("Am bugity error put LPrimary on any of beans for "+ typeToInject.getName());
        }
        return primaryCandidate;
    }

    public <T> T getBean(Class<T> clazz){
        if(clazz.isInterface()) {
            for (Object bean : beanMap.values()) {
                if (clazz.isAssignableFrom(bean.getClass())){
                    return clazz.cast(bean);
                }
            }
        }
        return clazz.cast(beanMap.get(clazz));
    }
    public Collection<Object> getAllBeans(){
        return beanMap.values();
    }
}
