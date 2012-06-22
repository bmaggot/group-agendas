package com.groupagendas.groupagenda.events;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class IconsAdapter extends BaseAdapter{
	private String[] mIcons;
	private Context mContext;
	
	public IconsAdapter(Context context, String[] icons) {
		mContext = context;
		mIcons = icons;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }
		
		int iconId = mContext.getResources().getIdentifier(mIcons[position], "drawable", "com.groupagendas.groupagenda");
		imageView.setImageResource(iconId);
        return imageView;
	}
	
	static class ViewHolder {
		TextView text;
		ImageView icon;
	}
	
	@Override
	public int getCount() {
		return mIcons.length;
	}

	@Override
	public Object getItem(int pos) {
		return mIcons[pos];
	}

	@Override
	public long getItemId(int pos) {
		return pos;
	}
}
