package app.ewallet;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;

public class MainActivity4 extends ActionBarActivity {
    public String terminalID = "001";
    SharedPreferences sp;

    public String url = "http://188.166.253.236/index.php/User_Controller/balance";
    public String urlTerminalBalance = "";

    public String name = "0";
    String item1;
    String item2;
    String item3,item4,qty1,qty2,qty3,qty4;
    public boolean getBalance = false;

    TextView tvID;
    TextView tvBal;
    TextView tvTotCost;
    TextView resultingBalance;
    String newBalTemp = "0";

    //LocalStudent database handler
    LocalDBhandler db = new LocalDBhandler(this);

    //BuyTransaction database handler
    LocalBuyTransHandler btdb = new LocalBuyTransHandler(this);

    //LocalStock Handler
    LocalStockHandler stdb = new LocalStockHandler(this);

    //Local itemOrder handler
    LocalitemOrder iodb  = new LocalitemOrder(this);

    //What we need to update in place of stocks
    LocalShopHandler shdb = new LocalShopHandler(this);

    //LocalBalanceDB handler
    LocalBalanceHandler bdb = new LocalBalanceHandler(this);


    //tv_actualbalance, tv_balance

    // Progress Dialog
    private ProgressDialog pDialog;

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();

    ArrayList<HashMap<String, String>> productsList;

    // url to get all products list
    private static String url_all_products = "INPUT SITE PHP";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_PRODUCTS = "products";
    private static final String TAG_PID = "pid";
    private static final String TAG_NAME = "name";

    // products JSONArray
    JSONArray products = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);
        new AsyncMethod().execute();

        Intent intent = getIntent();
        item1 = intent.getStringExtra("itemid1");
        item2 = intent.getStringExtra("itemid2");
        item3 = intent.getStringExtra("itemid3");
        item4 = intent.getStringExtra("itemid4");
        qty1 = intent.getStringExtra("qty1");
        qty2 = intent.getStringExtra("qty2");
        qty3 = intent.getStringExtra("qty3");
        qty4 = intent.getStringExtra("qty4");

        Log.i("item1", item1);

        if(item2==null)
        {
            Log.i("item2", "item2null");
        }
        else {
            Log.i("item2", item2);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public void checkOut(View view)
    {

        Intent intent0 = getIntent();
        final String idNumber = intent0.getExtras().getString("idnum");

        Date date = new Date();
        DateFormat df6 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStamp = df6.format(date);

        //check first if db is initially started with starting primary key of 10, or db already has contents

        sp = this.getSharedPreferences("myPrefs", MODE_WORLD_READABLE);
        SharedPreferences.Editor editor = sp.edit();
        String dbPrimaryKey = sp.getString("PRIMARYKEY", "initial");
        if(dbPrimaryKey.equals("initial"))
        {
            int currPrimaryKey = btdb.generatePrimaryKey();
            String primaryKey = String.valueOf(currPrimaryKey);
            sp = this.getPreferences(Context.MODE_PRIVATE);

            editor.putString("PRIMARYKEY", primaryKey);
            editor.commit();
            //iterates until btdb gets highest primary Key
            while(btdb.checkExist(currPrimaryKey))
            {
                currPrimaryKey++;
            }
            BuyTransaction bt = new BuyTransaction(currPrimaryKey, timeStamp, Integer.parseInt(idNumber),"001","false");
            btdb.addBuyTrans(bt);
            bt = btdb.getBuyTransaction(currPrimaryKey);
            Log.i("FIRST TIME", dbPrimaryKey);
            Log.i("PrimaryKey", String.valueOf(bt.getTransID()));
            Log.i("Timestamp", bt.getTimeStamp());
            Log.i("idnumber", String.valueOf(bt.getIDNum()));
            Log.i("shopnumber", bt.getShopID());

            if(!item1.equals("")) {
                //This is the new way we update the stocks
                int itemID = Integer.parseInt(item1);
                Item item = shdb.getItem(itemID);
                //item.setQty(item.getQty()-Integer.parseInt(qty1));
                int itemqty = item.getQty()-Integer.parseInt(qty1);
                shdb.updateItem(itemID, itemqty);

                stdb.update(itemID, itemqty);
                //
                Log.i("ITEMQTY1", String.valueOf(shdb.getItem(itemID).getQty()));
                Log.i("STOCKQTY1", String.valueOf(stdb.getStock(itemID).getQty()));

                Stock stock1 = stdb.getStock(itemID);
                //Stock stock1 = stdb.getStock(currPrimaryKey);

                Log.i("StockPrimaryKey", String.valueOf(stock1.getPrim()));
                Log.i("shopid", stock1.getShopID());
                Log.i("stockitemid", String.valueOf(stock1.getItemID()));
                Log.i("stocktimestamp", stock1.getTimeStamp());
                Log.i("stockqty", String.valueOf(stock1.getQty()));


                ItemOrder itemOrder1 = new ItemOrder(currPrimaryKey, Integer.parseInt(item1), Integer.parseInt(qty1));
                iodb.addItemOrder(itemOrder1);
                itemOrder1 = iodb.getItemOrder(Integer.parseInt(item1));
                Log.i("IoPrimKey", String.valueOf(itemOrder1.getBuyTransID()));
                Log.i("ioitemId", String.valueOf(itemOrder1.getItemID()));
                Log.i("ioqty", String.valueOf(itemOrder1.getQty()));
            }

            if(!item2.equals("")) {
                //This is the new way we update the stocks
                int itemID = Integer.parseInt(item2);
                Item item = shdb.getItem(itemID);
                int itemqty = item.getQty()-Integer.parseInt(qty2);
                shdb.updateItem(itemID, itemqty);
                stdb.update(itemID, itemqty);
                ItemOrder itemOrder1 = new ItemOrder(currPrimaryKey, Integer.parseInt(item2), Integer.parseInt(qty2));
                iodb.addItemOrder(itemOrder1);
                itemOrder1 = iodb.getItemOrder(Integer.parseInt(item2));
            }
            if(!item3.equals("")) {
                //This is the new way we update the stocks
                int itemID = Integer.parseInt(item3);
                Item item = shdb.getItem(itemID);
                int itemqty = item.getQty()-Integer.parseInt(qty3);
                shdb.updateItem(itemID, itemqty);
                stdb.update(itemID, itemqty);
                ItemOrder itemOrder1 = new ItemOrder(currPrimaryKey, Integer.parseInt(item3), Integer.parseInt(qty3));
                iodb.addItemOrder(itemOrder1);
            }
            if(!item4.equals("")) {
                //This is the new way we update the stocks
                int itemID = Integer.parseInt(item4);
                Item item = shdb.getItem(itemID);
                int itemqty = item.getQty()-Integer.parseInt(qty4);
                shdb.updateItem(itemID, itemqty);

                stdb.update(itemID, itemqty);

                ItemOrder itemOrder1 = new ItemOrder(currPrimaryKey, Integer.parseInt(item4), Integer.parseInt(qty4));
                iodb.addItemOrder(itemOrder1);

            }
            


        }
        //db already has existing data
        else
        {
            int currPrimaryKey = Integer.parseInt(dbPrimaryKey);
            //iterates until btdb gets highest primary Key
            while(btdb.checkExist(currPrimaryKey))
            {
                currPrimaryKey++;
            }

            btdb.setPrimaryKey(currPrimaryKey);
            BuyTransaction bt = new BuyTransaction(currPrimaryKey, timeStamp, Integer.parseInt(idNumber),"001", "false");
            btdb.addBuyTrans(bt);
            bt = btdb.getBuyTransaction(currPrimaryKey);
            Log.i("NOT FIRST TIME", dbPrimaryKey);
            Log.i("PrimaryKey", String.valueOf(bt.getTransID()));
            Log.i("Timestamp", bt.getTimeStamp());
            Log.i("idnumber", String.valueOf(bt.getIDNum()));
            Log.i("shopnumber", bt.getShopID());
            Log.i("HEHE", "added to existing db");
            String primKey = String.valueOf(btdb.getPrimaryKey());
            editor.putString("PRIMARYKEY", primKey);
            editor.commit();

            if(!item1.equals("")) {
                //This is the new way we update the stocks
                int itemID = Integer.parseInt(item1);
                Item item = shdb.getItem(itemID);
                int itemqty = item.getQty()-Integer.parseInt(qty1);
                shdb.updateItem(item.getID(), itemqty);
                stdb.update(item.getID(), itemqty);
                //item.setQty(item.getQty()-Integer.parseInt(qty1));
                Log.i("ITEMQTY2", String.valueOf(shdb.getItem(itemID).getQty()));

                Log.i("STOCKQTY2", String.valueOf(stdb.getStock(itemID).getQty()));
                //

                Stock stock1 = stdb.getStock(itemID);

                Log.i("StockPrimaryKey", String.valueOf(stock1.getPrim()));
                Log.i("shopid", stock1.getShopID());
                Log.i("stockitemid", String.valueOf(stock1.getItemID()));
                Log.i("stocktimestamp", stock1.getTimeStamp());
                Log.i("stockqty", String.valueOf(stock1.getQty()));


                ItemOrder itemOrder1 = new ItemOrder(currPrimaryKey, Integer.parseInt(item1), Integer.parseInt(qty1));
                iodb.addItemOrder(itemOrder1);
                itemOrder1 = iodb.getItemOrder(Integer.parseInt(item1));
                Log.i("IoPrimKey", String.valueOf(itemOrder1.getBuyTransID()));
                Log.i("ioitemId", String.valueOf(itemOrder1.getItemID()));
                Log.i("ioqty", String.valueOf(itemOrder1.getQty()));
            }
            if(!item2.equals("")) {
                //This is the new way we update the stocks
                int itemID = Integer.parseInt(item2);
                Item item = shdb.getItem(itemID);
                int itemqty = item.getQty()-Integer.parseInt(qty2);
                shdb.updateItem(item.getID(), itemqty);
                stdb.update(item.getID(), itemqty);
                ItemOrder itemOrder1 = new ItemOrder(currPrimaryKey, Integer.parseInt(item2), Integer.parseInt(qty2));
                iodb.addItemOrder(itemOrder1);
            }
            if(!item3.equals("")) {
                //This is the new way we update the stocks
                int itemID = Integer.parseInt(item3);
                Item item = shdb.getItem(itemID);
                int itemqty = item.getQty()-Integer.parseInt(qty3);
                shdb.updateItem(item.getID(), itemqty);
                stdb.update(item.getID(), itemqty);
                ItemOrder itemOrder1 = new ItemOrder(currPrimaryKey, Integer.parseInt(item3), Integer.parseInt(qty3));
                iodb.addItemOrder(itemOrder1);

            }
            if(!item4.equals("")) {
                //This is the new way we update the stocks
                int itemID = Integer.parseInt(item4);
                Item item = shdb.getItem(itemID);
                int itemqty = item.getQty()-Integer.parseInt(qty4);
                shdb.updateItem(item.getID(), itemqty);
                stdb.update(item.getID(), itemqty);
                ItemOrder itemOrder1 = new ItemOrder(currPrimaryKey, Integer.parseInt(item4), Integer.parseInt(qty4));
                iodb.addItemOrder(itemOrder1);
            }

        }



        new AsyncMethod().execute();
        Intent intent = new Intent(this, MainActivity5.class);
        startActivity(intent);
    }

    public void exit(View view)
    {
    }

    /**
     * This makes that 'loading screen' you see in mobile online games and such lol, it also does some stuff in the background, thus not
     * 'crashing' the system
     */
    private class AsyncMethod extends AsyncTask<Void, Void, Void> {
        ProgressDialog pdL = new ProgressDialog(MainActivity4.this);
        /**
         * This is the UI loading screen
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pdL.setMessage("\tLoading... Waiting...");
            pdL.show();

            tvID = (TextView) findViewById(R.id.tvidnumber);
            tvBal = (TextView) findViewById(R.id.tv_actualbalance);
            tvTotCost = (TextView) findViewById(R.id.tv_cost);
            resultingBalance = (TextView) findViewById(R.id.resulting_balance);
        }

        /**
         * These are the background tasks (ie. updating of the Database and shiz)
         * @param voids
         * @return
         */
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        protected Void doInBackground(Void... voids) {
            Intent intent = getIntent();
            final String idNumber = intent.getExtras().getString("idnum");
            final String total = intent.getExtras().getString("total");
            final Student stud = db.getStudent(Integer.parseInt(idNumber));


            if (getBalance == false) {
                //String link = "https://posttestserver.com/post.php";
                try {
                    String link = url;

                    RequestParams params = new RequestParams();
                    params.put("id_number", idNumber);
                    SyncHttpClient client = new SyncHttpClient();
                    RequestHandle requestHandle = client.post(link, params, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            //is not called
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tvID.setText("onSuccess");
                                }
                            });
                        }

                        // Happens when there's an error 4xx, and this is the thing that gets called somehow... and it works.
                        @Override
                        public void onFailure(int statusCode, Header[] headers, final byte[] responseBody, Throwable error) {
                            try {
                                final String potato = new String(responseBody);
                                JSONObject jo = new JSONObject(potato);
                                final String balance = jo.getString("balance");
                                newBalTemp = balance;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvID.setText(potato);
                                        tvID.setText(stud.getName());
                                        //tvBal.setTextSize(40);
                                        tvBal.setText(balance);
                                        tvTotCost.setText(String.valueOf(total));
                                        Double dNewBal = Double.parseDouble(newBalTemp) - Double.parseDouble(total);
                                        resultingBalance.setText(String.valueOf(dNewBal));
                                    }
                                });
                            } catch (JSONException e) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvID.setText(new String(responseBody));
                                    }
                                });
                            }
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvID.setText("Dead");
                        }
                    });
                }

                getBalance = true;
            } else {
                try {
                    try {
                        String link = url;
                        final Double dTotal = Double.parseDouble(total);
                        Double dNewBal = Double.parseDouble(newBalTemp) - dTotal;
                        String newBal = String.valueOf(dNewBal);

                        RequestParams params = new RequestParams();
                        params.put("id_number", idNumber);
                        params.put("new_balance",dNewBal);
                        SyncHttpClient client = new SyncHttpClient();
                        RequestHandle requestHandle = client.post(link, params, new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                //Is not called
                            }

                            // Happens when there's an error 4xx, and this is the thing that gets called somehow... and it works.
                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                try {
                                    JSONArray tempJA = new JSONArray(bdb.getJson());
                                    JSONObject tempJO = tempJA.getJSONObject(0);
                                    Double oldBal = tempJO.getDouble("bal");
                                    bdb.updateBalance(001, oldBal+dTotal );
                                } catch (JSONException e) {

                                }
                            }
                        });

             /**
              * For syncing ShopTerminal Balance
              */

                    link = urlTerminalBalance;
                    JSONArray jaBal = new JSONArray(bdb.getJson());

                    JSONObject joBal = jaBal.getJSONObject(0);

                        params = new RequestParams();
                        params.put("shop_terminal_id", terminalID);
                        params.put("balance", joBal.getDouble("bal"));

                        client = new SyncHttpClient();
                        requestHandle = client.post(link, params, new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                //Is not called
                            }

                            // Happens when there's an error 4xx, and this is the thing that gets called somehow... and it works.
                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                //remote balance should have been updated
                            }
                        });
                    } catch (Exception e) {

                    }

                } catch (Exception e) {

                }
            }




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