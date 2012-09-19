package com.pass_retrieve;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import com.groupagendas.groupagenda.R;

/**
 * Created by IntelliJ IDEA.
 * User: brodjag
 * Date: 14.06.12
 * Time: 0:15
 * To change this template use File | Settings | File Templates.
 */
public class forgot_pass2 extends Activity {
    public Activity con;
    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        con=this;
        setContentView(R.layout.forgot_pass_2);

        
        findViewById(R.id.forgot_pass2_login_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                httpRequestes2 r=  new httpRequestes2(con);
                r.changePass();
            }
        });


        ((EditText) findViewById(R.id.forgot_pass2_new_pass2)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                httpRequestes2 r=  new httpRequestes2(con);
                r.changePass();
                return true;
                }
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }
        });
        
        
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            startActivity(new Intent(this, forgot_pass1.class));
            finish();
            // moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}