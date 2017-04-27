package com.mmyz.simpleretrofit;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;

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

class Platform {
    /**
     * Platform一种环境只需要说明一次
     * 不需要多次去判断创建，所以采用静态常量来存储
     */
    private static final Platform PLATFORM = findPlatForm();

    public static Platform get(){
     return PLATFORM;
    }

    private static Platform findPlatForm() {
        try {
            Class.forName("android.os.Build");
            if (Build.VERSION.SDK_INT != 0){
                return new Android();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new Platform();
    }

    public Executor defaultCallbackExecutor(){
        return null;
    }


    static class Android extends Platform{
        @Override public Executor defaultCallbackExecutor(){
            return new MainThreadExecutor();
        }

        static class MainThreadExecutor implements Executor{
            private final Handler handler = new Handler(Looper.getMainLooper());

            @Override public void execute(Runnable runnable) {
                handler.post(runnable);
            }
        }
    }

}
