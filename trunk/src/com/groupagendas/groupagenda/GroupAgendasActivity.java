package com.groupagendas.groupagenda;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class GroupAgendasActivity extends Activity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		SharedPreferences prefs = getSharedPreferences("LATEST_CREDENTIALS", MODE_PRIVATE);
	    if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
	        if (prefs.getBoolean("logged", false)) {
	        	finish();
	        	return;
	        } else {
	        	prefs.edit().putBoolean("logged", false).commit();
	        	startActivity(new Intent(GroupAgendasActivity.this, LoginActivity.class));
	            finish();
	            return;
	        }
	    } else {
	    	if(prefs.getBoolean("logged", false)){
	    		startActivity(new Intent(GroupAgendasActivity.this, NavbarActivity.class));
	    		finish();
	    	} else {
	        	startActivity(new Intent(GroupAgendasActivity.this, LoginActivity.class));
	            finish();
	    	}
            return;
	    }
    }
}