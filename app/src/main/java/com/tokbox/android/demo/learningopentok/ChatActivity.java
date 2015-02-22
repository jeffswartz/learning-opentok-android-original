package com.tokbox.android.demo.learningopentok;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class ChatActivity extends ActionBarActivity implements WebServiceCoordinator.Listener {

    private WebServiceCoordinator mWebServiceCoordinator;

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

        // store references to UI elements
        mApiKeyTextView = (TextView) findViewById(R.id.api_key);
        mSessionIdTextView = (TextView) findViewById(R.id.session_id);
        mTokenTextView = (TextView) findViewById(R.id.token);

        // initialize WebServiceCoordinator and kick off request for necessary data
        mWebServiceCoordinator = new WebServiceCoordinator(this, this);
        mWebServiceCoordinator.fetchSessionConnectionData();
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

    @Override
    public void onSessionConnectionDataReady(String apiKey, String sessionId, String token) {
        mApiKey = apiKey;
        mSessionId = sessionId;
        mToken = token;

        updateTextViews();
    }

    private void updateTextViews() {
        mApiKeyTextView.setText(mApiKey);
        mSessionIdTextView.setText(mSessionId);
        mTokenTextView.setText(mToken);
    }

}
