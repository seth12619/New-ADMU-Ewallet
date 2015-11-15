package com.loadingterminal;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import cz.msebera.android.httpclient.Header;

public class MainActivity2 extends AppCompatActivity {
    SharedPreferences sp;

    public String urlBal = "http://188.166.253.236/index.php/User_Controller/balance";

    public String idNum = "0";

    LocalUserDBhandler db = new LocalUserDBhandler(this);
    LocalLoadHandler ldb = new LocalLoadHandler(this);

    User user = new User();

    TextView tvBal;
    TextView tvID;
    TextView tvName;

    EditText etAddBal;

    boolean getBalance = false;

    String newBalTemp = "0";

    Double add = 0.9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setIdNum();
        new AsyncMethod().execute();
    }

    public void setIdNum() {
        tvBal = (TextView) findViewById(R.id.tv_bal);
        etAddBal = (EditText) findViewById(R.id.et_addBal);

        Intent intent = getIntent();
        idNum = intent.getStringExtra("idnum");
        user = db.getStudent(Integer.parseInt(idNum));
    }

    public void confirm(View view) {
        String temp = etAddBal.getText().toString();
        add = Double.parseDouble(temp);
        new AsyncMethod().execute();

        Intent intent0 = (Intent) new Intent(this, MainActivity3.class);
        startActivity(intent0);
    }

    public void addRow() {
        Date date = new Date();
        DateFormat df6 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStamp = df6.format(date);

        sp = this.getSharedPreferences("myPrefs", MODE_WORLD_READABLE);
        SharedPreferences.Editor editor = sp.edit();
        String dbPrimaryKey = sp.getString("PRIMARYKEY", "initial");
        if(dbPrimaryKey.equals("initial"))
        {
            int currPrimaryKey = ldb.generatePrimaryKey();
            String primaryKey = String.valueOf(currPrimaryKey);
            sp = this.getPreferences(Context.MODE_PRIVATE);

            editor.putString("PRIMARYKEY", primaryKey);
            editor.commit();
            //iterates until btdb gets highest primary Key
            while(ldb.checkExist(currPrimaryKey))
            {
                currPrimaryKey++;
            }
            LoadTransaction lt = new LoadTransaction(currPrimaryKey, add, timeStamp, Integer.parseInt(idNum),"001","false");
            ldb.addTransaction(lt);
            lt = ldb.getLoadTransaction(currPrimaryKey);

        } else {
            int currPrimaryKey = Integer.parseInt(dbPrimaryKey);
            //iterates until btdb gets highest primary Key
            while(ldb.checkExist(currPrimaryKey))
            {
                currPrimaryKey++;
            }

            ldb.setPrimaryKey(currPrimaryKey);
            LoadTransaction lt = new LoadTransaction(currPrimaryKey, add, timeStamp, Integer.parseInt(idNum),"001","false");
            ldb.addTransaction(lt);
            lt = ldb.getLoadTransaction(currPrimaryKey);

        }
    }

    // Shortcut btns
    public void twenty(View view) {
        etAddBal.setText("20");
    }

    public void fifty(View view) {
        etAddBal.setText("50");
    }

    public void hundred(View view) {
        etAddBal.setText("100");
    }


    //-------------------------------------------------------------------------------------------------------------------------------------------------
    /**
     * This makes that 'loading screen' you see in mobile online games and such lol, it also does some stuff in the background, thus not
     * 'crashing' the system
     */
    private class AsyncMethod extends AsyncTask<Void, Void, Void> {
        ProgressDialog pdL = new ProgressDialog(MainActivity2.this);

        /**
         * This is the UI loading screen
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pdL.setMessage("\tLoading...");
            pdL.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Intent intent = getIntent();
            idNum = intent.getStringExtra("idnum");

            if (getBalance == false) {
                try {
                    String link = urlBal;

                    RequestParams params = new RequestParams();
                    params.put("id_number", idNum);
                    SyncHttpClient client = new SyncHttpClient();
                    RequestHandle requestHandle = client.post(link, params, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            //is not called
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tvBal.setText("onSuccess");
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
                                        user = db.getStudent(Integer.parseInt(idNum));
                                        tvID = (TextView) findViewById(R.id.tv_idnum);
                                        tvName = (TextView) findViewById(R.id.tv_name);

                                        tvID.setText(String.valueOf(user.getID()));
                                        tvName.setText(user.getName());
                                        tvBal.setText(newBalTemp);
                                    }
                                });
                            } catch (JSONException e) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvBal.setText(new String(responseBody));
                                    }
                                });
                            }
                        }
                    });
                } catch (Exception e) {
                }
                getBalance = true;
            } else {
                String link = urlBal;
                Double newBal = add + Double.parseDouble(newBalTemp);


                RequestParams params = new RequestParams();
                params.put("id_number", idNum);
                params.put("new_balance", newBal);
                SyncHttpClient client = new SyncHttpClient();
                RequestHandle requestHandle = client.post(link, params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        //is not called
                    }

                    // Happens when there's an error 4xx, and this is the thing that gets called somehow... and it works.
                    @Override
                    public void onFailure(int statusCode, Header[] headers, final byte[] responseBody, Throwable error) {
                    addRow();
                    }
                });

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
