package com.pass_retrieve;

import android.os.Bundle;
import android.widget.EditText;
import com.groupagendas.groupagenda.LoginActivity;
import com.groupagendas.groupagenda.R;

/**
 * Created by IntelliJ IDEA.
 * User: brodjag
 * Date: 19.06.12
 * Time: 0:21
 * To change this template use File | Settings | File Templates.
 */
public class login2_set extends LoginActivity {
    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((EditText) findViewById(R.id.login_emailText)).setText(getIntent().getStringExtra("mail"));
        ((EditText) findViewById(R.id.login_passwordText)).setText(getIntent().getStringExtra("pass"));
        new LoginTask().execute();
    }


}