package com.tokbox.android.demo.learningopentok;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.opentok.android.OpentokError;
import com.opentok.android.Session;
import com.opentok.android.Stream;


public class ChatActivity extends ActionBarActivity implements WebServiceCoordinator.Listener, Session.SessionListener {

    private static final String LOG_TAG = ChatActivity.class.getSimpleName();

    private WebServiceCoordinator mWebServiceCoordinator;

    private String mApiKey;
    private String mSessionId;
    private String mToken;
    private Session mSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

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

    private void initializeSession() {
        mSession = new Session(this, mApiKey, mSessionId);
        mSession.setSessionListener(this);
        mSession.connect(mToken);
    }

    /* Web Service Coordinator methods */

    @Override
    public void onSessionConnectionDataReady(String apiKey, String sessionId, String token) {
        mApiKey = apiKey;
        mSessionId = sessionId;
        mToken = token;

        initializeSession();
    }

    @Override
    public void onError(Exception error) {
        Log.e(LOG_TAG, error.getMessage());
    }

    /* Session Listener methods */

    @Override
    public void onConnected(Session session) {
        Log.i(LOG_TAG, "Session Connected");
    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(LOG_TAG, "Session Disconnected");
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.i(LOG_TAG, "Stream Received");
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(LOG_TAG, "Stream Dropped");
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.e(LOG_TAG, opentokError.getMessage());
    }
}
