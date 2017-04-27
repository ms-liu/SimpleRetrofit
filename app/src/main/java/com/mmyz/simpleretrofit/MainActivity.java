package com.mmyz.simpleretrofit;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mmyz.simpleretrofit.convert.DefaultConverterFactory;
import com.mmyz.simpleretrofit.http.GET;
import com.mmyz.simpleretrofit.http.Query;

public class MainActivity extends AppCompatActivity {

    private TextView tvContent;
    private ProgressBar pb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pb = (ProgressBar) findViewById(R.id.pb);
        tvContent = (TextView) findViewById(R.id.tv_content);
        loadData();
    }

    public void loadData() {
        Weather weather = RetrofitManager.create(Weather.class);
        Call<String> call = weather.getWeather("%E5%98%89%E5%85%B4&", "json", "5slgyqGDENN7Sy7pw29IUvrZ");
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                final String body = response.body();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pb.setVisibility(View.GONE);
                        tvContent.setVisibility(View.VISIBLE);
                        tvContent.setText(body);
                    }
                });

                System.out.println("==============="+body);
            }

            @Override
            public void onFailure(Call<String> call, final Throwable throwable) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pb.setVisibility(View.GONE);
                        tvContent.setVisibility(View.VISIBLE);
                        tvContent.setText(String.valueOf("异常："+throwable.toString()));
                    }
                });
                System.err.println("异常："+throwable.toString());
            }
        });
    }

    public interface Weather{
        @GET("/telematics/v3/weather")
        Call<String> getWeather(
                @Query("location")String city, @Query("output")String password, @Query("ak")String day);
    }

}
