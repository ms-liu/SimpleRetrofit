package com.mmyz.simpleretrofit;

import java.io.IOException;

/**
 * ==============================================
 * <p>
 * 类名：ParameterHandler 参数处理类
 * <p>
 * 作者：M-Liu
 * <p>
 * 时间：2017/4/24
 * <p>
 * 邮箱：ms_liu163@163.com
 * <p>
 * ==============================================
 */

public abstract class ParameterHandler<T> {
    /**
     * 实现方法参数添加的方法
     */
    abstract void apply(RequestBuilder builder,T value) throws IOException;

    /**
     * 迭代递归 针对数组参数，集合参数
     * @return
     */
    public ParameterHandler<Iterable<T>> iterable(){
        return new ParameterHandler<Iterable<T>>() {
            @Override
            void apply(RequestBuilder builder, Iterable<T> values) throws IOException {
                if (values == null)return;
                for (T v :
                        values) {
                    ParameterHandler.this.apply(builder,v);
                }
            }
        };
    }

    /**
     * 针对@Query这种参数注解的ParameterHandler
     * @param <T>
     */
    static final class Query<T> extends ParameterHandler<T>{

        private final String mQueryName;
        private final Converter<T, String> mValueConverter;
        private final boolean mUrlEncode;

        Query(String name, Converter<T,String> valueConverter, boolean urlEncode){
            this.mQueryName = name;
            this.mValueConverter = valueConverter;
            this.mUrlEncode = urlEncode;
        }

        @Override
        void apply(RequestBuilder builder, T value) throws IOException {
            if (mValueConverter == null) return;
            //添加请求参数
            builder.addQueryParams(mQueryName,mValueConverter.convert(value),mUrlEncode);

        }
    }
}
