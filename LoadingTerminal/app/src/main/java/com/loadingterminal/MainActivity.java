package com.loadingterminal;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;

public class MainActivity extends AppCompatActivity {
    SharedPreferences sp;

    int currPrimaryKey;

    public String urlUsers = "http://188.166.253.236/index.php/User_Controller/users";
    public String urlLoad = "http://188.166.253.236/index.php/Load_Transaction_Controller/sync";


    LocalUserDBhandler db = new LocalUserDBhandler(this);
    LocalLoadHandler ldb = new LocalLoadHandler(this);

    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        sp = this.getSharedPreferences("myPrefs", MODE_WORLD_READABLE);
        SharedPreferences.Editor editor = sp.edit();
        String dbPrimaryKey = sp.getString("PRIMARYKEY", "initial");
        if(dbPrimaryKey.equals("initial")) {
            db.drop();
            ldb.drop();

            currPrimaryKey = ldb.generatePrimaryKey();
        } else {        }


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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sync) { new AsyncMethod().execute();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void scan(View view)
    {
        IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
        integrator.initiateScan();

    }
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            String re = scanResult.getContents();
            Log.d("code", re);
            EditText et = (EditText) findViewById(R.id.id_number);
            et.setText(re);

        }

    }
    /**
     * Called when confirm button is pressed
     * @param view
     */
    public void confirm(View view) {
        EditText ed = (EditText) findViewById(R.id.id_number);
        Intent intent0 = (Intent) new Intent(this, MainActivity2.class);
        String idNumber = ed.getText().toString();
        int idNumberint;
        if(idNumber.equals("")) {
            idNumberint = 0;
        } else {
            idNumberint = Integer.parseInt(idNumber);
        }

        try {
            User student = db.getStudent(idNumberint);
            if (student.getID() > 0) {
                intent0.putExtra("idnum", idNumber);
                startActivity(intent0);
            } else {
                Toast toast = Toast.makeText(this, "ID Number Not in Database", Toast.LENGTH_SHORT);
                toast.show();
            }
        } catch (Exception e)
        {
            Toast toast = Toast.makeText(this, "Wrong Input!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    //-------------------------------------------------------------------------------------------------------------------------------------------------
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

        @Override
        protected Void doInBackground(Void... voids) {
            String link = urlUsers;

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
                            User stud = new User(Integer.parseInt(jo.getString("id_number")), jo.getString("last_name") + "," + jo.getString("first_name"), Integer.parseInt(jo.getString("pin")));
                            if (!db.checkExist(stud.getID())) {
                                db.addStud(stud);
                            }
                        }
                    } catch (JSONException e) {
                    }
                }
            });

            /**
             * One below is for syncing the loadTransaction
             */
            final JSONArray ja = new JSONArray();
            JSONObject jo;
            int i = 0;
            try {
                final JSONArray btJA = new JSONArray(ldb.getJson());
                while (i < btJA.length()) {
                    JSONObject ldJO = btJA.getJSONObject(i);


                    if (ldJO.getString("sendStamp").equals("false")) {
                        jo = new JSONObject();
                        jo.put("load_transaction_id", ldJO.getInt("loadID")); //must parse store number here at the start
                        jo.put("amount_loaded", ldJO.getDouble("amount"));
                        jo.put("load_transaction_ts", ldJO.getString("ts"));
                        jo.put("id_number",ldJO.getString("idnum"));
                        jo.put("load_terminal_id", ldJO.getString("loadTerminal"));
                        i++;
                        ja.put(jo);
                        ldb.updateSendStamp(ldJO.getInt("loadID"),"potato");
                    } else {
                        i++;
                    }
                }

            } catch (JSONException e) {

            }

            link = urlLoad;

            ByteArrayEntity be = new ByteArrayEntity(("params=" + ja.toString()).getBytes());
            final String paramString = be.toString();
            requestHandle = client.post(null, link, be, "application/json",new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, final byte[] responseBody) {
                    //never called
                }
                @Override
                public void onFailure(int statusCode, Header[] headers, final byte[] responseBody, Throwable error) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast toast = Toast.makeText(context, "Send Success", Toast.LENGTH_SHORT);
                            toast.show();
                            EditText ed = (EditText) findViewById(R.id.id_number);
                            //ed.setText(ja.toString());

                        }
                    });
                }
            });




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
