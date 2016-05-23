package com.tocong.juhe_weather_application;

import com.thinkland.juheapi.common.CommonFun;

import android.app.Application;


public class WeatherApplication extends Application{

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		CommonFun.initialize(getApplicationContext());
		
	}


}
