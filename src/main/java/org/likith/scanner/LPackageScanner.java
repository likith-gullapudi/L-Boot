package org.likith.scanner;

import org.likith.annotations.LComponent;
import org.likith.annotations.LConfiguration;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LPackageScanner {
    public static List<Class<?>> scan(String packageName) throws ClassNotFoundException {
        List<Class<?>> classes=new ArrayList<>();
        String path =packageName.replace('.','/');
        ClassLoader classLoader =Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource(path);
        File directory = new File(resource.getFile());
        fillClasses(directory,packageName,classes);
        return classes;
    }
    private static void fillClasses(File directory,String packageName, List<Class<?>> classes) throws ClassNotFoundException {
        for(File file: Objects.requireNonNull(directory.listFiles())) {
            if (file.isDirectory()) {
                fillClasses(file,packageName+"."+file.getName(),classes);
            }
            else if (file.getName().endsWith(".class")){
                String className=packageName+'.'+file.getName().replace(".class","");
                Class<?> clazz =Class.forName(className);
                if (clazz.isAnnotationPresent(LComponent.class) || clazz.isAnnotationPresent(LConfiguration.class)){
                    classes.add(clazz);
                }
            }
        }
    }
}
