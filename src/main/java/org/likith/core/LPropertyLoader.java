package org.likith.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class LPropertyLoader {

    private static Map<String, String> propMap = new HashMap<>();
    private static boolean loaded = false;

    /**
     * Load properties from application.properties file
     * @return Map of property key-value pairs
     */
    public static Map<String, String> loadProperties() {
        if (loaded) {
            return propMap;
        }

        Properties properties = new Properties();

        try (InputStream input = LPropertyLoader.class
                .getClassLoader()
                .getResourceAsStream("application.properties")) {

            if (input == null) {
                LStartupLogger.logWarning("application.properties not found in classpath");
                loaded = true;
                return propMap;
            }

            // Load properties file
            properties.load(input);

            // Convert Properties to Map
            for (String key : properties.stringPropertyNames()) {
                String value = properties.getProperty(key);
                propMap.put(key, value);
            }

            LStartupLogger.logInfo("Loaded " + propMap.size() + " properties from application.properties");
            loaded = true;

        } catch (IOException e) {
            LStartupLogger.logError("Failed to load application.properties: " + e.getMessage());
            e.printStackTrace();
        }

        return propMap;
    }

    /**
     * Get property value by key
     * @param key Property key
     * @return Property value or null if not found
     */
    public static String getProperty(String key) {
        if (!loaded) {
            loadProperties();
        }
        return propMap.get(key);
    }

    /**
     * Get property value with default fallback
     * @param key Property key
     * @param defaultValue Default value if property not found
     * @return Property value or default value
     */
    public static String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Get property as integer
     * @param key Property key
     * @param defaultValue Default value if property not found or invalid
     * @return Property value as integer
     */
    public static int getPropertyAsInt(String key, int defaultValue) {
        String value = getProperty(key);
        if (value == null) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            LStartupLogger.logWarning("Invalid integer value for property '" + key + "': " + value);
            return defaultValue;
        }
    }

    /**
     * Get property as boolean
     * @param key Property key
     * @param defaultValue Default value if property not found
     * @return Property value as boolean
     */
    public static boolean getPropertyAsBoolean(String key, boolean defaultValue) {
        String value = getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    /**
     * Check if property exists
     * @param key Property key
     * @return true if property exists
     */
    public static boolean hasProperty(String key) {
        if (!loaded) {
            loadProperties();
        }
        return propMap.containsKey(key);
    }

    /**
     * Get all properties
     * @return Unmodifiable map of all properties
     */
    public static Map<String, String> getAllProperties() {
        if (!loaded) {
            loadProperties();
        }
        return new HashMap<>(propMap);
    }
}