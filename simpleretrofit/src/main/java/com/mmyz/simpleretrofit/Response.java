package com.mmyz.simpleretrofit;

import okhttp3.Headers;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.ResponseBody;

/**
 * ==============================================
 * <p>
 * 类名：Response<T>
 *     封装OkHttp Response
 * <p>
 * 作者：M-Liu
 * <p>
 * 时间：2017/4/16
 * <p>
 * 邮箱：ms_liu163@163.com
 * <p>
 * ==============================================
 */

public class Response<T> {

    private static final String SUCCESS_MESSAGE ="ok";
    private static final String LOCAL_URL ="http://localhost/";

    //------------------------成功响应--------------------------------------------

    public static <T> Response<T> success(T body){
        return success(body,new okhttp3.Response.Builder()
                .code(200)
                .message(SUCCESS_MESSAGE)
                .protocol(Protocol.HTTP_1_1)
                .request(new Request.Builder().url(LOCAL_URL).build())
                .build());
    }

    public static <T> Response<T> success(T body, okhttp3.Response rawResponse) {
       if (rawResponse == null){
           throw new NullPointerException("rawResponse == null");
       }
        if (!rawResponse.isSuccessful()) {
            throw new IllegalArgumentException("innerResponse must be successful response");
        }
        return new Response<>(rawResponse, body, null);
    }
    //------------------------错误响应--------------------------------------------
    public static <T> Response<T> error(int errorCode,ResponseBody errorBody){
        return error(errorBody, new okhttp3.Response.Builder() //
                .code(errorCode)
                .protocol(Protocol.HTTP_1_1)
                .request(new Request.Builder().url(LOCAL_URL).build())
                .build());
    }

    public static <T> Response<T> error(ResponseBody errorBody,okhttp3.Response rawResponse){
        if (!rawResponse.isSuccessful()) {
            throw new IllegalArgumentException("innerResponse must be successful response");
        }
        return new Response<>(rawResponse,null,errorBody);
    }


    private final okhttp3.Response mInnerResponse;
    private final T mBody;
    private final ResponseBody mErrorBody;

    private Response(okhttp3.Response innerResponse, T body, ResponseBody errorBody) {
        this.mInnerResponse = innerResponse;
        this.mBody = body;
        this.mErrorBody = errorBody;
    }

    public int code(){
        return mInnerResponse.code();
    }

    public String message(){
        return mInnerResponse.message();
    }

    public Headers headers(){
        return mInnerResponse.headers();
    }

    public T body(){
        return mBody;
    }

    public ResponseBody errorBody(){
        return mErrorBody;
    }

    @Override
    public String toString() {
        return mInnerResponse.toString();
    }
}
