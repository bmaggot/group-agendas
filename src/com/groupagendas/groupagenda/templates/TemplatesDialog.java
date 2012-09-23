package com.groupagendas.groupagenda.templates;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.data.Data;

public class TemplatesDialog extends Dialog {
	/**
	 * Boolean if dialog is cancelable or not.
	 * @since 2012-09-24 
	 */
	private static final boolean CANCELABLE = false;
	
	/**
	 * Overloaded constructor of TemplatesDialog. Creates a titleless cancelable dialog with a
	 * custom template_dialog layout. Afterwards doesn't fill ListView with template titles.
	 * 
	 * @author meska.lt@gmail.com
	 * @param context Ð Current activity;
	 * @param listener Ð OnCancelListener method that will be executed during cancel of dialog. 
	 * @version 1.0
	 * @since 2012-09-24 
	 */
	public TemplatesDialog(Context context, OnCancelListener listener) {
		this(context, CANCELABLE, listener);
	}
	
	/**
	 * Overloaded constructor of TemplatesDialog. Creates a titleless dialog with a custom
	 * template_dialog layout. Afterwards doesn't fill ListView with template titles.
	 * 
	 * @author meska.lt@gmail.com
	 * @param context Ð Current activity;
	 * @param cancelable Ð If dialog should be cancelable;
	 * @param listener Ð OnCancelListener method that will be executed during cancel of dialog. 
	 * @version 1.0
	 * @since 2012-09-24 
	 */
	protected TemplatesDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.template_dialog);
	}
	
	/**
	 * Main constructor of TemplatesDialog. Creates a titleless dialog with a custom
	 * template_dialog layout. Afterwards fills ListView with template titles.
	 * 
	 * @author meska.lt@gmail.com
	 * @param context Ð Current activity.
	 * @param listener Ð OnCancelListener method that will be executed during cancel of dialog.
	 * @version 1.0
	 * @since 2012-09-24 
	 */
	public TemplatesDialog(Context context, TemplatesAdapter listAdapter) {
		super(context);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.template_dialog);
		
		ListView templateListView = (ListView) this.findViewById(R.id.template_list);

		if (listAdapter != null && templateListView != null) {
			templateListView.setAdapter(listAdapter);
			templateListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					int target_id = 0;

					try {
						target_id = Integer.parseInt(view.getTag().toString());
					} catch (Exception e) {
						Log.e("TemplatesDialog", "Didn't managed to get target's ID from View's tag.");
					}

					if (target_id > 0) {
						Data.templateInUse = target_id;
					}

					TemplatesDialog.this.dismiss();
				}
			});

			listAdapter.notifyDataSetChanged();
		}
		
	}	
}