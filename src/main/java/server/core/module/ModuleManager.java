package server.core.module;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import server.core.configuration.ConfigManager;
import server.core.service.factory.IInstanceFactory;
import server.core.util.Unthrow;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class ModuleManager implements Iterable<IModule> {
    public static final ModuleManager INSTANCE = new ModuleManager();
    private static final String CONF_NODE = "modules.module";

    private final HashMap<Class<?>, IModule> modules = new HashMap<>();

    private ModuleManager(){
    }

    public boolean initialize(String config) {
        Configurations configs = new Configurations();
        try {
            FileBasedConfigurationBuilder<XMLConfiguration> builder = configs.xmlBuilder(config);
            XMLConfiguration xml = builder.getConfiguration();
            List<String> modules = xml.getList(String.class, CONF_NODE);

            return modules.stream().allMatch(m -> Unthrow.wrap(() -> loadForName(m)));
        } catch (Exception e) {
            log.error("fail to load modules. ", e);
        }
        return false;
    }

    public boolean init(String... modules) {
        try {
            return Arrays.stream(modules).allMatch(m -> Unthrow.wrap(() -> loadForName(m)));
        } catch (Exception e) {
            log.error("fail to load modules. ", e);
        }
        return false;
    }

    public boolean reload(boolean reloadConfig) {
        if (reloadConfig && !ConfigManager.reload()) {
            return false;
        }

        modules.values().forEach(IModule::reload);
        return true;
    }

    public void update(long now) {
        modules.values().forEach(m -> m.update(now));
    }

    public void release() {
        modules.values().forEach(IModule::release);
    }

    @SuppressWarnings("unchecked")
    public <T extends IModule> T getModule(Class<T> clz) {
        return (T) modules.get(clz);
    }

    public IInstanceFactory getInstanceFactory() {
        return iterator().next().getFactory();
    }

    @Override
    public Iterator<IModule> iterator() {
        return modules.values().iterator();
    }

    private  <T extends IModule> boolean load(Class<T> clz) throws InstantiationException, IllegalAccessException {
        if(!modules.containsKey(clz)){
            T module = clz.newInstance();
            modules.put(clz, module);
            return module.initialize();
        }
        return true;
    }

    private boolean loadForName(String className) throws ClassNotFoundException, IllegalAccessException, InstantiationException, ConfigurationException {
        Class<?> clz = Class.forName(className);
        if(IModule.class.isAssignableFrom(clz)){
            return load(clz.asSubclass(IModule.class));
        }else{
            throw new ConfigurationException("class must implements IModule. class= " + className);
        }
    }
}
