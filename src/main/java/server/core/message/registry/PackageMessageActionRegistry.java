package server.core.message.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.core.message.action.IMessageAction;
import server.core.service.factory.IInstanceFactory;
import server.core.util.ClassUtil;

import java.util.Set;

public class PackageMessageActionRegistry<K> extends AbstractMessageActionRegistry<K> {
    private static final Logger logger = LoggerFactory.getLogger(PackageMessageActionRegistry.class);

    private IInstanceFactory factory;

    public PackageMessageActionRegistry(String pn, IInstanceFactory factory) {
        super();

        this.factory = factory;

        Set<Class<?>> classes = ClassUtil.getAllSubclasses(pn, IMessageAction.class);
        for (Class<?> clz : classes) {
            if (clz.isAnnotationPresent(Deprecated.class)) {
                continue;
            }
            reg(clz.asSubclass(IMessageAction.class));
        }
    }

    @SuppressWarnings("unchecked")
    private  <T extends IMessageAction> boolean reg(Class<T> clz) {
        try {
            T handler = factory.getInstance(clz);

            K command = (K) handler.getId();
            if (contain(command)) {
                throw new IllegalStateException("message action already registered, clz=" + clz);
            }
            add(command, handler);

            logger.debug("register message action: command= {}, class={}", command, handler.getClass().getName());
            return true;
        } catch (Exception e) {
            logger.error("fail to register message action, clz = {}", clz, e);
        }
        return false;
    }
}
