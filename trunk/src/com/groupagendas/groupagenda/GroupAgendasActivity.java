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
        
	    if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
	        return;
	    }

        if (!prefs.getBoolean("logged", false)) {
        	startActivity(new Intent(GroupAgendasActivity.this, LoginActivity.class));
            finish();
            return;
        }
        
        finish();
        return;
    }
}