package com.mmyz.simpleretrofit;

import android.text.TextUtils;

import com.mmyz.simpleretrofit.util.LogUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import okhttp3.*;
import okhttp3.Call;
import okhttp3.Response;

import static java.util.Collections.unmodifiableList;

public class Retrofit {

    private final Map<Method, ServiceMethod<?, ?>> mServiceMethodCache = new ConcurrentHashMap<>();

    final HttpUrl mBaseUrl;
    final Call.Factory mCallFactory;
    final List<Converter.Factory> mConverterFactories;
    final List<CallAdapter.Factory> mAdapterFactories;
    final Executor mCallbackExecutor;

    Retrofit(Call.Factory callFactory,
                    HttpUrl baseUrl,
                    List<Converter.Factory> converterFactories,
                    List<CallAdapter.Factory> adapterFactories,
                    Executor callbackExecutor) {
        this.mCallFactory = callFactory;
        this.mBaseUrl = baseUrl;
        /*
         * 重构
         *      因为ConverterFactory 只允许通过Build进行创建和添加
         *  在Retrofit中是不允许创建和添加，所以通过unmodifiableList
         *  获取一个不可修改（只读）的List<>集合。
         *      若果依然想要在Retrofit中进行创建和添加，将会报出异常为
         *  java.lang.UnsupportedOperationException
         *
         */
        this.mConverterFactories = unmodifiableList(converterFactories);
        this.mAdapterFactories = unmodifiableList(adapterFactories);

        this.mCallbackExecutor = callbackExecutor;
    }
    @SuppressWarnings("unchecked")//解决代理创建对象时，泛型未检查统一问题
    public <T> T create(final Class<T> apiService){
        if (!apiService.isInterface())
            throw new IllegalArgumentException("API的定义必须是接口形式");
        if (apiService.getInterfaces().length > 0)
            throw new IllegalArgumentException("API的定义不允许继承");

        LogUtils.log("5、Retrofit中提供create()方法，通过动态代理方式，创建API接口对象");

        return (T) Proxy.newProxyInstance(
                apiService.getClassLoader(),
                new Class<?>[]{apiService},
                new InvocationHandler() {
                private final Platform platform = Platform.get();
            @Override
            public Object invoke(Object o, Method method, Object[] args) throws Throwable {
                if (method.getDeclaringClass() == Object.class)
                    return method.invoke(this,args);

                //加载处理API接口方法
                ServiceMethod<Object,Object> serviceMethod =
                        (ServiceMethod<Object, Object>) loadServiceMethod(method);
                //创建OkHttpCall
                OkHttpCall<Object> okHttpCall = new OkHttpCall<>(serviceMethod, args);

                //通过对应的CallAdapter适配自定义并期望返回的Call
                return serviceMethod.mCallAdapter.adapt(okHttpCall);
            }
        });
    }

    private ServiceMethod<?, ?> loadServiceMethod(Method method) {
        LogUtils.log("6、加载API接口方法处理中心(ServiceMethod)，并缓存(优先加载缓存中)");
        //提高效率和线程安全性，采用ConcurrentHashMap(分段锁技术)来缓存
        ServiceMethod<?, ?> serviceMethod = mServiceMethodCache.get(method);
        //优先获取缓存中
        if (serviceMethod != null)return serviceMethod;
        synchronized (mServiceMethodCache){
            serviceMethod = mServiceMethodCache.get(method);
            if (serviceMethod == null){
                LogUtils.log("初次做当前API请，创建并缓存");
                //构建对应的ServiceMethod，并放入缓存中
                serviceMethod =  new ServiceMethod.Builder<>(this,method).build();
                mServiceMethodCache.put(method,serviceMethod);
            }else {
                LogUtils.log("不是初次做当前API请，获取缓存");
            }
        }
        return serviceMethod;
    }

    public CallAdapter<?,?> callAdapter(Type returnType, Annotation[] annotations) {
        return nextCallAdapter(null,returnType,annotations);
    }

    private CallAdapter<?, ?> nextCallAdapter(CallAdapter.Factory skipPast,
                                              Type returnType,
                                              Annotation[] annotations) {
        if (returnType == null)
            throw new IllegalArgumentException("returnType == null");
        if (annotations == null)
            throw new IllegalArgumentException("annotations == null");
        //从指定的Factory开始
        int start = mAdapterFactories.indexOf(skipPast) + 1;
        for (int i = start, count = mAdapterFactories.size(); i < count; i++) {
            LogUtils.log("9、通过构建Retrofit添加进来的CallAdapterFactory创建CallAdapter");
            CallAdapter<?, ?> adapter = mAdapterFactories.get(i).get(returnType, annotations, this);
            if (adapter != null) {
                return adapter;
            }
        }
        throw new IllegalArgumentException("不能找到一个CallAdapter，来处理返回值");
    }

    public <T> Converter<ResponseBody,T> responseBodyConverter(Type responseType,
                                                               Annotation[] annotations) {
        return nextResponseBodyConverter(null,responseType,annotations);
    }

    private <T> Converter<ResponseBody,T> nextResponseBodyConverter(Converter.Factory skipPast,
                                                                 Type responseType,
                                                                 Annotation[] annotations) {
        if (responseType == null)
            throw new IllegalArgumentException("responseType == null");
        if (annotations == null)
            throw new IllegalArgumentException("annotation == null");
        int start = mConverterFactories.indexOf(skipPast) + 1;
        for (int i=start;i<mConverterFactories.size();i++){
            LogUtils.log("12、通过构建Retrofit添加进来的ConverterFactory，创建请求响应转换器");
            Converter<ResponseBody, ?> converter =
                    mConverterFactories.get(i).responseBodyConverter(responseType, annotations, this);
            if (converter != null){
                return (Converter<ResponseBody, T>) converter;
            }
        }
        throw new IllegalArgumentException("找不到合适的converter，解析ResponseBody");
    }

    public <T>Converter<T, String> stringConverter(Type iterableType,
                                                Annotation[] parameterAnnotations) {
        if (iterableType == null)
            throw new IllegalArgumentException("type == null");
        if (parameterAnnotations == null)
            throw new IllegalArgumentException("parameterAnnotations == null");
        for (int i = 0 ,count = mConverterFactories.size();i<count;i++){
            Converter<?, String> converter =
                    mConverterFactories.get(i).stringConverter(
                            iterableType, parameterAnnotations, this);
            if (converter != null)
                return (Converter<T, String>) converter;
        }
        //正常返回一个默认的
        //这里我们选择直接报错
        throw new IllegalStateException("converter == null");
    }

    public static final class Builder{
        private final Platform mPlatForm;
        private HttpUrl mInnerBaseUrl;

        private final List<Converter.Factory> mConverterFactories = new ArrayList<>();
        private final List<CallAdapter.Factory> mAdapterFactories = new ArrayList<>();

        Builder(Platform platform){
            this.mPlatForm = platform;
        }

        public Builder(){
            this(Platform.get());
            LogUtils.log("0、创建Retrofit中Builder对象");
        }

        /**
         * 添加BaseUrl
         * 例：
         * <p>
         *   <code>
         *       https://api.github.com
         *   </code>
         * <p/>
         * @param baseUrl
         * @throws Exception
         */
        public Builder baseUrl(String baseUrl){
            if (baseUrl == null)
                throw new NullPointerException("baseUrl == null");
            HttpUrl url = HttpUrl.parse(baseUrl);
            baseUrl(url);
            LogUtils.log("1、添加BaseUrl");
            return this;
        }

        public Builder baseUrl(HttpUrl url){

            if (url == null)
                throw  new NullPointerException("baseUrl == null");

            List<String> pathSegments = url.pathSegments();

            if (!"".equals(pathSegments.get(pathSegments.size() -1)))
                throw new IllegalArgumentException("baseUrl 必须以/结尾");

            this.mInnerBaseUrl = url;
            return this;
        }

        public Builder addConverterFactory(Converter.Factory factory){
            if (factory == null)
                throw new NullPointerException("factory == null");
            mConverterFactories.add(factory);
            LogUtils.log("2、添加格式转换工厂");
            return this;
        }

        public Builder addCallAdapterFactory(CallAdapter.Factory factory){
            if (factory == null)
                throw new NullPointerException("factory == null");
            mAdapterFactories.add(factory);
            LogUtils.log("3、添加适配工厂");
            return this;
        }

//        private Builder callbackExecutor(Executor executor){
//            if (executor == null)
//                throw new NullPointerException("executor == null");
//            this.mExecutor = executor;
//            return this;
//        }

        public Retrofit build(){
            if (mInnerBaseUrl == null){
                throw new IllegalStateException("Base URL == null");
            }

            okhttp3.Call.Factory callFactory = new OkHttpClient();
//            Executor executor = mExecutor;
//            if (executor == null){
            Executor callbackExecutor = mPlatForm.defaultCallbackExecutor();
//            }

            List<CallAdapter.Factory> adapterFactories = new ArrayList<>(this.mAdapterFactories);
            List<Converter.Factory> converterFactories = new ArrayList<>(this.mConverterFactories);
            LogUtils.log("4、构建Retrofit对象");
            return new Retrofit(
                    callFactory,
                    mInnerBaseUrl,
                    converterFactories,
                    adapterFactories,
                    callbackExecutor);
        }
    }
}
