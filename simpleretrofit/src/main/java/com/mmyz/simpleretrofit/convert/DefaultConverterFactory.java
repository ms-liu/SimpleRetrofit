package com.mmyz.simpleretrofit.convert;

import com.mmyz.simpleretrofit.CallAdapter;
import com.mmyz.simpleretrofit.Converter;
import com.mmyz.simpleretrofit.Retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;

/**
 * ==============================================
 * <p>
 * 类名：
 * <p>
 * 作者：M-Liu
 * <p>
 * 时间：2017/4/25
 * <p>
 * 邮箱：ms_liu163@163.com
 * <p>
 * ==============================================
 */

public class DefaultConverterFactory extends Converter.Factory {
    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        return new DefaultResponseConverter();
    }

    @Override
    public Converter<?, String> stringConverter(Type iterableType, Annotation[] parameterAnnotations, Retrofit retrofit) {
        return StringConverter.INSTANCE;
    }
}
