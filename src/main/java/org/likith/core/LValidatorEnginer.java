package org.likith.core;

import org.likith.annotations.LComponent;
import org.likith.annotations.LNotNull;
import org.likith.annotations.LValidatable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class LValidatorEnginer {
    public static List<String> validate(Object bean){
        Class<?> clazz= bean.getClass();
        List<String> errors = new ArrayList<>();
        if (clazz.isAnnotationPresent(LValidatable.class)){
            for (Field field: clazz.getDeclaredFields()){
                if(field.isAnnotationPresent(LNotNull.class)){
                    LNotNull annotation= field.getAnnotation(LNotNull.class);
                    String errorMessage=annotation.value();
                    field.setAccessible(true);
                    try {
                        Object value= field.get(bean);
                        if (value==null){
                            errors.add(errorMessage);
                        }
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return errors;

    }
}
