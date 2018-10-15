package server.core.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonTool {
    private ObjectMapper mapper = new ObjectMapper();

    public String toString(Object obj) throws JsonProcessingException {
        return mapper.writeValueAsString(obj);
    }

    public <T> T toObject(Class<T> clz, String jsonStr) throws IOException {
        return mapper.readValue(jsonStr, clz);
    }
}
