package com.youthlin.mvc.util;

import com.youthlin.ioc.annotaion.AnnotationUtil;
import com.youthlin.mvc.annotation.ConvertWith;
import com.youthlin.mvc.annotation.Param;
import com.youthlin.mvc.converter.Converter;
import com.youthlin.mvc.converter.SimpleConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.util.Set;

import static com.youthlin.mvc.servlet.DispatcherServlet.getContext;

/**
 * 创建: youthlin.chen
 * 时间: 2017-11-18 23:00
 */
public class ObjectInjectUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectInjectUtil.class);

    /**
     * 获取 Controller 参数的值
     * 若 request 里直接有这个参数, 直接转换
     * 否则:
     * - 认为这个参数是个 Pojo, 对其每个字段注入值;
     * - 注入字段时, 若 request 有字段名字, 直接转换,
     * - - 否则再次注入 pojo, 拼上 filedName.innerFiledName 查找请求参数
     * <pre>
     *     // id; name
     *     public String hello(int id, String name){}
     * ---------------------------------------------------------------
     *     public class User{
     *         int id;
     *         String name;
     *     }
     *     //id; user.id; user.name
     *     public String hello(int id, User user){}
     * ---------------------------------------------------------------
     *
     *     ;@Resource
     *     public class MyConverter implements Converter{
     *         public User convert(String str, User.class){
     *             return JsonUtil.from(str, User.class);
     *         }
     *     }
     *     //id="1"; user="{id=1,name='xxx'}"
     *     public String hello(int id, @ConvertWith(MyConverter.class) User user){}
     * ---------------------------------------------------------------
     *
     *     public class User{
     *         int id;
     *         ;@ConverterWith(CatConverter.class) Cat cat;
     *     }
     *     //id=1; user.id=1, user.cat="xxx"
     *     public String hello(int id, User user){}
     * ---------------------------------------------------------------
     *     public class Cat{
     *         double[] weight;
     *     }
     *     public User{
     *         Cat cat;
     *     }
     *     //user.cat.weight=[0,1,2]
     *     public String hello(User user){}
     * </pre>
     *
     * @param request       请求
     * @param parameterType 要注入的类型
     * @param parameterName 要注入的名称
     * @param param         参数的注解 可能为 null
     * @param convertWith   转换器 若 request 里有值使用这个转换器转换字符串为目标类型 若为 null 将使用 {@link SimpleConverter}
     * @return 这个参数的值, 可能为 null
     * @throws IllegalArgumentException 如果有 Param 注解, 且是必填项, 但值为 null 则抛出异常
     */
    public static Object injectFromRequest(HttpServletRequest request, Class<?> parameterType, String parameterName,
            Param param, ConvertWith convertWith) {
        String stringValue = request.getParameter(parameterName);
        Converter converter = getConverter(convertWith, parameterType);
        if (stringValue != null) {
            return convert(stringValue, converter, parameterType);
        }

        if (parameterType.isPrimitive()) {
            if (parameterType.equals(boolean.class)) {
                return false;
            } else if (parameterType.equals(char.class)) {
                return '0';
            } else if (parameterType.equals(byte.class) || parameterType.equals(short.class)
                    || parameterType.equals(int.class) || parameterType.equals(long.class)
                    || parameterType.equals(float.class) || parameterType.equals(double.class)) {
                return 0;
            }
        }
        Set<String> keySet = request.getParameterMap().keySet();
        boolean has = false;
        for (String key : keySet) {
            if (key.startsWith(parameterName)) {
                has = true;
                break;
            }
        }
        if (!has) {
            return null;
        }
        if (parameterType.equals(Boolean.class) || parameterType.equals(BigDecimal.class)
                || parameterType.equals(String.class) || parameterType.equals(Character.class)
                || parameterType.equals(Byte.class) || parameterType.equals(Short.class)
                || parameterType.equals(Integer.class) || parameterType.equals(Long.class)
                || parameterType.equals(Float.class) || parameterType.equals(Double.class)) {
            return null;
        }
        try {
            Constructor<?> constructor = parameterType.getConstructor();
            Object result = constructor.newInstance();
            Field[] fields = parameterType.getDeclaredFields();
            for (Field field : fields) {
                Param fieldParam = field.getAnnotation(Param.class);
                ConvertWith fieldConvertWith = field.getAnnotation(ConvertWith.class);
                Object filedValue = injectFromRequest(request, field.getType(),
                        parameterName + "." + field.getName(), fieldParam, fieldConvertWith);
                if (filedValue != null) {
                    AnnotationUtil.setFiledValue(result, field, filedValue);
                }
            }
            return result;
        } catch (NoSuchMethodException e) {
            LOGGER.warn("Class {} has no default Constructor", parameterType, e);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            LOGGER.warn("Can not create instance of {}", parameterType, e);
        }
        if (param != null) {
            if (param.required()) {
                throw new IllegalArgumentException(parameterName + "(" + parameterType + ") is required");
            } else {
                String defaultValue = param.defaultValue();
                return convert(defaultValue, converter, parameterType);
            }
        }
        return null;
    }

    private static Converter getConverter(ConvertWith convertWith, AnnotatedElement annotatedElement) {
        if (convertWith == null) {
            return null;
        }
        Class<? extends Converter> converterClass = convertWith.value();
        Converter converter = getContext().getBean(converterClass);
        if (converter == null) {
            try {
                converter = converterClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new UnsupportedOperationException("Can not construct converter: " + converterClass, e);
            }
        }
        if (converter == null) {
            throw new UnsupportedOperationException(
                    "No Converter found: " + converterClass + " for element: " + annotatedElement);
        }
        return converter;
    }

    private static Object convert(String str, Converter converter, Class<?> type) {
        if (converter != null) {
            return converter.convert(str);
        }
        return getSimpleConverter(type).convert(str);
    }

    private static SimpleConverter getSimpleConverter(Class<?> type) {
        SimpleConverter converter = getContext().getBean(SimpleConverter.class);
        if (converter == null) {
            converter = SimpleConverter.newInstance(type);
            getContext().registerBean(converter);
        }
        return converter;
    }

}