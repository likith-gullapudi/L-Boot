package org.likith;

import org.likith.core.LBanner;
import org.likith.core.LStartupLogger;
import org.likith.core.LWebServer;
import org.likith.demo.TestController;
import org.likith.scanner.LPackageScanner;
import org.likith.core.LBootContext;
import org.likith.demo.TestControllerImpl;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class Main {
    public static void main(String[] args) throws Exception {
        LBanner.print();
        LStartupLogger.logStarting(Main.class);

        LBootContext context =new LBootContext();
        List<Class<?>> myClasses= LPackageScanner.scan("org.likith");
        context.init(myClasses);
        // start our server
        LWebServer server = context.getBean(LWebServer.class);
        server.setContext(context);

        server.start();

        // ADD THIS: Shutdown hook for graceful cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LStartupLogger.logInfo("Shutting down server...");
            server.shutdown();
        }));
    }
}