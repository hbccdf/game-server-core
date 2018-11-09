package server.core.module;

import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.core.configuration.ConfigManager;
import server.core.service.factory.IInstanceFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class ModuleManager implements Iterable<IModule> {
    private static final Logger log = LoggerFactory.getLogger(ModuleManager.class);
    public static final ModuleManager INSTANCE = new ModuleManager();
    private static final String CONF_NODE = "modules.module";

    private HashMap<Class<?>, IModule> modules = new HashMap<>();

    private ModuleManager(){
    }

    public boolean initialize(String config) {
        Configurations configs = new Configurations();
        try {
            FileBasedConfigurationBuilder<XMLConfiguration> builder = configs.xmlBuilder(config);
            XMLConfiguration xml = builder.getConfiguration();
            List<String> modules = xml.getList(String.class, CONF_NODE);
            for (String m : modules) {
                if (!loadForName(m)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            log.error("fail to load modules. ", e);
        }
        return false;
    }

    public boolean init(String... modules) {
        try {
            for (String m : modules) {
                if (!loadForName(m)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            log.error("fail to load modules. ", e);
        }
        return false;
    }

    public boolean reload(boolean reloadConfig) {
        if (reloadConfig && !ConfigManager.reload()) {
            return false;
        }

        for (IModule m : modules.values()) {
            m.reload();
        }
        return true;
    }

    public void update(long now) {
        for (IModule m : modules.values()) {
            m.update(now);
        }
    }

    public void release() {
        for (IModule m : modules.values()) {
            m.release();
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends IModule> T getModule(Class<T> clz) {
        return (T) modules.get(clz);
    }

    public IInstanceFactory getInstanceFactory() {
        return iterator().next().getInstanceFactory();
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
