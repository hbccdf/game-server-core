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
        return internalRead(clz, configPath, null, configProfile);
    }

    public static <T> T readProfile(Class<T> clz, String rootKey) {
        return internalRead(clz, configPath, rootKey, configProfile);
    }

    public static <T> T readConfigProfile(Class<T> clz, String configFile) {
        return internalRead(clz, configFile, null, configProfile);
    }

    public static <T> T readConfigProfile(Class<T> clz, String configFile, String rootKey) {
        return internalRead(clz, configFile, rootKey, configProfile);
    }

    public static <T> T read(Class<T> clz, String configFile) {
        return internalRead(clz, configFile, null, null);
    }

    public static <T> T read(Class<T> clz, String configFile, String rootKey) {
        return internalRead(clz, configFile, rootKey, null);
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
        return internalGetProperties(path, null, null);
    }

    public static Properties getProperties(String path, String rootKey) {
        return internalGetProperties(path, rootKey, null);
    }

    private static <T> T internalRead(Class<T> clz, String configFile, String rootKey, String profile) {
        Properties props = internalGetProperties(configFile, rootKey, profile);
        if (props == null || props.isEmpty()) {
            return null;
        }
        try {
            T obj = mapper.readPropertiesAs(props, clz);
            return obj;
        } catch (IOException e) {
            logger.error("read class config failed, class={}, configFile={}, rootKey={}, profile={}", clz.getName(), configFile, rootKey, profile, e);
        }
        return null;
    }

    private static Properties internalGetProperties(String path, String rootKey, String profile) {
        Configurations configs = new Configurations();
        try {
            Properties props = new Properties();
            PropertiesConfiguration p = configs.properties(path);
            extractProperties(p, props, rootKey);
            if (profile != null && !profile.equals("")) {
                String prifileRootKey = profile;
                boolean isInvalidRootKey = rootKey == null || rootKey.equals("");
                if (!isInvalidRootKey) {
                    prifileRootKey = profile + "." + rootKey;
                }
                extractProperties(p, props, prifileRootKey);
            }
            return props;
        } catch (ConfigurationException e) {
            logger.error("config error", e);
        }
        return null;
    }

    private static void extractProperties(PropertiesConfiguration p, Properties props, String rootKey) {
        Iterator<String> it = null;
        boolean isInvalidRootKey = rootKey == null || rootKey.equals("");
        if (isInvalidRootKey) {
            it = p.getKeys();
        } else {
            it = p.getKeys(rootKey);
        }
        while (it.hasNext()) {
            String key = it.next();
            String subKey = isInvalidRootKey ? key : key.substring(rootKey.length() + 1);
            props.setProperty(subKey, p.getString(key));
        }
    }
}
