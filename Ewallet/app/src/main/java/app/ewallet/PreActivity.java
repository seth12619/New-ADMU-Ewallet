package app.ewallet;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class PreActivity extends AppCompatActivity {

    public String urlShopPin = "http://188.166.253.236/index.php/Shop_Terminal_Controller/pin";
    public String terminalID = "001";

    Context context = this;

    //This terminal's pin
    public String shopPin = "ErrorX5C";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre);

        new AsyncMethod().execute();

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
            Toast toast = Toast.makeText(this, "INVALID PIN " + shopPin, Toast.LENGTH_SHORT);
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


                String link = urlShopPin;

                RequestParams params = new RequestParams();
                params.put("shop_terminal_id", terminalID);
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

                        } catch (Exception e) {
                        shopPin = "ErrorX5C";
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
