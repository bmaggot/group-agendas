package com.groupagendas.groupagenda;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class GroupAgendasActivity extends Activity {
	public static final String LOAD_REMOTE_DATA = "load_remote_data";
	public static final String LOAD_LOCAL_DATA = "load_local_data";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		SharedPreferences prefs = getSharedPreferences("LATEST_CREDENTIALS", MODE_PRIVATE);
        
        if (!prefs.getBoolean("logged", false)) {
        	startActivity(new Intent(GroupAgendasActivity.this, LoginActivity.class));
            finish();
        } else {
        	Intent intent = new Intent(GroupAgendasActivity.this, NavbarActivity.class);
        	startActivity(intent);
            finish();
        }
        
        finish();
    }
}