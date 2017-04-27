package com.mmyz.simpleretrofit.convert;

import com.mmyz.simpleretrofit.Converter;

import java.io.IOException;

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

public class DefaultResponseConverter implements Converter<ResponseBody,String> {
    @Override
    public String convert(ResponseBody responseBody) throws IOException {
        return responseBody.string();
    }

}
