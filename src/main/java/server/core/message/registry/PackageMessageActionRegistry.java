package server.core.message.registry;

import lombok.extern.slf4j.Slf4j;
import server.core.message.action.IMessageAction;
import server.core.service.factory.IInstanceFactory;
import server.core.util.ClassUtil;

import java.util.Set;

@Slf4j
public class PackageMessageActionRegistry<K> extends AbstractMessageActionRegistry<K> {
    private IInstanceFactory factory;

    public PackageMessageActionRegistry(String pn, IInstanceFactory factory) {
        super();

        this.factory = factory;

        Set<Class<?>> classes = ClassUtil.getAllSubclasses(pn, IMessageAction.class);
        classes.stream().filter(clz -> !clz.isAnnotationPresent(Deprecated.class))
                .forEach(clz -> reg(clz.asSubclass(IMessageAction.class)));
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

            log.debug("register message action: command= {}, class={}", command, handler.getClass().getName());
            return true;
        } catch (Exception e) {
            log.error("fail to register message action, clz = {}", clz, e);
        }
        return false;
    }
}
