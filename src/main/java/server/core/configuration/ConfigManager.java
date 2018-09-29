package server.core.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

public class ConfigManager {
    private static final JavaPropsMapper mapper = new JavaPropsMapper();

    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);

    private static final String configPath = "config.properties";

    private static String configProfile = "dev";

    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);

        Properties props = getProperties(configPath);
        configProfile = props.getProperty("profile", configProfile);
    }

    public static <T> T readProfile(Class<T> clz) {
        return readConfigProfile(clz, configPath);
    }

    public static <T> T readProfile(Class<T> clz, String rootKey) {
        return readConfigProfile(clz, configPath, rootKey);
    }

    public static <T> T readConfigProfile(Class<T> clz, String configFile) {
        //not root key config, need to read global key first
        T obj = read(clz, configFile);
        if (obj == null) {
            obj = read(clz, configFile, configProfile);
        }
        return obj;
    }

    public static <T> T readConfigProfile(Class<T> clz, String configFile, String rootKey) {
        //have root key config, need to read profile key first
        T obj = read(clz, configFile, configProfile + "." + rootKey);
        if (obj == null) {
            obj = read(clz, configFile, rootKey);
        }
        return obj;
    }

    public static <T> T read(Class<T> clz, String configFile) {
        Properties props = getProperties(configFile);
        if (props == null) {
            return null;
        }
        try {
            T obj = mapper.readPropertiesAs(props, clz);
            return obj;
        } catch (IOException e) {
            logger.error("read class config failed, class={}, configFile={}", clz.getName(), configFile, e);
        }
        return null;
    }

    public static <T> T read(Class<T> clz, String configFile, String rootKey) {
        Properties props = getProperties(configFile, rootKey);
        if (props == null || props.isEmpty()) {
            return null;
        }
        try {
            T obj = mapper.readPropertiesAs(props, clz);
            return obj;
        } catch (IOException e) {
            logger.error("read class config failed, class={}, configFile={}, rootKey={}", clz.getName(), configFile, rootKey, e);
        }
        return null;
    }

    public static Configuration properties(String path) {
        Configurations configs = new Configurations();
        try {
            return configs.properties(path);
        } catch (ConfigurationException e) {
            logger.error("fail to load config file, path={}", path, e);
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
            logger.error("config error", e);
        }
        return null;
    }

    public static Properties getProperties(String path, String rootKey) {
        Configurations configs = new Configurations();
        try {
            Properties props = new Properties();
            PropertiesConfiguration p = configs.properties(path);
            Iterator<String> it = p.getKeys(rootKey);
            while (it.hasNext()) {
                String key = it.next();
                String subKey = key.substring(rootKey.length() + 1);
                props.setProperty(subKey, p.getString(key));
            }
            return props;
        } catch (ConfigurationException e) {
            logger.error("config error", e);
        }
        return null;
    }
}
