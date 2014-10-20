package com.sf.heros.im.common;

import java.io.InputStream;
import java.util.Properties;

public class PropsLoader {

    private static final Properties props = new Properties();

    public static void load() {
        InputStream confIn = PropsLoader.class.getClassLoader().getResourceAsStream("conf.properties");
        try {
            props.load(confIn);
        } catch (Exception e) {
            throw new RuntimeException("load properties error.", e);
        }
    }

    public static Integer get(String key, Integer defaultVal) {
        try {
            return new Integer(props.getProperty(key, defaultVal.toString()));
        } catch (Exception e) {
            return defaultVal;
        }
    }

    public static String get(String key, String defaultVal) {
        return props.getProperty(key, defaultVal);
    }

    public static boolean get(String key, boolean defaultVal) {
        try {
            return new Boolean(props.getProperty(key, defaultVal + ""));
        } catch (Exception e) {
            return defaultVal;
        }
    }

}
