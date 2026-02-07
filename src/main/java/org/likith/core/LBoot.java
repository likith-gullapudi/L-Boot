// ./src/main/java/org/likith/core/LBoot.java
package org.likith.core;

import org.likith.scanner.LPackageScanner;

import java.util.ArrayList;
import java.util.List;

public class LBoot {

    /**
     * Entry point to start L-Boot application
     * @param mainClass The main class of the application (used to detect base package)
     */
    public static void run(Class<?> mainClass) {
        try {
            // Print banner
            LBanner.print();

            // Log startup
            LStartupLogger.logStarting(mainClass);

            // Get base package from main class
            String appBasePackage = mainClass.getPackageName();
            System.out.println("[L-Boot] Application package: " + appBasePackage);

            // Create boot context
            LBootContext context = new LBootContext();

            // Scan BOTH framework package AND application package
            List<Class<?>> allClasses = new ArrayList<>();

            // 1. Scan framework's core package (for LWebServer, etc.)
            System.out.println("[L-Boot] Scanning framework package: org.likith");
            List<Class<?>> frameworkClasses = LPackageScanner.scan("org.likith");
            allClasses.addAll(frameworkClasses);

            // 2. Scan application package (only if different from framework)
            if (!appBasePackage.startsWith("org.likith")) {
                System.out.println("[L-Boot] Scanning application package: " + appBasePackage);
                List<Class<?>> appClasses = LPackageScanner.scan(appBasePackage);
                allClasses.addAll(appClasses);
            }

            // Initialize context with all scanned classes
            context.init(allClasses);

            // Get the web server bean
            LWebServer server = context.getBean(LWebServer.class);
            server.setContext(context);

            // Start the server
            server.start();

        } catch (Exception e) {
            System.err.println("[L-Boot] Failed to start application: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Entry point to start L-Boot application with custom port
     * @param mainClass The main class of the application
     * @param port Custom port number
     */
    public static void run(Class<?> mainClass, Integer port) {
        // For now, custom port is not implemented
        // You can add System.setProperty("server.port", port.toString()) if needed
        run(mainClass);
    }
}