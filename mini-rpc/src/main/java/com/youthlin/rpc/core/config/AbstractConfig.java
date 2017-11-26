package com.youthlin.rpc.core.config;

import java.util.HashMap;
import java.util.Map;

/**
 * 创建: youthlin.chen
 * 时间: 2017-11-26 15:08
 */
public abstract class AbstractConfig implements Config {
    private Map<String, String> config = new HashMap<>();

    @Override
    public abstract String host();

    @Override
    public abstract int port();

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
    public Integer getConfig(String key, int dft) {
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
    public Long getConfig(String key, long dft) {
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
    public Double getConfig(String key, double dft) {
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
    public Boolean getConfig(String key, boolean dft) {
        String value = getConfig(key);
        if (value == null || value.isEmpty()) {
            return dft;
        }
        return Boolean.valueOf(value);
    }
}
