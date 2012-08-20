package com.groupagendas.groupagenda.calendar.listnsearch;

import com.groupagendas.groupagenda.R;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;


public class ListnSearchView extends LinearLayout {

	private StandardArrayAdapter arrayAdapter;

	private SectionListAdapter sectionAdapter;

	private SectionListView listView;

	private LayoutInflater mInflater;

	public ListnSearchView(Context context) {
		this(context, null);
	}
	
	
public ListnSearchView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mInflater = LayoutInflater.from(context);
//		arrayAdapter = new StandardArrayAdapter(this, R.id.example_text_view, exampleArray);
//		sectionAdapter = new SectionListAdapter(mInflater, arrayAdapter);
//		listView = (SectionListView) findViewById(getResources().getIdentifier("section_list_view", "id", this.getClass().getPackage().getName()));
//		listView.setAdapter(sectionAdapter);
		
	}





	private class StandardArrayAdapter extends ArrayAdapter<SectionListItem> {

		private final SectionListItem[] items;

		public StandardArrayAdapter(final Context context, final int textViewResourceId, final SectionListItem[] items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}

		@Override
		public View getView(final int position, final View convertView, final ViewGroup parent) {
			View view = convertView;
			if (view == null) {
				final LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
// TODO				view = vi.inflate(R.layout.example_list_view, null);
			}
			final SectionListItem currentItem = items[position];
			if (currentItem != null) {
//	TODO			final TextView textView = (TextView) view.findViewById(R.id.example_text_view);
//				if (textView != null) {
//					textView.setText(currentItem.item.toString());
//				}
			}
			return view;
		}
	}

	SectionListItem[] exampleArray = { // Comment to prevent re-format
	new SectionListItem("Test 1 - A", "A"), //
			new SectionListItem("Test 2 - A", "A"), //
			new SectionListItem("Test 3 - A", "A"), //
			new SectionListItem("Test 4 - A", "A"), //
			new SectionListItem("Test 5 - A", "A"), //
			new SectionListItem("Test 6 - B", "B"), //
			new SectionListItem("Test 7 - B", "B"), //
			new SectionListItem("Test 8 - B", "B"), //
			new SectionListItem("Test 9 - Long", "Long section"), //
			new SectionListItem("Test 10 - Long", "Long section"), //
			new SectionListItem("Test 11 - Long", "Long section"), //
			new SectionListItem("Test 12 - Long", "Long section"), //
			new SectionListItem("Test 13 - Long", "Long section"), //
			new SectionListItem("Test 14 - A again", "A"), //
			new SectionListItem("Test 15 - A again", "A"), //
			new SectionListItem("Test 16 - A again", "A"), //
			new SectionListItem("Test 17 - B again", "B"), //
			new SectionListItem("Test 18 - B again", "B"), //
			new SectionListItem("Test 19 - B again", "B"), //
			new SectionListItem("Test 20 - B again", "B"), //
			new SectionListItem("Test 21 - B again", "B"), //
			new SectionListItem("Test 22 - B again", "B"), //
			new SectionListItem("Test 23 - C", "C"), //
			new SectionListItem("Test 24 - C", "C"), //
			new SectionListItem("Test 25 - C", "C"), //
			new SectionListItem("Test 26 - C", "C"), //
	};



//	@Override
//	public void onCreate(final Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.main);
		
//	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		MenuInflater inflater = getMenuInflater();
//		// TODO		inflater.inflate(R.menu.test_menu, menu);
//		return true;
//	}

//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//		case R.id.standard_list:
//			arrayAdapter = new StandardArrayAdapter(this, R.id.example_text_view, exampleArray);
//			sectionAdapter = new SectionListAdapter(getLayoutInflater(), arrayAdapter);
//			listView.setAdapter(sectionAdapter);
//			return true;
//		case R.id.empty_list:
//			arrayAdapter = new StandardArrayAdapter(this, R.id.example_text_view, new SectionListItem[] {});
//			sectionAdapter = new SectionListAdapter(getLayoutInflater(), arrayAdapter);
//			listView.setAdapter(sectionAdapter);
//			return true;
//		default:
//			return super.onOptionsItemSelected(item);
//		}
//	}
}