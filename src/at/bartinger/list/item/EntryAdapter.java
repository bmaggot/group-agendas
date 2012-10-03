package at.bartinger.list.item;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.events.EventEditActivity;

public class EntryAdapter extends BaseAdapter implements Filterable{

	private List<Item> items;
	private List<Item> itemsAll;
	private LayoutInflater vi;
	
	private Context mContext;

	public EntryAdapter(Context context,List<Item> items) {
		mContext = context;
		this.items = items;
		this.itemsAll = items;
		vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		final Item i = items.get(position);
		if (i != null) {
			if(i.isSection()){
				SectionItem si = (SectionItem)i;
				v = vi.inflate(R.layout.list_item_section, null);

				v.setOnClickListener(null);
				v.setOnLongClickListener(null);
				v.setLongClickable(false);
				
				final TextView sectionView = (TextView) v.findViewById(R.id.list_item_section_text);
				sectionView.setText(si.getTitle());
			}else{
				final EntryItem ei = (EntryItem)i;
				v = vi.inflate(R.layout.list_item_entry, null);
				
				v.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(mContext, EventEditActivity.class);
						intent.putExtra("event_id", ei.event.getEvent_id());
						intent.putExtra("type", ei.event.getType());
						mContext.startActivity(intent);
					}
				});
				
				final TextView title = (TextView) v.findViewById(R.id.title);
				title.setText(ei.event.getTitle());
				
				
					final ImageView colorView = (ImageView) v.findViewById(R.id.colorView);
					
					int image = ei.event.getColorBubbleId(mContext);
					colorView.setImageResource(image);
				
				
				if (!ei.event.getIcon().equals("null")) {
					final ImageView iconView = (ImageView) v.findViewById(R.id.iconView);
					image = ei.event.getIconId(mContext);
					iconView.setImageResource(image);
				}
				
			}
		}
		return v;
	}
	
	@Override
	public Filter getFilter() {
		return new Filter() {
			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				items = (List<Item>) results.values;
				notifyDataSetChanged();
			}

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				List<Item> filteredResults = getFilteredResults(constraint);

				FilterResults results = new FilterResults();
				results.values = filteredResults;

				return results;
			}

			private List<Item> getFilteredResults(CharSequence constraint) {
				List<Item> items = EntryAdapter.this.itemsAll;
				List<Item> filtereItems = new ArrayList<Item>();
				EntryItem ei;
				for (int i = 0; i < items.size(); i++) {
					
					if(!items.get(i).isSection()){
						ei = (EntryItem)items.get(i);
						if(ei.event.getTitle().toLowerCase().startsWith(constraint.toString().toLowerCase())){
							if(items.get(i-1).isSection()){
								filtereItems.add(items.get(i-1));
							}
							filtereItems.add(items.get(i));
						}
					}
				}
				return filtereItems;
			}
		};
	}


	@Override
	public int getCount() {
		return items.size();
	}


	@Override
	public Object getItem(int pos) {
		return items.get(pos);
	}


	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

}
