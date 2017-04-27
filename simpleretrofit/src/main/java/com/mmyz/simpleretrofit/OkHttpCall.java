package com.mmyz.simpleretrofit;

import com.mmyz.simpleretrofit.util.LogUtils;
import com.mmyz.simpleretrofit.util.Utils;

import java.io.IOException;

import okhttp3.Request;
import okhttp3.ResponseBody;

/**
 * ==============================================
 * <p>
 * 类名：OkHttpCall
 * &nbsp&nbsp封装OkHttp中的Call
 * <p>
 * 作者：M-Liu
 * <p>
 * 时间：2017/4/25
 * <p>
 * 邮箱：ms_liu163@163.com
 * <p>
 * ==============================================
 */

public class OkHttpCall<T> implements Call<T> {

    private final ServiceMethod<T, ?> mServiceMethod;
    private final Object[] mArgs;
    private okhttp3.Call mRawCall;

    OkHttpCall(ServiceMethod<T,?> serviceMethod, Object [] args){
        LogUtils.log("17、创建OkHttpCall对象");
        this.mServiceMethod = serviceMethod;
        this.mArgs = args;
    }

    @Override
    public Response<T> execute() throws IOException {

        okhttp3.Call call = null;
        synchronized (this){
            call = mRawCall;
            if (call == null){
                call = mRawCall = createRawCall();
            }
        }
        LogUtils.log("18、调用execute，内部调用OkHTTP3.Call的execute()");
        return parserResponse(call.execute());
    }

    @Override
    public void enqueue(final Callback<T> callback) {
        okhttp3.Call call = null;
        synchronized (this){
            call = mRawCall;
            if (call == null){
                try {
                    call = mRawCall = createRawCall();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (call == null){
            callback.onFailure(OkHttpCall.this,new Throwable("不能成功创建请求对象：call == null"));
            return;
        }
        LogUtils.log("18、调用enqueue(带有请求回调监听)，内部调用OkHTTP3.Call的enqueue()");
        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                callback.onFailure(OkHttpCall.this,e);
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                try {
                    //给一点点延时感
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Response<T> tResponse = parserResponse(response);
                callback.onResponse(OkHttpCall.this,tResponse);
            }
        });
    }

    @Override
    public boolean isExecuted() {
        return false;
    }

    @Override
    public void cancel() {

    }

    @Override
    public boolean isCancel() {
        return false;
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public Call<T> clone() {
        return new OkHttpCall<>(mServiceMethod,mArgs);
    }

    @Override
    public synchronized Request request()throws IOException {
        okhttp3.Call call = mRawCall;
        if (call != null){
            return call.request();
        }

        return (mRawCall = createRawCall()).request();
    }

    private Response<T> parserResponse(okhttp3.Response rawResponse) throws IOException {
        ResponseBody rawBody = rawResponse.body();
        int code = rawResponse.code();
        if (code < 200 || code >= 300) {
            try {
                // Buffer the entire body to avoid future I/O.
                ResponseBody bufferedBody = Utils.buffer(rawBody);
                return Response.error(bufferedBody, rawResponse);
            } finally {
                rawBody.close();
            }
        }

        if (code == 204 || code == 205) {
            rawBody.close();
            return Response.success(null, rawResponse);
        }
        LogUtils.log("19、调用ServiceMethod.toResponse()，将OkHttp.ResponseBody转换成期望的对象类");
        T body = mServiceMethod.toResponse(rawBody);
        return Response.success(body, rawResponse);
    }

    private okhttp3.Call createRawCall() throws IOException {
        Request request = mServiceMethod.toRequest(mArgs);
        okhttp3.Call call = mServiceMethod.mCallFactory.newCall(request);
        if (call == null)
            throw new NullPointerException("不能创建请求对象");
        return call;
    }
}
