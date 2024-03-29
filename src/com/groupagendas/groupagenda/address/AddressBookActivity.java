package com.groupagendas.groupagenda.address;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.metadata.impl.AddressMetaData.AddressTable;

public class AddressBookActivity extends ListActivity {
	private boolean action;
	public static long selectedAddressId = 0;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.address_book);
		
		action = getIntent().getBooleanExtra("action", false);
		
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent addressBookInfoIntent = new Intent(AddressBookActivity.this, AddressBookInfoActivity.class);
		if (v.getTag() != null) {
			String a_id = v.getTag().toString();
			String where = AddressTable._ID + " = " + a_id;
			Address address = AddressManagement.getAddressFromLocalDb(AddressBookActivity.this, where);
			if(address.getIdInternal() != Integer.valueOf(a_id)){
				// tipo refrech
				ArrayList<Address> addresses = AddressManagement.getAddressesFromLocalDb(AddressBookActivity.this, null);
				setListAdapter(new AddressAdapter(this, addresses));
				Toast.makeText(AddressBookActivity.this, AddressBookActivity.this.getString(R.string.address_list_refresh), Toast.LENGTH_SHORT).show();
			} else {
				if(!action){
					addressBookInfoIntent.putExtra("addressId", a_id);
					startActivity(addressBookInfoIntent);
				} else {
					selectedAddressId = Long.parseLong(a_id);
					Log.e("address id", ""+selectedAddressId);
					finish();
				}
			}
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		ArrayList<Address> addresses = AddressManagement.getAddressesFromLocalDb(AddressBookActivity.this, null);
		setListAdapter(new AddressAdapter(this, addresses));
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.addresses_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.add_address:
			Intent addressCreate = new Intent(AddressBookActivity.this, AddressBookInfoActivity.class);
			addressCreate.putExtra("action", false);
			startActivity(addressCreate);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
