package com.mmyz.simpleretrofit;

import java.io.IOException;

import okhttp3.Request;


/**
 * ==============================================
 * <p>
 * 类名：
 * <p>
 * 作者：M-Liu
 * <p>
 * 时间：2017/4/16
 * <p>
 * 邮箱：ms_liu163@163.com
 * <p>
 * ==============================================
 */

public interface Call<T> extends Cloneable {

    /**
     * 异步发送网络请求
     */
    Response<T> execute()throws IOException;

    /**
     * 异步发送网络请求，并获取结果回调
     */
    void enqueue(Callback<T> callback);

    /**
     * 是否发送请求
     */
    boolean isExecuted();

    /**
     * 取消请求
     */
    void cancel();

    /**
     * 是否取消
     */
    boolean isCancel();

    /**
     * 如果当前Call已经存在，也去创建一个独立的Call对象
     */
    Call<T> clone();

    /**
     * 做基本网络请求（扩展）
     */
    Request request() throws IOException;
}
