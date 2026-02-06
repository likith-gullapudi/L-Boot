// LStartupLogger.java
package org.likith.core;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LStartupLogger {

    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String BLUE = "\u001B[34m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";

    private static long startTime;

    public static void logStarting(Class<?> mainClass) {
        startTime = System.currentTimeMillis();
        String timestamp = getCurrentTimestamp();
        System.out.println(timestamp + " " + GREEN + "INFO" + RESET + " " +
                CYAN + getProcessId() + RESET + " --- [" +
                BLUE + "main" + RESET + "] " +
                YELLOW + mainClass.getSimpleName() + RESET +
                " : Starting " + mainClass.getSimpleName() + "...");
    }

    public static void logBeanCreation(String beanName, String beanType) {
        String timestamp = getCurrentTimestamp();
        System.out.println(timestamp + " " + GREEN + "INFO" + RESET + " " +
                CYAN + getProcessId() + RESET + " --- [" +
                BLUE + "main" + RESET + "] " +
                "o.l.core.LBootContext                : Created bean: " +
                CYAN + beanName + RESET + " [" + beanType + "]");
    }

    public static void logRouteMapping(String method, String path, String handler) {
        String timestamp = getCurrentTimestamp();
        System.out.println(timestamp + " " + GREEN + "INFO" + RESET + " " +
                CYAN + getProcessId() + RESET + " --- [" +
                BLUE + "main" + RESET + "] " +
                "o.l.core.LWebServer                  : Mapped \"" +
                YELLOW + method + " " + path + RESET + "\" onto " +
                CYAN + handler + RESET);
    }

    public static void logServerStarted(int port) {
        long duration = System.currentTimeMillis() - startTime;
        String timestamp = getCurrentTimestamp();
        System.out.println(timestamp + " " + GREEN + "INFO" + RESET + " " +
                CYAN + getProcessId() + RESET + " --- [" +
                BLUE + "main" + RESET + "] " +
                "o.l.core.LWebServer                  : " +
                "L-Boot started on port(s): " + CYAN + port + RESET);
        System.out.println(timestamp + " " + GREEN + "INFO" + RESET + " " +
                CYAN + getProcessId() + RESET + " --- [" +
                BLUE + "main" + RESET + "] " +
                GREEN + "Started in " + (duration / 1000.0) + " seconds" + RESET);
    }

    public static void logComponentScan(String basePackage, int count) {
        String timestamp = getCurrentTimestamp();
        System.out.println(timestamp + " " + GREEN + "INFO" + RESET + " " +
                CYAN + getProcessId() + RESET + " --- [" +
                BLUE + "main" + RESET + "] " +
                "o.l.scanner.LPackageScanner          : Scanning package: " +
                CYAN + basePackage + RESET);
        System.out.println(timestamp + " " + GREEN + "INFO" + RESET + " " +
                CYAN + getProcessId() + RESET + " --- [" +
                BLUE + "main" + RESET + "] " +
                "o.l.scanner.LPackageScanner          : Found " +
                CYAN + count + RESET + " components");
    }

    private static String getCurrentTimestamp() {
        return LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
        );
    }

    private static String getProcessId() {
        return String.valueOf(ProcessHandle.current().pid());
    }
    public static void logInfo(String message) {
        String timestamp = getCurrentTimestamp();
        System.out.println(timestamp + " " + GREEN + "INFO" + RESET + " " +
                CYAN + getProcessId() + RESET + " --- [" +
                BLUE + "main" + RESET + "] " +
                "o.l.core.LPropertyLoader           : " + message);
    }

    public static void logWarning(String message) {
        String timestamp = getCurrentTimestamp();
        System.out.println(timestamp + " " + YELLOW + "WARN" + RESET + " " +
                CYAN + getProcessId() + RESET + " --- [" +
                BLUE + "main" + RESET + "] " +
                "o.l.core.LPropertyLoader           : " + message);
    }

    public static void logError(String message) {
        String timestamp = getCurrentTimestamp();
        String RED = "\u001B[31m";
        System.out.println(timestamp + " " + RED + "ERROR" + RESET + " " +
                CYAN + getProcessId() + RESET + " --- [" +
                BLUE + "main" + RESET + "] " +
                "o.l.core.LPropertyLoader           : " + message);
    }
}