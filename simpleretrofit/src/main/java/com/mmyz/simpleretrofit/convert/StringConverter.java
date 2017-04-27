package com.mmyz.simpleretrofit.convert;

import com.mmyz.simpleretrofit.Converter;

import java.io.IOException;

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

public class StringConverter implements Converter<Object,String> {
    static final StringConverter INSTANCE = new StringConverter();

    @Override
    public String convert(Object v) throws IOException {
        return v.toString();
    }
}
