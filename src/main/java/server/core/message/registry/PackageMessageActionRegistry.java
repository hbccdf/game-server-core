package server.core.message.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.core.message.action.AbstractGenericMessageAction;
import server.core.message.action.IMessageAction;
import server.core.module.IServiceHolder;
import server.core.service.factory.IInstaceFactory;
import server.core.util.ClassReflection;
import server.core.util.ClassUtil;

import java.lang.reflect.Modifier;
import java.util.Set;

public class PackageMessageActionRegistry<K> extends AbstractMessageActionRegistry<K> {
    private static final Logger logger = LoggerFactory.getLogger(PackageMessageActionRegistry.class);
    private IInstaceFactory factory;

    public PackageMessageActionRegistry(IServiceHolder holder, String pn, IInstaceFactory instaceFactory) {
        super(holder);
        this.factory = instaceFactory;

        Set<Class<?>> classes = ClassUtil.getClasses(pn);
        for (Class<?> clz : classes) {
            if (IMessageAction.class.isAssignableFrom(clz)) {
                if (clz.isInterface()) {
                    continue;
                }
                if (Modifier.isAbstract(clz.getModifiers())) {
                    continue;
                }
                if (clz.isAnnotationPresent(Deprecated.class)) {
                    continue;
                }
                Class<IMessageAction> cma = (Class<IMessageAction>) clz;
                reg(cma);
            }
        }
    }

    public <T extends IMessageAction> boolean reg(Class<T> clz) {
        try {
            T handler = factory.newInstace(clz);
            ClassReflection.set(handler, AbstractGenericMessageAction.FIELD_NAME_HOLDER, this.holder);

            K command = (K) handler.getId();
            if (contain(command)) {
                throw new IllegalStateException("message action already registed, clz=" + clz);
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
