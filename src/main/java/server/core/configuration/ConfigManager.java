package server.core.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.core.util.StringUtil;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

public class ConfigManager {
    private static final JavaPropsMapper mapper = new JavaPropsMapper();

    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);

    private static final String configPath = "config.properties";

    private static String configProfile = "dev";

    private static Properties configProps;

    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);

        loadConfig(true);
    }

    public static boolean reload() {
        Properties props = internalLoadConfig(true);
        if (props == null) {
            return false;
        }

        configProps = props;
        return true;
    }

    public static String getString(String key, String defaultValue) {
        return configProps.getProperty(key, defaultValue);
    }

    public static <T> T read(Class<T> clz) {
        return internalRead(clz, null);
    }

    public static <T> T read(Class<T> clz, String rootKey) {
        return internalRead(clz, rootKey);
    }

    private static <T> T internalRead(Class<T> clz, String rootKey) {
        try {
            Properties props = internalGetProperties(rootKey);
            T obj = mapper.readPropertiesAs(props, clz);
            return obj;
        } catch (IOException e) {
            logger.error("read class config failed, class={}, rootKey={}, profile={}", clz.getName(), rootKey, e);
        }
        return null;
    }

    private static void loadConfig(boolean withUserConfig) {
        Properties props = internalGetProperties(configPath, null, null);
        configProfile = props.getProperty("profile", configProfile);

        configProps = internalLoadConfig(withUserConfig);
    }

    private static Properties internalLoadConfig(boolean withUserConfig) {
        Properties props = internalGetProperties(configPath, null, configProfile);
        if (props == null || !withUserConfig) {
            return props;
        }

        String customPath = getCustomConfigPath(configPath);
        try {
            if (ConfigManager.class.getClassLoader().getResources(customPath).hasMoreElements()) {
                Properties customProps = internalGetProperties(customPath, null, configProfile);
                props.putAll(customProps);
            }
        } catch (Exception e) {
            logger.error("read custom file error, {}", customPath, e);
        }

        return props;
    }

    private static Properties internalGetProperties(String rootKey) {
        if (StringUtil.isNullOrEmpty(rootKey)) {
            return configProps;
        }

        Properties props = new Properties();
        for (Map.Entry<Object, Object> entry : configProps.entrySet()) {
            String key = (String)entry.getKey();
            if (key.startsWith(rootKey)) {
                if (rootKey.equals(key)) {
                    continue;
                }
                String subKey = key.substring(rootKey.length() + 1);
                props.setProperty(subKey, (String) entry.getValue());
            }
        }
        return props;
    }

    private static Properties internalGetProperties(String path, String rootKey, String profile ) {
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
            if (!isInvalidRootKey && key.equals(rootKey)) {
                continue;
            }
            String subKey = isInvalidRootKey ? key : key.substring(rootKey.length() + 1);
            props.setProperty(subKey, p.getString(key));
        }
    }

    private static String getCustomConfigPath(String path) {
        int index = path.lastIndexOf('.');
        if (index >= 0) {
            return path.substring(0, index) + ".user" + path.substring(index);
        } else {
            return path + ".user";
        }
    }
}
