package com.hx.xbry.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static com.hx.xbry.bean.Constants.PUBLIC_CONF_DIR;

/**
 * @ClassName PropertiesUtils
 * @Description .properties处理工具类
 * @Author fmy
 * @Date 2019/10/23 10:52
 * @Version 1.0
 **/
public class PropertiesUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesUtils.class);
    private static final Properties properties;

    static {
        properties = new Properties();
        try {
            File confFile = new File(getConfDir());
            if (!confFile.exists()) {
                if (confFile.mkdirs()) {
                    LOGGER.info("Create Directory:" + confFile.getAbsolutePath());
                }
            }
            File[] files = confFile.listFiles((dir, name) -> name.endsWith(".properties"));
            if (files != null) {
                for (File file : files) {
                    try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                        Properties pro0 = new Properties();
                        pro0.load(reader);
                        properties.putAll(pro0);
                    } catch (UnsupportedEncodingException e) {
                        LOGGER.error("", e);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("", e);
            System.exit(0);
        }
    }

    /**
     * @Description 获取配置文件路径
     * @Author fmy
     * @Date 2019/10/23 10:55
     * @Param []
     * @Return java.lang.String
     **/
    private static String getConfDir() {
        return PUBLIC_CONF_DIR;
    }

    /**
     * @Description 通过键值获取值
     * @Author fmy
     * @Date 2019/10/23 10:56
     * @Param [key]
     * @Return java.lang.String
     **/
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * @Description 通过键值获取值
     * @Author fmy
     * @Date 2019/10/23 10:56
     * @Param [key, defaultValue]
     * @Return java.lang.String
     **/
    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

}
