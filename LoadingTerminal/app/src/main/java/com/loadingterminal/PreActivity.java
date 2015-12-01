package com.loadingterminal;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class PreActivity extends AppCompatActivity {

    public String urlLoadPin = "http://188.166.253.236/index.php/Load_Terminal_Controller/pin";
    public String terminalID = "001";

    Context context = this;

    //This terminal's pin
    public String shopPin = "ErrorX5C";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        new AsyncMethod().execute();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_preact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_sync:
                new AsyncMethod().execute();
                break;
            default: break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void startButton(View view)
    {
        Intent intent = new Intent(this, MainActivity.class);
        EditText et = (EditText) findViewById(R.id.et_pin1);
        String input = et.getText().toString();
        if(input.equals(shopPin) && !(input.equals("ErrorX5C")))
        {
            startActivity(intent);
            this.finish();
        }
        else if(input.equals("ErrorX5C")) {
            Toast toast = Toast.makeText(this, "Connection Failure", Toast.LENGTH_SHORT);
            toast.show();
        }
        else{
            Toast toast = Toast.makeText(this, "INVALID PIN ", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private class AsyncMethod extends AsyncTask<Void, Void, Void> {
        ProgressDialog pdL = new ProgressDialog(PreActivity.this);

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


            String link = urlLoadPin;

            RequestParams params = new RequestParams();
            params.put("load_terminal_id", terminalID);
            SyncHttpClient client = new SyncHttpClient();
            RequestHandle requestHandle = client.post(link, params, new AsyncHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    //never called
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    try {
                        final String potato = new String(responseBody);
                        JSONArray ja = new JSONArray(potato);
                        JSONObject jo = ja.getJSONObject(0);

                        final String thePin = jo.getString("pin");
                        shopPin = thePin;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast toast = Toast.makeText(context, "Connect Success", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        });

                    } catch (Exception e) {
                        shopPin = "ErrorX5C";
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast toast = Toast.makeText(context, "Connecion Error", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        });
                    }

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
