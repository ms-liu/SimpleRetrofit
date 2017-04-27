package com.mmyz.simpleretrofit;

import com.mmyz.simpleretrofit.convert.DefaultResponseConverter;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/**
 * ==============================================
 * <p>
 * 类名：Converter(Interface)
 *   装换器接口
 *
 *   内部包含了一个创建转换器的抽象工厂类
 *
 *   通过调用不同“加工方法”创建出不同转换器
 *
 *    泛型：
 *    F=>转换前类型
 *    T=>转换后类型
 * <p>
 * 作者：M-Liu
 * <p>
 * 时间：2017/4/16
 * <p>
 * 邮箱：ms_liu163@163.com
 * <p>
 * ==============================================
 */

public interface Converter<F,T> {
    /**
     * 具体转换方法
     * @param f
     * @return
     * @throws IOException
     */
    T convert(F f) throws IOException;

    abstract class Factory{

        //创建ResponseBody转换器 ResponseBody -> ?
        public Converter<ResponseBody,?> responseBodyConverter(
                Type type, Annotation[] annotations, Retrofit retrofit){
            return null;
        }

        //创建RequestBody转换器 ? -> RequestBody
        public Converter<?, RequestBody> requestBodyConverter(
                Type type,Annotation[] parameterAnnotations,Retrofit retrofit){
            return null;
        }

        //创建String转换器  ? -> String
        public Converter<?,String> stringConverter(Type iterableType, Annotation[] parameterAnnotations, Retrofit retrofit){
            return null;
        }
    }
}
