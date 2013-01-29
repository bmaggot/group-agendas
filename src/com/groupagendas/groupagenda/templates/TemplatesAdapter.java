package com.groupagendas.groupagenda.templates;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.calendar.adapters.AbstractAdapter;
import com.groupagendas.groupagenda.utils.DrawingUtils;

/*
 * @author meska.lt@gmail.com
 * @version 0.7
 * @since	2012-09-24
 * 
 * Extended Adapter that is the bridge between a ListView and the TemplatesDialogData that
 * backs the list. Frequently that data comes from a Cursor, but that is not required. The
 * ListView can display any data provided that it is wrapped in a ListAdapter.
 */
public class TemplatesAdapter extends AbstractAdapter<Template> {
	private static int COLOR_BUBBLE_DIAMETER;
	float density;

	/**
	 * @author meska.lt@gmail.com
	 * @param context
	 *            - object of a present activity;
	 * @param list
	 *            - List of TemplatesDialogData objects.
	 * @since 2012-09-24
	 */
	public TemplatesAdapter(Context context, List<Template> list) {
		super(context, list);
		density = context.getResources().getDisplayMetrics().density;
		COLOR_BUBBLE_DIAMETER = Math.round(12*density);
	}

	/**
	 * Get a TextView containing template's title and ID in the tag.
	 * 
	 * Method inflates a custom layout, sets templates title as text and id as a
	 * tag.
	 * 
	 * @author meska.lt@gmail.com
	 * @param i
	 *            - Integer value of template title's index in the list of
	 *            TemplaDialogData objects.
	 * @param view
	 *            -
	 * @param viewGroup
	 *            -
	 * @return TextView object containing submitted id matching template's title
	 *         as a text and template id as a tag.
	 */
	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		Template projection = list.get(i);
		
		if (view == null) {
			view = mInflater.inflate(R.layout.template_dialog_item, null);
			ListView.LayoutParams lParams = new ListView.LayoutParams(LayoutParams.FILL_PARENT, Math.round(40*density));
			view.setLayoutParams(lParams);
		}
		
		ViewHolder holder = new ViewHolder();
		holder.colorPlaceholder = (ImageView) view.findViewById(R.id.event_color_placeholder);
		holder.title = (TextView) view.findViewById(R.id.entry_title);
		holder.position = projection.getTemplate_id();
		view.setTag(holder);
		
		holder.title.setText(projection.getTemplate_title());
		holder.colorPlaceholder.setImageBitmap(
				DrawingUtils.getCircleBitmap(
						getContext(),
						COLOR_BUBBLE_DIAMETER, 
						COLOR_BUBBLE_DIAMETER, 
						projection.getColor(), 
						false
				)
		);

		return view;
	}
	
	class ViewHolder {
		int position;
		ImageView colorPlaceholder;
		TextView title;
	}
	
	/**
	 * 
	 * Returns corresponding template's internal id.
	 * 
	 * @param view - a convert view that contains corresponding template's id. 
	 * @return 
	 * 			If successful - returns integer greater than -1;
	 *  		If not - returns -1.
	 */
	public int getTemplateId(View view) {
		if (view.getTag() != null) {
			return ((ViewHolder) view.getTag()).position;
		} else {
			return -1;
		}
	}
}