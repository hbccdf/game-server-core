package server.core.configuration;

import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

public class ConfigManager {
    private static final JavaPropsMapper mapper = new JavaPropsMapper();

    public static <T> T read(Class<T> clz, String configFile) {
        Properties props = getProperties(configFile);
        if (props == null) {
            return null;
        }
        try {
            T obj = mapper.readPropertiesAs(props, clz);
            return obj;
        } catch (IOException e) {
            //todo logger
        }
        return null;
    }

    public static Properties getProperties(String path) {
        Configurations configs = new Configurations();
        try {
            Properties props = new Properties();
            PropertiesConfiguration p = configs.properties(path);
            Iterator<String> it = p.getKeys();
            while (it.hasNext()) {
                String key = it.next();
                props.setProperty(key, p.getString(key));
            }
            return props;
        } catch (ConfigurationException e) {
            //todo logger
        }
        return null;
    }
}
