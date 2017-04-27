package com.mmyz.simpleretrofit;

import com.mmyz.simpleretrofit.convert.DefaultConverterFactory;

/**
 * ==============================================
 * <p>
 * 类名：
 * <p>
 * 作者：M-Liu
 * <p>
 * 时间：2017/4/27
 * <p>
 * 邮箱：ms_liu163@163.com
 * <p>
 * ==============================================
 */

public class RetrofitManager {
    public static final String API_URL = "http://api.map.baidu.com";

    public static <T>T create(Class<T> clazz) {
        return new Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(new DefaultConverterFactory())
                .addCallAdapterFactory(DefaultCallAdapterFactory.INSTANCE)
                .build().create(clazz);
    }

}
