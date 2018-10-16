package server.core.module;

import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class ModuleManager implements Iterable<IModule> {
    private static final Log log = LogFactory.getLog(ModuleManager.class);
    public static final ModuleManager INSTANCE = new ModuleManager();
    private static final String CONF_NODE = "modules.module";

    private HashMap<Class<?>, IModule> modules = new HashMap<>();

    private ModuleManager(){

    }
    public boolean initialize(String config){
        Configurations configs = new Configurations();
        try {
            FileBasedConfigurationBuilder<XMLConfiguration> builder = configs.xmlBuilder(config);
            XMLConfiguration xml = builder.getConfiguration();
            List<String> modules = xml.getList(String.class, CONF_NODE);
            for (String m : modules) {
                Class<?> clz = Class.forName(m);
                if(IModule.class.isAssignableFrom(clz)){
                    if (!load((Class<IModule>) clz)) {
                        return false;
                    }
                }else{
                    throw new ConfigurationException("class must implements IModule. class= " + clz);
                }
            }
            return true;
        } catch (Exception e) {
            log.error("fail to load modules. ", e);
        }
        return false;
    }

    public <T extends IModule> boolean load(Class<T> clz) throws InstantiationException, IllegalAccessException {
        if(!modules.containsKey(clz)){
            T module = clz.newInstance();
            modules.put(clz, module);
            if (!module.initialize()) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public <T extends IModule> T getModule(Class<T> clz) {
        return (T) modules.get(clz);
    }

    public void update(long now) {
        Iterator<IModule> it = modules.values().iterator();
        while(it.hasNext()){
            IModule m = it.next();
            if(m != null){
                m.update(now);
            }
        }
    }

    public void release() {
        Iterator<IModule> it = modules.values().iterator();
        while(it.hasNext()){
            IModule m = it.next();
            if(m != null){
                m.release();
            }
        }
    }
    @Override
    public Iterator<IModule> iterator() {
        return modules.values().iterator();
    }
}
