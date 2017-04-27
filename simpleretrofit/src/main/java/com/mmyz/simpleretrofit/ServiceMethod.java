package com.mmyz.simpleretrofit;

import com.mmyz.simpleretrofit.http.GET;
import com.mmyz.simpleretrofit.http.Query;
import com.mmyz.simpleretrofit.util.LogUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.*;
import okhttp3.Call;

/**
 * ==============================================
 * <p>
 * 类名：
 * <p>
 * 作者：M-Liu
 * <p>
 * 时间：2017/4/21
 * <p>
 * 邮箱：ms_liu163@163.com
 * <p>
 * ==============================================
 */

final class ServiceMethod<R,T> {
    static final String PARAM = "[a-zA-Z][a-zA-Z0-9_-]*";
    static final Pattern PARAM_URL_REGEX = Pattern.compile("\\{(" + PARAM + ")\\}");
    static final Pattern PARAM_NAME_REGEX = Pattern.compile(PARAM);
    private final String mRelativeUrl;
    final CallAdapter<R, T> mCallAdapter;
    final Call.Factory mCallFactory;
    private final Converter<ResponseBody, R> mResponseConverter;
    private ParameterHandler<?>[] mParameterHandlers;
    private Method mMethod;
    private String mMethodType;
    private HttpUrl mBaseUrl;
//    private final CallAdapter<R, T> mCallAdapter;

    public  ServiceMethod(Builder<R, T> builder) {
        this.mCallFactory = builder.mRetrofit.mCallFactory;
        this.mCallAdapter = builder.mCallAdapter;
        this.mResponseConverter = builder.mResponseConverter;
        this.mBaseUrl = builder.mRetrofit.mBaseUrl;
        this.mRelativeUrl = builder.mRelativeUrl;
        this.mMethod = builder.mMethod;
        this.mMethodType = builder.mMethodType;
        this.mParameterHandlers = builder.mParameterHandlers;
    }

    public Request toRequest(Object... args) throws IOException {
        RequestBuilder requestBuilder = new RequestBuilder(mMethodType, mBaseUrl, mRelativeUrl);

        @SuppressWarnings("unchecked")
        ParameterHandler<Object>[] mParameterHandlers = (ParameterHandler<Object>[]) this.mParameterHandlers;

        int argumentCount = args != null ? args.length : 0;

        if (argumentCount != mParameterHandlers.length){
            throw new IllegalStateException(
                    "待处理参数数量("+argumentCount+")与参数处理器数量("+mParameterHandlers.length+")不对应"
            );
        }

        for (int i=0;i<argumentCount;i++){
            mParameterHandlers[i].apply(requestBuilder,args[i]);
        }

        return requestBuilder.build();
    }

    public R toResponse(ResponseBody rawBody) throws IOException {
        return mResponseConverter.convert(rawBody);
    }


    static final class Builder<T,R>{
        private final Retrofit mRetrofit;
        private final Method mMethod;
        private final Annotation[] mMethodAnnotations;
        private final Type[] mParameterTypes;
        private final Annotation[][] mParameterAnnotationsArray;
        private CallAdapter<T, R> mCallAdapter;
        private Type mResponseType;
        private Converter<ResponseBody, T> mResponseConverter;
        private String mMethodType;
        private String mRelativeUrl;
        private Set<String> mRelativeUrlParamNames;
        private boolean goQuery;
        private ParameterHandler<?>[] mParameterHandlers;

        Builder(Retrofit retrofit, Method method){
            LogUtils.log("7、初次创建API接口方法处理中心(ServiceMethod)");
            //Retrofit实例
            this.mRetrofit = retrofit;

            //API接口中定义的方法
            this.mMethod = method;

            //API接口中方法上定义的注解--->@GET
            this.mMethodAnnotations = method.getAnnotations();

            //API接口中方法中的参数化注解
            this.mParameterTypes = method.getGenericParameterTypes();

            //API接口中方法中的所有参数注解
            this.mParameterAnnotationsArray = method.getParameterAnnotations();

        }

        public ServiceMethod build(){
            this.mCallAdapter = createCallAdapter();
            LogUtils.log("10、获取CallAdapter中适配好的响应类型");
            this.mResponseType = mCallAdapter.responseType();

            if (mResponseType == okhttp3.Response.class || mResponseType == Response.class)
                throw new IllegalStateException("方法返回类型错误，需要的是ResponseBody");

            this.mResponseConverter = createResponseConverter();

            for (Annotation annotation:
                 mMethodAnnotations) {
                LogUtils.log("13、解析方法上注解");
                parseMethodAnnotation(annotation);
            }

            if (mMethodType == null)
                throw new IllegalStateException("接口方法中必须需要是哪一种请求类型：@GET,@POST...");

            int parameterCount = mParameterAnnotationsArray.length;
            LogUtils.log("16、根据API接口中方法，注解参数的数量，创建对应长度的参数处理器数组");
            //根据参数数量，创建对应长度的参数处理器数组。
            mParameterHandlers = new ParameterHandler<?>[parameterCount];
            LogUtils.log("循环遍历注解参数====START");
            for (int i = 0; i < parameterCount; i++){
                Type parameterType = mParameterTypes[i];
                Annotation[] parameterAnnotations = mParameterAnnotationsArray[i];
                mParameterHandlers[i] = parserParameter(i,parameterType,parameterAnnotations);
            }
            LogUtils.log("循环遍历注解参数====END");
            return new ServiceMethod<>(this);
        }

        private ParameterHandler<?> parserParameter(int i,
                                                    Type parameterType,
                                                    Annotation[] parameterAnnotations) {
            ParameterHandler<?> parameterHandler = null;
            for (Annotation annotation :
                    parameterAnnotations) {
                LogUtils.log(">>>>>>根据API接口中方法的每一个注解参数，创建对应的参数处理器");
                //处理每一个参数注解，并返回参数处理器
                ParameterHandler<?> annotationAction =
                        parserParameterAnnotation(i, parameterType, parameterAnnotations, annotation);
                if (annotationAction == null)
                    continue;
                if (parameterHandler != null )
                    throw new IllegalStateException("同时有多种请求注解");

                parameterHandler = annotationAction;
            }

            if (parameterHandler == null)
                throw new IllegalStateException("未指定请求注解");

            return parameterHandler;
        }

        private ParameterHandler<?> parserParameterAnnotation(
                int i,
                Type parameterType,
                Annotation[] parameterAnnotations,
                Annotation annotation) {

            if (annotation instanceof Query){
                Query query = (Query) annotation;
                String queryName = query.value();
                LogUtils.log(">>>>>>判断参数注解类型是Query，创建对应Query参数处理器");
//                Class<?> rawParameterType = Utils.getRawType(parameterType);
//                //判断参数注解是否实现Iterable接口 List<String>
//                if (Iterable.class.isAssignableFrom(rawParameterType)){
//                    //判断type对象是否是ParameterizedType类型
//                    if (!(parameterType instanceof ParameterizedType))
//                        throw new IllegalStateException("参数类型必须是ParameterizedType类型");
//
//                    ParameterizedType parameterizedType = (ParameterizedType) parameterType;
//                    Type iterableType = Utils.getParameterUpperBound(i, parameterizedType);
//                    //获取String类型转换器
//                    Converter<?,String> converter = mRetrofit.stringConverter(iterableType,parameterAnnotations);
//                    //创建Query类型参数处理
//                    return new ParameterHandler.Query<>(queryName,converter,false).iterable();
//                }else {
                    Converter<?,String> converter =
                            mRetrofit.stringConverter(mResponseType,parameterAnnotations);
                    //创建Query类型参数处理
                    return new ParameterHandler.Query<>(queryName,converter,false);
//                }
            }
            throw new IllegalStateException("注解不是@Query或者未添加参数注解");
        }

        /**
         * 解析方法
         * @param annotation Annotation 方法上注解
         */
        private void parseMethodAnnotation(Annotation annotation){
            LogUtils.log("14、判断是那种请求注解");
            if (annotation instanceof GET){
                parseHttpMethodAndPath("GET",((GET) annotation).value(),false);
            }
        }

        /**
         *  解析请求类型和请求链接
         * @param methodType String 请求类型
         * @param value String 请求链接
         * @param hasRequestBody boolean 是否含有请求体，一般Post或者multipart/form-data会带有
         */
        private void parseHttpMethodAndPath(String methodType, String value, boolean hasRequestBody){
            if (this.mMethodType != null)
                throw new IllegalStateException("因为一次请求只可以有一种HTTP请求类型，但当前却又指明了一种请求类型["+methodType+"]");

            this.mMethodType = methodType;

            if (value.isEmpty())
                return;

            //排除开发人员直接在注解的URL中，填写请求参数
            //如果有，报错并建议将它通过@Query的形式携带
            int questionIndex = value.indexOf("?");
            if (questionIndex != -1 && questionIndex < value.length()-1){
                String queryParamsInUrl = value.substring(questionIndex + 1);
                Matcher matcher = PARAM_URL_REGEX.matcher(queryParamsInUrl);
                if (matcher.find())
                    throw new IllegalStateException("在URL中不要加上参数，可以通过@Query的形式讲参数放入到方法参数中");
            }
            LogUtils.log("15、获取相对的URL路径");
            this.mRelativeUrl = value;
            this.mRelativeUrlParamNames = parsePathParameters(value);
        }

        /**
         *     获取不同的访问路径参数，同时通过Set集合存储
         * 从而去除掉相同路径参数
         * @param relativePath String 访问路径
         * @return Set
         */
        private Set<String> parsePathParameters(String relativePath){
            Matcher matcher = PARAM_URL_REGEX.matcher(relativePath);
            Set<String> parameters = new LinkedHashSet<>();
            while (matcher.find()){
                parameters.add(matcher.group(1));
            }
            return parameters;
        }

        private Converter<ResponseBody,T> createResponseConverter(){
            LogUtils.log("11、创建请求响应转换器");
            Annotation[] annotations = mMethod.getAnnotations();
            try {
                return mRetrofit.responseBodyConverter(mResponseType,annotations);
            }catch (RuntimeException e){
                throw new RuntimeException("找不到合适的converter，解析ResponseBody");
            }
        }

        @SuppressWarnings("unchecked")
        private CallAdapter<T,R> createCallAdapter() {
            LogUtils.log("8、在ServiceMethod中创建适配器");
            //获取API接口中定义的方法的返回值类型
            Type returnType = mMethod.getGenericReturnType();

            //此略去返回值类型判断

            Annotation[] annotations = mMethod.getAnnotations();
            try {
                return (CallAdapter<T, R>) mRetrofit.callAdapter(returnType,annotations);
            }catch (RuntimeException e){
                throw new RuntimeException("不能创建与returnType对应的CallAdapter");
            }
        }
    }
}
