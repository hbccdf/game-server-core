package server.core.util;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

@Slf4j
public class ClassReflection {
    public static void set(Object source, String fieldName, Object value) {
        for (Class<?> clz = source.getClass(); clz != Object.class; clz = clz.getSuperclass()) {
            try {
                Field field = clz.getDeclaredField(fieldName);
                if (field != null) {
                    field.setAccessible(true);
                    field.set(source, value);
                }
            } catch (Exception e) {
                log.error("", e);
            }
        }
    }

    public static Object getFieldValue(Field field, Object obj) throws IllegalAccessException {
        field.setAccessible(true);
        return field.get(obj);
    }
}
