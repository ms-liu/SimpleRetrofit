package com.mmyz.simpleretrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;


/**
 * ==============================================
 * <p>
 * 类名：CallAdapter(Interface)
 *
 * <p>
 * 作者：M-Liu
 * <p>
 * 时间：2017/4/16
 * <p>
 * 邮箱：ms_liu163@163.com
 * <p>
 * ==============================================
 */

public interface CallAdapter<R,T> {
    Type responseType();

    T adapt(Call<R> call);

    abstract class Factory{
        public abstract CallAdapter<?,?> get(Type returnType,
                                             Annotation[] annotations,
                                             Retrofit retrofit);
    }
}
