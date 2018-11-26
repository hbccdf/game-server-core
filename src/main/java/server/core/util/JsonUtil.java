package server.core.util;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;

public class JsonUtil {
    public static String toString(Object obj) throws JsonProcessingException {
        return new JsonTool().toString(obj);
    }

    public static <T> T toObject(Class<T> clz, String jsonStr) throws IOException {
        return new JsonTool().toObject(clz, jsonStr);
    }
}
