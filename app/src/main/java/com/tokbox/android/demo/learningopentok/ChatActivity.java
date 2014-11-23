package com.tokbox.android.demo.learningopentok;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;


public class ChatActivity extends ActionBarActivity {

    private static final String CHAT_SERVER_URL = "http://192.168.1.8:5000";
    private static final String LOG_TAG = ChatActivity.class.getSimpleName();

    private String mApiKey;
    private String mSessionId;
    private String mToken;

    private TextView mSessionIdTextView;
    private TextView mApiKeyTextView;
    private TextView mTokenTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mApiKeyTextView = (TextView) findViewById(R.id.api_key);
        mSessionIdTextView = (TextView) findViewById(R.id.session_id);
        mTokenTextView = (TextView) findViewById(R.id.token);

        retrieveSessionConnectionData();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
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

    private void retrieveSessionConnectionData() {
        RequestQueue reqQueue = Volley.newRequestQueue(this);
        reqQueue.add(new JsonObjectRequest(Request.Method.GET, CHAT_SERVER_URL + "/", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    mApiKey = response.getString("apiKey");
                    mSessionId = response.getString("sessionId");
                    mToken = response.getString("token");

                    Log.i(LOG_TAG, mApiKey);
                    Log.i(LOG_TAG, mSessionId);
                    Log.i(LOG_TAG, mToken);

                    updateTextViews();

                } catch (JSONException e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO: handle errors
                Log.e(LOG_TAG, "Session Connection Data request failed");
                Log.e(LOG_TAG, error.toString());
            }
        }));
    }

    private void updateTextViews() {
        mApiKeyTextView.setText(mApiKey);
        mSessionIdTextView.setText(mSessionId);
        mTokenTextView.setText(mToken);
    }
}
