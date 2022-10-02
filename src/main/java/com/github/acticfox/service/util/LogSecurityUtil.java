/*
 * Copyright 2018 zhichubao.com All right reserved. This software is the confidential and proprietary information of
 * zhichubao.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into with zhichubao.com .
 */
package com.github.acticfox.service.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.BeanContext;
import com.alibaba.fastjson.serializer.ContextValueFilter;
import com.github.acticfox.common.api.annotation.rpclog.SecurityField;
import com.github.acticfox.common.api.enums.SecurityFieldType;

/**
 * 类LogUtil.java的实现描述：TODO 类实现描述
 * 
 * @author fanyong.kfy 2018年11月19日 下午2:46:46
 */
public class LogSecurityUtil {

    private static ContextValueFilter valueFilter = new ContextValueFilter() {
        @Override
        public Object process(BeanContext context, Object arg1, String name, Object value) {
            if (context == null) {
                return value;
            }
            SecurityField annotation = context.getAnnation(SecurityField.class);
            if (annotation == null) {
                return value;
            } else if (annotation.ignore()) {
                return null;
            } else {
                return transformSecurityField(annotation, value);
            }
        }
    };

    private static String transformSecurityField(SecurityField securityField, Object originalValue) {
        if (originalValue == null) {
            return null;
        }
        if (securityField.ignore()) {
            return null;
        }
        SecurityFieldType securityFieldType = securityField.fieldType();
        String originalParameter = originalValue.toString();
        String transformValue = originalParameter;
        switch (securityFieldType) {
            case EMAIL:
                transformValue = MaskUtil.displayEmail(originalParameter);
                break;
            case MOBILE:
                transformValue = MaskUtil.displayMobile(originalParameter);
                break;
            case IDNO:
                transformValue = MaskUtil.displayIDCard(originalParameter);
                break;
            case REALNAME:
                transformValue = MaskUtil.displayName(originalParameter);
                break;
            case TELEPHONE:
                transformValue = MaskUtil.displayTelephone(originalParameter);
                break;
            case BANKCARD:
                transformValue = MaskUtil.displayBankCard(originalParameter);
                break;
            default:
                break;
        }

        return transformValue;
    }

    public static boolean isJavaClass(Class<?> clz) {
        return clz != null && clz.getClassLoader() == null;
    }

    public static final String formatJsonString(Object obj) {
        if (obj == null) {
            return StringUtils.EMPTY;
        }

        return JSON.toJSONString(obj, valueFilter);
    }

    public static final String[] formatJsonArray(Object[] objArray) {
        if (objArray == null || objArray.length == 0) {
            return null;
        }
        String[] formatStrs = new String[objArray.length];
        for (int i = 0; i < objArray.length; i++) {
            formatStrs[i] = formatJsonString(objArray[i]);
        }

        return formatStrs;
    }

    public static Object[] transformParameter(Method method, Object[] parameters) {
        if (parameters == null || parameters.length == 0) {
            return null;
        }
        Object[] originalParameters = new Object[parameters.length];
        for (int j = 0; j < parameters.length; j++) {
            originalParameters[j] = parameters[j];
            if (originalParameters[j] == null) {
                continue;
            }
            if (isJavaClass(originalParameters[j].getClass())) {
                continue;
            }
            Field[] fields = originalParameters[j].getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(SecurityField.class)) {
                    SecurityField annotation = field.getAnnotation(SecurityField.class);
                    try {
                        field.setAccessible(true);
                        String transformVal = transformSecurityField(annotation, field.get(originalParameters[j]));
                        field.set(originalParameters[j], transformVal);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        log.error("transform securityField error ", e);
                    }
                }
            }
        }

        int i = 0;
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (Annotation[] parameterAnnotation : parameterAnnotations) {
            for (Annotation annotation : parameterAnnotation) {
                if (annotation instanceof SecurityField) {
                    SecurityField paramAnnotation = (SecurityField)annotation;
                    if (originalParameters[i] == null) {
                        i++;
                        continue;
                    }
                    originalParameters[i] = transformSecurityField(paramAnnotation, originalParameters[i].toString());
                    i++;
                }
            }
        }

        return originalParameters;
    }

    private static final Logger log = LoggerFactory.getLogger(LogSecurityUtil.class);

}
