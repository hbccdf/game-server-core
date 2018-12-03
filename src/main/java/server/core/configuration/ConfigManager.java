package server.core.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import server.core.util.StringUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class ConfigManager {
    private static final JavaPropsMapper mapper = new JavaPropsMapper();

    private static final String CONFIG_PATH = "config.properties";

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

    public static Properties getProperties() {
        return configProps;
    }

    public static String getString(String key, String defaultValue) {
        return configProps.getProperty(key, defaultValue);
    }

    public static String getProfile() {
        return configProfile;
    }

    public static void updateConfig(String key, String value) {
        configProps.setProperty(key, value);
    }

    public static boolean writeCustomConfig(Map<String, String> configs) {
        String customPath = getCustomConfigFile(CONFIG_PATH);
        Properties props = internalGetProperties(customPath, null, null);
        if (props == null) {
            props = new Properties();
        }
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            props.setProperty(entry.getKey(), entry.getValue());
        }
        try (FileWriter fw = new FileWriter(customPath)) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<Object, Object> entry : props.entrySet()) {
                sb.append((String)entry.getKey()).append("=").append((String)entry.getValue()).append("\n");
            }
            fw.write(sb.toString());
            fw.flush();
            return true;
        } catch (Exception e) {
            log.error("write config file error, {}", e);
        }
        return false;
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
            return mapper.readPropertiesAs(props, clz);
        } catch (IOException e) {
            log.error("read class config failed, class={}, rootKey={}, profile={}", clz.getName(), rootKey, e);
        }
        return null;
    }

    private static void loadConfig(boolean withUserConfig) {
        try {
            String path = getSysConfigFile(CONFIG_PATH);
            Properties props = internalGetProperties(CONFIG_PATH, null, null);
            assert props != null;

            String profile = props.getProperty("profile");
            if (profile == null && isFileExist(path)) {
                props = internalGetProperties(path, null, null);
                assert props != null;
                configProfile = props.getProperty("profile", configProfile);
                log.info("profile is {}, with {}", configProfile, path);
            } else if (profile != null) {
                configProfile = profile;
                log.info("profile is {}, with {}", configProfile, CONFIG_PATH);
            } else {
                log.info("profile is {}, with default", configProfile);
            }
        } catch (Exception e) {
            log.error("", e);
        }

        configProps = internalLoadConfig(withUserConfig);
    }

    private static Properties internalLoadConfig(boolean withUserConfig) {
        Properties props = null;
        String sysPath = getSysConfigFile(CONFIG_PATH);
        String customPath = getCustomConfigFile(CONFIG_PATH);

        try {
            if (isFileExist(sysPath)) {
                props = internalGetProperties(sysPath, null, configProfile);
                assert props != null;
            }

            Properties configProps = internalGetProperties(CONFIG_PATH, null, configProfile);
            assert configProps != null;
            if (props == null) {
                props = configProps;
            } else {
                props.putAll(configProps);
            }

            if (withUserConfig && isFileExist(customPath)) {
                Properties customProps = internalGetProperties(customPath, null, configProfile);
                assert customProps != null;
                props.putAll(customProps);
            }

        } catch (Exception e) {
            log.error("read config file error, sys {} custom {}, {}", sysPath, customPath, e);
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

    private static Properties internalGetProperties(String path, String rootKey, String profile) {
        Configurations configs = new Configurations();
        try {
            Properties props = new Properties();
            PropertiesConfiguration p = configs.properties(path);
            extractProperties(p, props, rootKey);
            if (!StringUtil.isNullOrEmpty(profile)) {
                String profileRootKey = profile;
                boolean isInvalidRootKey = StringUtil.isNullOrEmpty(rootKey);
                if (!isInvalidRootKey) {
                    profileRootKey = profile + "." + rootKey;
                }
                extractProperties(p, props, profileRootKey);
            }
            return props;
        } catch (ConfigurationException e) {
            log.error("config error, path {}, rootKey {}, profile {}", path, rootKey, profile, e);
        }
        return null;
    }

    private static void extractProperties(PropertiesConfiguration p, Properties props, String rootKey) {
        Iterator<String> it;
        boolean isInvalidRootKey = StringUtil.isNullOrEmpty(rootKey);
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

    private static String getCustomConfigFile(String path) {
        return getSuffixConfigFile(path, ".user");
    }

    private static String getSysConfigFile(String path) {
        return getSuffixConfigFile(path, ".sys");
    }

    private static String getSuffixConfigFile(String path, String suffix) {
        int index = path.lastIndexOf('.');
        if (index >= 0) {
            return path.substring(0, index) + suffix + path.substring(index);
        } else {
            return path + suffix;
        }
    }

    private static boolean isFileExist(String path) throws IOException {
        return new File(path).exists() || ConfigManager.class.getClassLoader().getResources(path).hasMoreElements();
    }
}
