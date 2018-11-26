package server.core.service.zk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Charsets;
import lombok.Data;
import server.core.util.JsonUtil;

import java.io.IOException;

@Data
public class EndPoint {
    private int id;
    private String ip;
    private int port;
    private long timestamp;

    public byte[] encode() throws JsonProcessingException {
        return JsonUtil.toString(this).getBytes(Charsets.UTF_8);
    }

    public static EndPoint decode(byte[] data) throws IOException {
        return JsonUtil.toObject(EndPoint.class, new String(data, Charsets.UTF_8));
    }
}
