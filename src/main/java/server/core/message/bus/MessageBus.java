package server.core.message.bus;

import lombok.extern.slf4j.Slf4j;
import network.protocol.manager.ProtocolManager;
import server.core.service.factory.IInstanceFactory;
import server.core.util.ClassUtil;

import java.lang.reflect.Method;
import java.util.*;

@Slf4j
public class MessageBus {
    private final HashMap<Integer, List<BusNode>> msgs = new HashMap<>();
    private final IInstanceFactory factory;

    public MessageBus(IInstanceFactory factory) {
        this.factory = factory;
    }

    public void init(String packageName) {
        Set<Class<?>> classes = ClassUtil.getClassesWithAnnotation(packageName, Register.class);
        classes.forEach(clz -> {
            List<Method> methods = ClassUtil.getMethodsWithAnyAnnotations(clz, false, Message.class, MessageId.class);
            methods.forEach(m -> reg(m, clz));

        });
    }

    public <T> void post(T obj) {
        Class<?> clz = obj.getClass();
        Integer id = ProtocolManager.getId(clz);
        if (id <= 0) {
            log.error("invalid msg, {}, id {}", clz.getName(), id);
            return;
        }

        if (!msgs.containsKey(id)) {
            log.debug("no handler for msg {}", id);
            return;
        }

       msgs.get(id).forEach(b -> b.invoke(obj));
    }

    private void reg(Method m, Class<?> clz) {
        Message msg = m.getAnnotation(Message.class);
        if (msg != null) {
            reg(ProtocolManager.getId(msg.value()), clz, m);
        }

        MessageId msgId = m.getAnnotation(MessageId.class);
        if (msgId != null && ProtocolManager.getClass(msgId.value()) != null) {
            reg(msgId.value(), clz, m);
        }
    }

    private void reg(Integer msgId, Class<?> clz, Method method) {
        if (msgId > 0 && clz != null) {
            msgs.computeIfAbsent(msgId, k -> new LinkedList<>());
            msgs.get(msgId).add(new BusNode(factory.getInstance(clz), method));
        }
    }

    private static class BusNode {
        private final Object obj;
        private final Method method;

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
