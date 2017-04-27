package com.mmyz.simpleretrofit;

import com.mmyz.simpleretrofit.convert.DefaultConverterFactory;
import com.mmyz.simpleretrofit.http.GET;
import com.mmyz.simpleretrofit.http.Query;

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

public class Test {

    public static final String API_URL = "http://api.map.baidu.com";

    public interface Weather{
        @GET("/telematics/v3/weather")
        Call<String> getWeather(
                @Query("location")String city,@Query("output")String password,@Query("ak")String day);
    }

    public static void main(String []args) throws Exception {
        //new出构建Retrofit的Builder器
        Retrofit.Builder builder = new Retrofit.Builder();

        //添加请求BaseURL、转换工厂、请求适配工厂、 调用构建方法，构建出Retrofit对象
        Retrofit retrofit = builder
                    .baseUrl(API_URL)
                    .addConverterFactory(new DefaultConverterFactory())
                    .addCallAdapterFactory(DefaultCallAdapterFactory.INSTANCE)
                    .build();

        //Retrofit调用create对象，创建API接口对象
        Weather weather = retrofit.create(Weather.class);

        //调用API接口中的方法，获取到Call对象
        Call<String> call = weather.getWeather("%E5%98%89%E5%85%B4&", "json", "5slgyqGDENN7Sy7pw29IUvrZ");

        //调用Call的请求方法，
        call.enqueue(new Callback<String>() {
            //得到相应结果
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                String body = response.body();
                System.out.println("==============="+body);
            }
            //得到失败结果
            @Override
            public void onFailure(Call<String> call, Throwable throwable) {
                System.err.println("异常："+throwable.toString());
            }
        });
    }
}
