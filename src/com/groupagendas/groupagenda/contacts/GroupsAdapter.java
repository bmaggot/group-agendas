package com.groupagendas.groupagenda.contacts;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.utils.Utils;

public class GroupsAdapter extends BaseAdapter implements Filterable{
	private List<Group> groups;
	private List<Group> groupsAll;
	private LayoutInflater mInflater;
	private Activity mActivity;

	public GroupsAdapter(List<Group> objects, Activity activity) {
		groups = objects;
		groupsAll = objects;
		mActivity = activity;
		mInflater = LayoutInflater.from(activity);
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_group_entry, null);
			holder = new ViewHolder();
			holder.contact = (TextView) convertView.findViewById(R.id.list_groups_contacts_number);
			holder.name = (TextView) convertView.findViewById(R.id.list_groups_name);
			holder.image = (ImageView) convertView.findViewById(R.id.group_icon);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		final Group group = groups.get(position);
		holder.name.setText(group.title);
		final int contactsCount = group.contact_count;
		final StringBuilder contactsText;
		if(contactsCount == 1){
			contactsText = new StringBuilder("1 ").append(mActivity.getString(R.string.contact));
		}else{
			contactsText = new StringBuilder(String.valueOf(contactsCount)).append(" ").append(mActivity.getString(R.string.contacts));
		}
		
		holder.contact.setText(contactsText.toString());
		if(group.image){
			Bitmap bitmap = Utils.getResizedBitmap(BitmapFactory.decodeByteArray(group.image_bytes, 0, group.image_bytes.length), 72, 72);
			holder.image.setImageBitmap(bitmap);
		}else{
			holder.image.setImageResource(R.drawable.group_icon);
		}
		
		return convertView;
	}
	
	public void setItems(List<Group> items){
		groups = items;
		groupsAll = items;
	}
	
	static class ViewHolder {
		TextView name;
		TextView contact;
		ImageView image;
	}
	
	@Override
	public int getCount() {
		return groups.size();
	}

	@Override
	public Object getItem(int position) {
		return groups.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public Filter getFilter() {
		return new Filter() {
            @SuppressWarnings("unchecked")
			@Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                groups = (List<Group>) results.values;
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
                List<Group> items = GroupsAdapter.this.groupsAll;
                List<Group> filtereItems = new ArrayList<Group>();
                for(int i=0;i< items.size();i++){
                    if(items.get(i).title.toLowerCase().startsWith(constraint.toString().toLowerCase())){
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
