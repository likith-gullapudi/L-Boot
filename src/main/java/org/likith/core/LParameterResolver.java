// LParameterResolver.java
package org.likith.core;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import org.likith.annotations.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
@LComponent
public class LParameterResolver {
    @LAutowired
    private static Gson converter;

    public static Object[] resolveParameters(
            Method method,
            HttpExchange exchange,
            Map<String, String> pathVariables
    ) throws IOException {

        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];

            // Handle @LRequestBody
            if (param.isAnnotationPresent(LRequestBody.class)) {
                String body = new String(exchange.getRequestBody().readAllBytes());
                args[i] = converter.fromJson(body, param.getType());
            }
            // Handle @LPathVariable
            else if (param.isAnnotationPresent(LPathVariable.class)) {
                LPathVariable annotation = param.getAnnotation(LPathVariable.class);
                String varName = annotation.value();
                args[i] = pathVariables.get(varName);
            }
            // Handle @LRequestParam
            else if (param.isAnnotationPresent(LRequestParam.class)) {
                LRequestParam annotation = param.getAnnotation(LRequestParam.class);
                String paramName = annotation.value();
                String query = exchange.getRequestURI().getQuery();
                args[i] = extractQueryParam(query, paramName);
            }
            // Handle raw String (backward compatibility)
            else if (param.getType() == String.class) {
                args[i] = new String(exchange.getRequestBody().readAllBytes());
            }
            else {
                args[i] = null;
            }
        }

        return args;
    }

    private static String extractQueryParam(String query, String paramName) {
        if (query == null) return null;

        for (String pair : query.split("&")) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2 && keyValue[0].equals(paramName)) {
                return keyValue[1];
            }
        }
        return null;
    }

    // Check if path matches pattern and extract variables
    public static Map<String, String> matchPath(String pattern, String actualPath) {
        String[] patternParts = pattern.split("/");
        String[] actualParts = actualPath.split("/");

        if (patternParts.length != actualParts.length) {
            return null; // No match
        }

        Map<String, String> variables = new HashMap<>();

        for (int i = 0; i < patternParts.length; i++) {
            String patternPart = patternParts[i];
            String actualPart = actualParts[i];

            if (patternPart.startsWith("{") && patternPart.endsWith("}")) {
                String varName = patternPart.substring(1, patternPart.length() - 1);
                variables.put(varName, actualPart);
            } else if (!patternPart.equals(actualPart)) {
                return null;
            }
        }

        return variables;
    }
}