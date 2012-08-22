package com.groupagendas.groupagenda.contacts;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.utils.Utils;

public class ContactsAdapter extends BaseAdapter implements Filterable {
	private List<Contact> contacts;
	private List<Contact> contactsAll;
	private LayoutInflater mInflater;

	public ContactsAdapter(List<Contact> objects, Activity activity) {

		contacts = objects;
		contactsAll = objects;

		mInflater = LayoutInflater.from(activity);
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_contact_entry, null);
			holder = new ViewHolder();
			holder.email = (TextView) convertView.findViewById(R.id.list_contacts_email);
			holder.name = (TextView) convertView.findViewById(R.id.list_contacts_name);
			holder.image = (ImageView) convertView.findViewById(R.id.contact_icon);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final Contact contact = contacts.get(position);
		StringBuilder sb = new StringBuilder(contact.name);
		if (contact.lastname != null && !contact.lastname.equals("null"))
			sb.append(" ").append(contact.lastname);
		holder.name.setText(sb.toString());

		/*
		 * This section of code is disabled in order not to display contact's
		 * email address.
		 */

		// if(contact.email != null && !contact.email.equals("null"))
		// holder.email.setText(contact.email);

		if (contact.image) {
			Bitmap bitmap = Utils.getResizedBitmap(BitmapFactory.decodeByteArray(contact.image_bytes, 0, contact.image_bytes.length), 72,
					72);
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
	}

	@Override
	public int getCount() {
		return contacts.size();
	}

	@Override
	public Object getItem(int position) {
		return contacts.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void setItems(List<Contact> items) {
		contacts = items;
		contactsAll = items;
	}

	@Override
	public Filter getFilter() {
		return new Filter() {
			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				contacts = (List<Contact>) results.values;
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
				List<Contact> items = ContactsAdapter.this.contactsAll;
				List<Contact> filtereItems = new ArrayList<Contact>();

				for (int i = 0; i < items.size(); i++) {
					if (items.get(i).name.toLowerCase().startsWith(constraint.toString().toLowerCase())
							|| items.get(i).lastname.toLowerCase().startsWith(constraint.toString().toLowerCase())) {
						filtereItems.add(items.get(i));
					}
				}

				return filtereItems;
			}
		};
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}

}
