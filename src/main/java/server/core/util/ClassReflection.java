package server.core.util;

import java.lang.reflect.Field;

public class ClassReflection {
    public static void set(Object source, String fieldName, Object value) {
        //inject
        for (Class<?> clz = source.getClass(); clz != Object.class; clz = clz.getSuperclass()) {
            try {
                Field field = clz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(source, value);
            } catch (Exception e) {
                //do nothing
            }
        }
    }

    public static Object getFieldValue(Field field, Object obj) throws IllegalAccessException {
        field.setAccessible(true);
        return field.get(obj);
    }
}
