package com.loadingterminal;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    public String urlUsers = "http://188.166.253.236/index.php/User_Controller/users";


    LocalUserDBhandler db = new LocalUserDBhandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
