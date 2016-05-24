package com.tocong.juhe_weather_application.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.thinkland.juheapi.common.JsonCallBack;
import com.thinkland.juheapi.data.air.AirData;
import com.thinkland.juheapi.data.weather.WeatherData;
import com.tocong.juhe_weather_application.bean.FutureWeatherBean;
import com.tocong.juhe_weather_application.bean.HoursWeatherBean;
import com.tocong.juhe_weather_application.bean.PMBean;
import com.tocong.juhe_weather_application.bean.WeatherBean;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class WeatherService extends Service {
	private String city;
	private final String tag = "WeatherService";
	private WeatherServiceBinder binder = new WeatherServiceBinder();
	private boolean isRunning = false;
	private List<HoursWeatherBean> list;
	private PMBean pmBean;
	private WeatherBean weatherBean;
	private onParseCallBack callBack;

	private final int REPEAT_MSG = 0x01;
	private final int CALLBACK_OK = 0x02;
	private final int CALLBACK_ERROR = 0X04;

	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
				switch (msg.what) {
				case REPEAT_MSG:
					getCityWeather();
					sendEmptyMessageDelayed(REPEAT_MSG, 1000*60*30);
					break;
				case  CALLBACK_OK:
					if (callBack != null) {
						callBack.OnParserComplete(list, pmBean, weatherBean);
					}
					isRunning = false;
					break;
				case CALLBACK_ERROR:
					Toast.makeText(getApplicationContext(), "loading error", Toast.LENGTH_SHORT).show();
			
				}
		};

	};

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return binder;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		city = "北京";
		mHandler.sendEmptyMessage(REPEAT_MSG);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.i(tag, "onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}

	public void setCallBack(onParseCallBack callback) {
		this.callBack = callback;
	}

	public void removeCallBack() {
		callBack = null;
	}
	public void getCityWeather(String city) {
		this.city = city;
		getCityWeather();
	}
	public void getCityWeather() {
		if (isRunning) {
			return;
		}
		isRunning = true;
		final CountDownLatch countDownLatch = new CountDownLatch(3);
		WeatherData data = WeatherData.getInstance();
		data.getByCitys(city, 2, new JsonCallBack() {

			@Override
			public void jsonLoaded(JSONObject arg0) {
				// TODO Auto-generated method stub
				weatherBean = parserWeather(arg0);
				countDownLatch.countDown();
			}
		});
			data.getForecast3h(city,new JsonCallBack() {
				
				@Override
				public void jsonLoaded(JSONObject arg0) {
					// TODO Auto-generated method stub
					list=parserForecast3h(arg0);
					countDownLatch.countDown();
				}
			});
			AirData airData=AirData.getInstance();
			airData.cityAir(city, new JsonCallBack() {
				
				@Override
				public void jsonLoaded(JSONObject arg0) {
					// TODO Auto-generated method stub
					countDownLatch.countDown();
					pmBean=parserPM(arg0);
				}
			});
			new Thread(){
				public void run() {
					try {
						countDownLatch.await();
						mHandler.sendEmptyMessage(CALLBACK_OK);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						mHandler.sendEmptyMessage(CALLBACK_ERROR);
						return;
					}
					
					
					
				};
				
				
			}.start();
			
	}

	// 解析城市查询接口
	private WeatherBean parserWeather(JSONObject json) {
		WeatherBean bean = null;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
		try {
			int code = json.getInt("resultcode");
			int error_code = json.getInt("error_code");
			System.out.println(json.getString("reason"));
			if (error_code == 0 && code == 200) {
				JSONObject resultJson = json.getJSONObject("result");
				bean = new WeatherBean();

				// 当天
				JSONObject todayJson = resultJson.getJSONObject("today");
				bean.setCity(todayJson.getString("city"));
				bean.setUv_index(resultJson.getString("uv_index"));
				bean.setTemp(todayJson.getString("temperature"));
				bean.setWeather_id(todayJson.getJSONObject("weather_id")
						.getString("fa"));
				bean.setDressing_index(resultJson.getString("dressing_index"));
				bean.setWeather_str(todayJson.getString("weather"));
				// 当天实况天气sk
				JSONObject skJson = resultJson.getJSONObject("sk");
				bean.setWind(skJson.getString("wind_direction")
						+ skJson.getString("wind_strength"));
				bean.setNow_temp(skJson.getString("temp"));
				bean.setRelease(skJson.getString("time"));
				bean.setHumidity(skJson.getString("humidity"));

				// 未来天气
				Date date = new Date(System.currentTimeMillis());
				JSONArray futureArray = resultJson.getJSONArray("future");
				List<FutureWeatherBean> futureList = new ArrayList<FutureWeatherBean>();
				for (int i = 0; i < futureArray.length(); i++) {
					JSONObject futureJson = futureArray.getJSONObject(i);
					FutureWeatherBean futureBean = new FutureWeatherBean();
					Date datef = simpleDateFormat.parse(futureJson
							.getString("date"));
					if (!datef.after(date)) {
						continue;
					}
					futureBean.setTemp(futureJson.getString("temperature"));
					futureBean.setWeek(futureJson.getString("week"));
					futureBean.setWeather_id(futureJson.getJSONObject(
							"weather_id").getString("fa"));
					futureList.add(futureBean);
					if (futureList.size() == 3) {
						break;
					}
				}

				bean.setFutureList(futureList);

			} else {

				Toast.makeText(getApplicationContext(), "天气错误", 1).show();
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return bean;

	}

	// 解析三小时预报

	private List<HoursWeatherBean> parserForecast3h(JSONObject json) {
		List<HoursWeatherBean> list = null;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
				"yyyyMMddhhmmss");
		Date date = new Date(System.currentTimeMillis());
		int code;
		try {
			code = json.getInt("resultcode");
			int error_code = json.getInt("error_code");
			if (code == 200 && error_code == 0) {
				list = new ArrayList<HoursWeatherBean>();
				JSONArray resultArray = json.getJSONArray("result");
				for (int i = 0; i < resultArray.length(); i++) {
					JSONObject hourJson = resultArray.getJSONObject(i);
					Date hDate = simpleDateFormat.parse(hourJson
							.getString("sfdate"));
					if (!hDate.after(date)) {
						continue;
					}
					HoursWeatherBean bean = new HoursWeatherBean();
					bean.setWeather_id(hourJson.getString("weatherid"));
					bean.setTemp(hourJson.getString("temp1"));
					Calendar c = Calendar.getInstance();
					c.setTime(hDate);
					bean.setTime(c.get(Calendar.HOUR_OF_DAY) + "");
					list.add(bean);
					if (list.size() == 5) {
						break;
					}
				}

			} else {

				Toast.makeText(getApplicationContext(), "小时错误", 1).show();
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return list;

	}

	// 解析pm
	private PMBean parserPM(JSONObject json) {
		PMBean bean = null;
		try {
			int code = json.getInt("resultcode");
			int error_code = json.getInt("error_code");
			if (error_code == 0 && code == 200) {
				bean = new PMBean();
				JSONObject pmJSON = json.getJSONArray("result")
						.getJSONObject(0).getJSONObject("citynow");
				bean.setAqi(pmJSON.getString("AQI"));
				bean.setQuality(pmJSON.getString("quality"));

			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return bean;

	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.v(tag, "onDestory");
	}

	public interface onParseCallBack {

		public void OnParserComplete(List<HoursWeatherBean> list,
				PMBean pmBean, WeatherBean weatherBean);

	}

	public class WeatherServiceBinder extends Binder {
		public WeatherService getService() {
			return WeatherService.this;

		}

	}
}
