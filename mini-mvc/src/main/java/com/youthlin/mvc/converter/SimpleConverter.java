package com.youthlin.mvc.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

/**
 * 创建: youthlin.chen
 * 时间: 2017-11-17 23:11
 */
@Resource
public class SimpleConverter implements Converter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleConverter.class);

    @SuppressWarnings("unchecked")
    @Override
    public <T> T convert(String from, Class<T> clazz) {
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
        return null;
    }

}
