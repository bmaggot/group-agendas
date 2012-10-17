package com.groupagendas.groupagenda.settings;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.groupagendas.groupagenda.R;

public class AutoColorAdapter extends BaseAdapter{
	private Context mContext;
	private LayoutInflater mInflater;
	private ArrayList<AutoColorItem> mItems;
	
	public AutoColorAdapter(Context context, ArrayList<AutoColorItem> items){
		mContext = context;
		mItems = items;
		mInflater = LayoutInflater.from(context);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.auto_color_item, null);
			holder = new ViewHolder();
			holder.text = (TextView) convertView.findViewById(R.id.text);
			holder.image = (ImageView) convertView.findViewById(R.id.image);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		final AutoColorItem item = mItems.get(position);
		
		holder.text.setText(item.keyword);
		
		if(!item.color.equals("")){
			String colorStr = "calendarbubble_"+item.color+"_";
			
			int iconId = mContext.getResources().getIdentifier(colorStr, "drawable", "com.groupagendas.groupagenda");
			holder.image.setImageResource(iconId);
		}

		return convertView;
	}
	
	static class ViewHolder {
		TextView text;
		ImageView image;
	}
	
	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public Object getItem(int pos) {
		return mItems.get(pos);
	}

	@Override
	public long getItemId(int pos) {
		return pos;
	}

}