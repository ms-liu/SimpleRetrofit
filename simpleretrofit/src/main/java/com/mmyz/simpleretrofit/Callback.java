package com.mmyz.simpleretrofit;

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

public interface Callback<T> {
    /**
     * 请求响应回调
     */
    void onResponse(Call<T> call,Response<T> response);

    /**
     * 请求失败回调
     */
    void onFailure(Call<T> call,Throwable throwable);
}
