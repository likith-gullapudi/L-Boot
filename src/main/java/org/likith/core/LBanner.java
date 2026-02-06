// LBanner.java
package org.likith.core;

public class LBanner {

    private static final String BANNER = """
            
              _          ____              _   
             | |        |  _ \\            | |  
             | |  ______| |_) | ___   ___ | |_ 
             | | |______|  _ < / _ \\ / _ \\| __|
             | |____    | |_) | (_) | (_) | |_ 
             |______|   |____/ \\___/ \\___/ \\__|
                                                
            """;

    public static void print() {
        System.out.println(BANNER);
        System.out.println("  L-Boot Framework                    (v1.0.0)");
        System.out.println();
    }
}