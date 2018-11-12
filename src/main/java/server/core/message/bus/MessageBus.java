package server.core.message.bus;

import lombok.extern.slf4j.Slf4j;
import network.protocol.manager.ProtocolManager;
import server.core.service.factory.IInstanceFactory;
import server.core.util.ClassUtil;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Slf4j
public class MessageBus {
    private final HashMap<Integer, List<BusNode>> msgs = new HashMap<>();
    private final IInstanceFactory factory;

    public MessageBus(IInstanceFactory factory) {
        this.factory = factory;
    }

    public void init(String packageName) {
        Set<Class<?>> classes = ClassUtil.getClassesWithAnnotation(packageName, Register.class);
        for (Class<?> clz : classes) {
            Method[] methods = clz.getDeclaredMethods();
            for (Method m : methods) {
                Message msg = m.getAnnotation(Message.class);
                if (msg != null && msg.value() != null) {
                    Class<?> msgClass = msg.value();
                    Integer msgId = ProtocolManager.getId(msgClass);
                    if (msgId > 0) {
                        msgs.computeIfAbsent(msgId, k -> new LinkedList<>());
                        msgs.get(msgId).add(new BusNode(factory.getInstance(clz), m));
                    }
                }

                MessageId msgId = m.getAnnotation(MessageId.class);
                if (msgId != null && msgId.value() > 0) {
                    Class<?> msgClass = ProtocolManager.getClass(msgId.value());
                    if (msgClass != null) {
                        msgs.computeIfAbsent(msgId.value(), k -> new LinkedList<>());
                        msgs.get(msgId).add(new BusNode(factory.getInstance(clz), m));
                    }
                }
            }
        }
    }

    public <T> void post(T obj) {
        Class<?> clz = obj.getClass();
        Integer id = ProtocolManager.getId(clz);
        if (id <= 0 || !msgs.containsKey(id)) {
            log.error("invalid msg, {}, id {}", clz.getName(), id);
            return;
        }

        List<BusNode> list = msgs.get(id);
        for (BusNode node : list) {
            node.invoke(obj);
        }
    }

    private static class BusNode {
        private Object obj;
        private Method method;

        public BusNode(Object obj, Method method) {
            this.obj = obj;
            this.method = method;
            this.method.setAccessible(true);
        }

        void invoke(Object... args) {
            try {
                method.invoke(obj, args);
            } catch (Exception e) {
                log.error("", e);
            }
        }
    }
}
