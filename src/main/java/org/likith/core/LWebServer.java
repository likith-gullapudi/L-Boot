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
import com.sun.net.httpserver.HttpExchange;

@LComponent
public class LWebServer {
    private LBootContext context;
    private final Map<String, RouteHandler> routeMap = new HashMap<>();
    @LAutowired
    private Gson converter;

    @LValue("server.port")
    private int port;

    private static class RouteHandler {
        Object bean;
        Method method;
        String httpMethod; // "GET", "POST", etc.
        String path;

        RouteHandler(Object bean, Method method, String httpMethod,String path) {
            this.bean = bean;
            this.method = method;
            this.httpMethod = httpMethod;
            this.path=path;

        }
    }

    public LWebServer() {
        this.context = null;  // We'll set this differently
    }

    // Or use setter injection for context
    public void setContext(LBootContext context) {
        this.context = context;
    }

    public void start() throws Exception {
        buildRouteMap();

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            String httpMethod = exchange.getRequestMethod(); // "GET", "POST", etc.
            String response = "";
            int statusCode = 404;

            try {
                String routeKey = httpMethod + ":" + path;
                RouteHandler handler = findHandler(httpMethod,path);

                if (handler != null) {
                    // Extract path variables
                    Map<String, String> pathVars = LParameterResolver.matchPath(
                            handler.path, path
                    );

                    // Resolve all parameters
                    Object[] args = LParameterResolver.resolveParameters(
                            handler.method, exchange, pathVars
                    );

                    // Invoke method
                    Object result = handler.method.invoke(handler.bean, args);

                    // Convert response to JSON if it's an object
                    if (result instanceof String) {
                        response = (String) result;
                    } else {
                        response =converter.toJson(result);
                    }

                    statusCode = 200;
                } else {
                    response = "404 Not Found: " + path;
                }

            } catch (InvocationTargetException e) {
                e.printStackTrace();
                statusCode = 500;
                response = "500 Internal Server Error: " + e.getCause().getMessage();
            } catch (Exception e) {
                e.printStackTrace();
                statusCode = 500;
                response = "500 Internal Server Error: " + e.getMessage();
            }

            exchange.sendResponseHeaders(statusCode, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });

        server.start();
        LStartupLogger.logServerStarted(port);
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
                        routeMap.put(routeKey, new RouteHandler(bean, beanMethod, "GET",mapping.value()));
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
}