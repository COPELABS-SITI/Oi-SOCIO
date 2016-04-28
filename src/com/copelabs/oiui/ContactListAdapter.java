package com.copelabs.oiui;

import java.util.ArrayList;
import java.util.List;

import com.copelabs.android.oi.R;
import com.copelabs.oiaidllibrary.UserDevice;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class ContactListAdapter extends ArrayAdapter<UserDevice> {

	private List<UserDevice> items;
	private Context mContext;
	
	private static class ViewHolder {
        private TextView itemView;
    }
	
	private ViewHolder viewHolder;
	
	public ContactListAdapter(Context mContext, List<UserDevice> items) {
	    super(mContext, android.R.layout.simple_list_item_1, items);
	    this.items = items;
	    this.mContext = mContext;
	}
	
	private View getCustomView(int position, View convertView, ViewGroup parent) {
		convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.view_spinner_item, parent, false);
		TextView itemView = (TextView) convertView.findViewById(R.id.text1);
		UserDevice mUserDevice = getItem(position);
		itemView.setText(mUserDevice.getDevName());
		itemView.setTag(mUserDevice.getDevAdd());
		return convertView;
	}
	
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return getCustomView(position, convertView, parent);
	}
	
	@Override
	public UserDevice getItem(int position) {
	    return items.get(position);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return getCustomView(position, convertView, parent);
	}
}
