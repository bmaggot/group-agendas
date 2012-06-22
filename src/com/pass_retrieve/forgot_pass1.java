package com.pass_retrieve;

import android.app.Activity;
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
 * Date: 12.06.12
 * Time: 21:25
 * To get retrieve code to mail
 */
public class forgot_pass1 extends Activity {
    private Activity con;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        con=this;
        setContentView(R.layout.forgot_pass_1);
        findViewById(R.id.forgot_pass_send_pass).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { MonClick();}
        });

        ((EditText) findViewById(R.id.forgot_pass_emailText)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
           @Override
           public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
               if (i == EditorInfo.IME_ACTION_DONE) {
                   MonClick();
                   return true;
               }
               return false;
           }
       });
    }


    public void MonClick(){
        httpRequestes r=  new httpRequestes(con);
        String email=((EditText) (con.findViewById(R.id.forgot_pass_emailText))).getText().toString();
        if(email.equals(0) || email.length()<4 || !email.contains("@")) {r.showMessage("check email value"); return;}
        r.sendToEmail(email);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
           // startActivity(new Intent(this, LoginActivity.class));
            finish();
           // moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


}