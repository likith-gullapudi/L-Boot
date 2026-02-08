package org.likith.core;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class LExceptionHandlerManager {
    private Map<Class<? extends Exception>, Method> exceptionHandlerMap= new HashMap<>();
    private LBootContext context;
    public LExceptionHandlerManager(LBootContext context) {
        this.context = context;
        buildExceptionHandlerMap();
    }
    private void buildExceptionHandlerMap() {
        for (Object bean : context.getAllBeans()) {
            Class<?> clazz = context.getOriginalClass(bean);

            if (clazz.isAnnotationPresent(org.likith.annotations.LControllerAdvice.class)) {
                LStartupLogger.logInfo("Found exception handler: " + clazz.getSimpleName());
                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(org.likith.annotations.LExceptionHandler.class)) {
                        LStartupLogger.logInfo("Found exception handler: " + method.getName());
                        Class<? extends Exception> exceptionType = method.getAnnotation(org.likith.annotations.LExceptionHandler.class).value();
                        exceptionHandlerMap.put(exceptionType, method);
                        LStartupLogger.logInfo(String.format(
                                "  Registered handler for %s -> %s.%s()",
                                exceptionType.getSimpleName(),
                                clazz.getSimpleName(),
                                method.getName()
                        ));
                    }
                }
            }
        }
    }

    /**
     * Handle an exception by finding appropriate handler
     * @param exception The exception to handle
     * @return Response object from handler, or null if no handler found
     */
    public Object handleException(Exception exception) {
        Class<? extends Exception> exceptionClass = exception.getClass();
        Method handlerMethod = exceptionHandlerMap.get(exceptionClass);
        if (handlerMethod == null) {
            // Try to find a handler for a superclass of the exception
            handlerMethod=findHandlerForSuperclass(exceptionClass);
        }
        if (handlerMethod != null) {
            try {
                Object handlerBean = context.getBean(handlerMethod.getDeclaringClass());
                return handlerMethod.invoke(handlerBean, exception);
            } catch (Exception e) {
                LStartupLogger.logError("Error invoking exception handler: " + e.getMessage());
                return null;
            }
        } else {
            LStartupLogger.logInfo("No handler found for exception: " + exceptionClass.getSimpleName());
            return null;
        }
    }
    /**
     * Find handler for exception's superclass
     */
    private Method findHandlerForSuperclass(Class<?> exceptionClass) {
        for(Class<?> currentClass = exceptionClass.getSuperclass();
            currentClass != null && Exception.class.isAssignableFrom(currentClass);
            currentClass = currentClass.getSuperclass()) {

            Method handler = (Method)this.exceptionHandlerMap.get(currentClass);
            if (handler != null) {
                return handler;
            }
        }
        return null;
    }
    /**
     * Check if there's a handler for this exception
     */
    public boolean hasHandlerFor(Exception exception) {
        Class<?> exceptionClass = exception.getClass();

        if (exceptionHandlerMap.containsKey(exceptionClass)) {
            return true;
        }

        return findHandlerForSuperclass(exceptionClass) != null;
    }


}
