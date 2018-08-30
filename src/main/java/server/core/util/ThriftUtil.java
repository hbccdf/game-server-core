package server.core.util;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TMemoryBuffer;
import org.apache.thrift.transport.TTransportException;

public class ThriftUtil {
    public static final ThriftProtocol DEFAULT_THRIFT_PROTOCOL = ThriftProtocol.Compact;

    public static <T extends TBase<?, ?>> byte[] marshal(ThriftProtocol protoType, T source) throws TException {
        TProtocol tp;
        TMemoryBuffer buf = new TMemoryBuffer(64);
        tp = getProto(protoType, buf);

        source.write(tp);
        byte[] ret = new byte[buf.length()];
        buf.read(ret, 0, ret.length);
        return ret;
    }

    public static <T extends TBase<?, ?>> T unmarshal(ThriftProtocol protoType, T template, byte[] data) throws TException {
        TMemoryBuffer buf = new TMemoryBuffer(32);
        buf.write(data);
        TProtocol tp = getProto(protoType, buf);

        template.read(tp);
        return template;
    }

    public static <T extends TBase<?, ?>> byte[] marshal(T source) throws TException {
        return marshal(DEFAULT_THRIFT_PROTOCOL, source);
    }

    public static <T extends TBase<?, ?>> T unmarshal(T template, byte[] data) throws TException {
        return unmarshal(DEFAULT_THRIFT_PROTOCOL, template, data);
    }

    private static TProtocol getProto(ThriftProtocol protoType, TMemoryBuffer buf) {
        TProtocol tp;
        if(protoType == ThriftProtocol.Binary){
            tp = new TBinaryProtocol(buf);
        }else if(protoType == ThriftProtocol.Json){
            tp = new TJSONProtocol(buf);
        }else{
            tp = new TCompactProtocol(buf);
        }
        return tp;
    }
}
