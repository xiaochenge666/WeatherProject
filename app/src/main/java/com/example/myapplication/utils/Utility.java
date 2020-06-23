package com.example.myapplication.utils;

import android.text.TextUtils;

import com.example.myapplication.db.City;
import com.example.myapplication.db.County;
import com.example.myapplication.db.Province;
import com.example.myapplication.json.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Utility {

    /*处理返回的省级数据*/
    public static boolean handelProvinceRes(String res){
        if (!TextUtils.isEmpty(res)){
            try {
                JSONArray allProvinces = new JSONArray(res);
                //遍历所有省级数据
                for (int i = 0;i<allProvinces.length();i++){
                    JSONObject obj = allProvinces.getJSONObject(i);
                    Province province = new Province();//实例化一个升级对象
                    province.setProvinceCode(obj.getInt("id"));
                    province.setProvinceName(obj.getString("name"));
                    province.save();//将此对象保存到数据库
                }
                return true;

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return false;

    }

    /*处理返回的市级数据*/
    public static boolean handelCityRes(String res,int provinceId){
        if (!TextUtils.isEmpty(res)){
            try {
                JSONArray allCity = new JSONArray(res);
                for (int i = 0;i<allCity.length();i++){
                    JSONObject obj = allCity.getJSONObject(i);
                    City city = new City();
                    city.setProvinceCode(provinceId);
                    city.setCityName(obj.getString("name"));
                    city.setCityCode(obj.getInt("id"));
                    city.save();//将此对象保存到数据库
                }
                return true;

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return false;

    }

    /*处理县级数据*/
    public static boolean handelCountryRes(String res,int cityId){
        if (!TextUtils.isEmpty(res)){
            try {
                JSONArray allCountry = new JSONArray(res);
                for (int i = 0;i<allCountry.length();i++){
                    JSONObject obj = allCountry.getJSONObject(i);
                    County country = new County();
                    country.setCityId(cityId);
                    country.setCountyName(obj.getString("name"));
                    country.setWeatherId(obj.getString("weather_id"));
                    country.save();//将此对象保存到数据库
                }
                return true;

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return false;

    }

    /*
    * 解析返回的json数据为实体类
    * */
    public static Weather handleWeatherRes(String res){

        try {
            JSONObject jsonObject = new JSONObject(res);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent,Weather.class); //将Json数据映射为Java实体类对象
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;

    }










}
