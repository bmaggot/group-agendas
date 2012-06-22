package com.groupagendas.groupagenda;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.groupagendas.groupagenda.utils.Prefs;

public class GroupAgendasActivity extends Activity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Prefs prefs = new Prefs(this);
        
        if(prefs.getLogged()){
        	startActivity(new Intent(this, NavbarActivity.class));
        }else{
        	startActivity(new Intent(this, LoginActivity.class));
        }
        finish();
    }
}