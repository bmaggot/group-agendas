package com.groupagendas.groupagenda.contacts;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.calendar.adapters.AbstractAdapter;
import com.groupagendas.groupagenda.utils.Utils;

public class GroupsAdapter extends AbstractAdapter<Group> implements Filterable {
	List<Group> allGroups;
	List<Group> selectedGroups;
	ArrayList<Integer> selection = new ArrayList<Integer>();

	public GroupsAdapter(List<Group> objects, Activity context, List<Group> selectedGroups) {
		super(context, objects);
		allGroups = objects;
		this.selectedGroups = selectedGroups;

		if (selectedGroups != null)
			for (Group g : selectedGroups) {
				toggleSelected(g.group_id);
			}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = new ViewHolder();

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_group_entry, null);
		}

		holder.contact = (TextView) convertView.findViewById(R.id.list_groups_contacts_number);
		holder.name = (TextView) convertView.findViewById(R.id.list_groups_name);
		holder.image = (ImageView) convertView.findViewById(R.id.group_icon);

		Group group = list.get(position);
		if (group.title != null)
			holder.name.setText(group.title);
		else
			holder.name.setText("");

		StringBuilder contactsText;
		if (group.contact_count == 1) {
			contactsText = new StringBuilder("1 ").append(getContext().getResources().getString(R.string.contact));
		} else if (group.contact_count % 10 == 1) {
			contactsText = new StringBuilder(String.valueOf(group.contact_count)).append(getContext().getResources().getString(
					R.string.contact));
		} else {
			contactsText = new StringBuilder(String.valueOf(group.contact_count)).append(" ").append(
					getContext().getResources().getString(R.string.contacts));
		}
		holder.contact.setText(contactsText.toString());

		convertView.setTag(String.valueOf(group.group_id));

		if (group.image && group.image_bytes != null) {
			Bitmap bitmap = Utils.getResizedBitmap(BitmapFactory.decodeByteArray(group.image_bytes, 0, group.image_bytes.length), 72, 72);
			if (bitmap != null)
				holder.image.setImageBitmap(bitmap);
		} else {
			holder.image.setImageResource(R.drawable.group_icon);
		}
		
		if (selection.contains(group.group_id)) {
			convertView.setSelected(true);
			convertView.setPressed(true);
			convertView.setBackgroundColor(Color.parseColor("#21C0DB"));
		} else {
			convertView.setSelected(false);
			convertView.setPressed(false);
			convertView.setBackgroundColor(Color.WHITE);
		}
		return convertView;
	}

	static class ViewHolder {
		TextView name;
		TextView contact;
		ImageView image;
	}

	@Override
	public Filter getFilter() {
		return new Filter() {
			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				list = (List<Group>) results.values;
				notifyDataSetChanged();
			}

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				List<Group> filteredResults = getFilteredResults(constraint);

				FilterResults results = new FilterResults();
				results.values = filteredResults;

				return results;
			}

			private List<Group> getFilteredResults(CharSequence constraint) {
				List<Group> items = allGroups;
				List<Group> filteredItems = new ArrayList<Group>();
				for (int i = 0; i < items.size(); i++) {
					if (items.get(i).title.toLowerCase().startsWith(constraint.toString().toLowerCase())) {
						filteredItems.add(items.get(i));
					}
				}

				if (filteredItems.size() < 1)
					filteredItems = items;

				return filteredItems;
			}
		};
	}
	
	public ArrayList<Integer> getSelection() {
		if (selection != null) {
			return selection;
		} else {
			return new ArrayList<Integer>();
		}
	}
	
	public ArrayList<Group> getSelected() {
		ArrayList<Group> selected = new ArrayList<Group>();
		
		for (Integer tmp : selection) {
			for (Group g : allGroups) {
				if (tmp == g.group_id) {
					selected.add(g);
				}
			}
		}
		
		return selected;
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}
	
	public boolean toggleSelected(Integer position) {
		if (selection.contains(position)) {
			selection.remove(position);
			return false;
		} else {
			selection.add(position);
			return true;
		}
	}

}
