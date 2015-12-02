package app.ewallet;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SubActivity2 extends AppCompatActivity {

    LocalShopHandler shdb = new LocalShopHandler(this);
    Button btn;

    TextView tv_itemNo;

    EditText itemName;
    EditText cost;
    EditText qty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        int itemNo = Integer.parseInt(intent.getStringExtra("itemNo"));
        tv_itemNo = (TextView) findViewById(R.id.tv_itemNo);
        tv_itemNo.setText(String.valueOf(itemNo));

        if (shdb.checkExist(itemNo)) {
            btn = (Button) findViewById(R.id.btn_add);
            btn.setText("Edit Item");
            setEditItem(itemNo);
        }
    }

    public void setEditItem(int itemNo) {
        itemName = (EditText) findViewById(R.id.et_itemName);
        cost = (EditText) findViewById(R.id.et_cost);
        qty = (EditText) findViewById(R.id.et_qty);

        Item item = shdb.getItem(itemNo);

        itemName.setText(item.getName());
        cost.setText(String.valueOf(item.getCost()));
        qty.setText(String.valueOf(item.getQty()));

    }

    public void addItem(View view) {
        itemName = (EditText) findViewById(R.id.et_itemName);
        cost = (EditText) findViewById(R.id.et_cost);
        qty = (EditText) findViewById(R.id.et_qty);

        Intent intent = getIntent();
        int itemNo = Integer.parseInt(intent.getStringExtra("itemNo"));

        if (shdb.checkExist(itemNo)) {
            shdb.updateItem(itemNo, Integer.parseInt(qty.getText().toString()));
           // shdb.updateItemName(itemNo, itemName.getText().toString());
           // shdb.updateCost(itemNo, Double.parseDouble(cost.getText().toString()));
            Intent intent0 = new Intent(this, MainActivity.class);
            startActivity(intent0);
        } else {
            Item item = new Item(itemNo, itemName.getText().toString(), Double.parseDouble(cost.getText().toString()), Integer.parseInt(qty.getText().toString()));
            shdb.addItem(item);
            Intent intent0 = new Intent(this, MainActivity.class);
            startActivity(intent0);
        }

    }

}
