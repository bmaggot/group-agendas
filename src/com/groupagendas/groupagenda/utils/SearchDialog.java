package com.groupagendas.groupagenda.utils;

import android.app.Activity;
import android.app.Dialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.groupagendas.groupagenda.R;

public class SearchDialog extends Dialog {

	public SearchDialog(Activity context, int styleResId, final ListAdapter adapter, final Object destination) {
		super(context);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.search_dialog);
		
		TextWatcher filterTextWatcher = new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s != null) {
					((ArrayAdapter<?>) adapter).getFilter().filter(s);
				}
			}
		};

		EditText dialogSearch = (EditText) findViewById(R.id.dialog_search);
		dialogSearch.addTextChangedListener(filterTextWatcher);
		
		ListView dialogList = (ListView) findViewById(R.id.dialog_list);
		dialogList.setAdapter(adapter);
		dialogList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				((Spinner) destination).setSelection(position);
				SearchDialog.this.dismiss();
			}
		});		
		
		
	}
}
