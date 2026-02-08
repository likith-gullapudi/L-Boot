package org.likith.core;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import org.likith.annotations.*;
import org.likith.core.LBootContext;

import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import com.sun.net.httpserver.HttpExchange;

@LComponent
public class LWebServer {
    private LBootContext context;
    private final Map<String, RouteHandler> routeMap = new HashMap<>();
    private ExecutorService threadPool;

    @LAutowired
    private Gson converter;

    @LValue("server.port")
    private int port;

    @LValue("server.threadpool.size")
    private int threadPoolSize = 10; // Default 10 threads

    private LExceptionHandlerManager exceptionHandlerManager;

    private static class RouteHandler {
        Object bean;
        Method method;
        String httpMethod;
        String path;

        RouteHandler(Object bean, Method method, String httpMethod, String path) {
            this.bean = bean;
            this.method = method;
            this.httpMethod = httpMethod;
            this.path = path;
        }
    }

    public LWebServer() {
        this.context = null;
    }

    public void setContext(LBootContext context) {
        this.context = context;
        this.exceptionHandlerManager=new LExceptionHandlerManager(context);
    }

    public void start() throws Exception {
        buildRouteMap();

        threadPool = Executors.newFixedThreadPool(threadPoolSize);

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.setExecutor(threadPool);

        server.createContext("/", exchange -> {
            // Log thread information for this request
            String threadName = Thread.currentThread().getName();
            long threadId = Thread.currentThread().getId();

            String path = exchange.getRequestURI().getPath();
            String httpMethod = exchange.getRequestMethod();

            // Informative log statement
            LStartupLogger.logInfo(String.format(
                    "[Thread: %s (ID: %d)] Processing %s request: %s",
                    threadName, threadId, httpMethod, path
            ));

            String response = "";
            int statusCode = 404;

            try {
                RouteHandler handler = findHandler(httpMethod, path);

                if (handler != null) {
                    Map<String, String> pathVars = LParameterResolver.matchPath(
                            handler.path, path
                    );

                    Object[] args = LParameterResolver.resolveParameters(
                            handler.method, exchange, pathVars
                    );

                    Object result = handler.method.invoke(handler.bean, args);

                    if (result instanceof String) {
                        response = (String) result;
                    } else {
                        response = converter.toJson(result);
                    }

                    statusCode = 200;
                } else {
                    response = "404 Not Found: " + path;
                }

            } catch (InvocationTargetException e) {
                // The actual exception thrown by the controller method
                Throwable cause = e.getCause();

                if (cause instanceof Exception) {
                    Exception actualException = (Exception) cause;

                    // Try to handle using exception handler
                    if (exceptionHandlerManager.hasHandlerFor(actualException)) {
                        Object handlerResult = exceptionHandlerManager.handleException(actualException);

                        if (handlerResult != null) {
                            if (handlerResult instanceof String) {
                                response = (String) handlerResult;
                            } else {
                                response = converter.toJson(handlerResult);
                            }

                            // Get status code from exception (you can enhance this)
                            statusCode = getStatusCodeFromException(actualException);
                        } else {
                            // Handler returned null
                            statusCode = 500;
                            response = "500 Internal Server Error";
                        }
                    } else {
                        // No handler found, use default error response
                        e.printStackTrace();
                        statusCode = 500;
                        response = "500 Internal Server Error: " + cause.getMessage();
                    }
                } else {
                    e.printStackTrace();
                    statusCode = 500;
                    response = "500 Internal Server Error";
                }

            } catch (Exception e) {
                // Try to handle using exception handler
                if (exceptionHandlerManager.hasHandlerFor(e)) {
                    Object handlerResult = exceptionHandlerManager.handleException(e);

                    if (handlerResult != null) {
                        if (handlerResult instanceof String) {
                            response = (String) handlerResult;
                        } else {
                            response = converter.toJson(handlerResult);
                        }

                        statusCode = getStatusCodeFromException(e);
                    } else {
                        statusCode = 500;
                        response = "500 Internal Server Error";
                    }
                } else {
                    // No handler, use default
                    e.printStackTrace();
                    statusCode = 500;
                    response = "500 Internal Server Error: " + e.getMessage();
                }
            }

            exchange.sendResponseHeaders(statusCode, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });

        server.start();
        LStartupLogger.logServerStarted(port);
        LStartupLogger.logInfo("ThreadPool initialized with " + threadPoolSize + " threads");
    }

    private void buildRouteMap() throws NoSuchMethodException {
        for (Object bean : context.getAllBeans()) {
            Class<?> originalClass = context.getOriginalClass(bean);

            if (originalClass.isAnnotationPresent(Lcontroller.class)) {
                for (Method originalMethod : originalClass.getMethods()) {
                    if (originalMethod.isAnnotationPresent(LGetMapping.class)) {
                        LGetMapping mapping = originalMethod.getAnnotation(LGetMapping.class);

                        // Get the actual method from bean's class
                        Method beanMethod = bean.getClass().getMethod(
                                originalMethod.getName(),
                                originalMethod.getParameterTypes()
                        );

                        String routeKey = "GET:" + mapping.value();
                        routeMap.put(routeKey, new RouteHandler(bean, beanMethod, "GET", mapping.value()));
                        String handler = originalClass.getSimpleName() + "." + beanMethod.getName() + "()";
                        LStartupLogger.logRouteMapping("GET", mapping.value(), handler);
                    }
                    if (originalMethod.isAnnotationPresent(LPostMapping.class)) {
                        LPostMapping mapping = originalMethod.getAnnotation(LPostMapping.class);

                        // Get the actual method from bean's class
                        Method beanMethod = bean.getClass().getMethod(
                                originalMethod.getName(),
                                originalMethod.getParameterTypes()
                        );

                        String routeKey = "POST:" + mapping.value();
                        routeMap.put(routeKey, new RouteHandler(bean, beanMethod, "POST",mapping.value()));
                        String handler = originalClass.getSimpleName() + "." + beanMethod.getName() + "()";
                        LStartupLogger.logRouteMapping("POST", mapping.value(), handler);
                    }



                }
            }
        }
    }
    private RouteHandler findHandler(String httpMethod, String path) {
        // First try exact match
        String routeKey = httpMethod + ":" + path;
        if (routeMap.containsKey(routeKey)) {
            return routeMap.get(routeKey);
        }

        // Then try pattern matching
        for (RouteHandler handler : routeMap.values()) {
            if (handler.httpMethod.equals(httpMethod)) {
                Map<String, String> match = LParameterResolver.matchPath(
                        handler.path, path
                );
                if (match != null) {
                    return handler;
                }
            }
        }

        return null;
    }
    /**
     * Get HTTP status code based on exception type
     * Determines status code from exception class name patterns
     */
    private int getStatusCodeFromException(Exception exception) {
        String exceptionName = exception.getClass().getSimpleName();

        // Common patterns for HTTP status codes
        if (exceptionName.contains("NotFound")) {
            return 404;
        } else if (exceptionName.contains("AlreadyExists") || exceptionName.contains("Conflict")) {
            return 409;
        } else if (exceptionName.contains("BadRequest") || exceptionName.contains("Invalid")) {
            return 400;
        } else if (exceptionName.contains("Unauthorized")) {
            return 401;
        } else if (exceptionName.contains("Forbidden")) {
            return 403;
        }

        return 500; // Default to internal server error
    }








    // Graceful shutdown method
    public void shutdown() {
        if (threadPool != null) {
            threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
            }
        }
    }
}