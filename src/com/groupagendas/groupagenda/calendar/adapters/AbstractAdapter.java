package com.groupagendas.groupagenda.calendar.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class AbstractAdapter<T> extends BaseAdapter {
    protected Context context;
    protected LayoutInflater mInflater;
    protected List<T> list;
    
    public AbstractAdapter(Context context, List<T> list) {
        this.context = context;
        this.list = list;
        mInflater = LayoutInflater.from(context);
    }
    
	@Override
	public int getCount() {
	       if (list != null) {
	            return list.size();
	        }
	        return 0;
	}

	@Override
	public Object getItem(int i) {
        if (list != null && list.size() >= i) {
            return list.get(i);
        }
        return null;
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	@Override
	public abstract View getView(int i, View view, ViewGroup viewGroup); 

	public void setContext(Context context) {
		this.context = context;
	}

	public void setList(List<T> list) {
		this.list = list;
	}

	public Context getContext() {
		return context;
	}

	
}
