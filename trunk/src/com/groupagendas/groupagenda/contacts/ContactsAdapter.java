package com.groupagendas.groupagenda.contacts;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.calendar.adapters.AbstractAdapter;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.utils.DrawingUtils;
import com.groupagendas.groupagenda.utils.Utils;

public class ContactsAdapter extends AbstractAdapter<Contact> implements Filterable {
	private int bubbleHeightDP = 15;
	List<Contact> allContacts;

	public ContactsAdapter(List<Contact> objects, Activity context) {
		super(context, objects);
		allContacts = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = new ViewHolder();

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_contact_entry, null);
		}

		holder.email = (TextView) convertView.findViewById(R.id.list_contacts_email);
		holder.name = (TextView) convertView.findViewById(R.id.list_contacts_name);
		holder.image = (ImageView) convertView.findViewById(R.id.contact_icon);
		holder.color = (ImageView) convertView.findViewById(R.id.contact_color);

//		int contact_id = Integer.parseInt(convertView.getTag().toString());
		Contact contact = list.get(position);

		StringBuilder sb = new StringBuilder();
		if (contact.name != null && !contact.name.equals("null"))
			sb.append(contact.name);
		if (contact.lastname != null && !contact.lastname.equals("null")) {
			sb.append(" ");
			sb.append(contact.lastname);
		}
		holder.name.setText(sb.toString());

		if (context != null) {
			Bitmap bitmap = DrawingUtils.getColoredRoundRectangle(context, bubbleHeightDP, contact, false);
			holder.color.setBackgroundDrawable(new BitmapDrawable(bitmap));
		}

		/*
		 * This section of code is disabled in order not to display contact's
		 * email address.
		 */
		// if(contact.email != null && !contact.email.equals("null"))
		// holder.email.setText(contact.email);
		convertView.setTag("" + contact.contact_id);
		
		if (contact.contact_id == 0)
			holder.email.setText("" + contact.created);

		if (contact.image && contact.image_bytes != null) {
			Bitmap bitmap = Utils.getResizedBitmap(BitmapFactory.decodeByteArray(contact.image_bytes, 0, contact.image_bytes.length), 72, 72);
			if (bitmap != null)
				holder.image.setImageBitmap(bitmap);
		} else {
			holder.image.setImageResource(R.drawable.group_icon);
		}

		if (Data.newEventPar) {
			if (Data.selectedContacts.size() > 0) {
				for (int i = 0; i < Data.selectedContacts.size(); i++) {
					if (Data.selectedContacts.get(i).contact_id == contact.contact_id) {
						convertView.setBackgroundColor(-14565157);
						convertView.setDrawingCacheBackgroundColor(0);
						break;
					} else {
						convertView.setBackgroundColor(-1);
					}
				}
			}
		}

		return convertView;
	}

	static class ViewHolder {
		TextView name;
		TextView email;
		ImageView image;
		ImageView color;
	}

	@Override
	public Filter getFilter() {
		return new Filter() {
			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				list = (List<Contact>) results.values;
				notifyDataSetChanged();
			}

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				List<Contact> filteredResults = getFilteredResults(constraint);

				FilterResults results = new FilterResults();
				results.values = filteredResults;

				return results;
			}

			private List<Contact> getFilteredResults(CharSequence constraint) {
				List<Contact> items = allContacts;
				List<Contact> filteredItems = new ArrayList<Contact>();

				for (int i = 0; i < items.size(); i++) {
					if (items.get(i).name.toLowerCase().startsWith(constraint.toString().toLowerCase())
							|| items.get(i).lastname.toLowerCase().startsWith(constraint.toString().toLowerCase())) {
						filteredItems.add(items.get(i));
					}
				}

				if (filteredItems.size() < 1)
					filteredItems = items;

				return filteredItems;
			}
		};
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}

}
