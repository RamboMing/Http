package com.rambo.httplib.utils;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author: lanming
 * @date: 2016-04-08
 */
public class JsonUtil extends JSON {
    private static final SerializerFeature[] features;

    private JsonUtil() {
    }

    public static String toJson(Object target) {
        return toJson(target, (String) null);
    }

    public static String toJson(Object target, String fmt) {
        if (TextUtils.isEmpty(fmt)) {
            return JSON.toJSONString(target, features);
        } else {
            SerializeConfig sc = new SerializeConfig();
            sc.put(Date.class, new SimpleDateFormatSerializer(fmt));
            return JSON.toJSONString(target, sc, features);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        return JSON.parseObject(json, clazz);
    }

    public static <T> T fromJson(String json, TypeReference<T> type) {
        return JSON.parseObject(json, type, new Feature[0]);
    }

    public static <T> List<T> fromJsonList(String json, Class<T> clazz) {
        return JSON.parseArray(json, clazz);
    }

    public static <T> List<Object> fromJsonList(String json, Type[] types) {
        return JSON.parseArray(json, types);
    }

    static {
        features = new SerializerFeature[]{SerializerFeature.WriteNullListAsEmpty, SerializerFeature.WriteNullNumberAsZero, SerializerFeature.WriteNullBooleanAsFalse, SerializerFeature.DisableCircularReferenceDetect, SerializerFeature.WriteDateUseDateFormat};
    }
}
