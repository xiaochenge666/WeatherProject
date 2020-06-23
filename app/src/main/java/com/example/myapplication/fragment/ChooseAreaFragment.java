package com.example.myapplication.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.WeatherActivity;
import com.example.myapplication.db.City;
import com.example.myapplication.db.County;
import com.example.myapplication.db.Province;
import com.example.myapplication.utils.HttpUtil;
import com.example.myapplication.utils.Utility;

import org.jetbrains.annotations.NotNull;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
    private TextView titleText;
    private Button backBtn;
    private ListView listView;
    ArrayAdapter adapter;

    private List<Province> provinceList;//所有省级列表
    private List<City> cityList;//当前省级下的所有城市
    private List<County> countryList;//当前城市下的所有市
    private List<String> dataList = new ArrayList<>();

    private Province selectProvince;
    private City selectCity;


    /*
    * 等级定义
    * */

    private final static int LEVEL_PROVINCE = 0;
    private final static int LEVEL_CITY = 1;
    private final static int LEVEL_COUNTRY = 2;
    private int currentLevel;//当前所处的等级


    private ProgressDialog progressDialog;//对话框


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area,container,false);
        titleText = view.findViewById(R.id.title_text);
        backBtn = view.findViewById(R.id.back_btn);
        listView = view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter(getContext(),R.layout.place_item,dataList);
        listView.setAdapter(adapter);
        return view;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        /*
         * 当点击listView中的任意一个条目
         * */
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (currentLevel){
                    case LEVEL_PROVINCE:
                        selectProvince = provinceList.get(i);//设置当前选中的省
                        queryCitys();
                        break;
                    case LEVEL_CITY:
                        selectCity = cityList.get(i);//设置当前选中的市
                        queryCountries();
                        break;
                    case LEVEL_COUNTRY:
                        String weatherId = countryList.get(i).getWeatherId();
                        if(getActivity() instanceof MainActivity){
                            Intent intent = new Intent(getActivity(), WeatherActivity.class);
                            intent.putExtra("weather_id",weatherId);
                            startActivity(intent);
                            getActivity().finish();
                        }else if(getActivity() instanceof WeatherActivity){
                            WeatherActivity activity = (WeatherActivity) getActivity();
                            activity.drawerLayout.closeDrawers();
                            activity.swipeRefresh.setRefreshing(false);
                            activity.requestWeather(weatherId);
                        }

                        break;
                    default:
                }
            }
        });

        /*
         * 点击上一级按钮
         * */
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (currentLevel){
                    case LEVEL_COUNTRY:
                        //查询所有的城市数据
                        queryCitys();
                        break;
                    case LEVEL_CITY:
                        //查询所有省级数据
                        queryProvinces();
                        break;
                    default:

                }
            }
        });


        /*
        * 在第一次打开页面时，查询出所有省级信息
        * */
        queryProvinces();
    }

    /*
    * 根据传入的地址，类型查询信息
    * */
    private void queryFormService(String addr, final int type){
        showProgressDialog();
        HttpUtil.sendOkHttpReq(addr, new Callback() {
            /*
            * 请求失败
            * */
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败，请检查网络环境是否正常！",Toast.LENGTH_SHORT).show();
                    }
                });
            }
            /*
            * 请求成功！
            * */
            @Override
            public void onResponse(@NotNull final Call call, @NotNull Response response) throws IOException {
                String resText = response.body().string();
                boolean result = false;
                switch (type){
                    case LEVEL_PROVINCE:
                        result = Utility.handelProvinceRes(resText);
                        break;
                    case LEVEL_CITY:
                        result = Utility.handelCityRes(resText,selectProvince.getId());
                        break;
                    case LEVEL_COUNTRY:
                        result = Utility.handelCountryRes(resText,selectCity.getId());
                        break;
                }

                /*
                *如果查询成功，渲染到界面上去
                 *  */
                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            switch (type){
                                case LEVEL_PROVINCE:
                                    queryProvinces();
                                    break;
                                case LEVEL_CITY:
                                    //查询城市
                                    queryCitys();
                                    break;
                                case LEVEL_COUNTRY:
                                    //查询县
                                    queryCountries();
                                    break;
                            }
                        }
                    });

                }
            }
        });


    }

    /*
    * 查询全国所有省的数据（优先从数据库查）
    * */

    private void queryProvinces(){
        titleText.setText("中国");
        backBtn.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);

        if (provinceList.size()>0){//从数据库查
            dataList.clear();
            for (Province province:provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }else{ //请求服务器
            String addr = "http://guolin.tech/api/china";
            //请求服务器
            queryFormService(addr,LEVEL_PROVINCE);
        }






    }


    /*
    * 查询全市的数据
    * */
    private void queryCitys(){
        titleText.setText(selectProvince.getProvinceName());
        backBtn.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceCode = ?",String.valueOf(selectProvince.getId())).find(City.class);

        if (cityList.size()>0){//从数据库查
            dataList.clear();
            for (City city:cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }else{ //请求服务器
            int provinceCode = selectProvince.getProvinceCode();
            String addr = "http://guolin.tech/api/china/"+provinceCode;
            //请求服务器
            queryFormService(addr,LEVEL_CITY);
        }

    }


    /*
     * 查询全市的数据
     * */
    private void queryCountries(){
        titleText.setText(selectCity.getCityName());
        backBtn.setVisibility(View.VISIBLE);
        countryList = DataSupport.where("cityId = ?",String.valueOf(selectCity.getId())).find(County.class);

        if (countryList.size()>0){//从数据库查
            dataList.clear();
            for (County county:countryList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTRY;

        }else{ //请求服务器
            int provinceCode = selectProvince.getProvinceCode();
            int cityCode = selectCity.getCityCode();
            String addr = "http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            //请求服务器
            queryFormService(addr,LEVEL_COUNTRY);
        }
    }





    /*
    * 打开对话框
    * */
    private void showProgressDialog(){

        if (progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();

    }

    /*
    * 关闭对话框
    * */

    private void closeProgressDialog(){
        if (progressDialog != null){
            progressDialog.dismiss();
        }
    }







}
