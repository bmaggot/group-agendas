package com.groupagendas.groupagenda.settings;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.groupagendas.groupagenda.R;

public class SettingsAdapter extends BaseAdapter{
	private String[] mItems;
	private LayoutInflater mInflater;
	
	public SettingsAdapter(Activity activity, String[] items){
		mItems = items;
		mInflater = LayoutInflater.from(activity);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.settings_item, null);
			holder = new ViewHolder();
			holder.text = (TextView) convertView.findViewById(R.id.text);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.text.setText(mItems[position]);

		return convertView;
	}
	
	@Override
	public int getCount() {
		return mItems.length;
	}

	@Override
	public Object getItem(int pos) {
		return mItems[pos];
	}

	@Override
	public long getItemId(int pos) {
		return pos;
	}

	static class ViewHolder {
		TextView text;
	}

}
