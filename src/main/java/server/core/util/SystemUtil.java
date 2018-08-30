package server.core.util;

import org.apache.thrift.EncodingUtils;

public class SystemUtil {
    public static byte[] getBytes(int x) {
        byte[] result = new byte[4];
        EncodingUtils.encodeBigEndian(x, result);
        return result;
    }
}
