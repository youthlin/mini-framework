package com.youthlin.rpc.core.config;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 创建: youthlin.chen
 * 时间: 2017-11-26 15:08
 */
public abstract class AbstractConfig implements ServiceConfig {
    private Map<String, String> config = new HashMap<>();

    @Override
    public Map<String, String> getConfig() {
        return config;
    }

    @Override
    public String getConfig(String key) {
        return config.get(key);
    }

    @Override
    public String getConfig(String key, String dft) {
        String value = getConfig(key);
        if (value == null) {
            value = dft;
        }
        return value;
    }

    @Override
    public int getConfig(String key, int dft) {
        String value = getConfig(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ignore) {
            }
        }
        return dft;
    }

    @Override
    public long getConfig(String key, long dft) {
        String value = getConfig(key);
        if (value != null) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException ignore) {
            }
        }
        return dft;
    }

    @Override
    public double getConfig(String key, double dft) {
        String value = getConfig(key);
        if (value != null) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException ignore) {
            }
        }
        return dft;
    }

    @Override
    public boolean getConfig(String key, boolean dft) {
        String value = getConfig(key);
        if (value == null || value.isEmpty()) {
            return dft;
        }
        return Boolean.valueOf(value);
    }

    @Override
    public String getConfig(Method method, String key) {
        return null;
    }

    @Override
    public String getConfig(Method method, String key, String dft) {
        return null;
    }

    @Override
    public Integer getConfig(Method method, String key, int dft) {
        return null;
    }

    @Override
    public Long getConfig(Method method, String key, long dft) {
        return null;
    }

    @Override
    public Double getConfig(Method method, String key, double dft) {
        return null;
    }

    @Override
    public Boolean getConfig(Method method, String key, boolean dft) {
        return null;
    }

    @Override
    public <T> T getConfig(Method method, String key, T dft) {
        return null;
    }
}
