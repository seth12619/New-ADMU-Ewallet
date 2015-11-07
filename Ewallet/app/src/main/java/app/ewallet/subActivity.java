package app.ewallet;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

//This activity is where the edit stocks stuff should go to
public class SubActivity extends AppCompatActivity {

    EditText itemNum;
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


    }

    public void confirm(View view) {
        itemNum = (EditText) findViewById(R.id.et_itemNo);
        try {
            String original = itemNum.getText().toString();
            int origLength = original.length();
            int theItemNo = Integer.parseInt(itemNum.getText().toString());
            String stringItem = String.valueOf(theItemNo);
            int length = stringItem.length();
            if (length < 3 || length != origLength) {
                tv = (TextView) findViewById(R.id.tv_warning);
                Toast toast = Toast.makeText(this, "Invalid Input", Toast.LENGTH_SHORT);
                tv.setText("Please input a number that has 3 or more digits, and does not start with a 0");
            } else {
                //go to next activity
            }
        } catch (Exception e) {
            tv = (TextView) findViewById(R.id.tv_warning);
            Toast toast = Toast.makeText(this, "Invalid Input", Toast.LENGTH_SHORT);
            tv.setText("Please input a number that has 3 or more digits, and does not start with a 0");
        }
    }

}
