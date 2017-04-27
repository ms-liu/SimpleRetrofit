package com.mmyz.simpleretrofit;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * ==============================================
 * <p>
 * 类名：
 * <p>
 * 作者：M-Liu
 * <p>
 * 时间：2017/4/24
 * <p>
 * 邮箱：ms_liu163@163.com
 * <p>
 * ==============================================
 */

public class RequestBuilder {

    private final String mMehod;
    private final HttpUrl mBaseUrl;
    private final Request.Builder mRequestBuild;
    private String mRelativeUrl;
    private HttpUrl.Builder mUrlBuilder;

    /**
     *
     * @param method 请求类型
     * @param baseUrl HttpUrl 基础请求连接
     * @param relativeUrl String 相对请求连接（API接口方法中注解添加）
     */
    RequestBuilder(String method, HttpUrl baseUrl, String relativeUrl){
        this.mMehod = method;
        this.mBaseUrl = baseUrl;
        this.mRelativeUrl = relativeUrl;
        this.mRequestBuild = new Request.Builder();
    }

    /**
     * 添加请求参数
     * @param queryName String 参数名
     * @param parameterValue String 参数值
     * @param encode boolean 是否需要编码
     */
    void addQueryParams(String queryName, String parameterValue, boolean encode) {
        if (mRelativeUrl != null){
            this.mUrlBuilder = mBaseUrl.newBuilder(mRelativeUrl);
            if (mUrlBuilder == null)
                throw new IllegalArgumentException("不能很好的组装出请求连接。BaseUrl:"
                        +mBaseUrl+",RelativeUrl:"+mRelativeUrl);
            mRelativeUrl = null;
        }

        if (encode){
            //通过编码形式添加参数
            mUrlBuilder.addEncodedQueryParameter(queryName,parameterValue);
        }else {
            //直接添加
            mUrlBuilder.addQueryParameter(queryName,parameterValue);
        }

    }

    public Request build() {
        HttpUrl url;
        HttpUrl.Builder urlBuilder = this.mUrlBuilder;
        if (urlBuilder != null){
            url = urlBuilder.build();
        }else {
            url = mBaseUrl.resolve(mRelativeUrl);
            if (url == null){
                throw new IllegalArgumentException("通过当前的地址不能很好的组装出请求连接。BaseUrl:"
                        +mBaseUrl+",RelativeUrl:"+mRelativeUrl);
            }
        }
        //简化 创建空RequestBody
        RequestBody requestBody = RequestBody.create(null, new byte[0]);
        return mRequestBuild
                .url(url)
                .method(mMehod,null)
                .build();

    }
}
