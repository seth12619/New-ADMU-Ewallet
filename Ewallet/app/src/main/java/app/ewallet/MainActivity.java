package app.ewallet;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import cz.msebera.android.httpclient.entity.StringEntity;

public class MainActivity extends ActionBarActivity {
    SharedPreferences sp;


    Boolean atLeastOne = false;
    String item1Name = "";
    String item2Name = "";
    String item3Name = "";
    String item4Name = "";
    String item1Price = "";
    String item2Price = "";
    String item3Price = "";
    String item4Price = "";
    //EditText itemEt1, itemEt2, itemEt3, itemEt4;
    //EditText qtyEt1, qtyEt2, QtyEt3, QtyEt4;

    public String url = "http://188.166.253.236/index.php/User_Controller/users";
    public String urlSync = "http://188.166.253.236/index.php/Buy_Transaction_Controller/sync";
    public String urlStock = "http://188.166.253.236/index.php/Stock_Controller/sync";
    public String urlItemOrder = "http://188.166.253.236/index.php/Item_Order_Controller/sync";

    Context context = this;

    //For handling the local DB purposes
    LocalShopHandler  dbShop = new LocalShopHandler(this);
    LocalDBhandler db = new LocalDBhandler(this);
    LocalBuyTransHandler btdb = new LocalBuyTransHandler(this);
    LocalStockHandler stdb = new LocalStockHandler(this);
    LocalitemOrder ioh = new LocalitemOrder(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        /**
         * Stub constructors for hard coded inner database for the ff:
         */
        Date date = new Date();
        DateFormat df6 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStamp = df6.format(date);

        //btdb.drop();
        //BuyTransaction bt0 = new BuyTransaction(1, timeStamp, 131356, 001);
        //btdb.addBuyTrans(bt0);
        //bt0 = new BuyTransaction(2, timeStamp, 131356, 001);
        //btdb.addBuyTrans(bt0);


        //Setting up of the Stock syncing (We basically send a stock report to the database)

        updateDatabase(dbShop);


        sp = this.getSharedPreferences("myPrefs", MODE_WORLD_READABLE);
        SharedPreferences.Editor editor = sp.edit();
        String dbPrimaryKey = sp.getString("PRIMARYKEY", "initial");
        if(dbPrimaryKey.equals("initial")) {
            btdb.drop();
            stdb.drop();
            int currPrimaryKey = stdb.generatePrimaryKey();

            Log.i("initial", dbPrimaryKey);
            Log.i("initialkey", String.valueOf(currPrimaryKey));

            try {
                JSONArray jaItem = new JSONArray(dbShop.getJson());
                int i = 0;
                while (i < jaItem.length()) {
                    JSONObject joItem = jaItem.getJSONObject(i);
                    sp = this.getPreferences(Context.MODE_PRIVATE);
                    Stock stock1 = new Stock(currPrimaryKey, "001", joItem.getInt("itemID"), timeStamp, joItem.getInt("qty"));
                    stdb.addStock(stock1);
                    currPrimaryKey += 1;
                    i++;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {
            Log.i("HAS LOCAL DB", dbPrimaryKey);
            /*
            stdb.drop();
            int currPrimaryKey = Integer.parseInt(dbPrimaryKey) + 1;
            int itemNo = 10001;


            while (dbShop.checkExist(itemNo)) {
                String primaryKey = String.valueOf(currPrimaryKey);
                Item item = dbShop.getItem(itemNo);
                sp = this.getPreferences(Context.MODE_PRIVATE);
                Stock stock1 = new Stock(currPrimaryKey, "001", item.getID(), timeStamp, item.getQty());
                stdb.addStock(stock1);
                currPrimaryKey += 1;
                itemNo++;
            }
            */
        }

/**
        ioh.drop();
        ItemOrder io = new ItemOrder(65, 101, 99);
        ItemOrder io1 = new ItemOrder(66, 103, 120);
        ioh.addItemOrder(io);
        ioh.addItemOrder(io1);
*/


        new AsyncMethod().execute();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case  R.id.action_sync: new AsyncMethod().execute();
                break;
            case R.id.action_showBalance: //put toast for Terminal's current balance here
                break;
            case R.id.action_editStock: Intent intent0 = new Intent(this, SubActivity.class);
                startActivity(intent0);
                break;
        }
        //noinspection SimplifiableIfStatement



        return super.onOptionsItemSelected(item);
    }

    /**
     * Updates the local database for items
     * @param db
     */
    public void updateDatabase(LocalShopHandler db) {
        Item item1 = new Item(101 , "Non-Colored Photocopy", 0.75, 100);
        Item item2 = new Item(102 , "Colored Photocopy", 3.50, 200);
        Item item3 = new Item(103 , "Printing", 4.00, 300);
        Item item4 = new Item(104 , "Colored Printing", 7.75, 400);
        Item item5 = new Item(105 , "Adobo Rice", 80.00, 500);

        if (!db.checkExist(item1.getID())) {
            dbShop.addItem(item1);
        } else { }
        if (!db.checkExist(item2.getID())) {
            dbShop.addItem(item2);
        } else { }
        if (!db.checkExist(item3.getID())) {
            dbShop.addItem(item3);
        } else { }
        if (!db.checkExist(item4.getID())) {
            dbShop.addItem(item4);
        } else { }
        if (!db.checkExist(item5.getID())) {
            dbShop.addItem(item5);
        } else { }
    }


    /**
     * Sends everything to next activity so that the next activity can use it
     * Called when Accept btn is clicked
     * @param view
     */
    public void onConfirmItems(View view) {
        Intent intent = new Intent(this, MainActivity2.class);


        EditText qtyEt1 = (EditText) findViewById(R.id.qty_editText1);
        EditText qtyEt2 = (EditText) findViewById(R.id.qty_editText2);
        EditText qtyEt3 = (EditText) findViewById(R.id.qty_editText3);
        EditText qtyEt4 = (EditText) findViewById(R.id.qty_editText4);
        String qty1 = qtyEt1.getText().toString();
        String qty2 = qtyEt2.getText().toString();
        String qty3 = qtyEt3.getText().toString();
        String qty4 = qtyEt4.getText().toString();


        EditText itemEt1 = (EditText) findViewById(R.id.item_editText1);
        EditText itemEt2 = (EditText) findViewById(R.id.item_editText2);
        EditText itemEt3 = (EditText) findViewById(R.id.item_editText3);
        EditText itemEt4 = (EditText) findViewById(R.id.item_editText4);
        String item1 = itemEt1.getText().toString();
        String item2 = itemEt2.getText().toString();
        String item3 = itemEt3.getText().toString();
        String item4 = itemEt4.getText().toString();


        if(!item1.equals(""))
        {
            int item1Int = Integer.parseInt(item1);
            Item actualItem1 = dbShop.getItem(item1Int);
            item1Name = actualItem1.getName();
            item1Price = String.valueOf(actualItem1.getCost());
        }
        else
        {
            item1Name = "";
            item1Price = "";
        }
        intent.putExtra("itemid1", item1);
        intent.putExtra("item1", item1Name);
        intent.putExtra("item1Price", item1Price);
        intent.putExtra("qty1", qty1);


        if(!item2.equals(""))
        {
            int item2Int = Integer.parseInt(item2);
            Item actualItem2 = dbShop.getItem(item2Int);
            item2Name = actualItem2.getName();
            item2Price = String.valueOf(actualItem2.getCost());
        }
        else
        {
            item2Name = "";
            item2Price = "";
        }
        intent.putExtra("itemid2", item2);
        intent.putExtra("item2", item2Name);
        intent.putExtra("item2Price", item2Price);
        intent.putExtra("qty2", qty2);

        if(!item3.equals(""))
        {
            int item3Int = Integer.parseInt(item3);
            Item actualItem3 = dbShop.getItem(item3Int);
            item3Name = actualItem3.getName();
            item3Price = String.valueOf(actualItem3.getCost());
        }
        else
        {
            item3Name = "";
            item3Price = "";
        }
        intent.putExtra("itemid3", item3);
        intent.putExtra("item3", item3Name);
        intent.putExtra("item3Price", item3Price);
        intent.putExtra("qty3", qty3);


        if(!item4.equals(""))
        {
            int item4Int = Integer.parseInt(item4);
            Item actualItem4 = dbShop.getItem(item4Int);
            item4Name = actualItem4.getName();
            item4Price = String.valueOf(actualItem4.getCost());
        }
        else
        {
            item4Name = "";
            item4Price = "";
        }
        intent.putExtra("itemid4", item4);
        intent.putExtra("item4", item4Name);
        intent.putExtra("item4Price", item4Price);
        intent.putExtra("qty4", qty4);

        startActivity(intent);

    }

    /**
     * This makes that 'loading screen' you see in mobile online games and such lol, it also does some stuff in the background, thus not
     * 'crashing' the system
     */
    private class AsyncMethod extends AsyncTask<Void, Void, Void> {
        ProgressDialog pdL = new ProgressDialog(MainActivity.this);

        /**
         * This is the UI loading screen
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pdL.setMessage("\tLoading...");
            pdL.show();
        }

        /**
         * These are the background tasks (ie. updating of the Database and shiz)
         * Note: Check if onSuccess or onFailure is working
         * @param voids
         * @return
         */
        @Override
        protected Void doInBackground(Void... voids) {

                String link = url;

                RequestParams params = new RequestParams();
                SyncHttpClient client = new SyncHttpClient();
                RequestHandle requestHandle = client.post(link, params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        //is not called

                    }

                    // Happens when there's an error 4xx, and this is the thing that gets called somehow... and it works.
                    @Override
                    public void onFailure(int statusCode, Header[] headers, final byte[] responseBody, Throwable error) {
                        try {
                            db.drop();
                            JSONArray ja = new JSONArray(new String(responseBody));
                            for (int i = 0; i < ja.length(); i++) {
                                JSONObject jo = ja.getJSONObject(i);
                                Student stud = new Student(Integer.parseInt(jo.getString("id_number")), jo.getString("last_name") + "," + jo.getString("first_name"), Integer.parseInt(jo.getString("pin")));
                                if (!db.checkExist(stud.getID())) {
                                    db.addStud(stud);
                                }
                            }

                        } catch (JSONException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                }
                            });
                        }
                    }
                });


            /**
             *
             * The one below is for syncing the Buy Transactions
             */
                final JSONArray ja = new JSONArray();
                JSONObject jo;
                int i = 10001;
            try {
                while (btdb.checkExist(i)) {

                        BuyTransaction tempBT = btdb.getBuyTransaction(i);
                        jo = new JSONObject();
                        jo.put("buy_transaction_id", tempBT.getTransID()); //must parse store number here at the start
                        jo.put("buy_transaction_ts", tempBT.getTimeStamp());
                        jo.put("id_number", tempBT.getIDNum());
                        jo.put("shop_terminal_id", tempBT.getShopID());
                        i++;
                        ja.put(jo);
                    }

            } catch (JSONException e) {

            }


                params = new RequestParams();
                params.put("params", ja.toString());

            requestHandle = client.post(urlSync, params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, final byte[] responseBody) {
                        //never called
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                EditText et = (EditText) findViewById(R.id.qty_editText3);
                                //et.setText((new String(responseBody)));
                                EditText itemEt4 = (EditText) findViewById(R.id.item_editText4);
                                //itemEt4.setText(ja.toString());

                            }
                        });
                    }
                    @Override
                    public void onFailure(int statusCode, Header[] headers, final byte[] responseBody, Throwable error) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                EditText et = (EditText) findViewById(R.id.qty_editText3);
                                et.setText((new String(responseBody)));
                                EditText itemEt4 = (EditText) findViewById(R.id.item_editText4);
                                itemEt4.setText(ja.toString());

                            }
                        });
                    }
                });

            /**
             * For syncing Stocks
            **/


            final JSONArray ja1 = new JSONArray();
            jo = new JSONObject();
            try {
                JSONArray stockJA = new JSONArray(stdb.getJson());
                int j = 0;
                while (j < stockJA.length()) {
                    JSONObject stockJO = stockJA.getJSONObject(j);
                    jo = new JSONObject();
                    jo.put("shop_terminal_id", "00" + stockJO.getInt("shopID"));
                    jo.put("item_id",  stockJO.getInt("itemID"));
                    jo.put("quantity", stockJO.getInt("qty"));
                    j++;
                    ja1.put(jo);
                }
            } catch (JSONException e) {

            }

            params = new RequestParams();
            params.put("params", ja1.toString());
            final String tomato = params.toString();

            requestHandle = client.post(urlStock, params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    //never called
                }
                @Override
                public void onFailure(int statusCode, Header[] headers, final byte[] responseBody, Throwable error) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            EditText et = (EditText) findViewById(R.id.qty_editText3);
                            //et.setText((new String(responseBody)));
                            EditText itemEt4 = (EditText) findViewById(R.id.item_editText4);
                         //   itemEt4.setText(ja1.toString());

                        }
                    });
                }
            });

            /**
             * For syncing ItemOrders
             */
/** delete this
            final JSONArray ja2 = new JSONArray();
            jo = new JSONObject();
            i = 10;
            try {
                while (btdb.checkExist(i)) {
                    Stock stock = sh.getStock(i);
                    jo = new JSONObject();
                    jo.put("shop_terminal_id", stock.getShopID());
                    jo.put("item_id", stock.getItemID());
                    jo.put("stock_ts", stock.getTimeStamp());
                    i++;
                    ja2.put(jo);
                }
            } catch (JSONException e) {

            }

            params = new RequestParams();
            params.put("params", ja2.toString());

            requestHandle = client.post(urlStock, params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    //never called
                }
                @Override
                public void onFailure(int statusCode, Header[] headers, final byte[] responseBody, Throwable error) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            EditText et = (EditText) findViewById(R.id.qty_editText3);
                        //    et.setText((new String(responseBody)));
                            EditText itemEt4 = (EditText) findViewById(R.id.item_editText4);
                         //   itemEt4.setText(ja2.toString());

                        }
                    });
                }
            }); Delete this **/

            return null;
        }

        /**
         * When everything is done; this gets rid of loading screen
         */
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            pdL.dismiss();
        }
    }
}