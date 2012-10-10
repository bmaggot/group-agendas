package com.groupagendas.groupagenda.events;

import com.groupagendas.groupagenda.utils.DrawingUtils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class ColorsAdapter extends BaseAdapter{
	private String[] mColors;
	private Context mContext;
	
	public ColorsAdapter(Context context, String[] colors) {
		mContext = context;
		mColors = colors;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(30, 54));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }
        
        String nameColor = "calendarbubble_"+mColors[position]+"_";
		int image = mContext.getResources().getIdentifier(nameColor, "drawable", "com.groupagendas.groupagenda");
        
        imageView.setImageBitmap(DrawingUtils.getColoredRoundRectangle(mContext, 20, mColors[position], false));
        return imageView;
	}
	
	static class ViewHolder {
		TextView text;
		ImageView colorView;
	}
	
	@Override
	public int getCount() {
		return mColors.length;
	}

	@Override
	public Object getItem(int pos) {
		return mColors[pos];
	}

	@Override
	public long getItemId(int pos) {
		return pos;
	}
}