package com.groupagendas.groupagenda.templates;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.calendar.adapters.AbstractAdapter;

/*
 * @author meska.lt@gmail.com
 * @version 0.7
 * @since	2012-09-24
 * 
 * Extended Adapter that is the bridge between a ListView and the TemplatesDialogData that
 * backs the list. Frequently that data comes from a Cursor, but that is not required. The
 * ListView can display any data provided that it is wrapped in a ListAdapter.
 */
public class TemplatesAdapter extends AbstractAdapter<TemplatesDialogData> {
	/**
	 * @author meska.lt@gmail.com
	 * @param context Ð object of a present activity;
	 * @param list Ð List of TemplatesDialogData objects.
	 * @since 2012-09-24
	 */
	public TemplatesAdapter(Context context, List<TemplatesDialogData> list) {
		super(context, list);
	}

	/**
	 * Get a TextView containing template's title and ID in the tag.
	 * 
	 * Method inflates a custom layout, sets templates title as text and id as a tag.
	 * 
	 * @author meska.lt@gmail.com
	 * @param i Ð Integer value of template title's index in the list of TemplaDialogData objects.
	 * @param view Ð 
	 * @param viewGroup Ð 
	 * @return TextView object containing submited id matching template's title as a text and template id as a tag.
	 */
	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		if (view == null) {
			view = mInflater.inflate(R.layout.template_dialog_item, null);
		}
		
		TemplatesDialogData dialogData = list.get(i);

		((TextView) view).setText(dialogData.getTitle());
		view.setTag(dialogData.getID());
		
		return view;
	}
}