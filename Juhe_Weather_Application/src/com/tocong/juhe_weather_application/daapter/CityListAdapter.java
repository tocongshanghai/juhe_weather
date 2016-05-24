package com.tocong.juhe_weather_application.daapter;

import java.util.List;

import com.tocong.juhe_weather_application.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class CityListAdapter extends BaseAdapter{
List<String> list;
private LayoutInflater  layoutInflater;
public CityListAdapter(Context context,List<String> list) {
	// TODO Auto-generated constructor stub
	this.list = list;
	layoutInflater = LayoutInflater.from(context);
}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public String getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		View view=null;
		if (convertView==null) {
		view=	layoutInflater.inflate(R.layout.item_city_list,null);
		}else{
			view=convertView;
			
		}
		
		TextView tv_cityTextView=(TextView) view.findViewById(R.id.tv_city);
		tv_cityTextView.setText(getItem(position));
		
		return view;
	}

}
