package com.loadingterminal;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class PreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre);

    }

    public void startButton(View view)
    {
        Intent intent = new Intent(this, MainActivity.class);
        EditText et = (EditText) findViewById(R.id.et_pin1);
        String input = et.getText().toString();
        if(input.equals("4321"))
        {
            startActivity(intent);
            this.finish();
        }
        else{
            Toast toast = Toast.makeText(this, "INVALID PIN", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
