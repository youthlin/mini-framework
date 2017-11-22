package com.youthlin.mvc.support.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * 创建: youthlin.chen
 * 时间: 2017-11-17 23:11
 */
@SuppressWarnings("WeakerAccess")
public class SimpleConverter<T> implements Converter<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleConverter.class);
    protected static final Map<Class, SimpleConverter> INSTANCE_MAP = new HashMap<>();
    protected static final Object OBJECT_MAPPER;

    static {
        Object t = null;
        try {
            t = new ObjectMapper();
        } catch (Throwable ignore) {
            LOGGER.warn("Jackson Object Mapper is not available.");
        }
        OBJECT_MAPPER = t;
    }

    public static <T> SimpleConverter<T> getInstance(Class<T> clazz) {
        @SuppressWarnings("unchecked")
        SimpleConverter<T> simpleConverter = (SimpleConverter<T>) INSTANCE_MAP.get(clazz);
        if (simpleConverter == null) {
            simpleConverter = new SimpleConverter<>(clazz);
            INSTANCE_MAP.put(clazz, simpleConverter);
        }
        return simpleConverter;
    }

    private Class<T> clazz;

    protected SimpleConverter(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T convert(String from) {
        if (clazz.isAssignableFrom(String.class)) {
            return (T) from;
        } else if (clazz.isAssignableFrom(double.class) || clazz.isAssignableFrom(Double.class)) {
            return (T) (Double) Double.parseDouble(from);
        } else if (clazz.isAssignableFrom(float.class) || clazz.isAssignableFrom(Float.class)) {
            return (T) (Float) Float.parseFloat(from);
        } else if (clazz.isAssignableFrom(long.class) || clazz.isAssignableFrom(Long.class)) {
            return (T) (Long) Long.parseLong(from);
        } else if (clazz.isAssignableFrom(int.class) || clazz.isAssignableFrom(Integer.class)) {
            return (T) (Integer) Integer.parseInt(from);
        } else if (clazz.isAssignableFrom(short.class) || clazz.isAssignableFrom(Short.class)) {
            return (T) (Short) Short.parseShort(from);
        } else if (clazz.isAssignableFrom(byte.class) || clazz.isAssignableFrom(Byte.class)) {
            return (T) (Byte) Byte.parseByte(from);
        } else if (clazz.isAssignableFrom(boolean.class) || clazz.isAssignableFrom(Boolean.class)) {
            return (T) (Boolean) Boolean.parseBoolean(from);
        } else if (clazz.isAssignableFrom(char.class) || clazz.isAssignableFrom(Character.class)) {
            if (from.length() == 1) {
                return (T) (Character) from.charAt(0);
            } else {
                throw new ClassCastException('\"' + from + "\" can not cast to char.");
            }
        } else {
            if (canReadJson()) {
                return fromJson(from);
            }
            try {
                //new BigDecimal(String)
                Constructor<T> constructor = clazz.getConstructor(String.class);
                if (constructor != null && Modifier.isPublic(constructor.getModifiers())) {
                    return constructor.newInstance(from);
                }
            } catch (NoSuchMethodException ignore) {
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                LOGGER.error("Invoke constructor of {} to convert String {} error", clazz, from, e);
            }

        }
        LOGGER.warn("Can not convert to type: {} from String value: {}", clazz, from);
        return doConvert(from);
    }

    private boolean canReadJson() {
        return OBJECT_MAPPER != null;
    }

    private T fromJson(String json) {
        try {
            return ((ObjectMapper) OBJECT_MAPPER).readValue(json, clazz);
        } catch (Exception e) {
            LOGGER.warn("Can not read json for type {} : {}", clazz, json, e);
            throw new IllegalArgumentException("Can not convert request body to " + clazz + ": " + json, e);
        }
    }

    protected T doConvert(String from) {
        if (clazz.isArray()) {
            //return (T) Array.newInstance(clazz, 0);
            throw new UnsupportedOperationException(
                    "Array is not supported: No @ConvertWith found, and Jackson is not available");
        }
        return null;
    }

}
