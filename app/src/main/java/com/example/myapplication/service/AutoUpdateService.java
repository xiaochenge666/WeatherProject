package com.example.myapplication.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;

import com.example.myapplication.json.Weather;
import com.example.myapplication.utils.HttpUtil;
import com.example.myapplication.utils.Utility;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //更新天气
        updateWeather();
        //更新背景
        updateBg();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 8 *60 *60 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this,0,i,0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return super.onStartCommand(intent, flags, startId);
    }


    //更新天气
    private void updateWeather(){
        SharedPreferences pf = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherStr = pf.getString("weather",null);

        Weather weather = Utility.handleWeatherRes(weatherStr);

        String weatherId = weather.basic.weatherId;
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=bc0418b57b2d4918819d3974ac1285d9";

        HttpUtil.sendOkHttpReq(weatherUrl, new Callback() {

            /*
             * 请求失败！
             * */
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();// 打印异常信息
            }
            /*
             * 请求成功！
             * */
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String res = response.body().string();
                Weather weather = Utility.handleWeatherRes(res);
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                editor.putString("weather",res);
                editor.apply();
            }
        });

    }
    //更新背景图
    private void updateBg(){
            String requestBingPic = "http://guolin.tech/api/bing_pic";
            HttpUtil.sendOkHttpReq(requestBingPic, new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String bingPic = response.body().string();
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                    editor.putString("bing_pic", bingPic);
                    editor.apply();
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }
            });

    }


}
