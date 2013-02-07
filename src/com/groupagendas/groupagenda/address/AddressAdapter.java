package com.groupagendas.groupagenda.address;

import java.util.List;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.calendar.adapters.AbstractAdapter;

public class AddressAdapter extends AbstractAdapter<Address> {
	List<Address> allAddresses;

	public AddressAdapter(Activity context, List<Address> objects) {
		super(context, objects);
		allAddresses = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = new ViewHolder();

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.address_book_item, null);
		}

		holder.title = (TextView) convertView.findViewById(R.id.text);

		Address address = list.get(position);

		
		holder.title.setText(address.getTitle());
		convertView.setTag(""+address.getIdInternal());
		return convertView;
	}

	static class ViewHolder {
		TextView title;
	}
	
}
