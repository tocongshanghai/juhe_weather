package com.tocong.juhe_weather_application;


import com.tocong.juhe_weather_application.R;
import com.tocong.juhe_weather_application.swiperefresh.PullToRefreshBase;
import com.tocong.juhe_weather_application.swiperefresh.PullToRefreshBase.OnRefreshListener;
import com.tocong.juhe_weather_application.swiperefresh.PullToRefreshScrollView;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ScrollView;

public class WeatherActivity extends Activity{
	
	 private PullToRefreshScrollView mPullToRefreshScrollView;
	    private ScrollView mScrollView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_weather);
		init();
	}
	
	 private void init() {
	        mPullToRefreshScrollView = (PullToRefreshScrollView) findViewById(R.id.pull_refresh_scrollview);
	        mPullToRefreshScrollView.setOnRefreshListener(new OnRefreshListener<ScrollView>() {

	            @Override
	            public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
	                // TODO Auto-generated method stub
	               
	                
	            }
	            
	        });

	        mScrollView = mPullToRefreshScrollView.getRefreshableView();
	        
	 }

}
