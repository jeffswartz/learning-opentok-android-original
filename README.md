Learning OpenTok Android Sample App
===================================

This sample app shows how to accomplish basic tasks using the [OpenTok Android SDK] [1].
It connects the user with another client so that they can share an OpenTok audio-video
chat session. The app uses the OpenTok Android SDK to implement the following:

* Connect to an OpenTok session
* Publish an audio-video stream to the session
* Subscribe to another client's audio-video stream
* Implement text chat
* Record the session, stop the recording, and view the recording

The code for this sample is found the following git branches:

* *step-0* -- This branch shows you how to set up your project to use the OpenTok Android SDK.

* *step-3* -- This branch shows you how to connect to the OpenTok session.

* *step-4* -- This branch shows you how publish a stream to the OpenTok session.

* *step-5* -- This branch shows you how to subscribe to a stream on the OpenTok session.

* *archiving* -- This branch shows you how to record the session.

* *signaling.step-1* -- This branch shows you how to use the OpenTok signaling API.

* *signaling.step-2* -- This branch shows you how to impliment text chat using the OpenTok
signaling API.

* *signaling.step-3* -- This branch adds some UI improvements for the text chat feature.

You will also need to clone the OpenTok PHP Getting Started repo and run its code on a
PHP-enabled web server. See the next section for more information.


## 0: Starting Point

The step-0 branch includes a basic Android application that already includes the OpenTok Android SDK
and any settings the app needs to get running.

1. In Android Studio, select the File > Open command. Navigate to the root directory of this
   project and click the Choose button. Open the project in a new window.

   The Java code for the application is the ChatActivity class in the
   com.tokbox.android.demo.learningopentok package.

2. Debug the project on a supported device.

   For a list of supported devices, see the "Developer and client requirements"
   on [this page] [1].

## 1: Creating a Session (server side)

Before you can test the application, you need to set up a web service to handle some
OpenTok-related API calls. The web service securely creates an OpenTok session.

The OpenTok PHP Getting Started repo includes code for setting up a web service that
handles the following API calls:

* "/service" -- The Android client calls this endpoint to get an OpenTok session ID, token,
  and API key.

* "/start" -- The Android client calls this endpoint to start recording the OpenTok session to
  an archive.

* "/stop" -- The Android client calls this endpoint to stop recording the archive.

* "/view" -- The Android client load this endpoint in a web browser to display the archive
  recording.

Download the repo and run its code on a PHP-enabled web server. (TODO: Add a link.)

The HTTP POST request to the /service endpoint returns a response that includes the OpenTok
session ID and token.

## 2: Generating a Token (server side)

The web service also creates a token that the client uses to connect to the OpenTok session.
The HTTP GET request to the /service endpoint returns a response that includes the OpenTok
session ID and token.

You will want to authenticate each user (using your own server-side authentication techniques)
before sending an OpenTok token. Otherwise, malicious users could call your web service and
use tokens, causing streaming minutes to be charged to your OpenTok developer account. Also,
it is a best practice to use an HTTPS URL for the web service that returns an OpenTok token,
so that it cannot be intercepted and misused.

## 3: Connecting to the session

The code for this section is added in the step-3 branch of the repo.

First, set the app to use the web service described in the previous two sections:

1. Open the WebServiceCoordinator.java file. This is in the com.tokbox.android.demo.learningopentok
   package.

2. Edit the `CHAT_SERVER_URL` and `SESSION_INFO_ENDPOINT` values to match the URL and end-point
   of the web service:

        private static final String CHAT_SERVER_URL = "https://example.com";
        private static final String SESSION_INFO_ENDPOINT = CHAT_SERVER_URL + "/session";

You can now test the app in the debugger. On successfully connecting to the session, the
app logs "Session Connected" to the debug console.

The `onCreate()` method of the main ChatActivity object instantiates a WebServiceCoordinator
object and calls its `fetchSessionConnectionData()` method. This method makes an API call to
the /session endpoint of the web service to obtain the OpenTok API key, session ID, and a token
to connect to the session.

Once the session ID is obtained, the WebServiceCoordinator calls the
`onSessionConnectionDataReady()` method of the ChatActivity object, passing in the OpenTok API key,
session ID, and token. This method sets the properties to these values and then calls the
`initializeSession()` method:

    private void initializeSession() {
      mSession = new Session(this, mApiKey, mSessionId);
      mSession.setSessionListener(this);
      mSession.connect(mToken);
    }

The Session class is defined by the OpenTok Android SDK. It represents the OpenTok session
(which connects users).

This app sets `this` as the listener for Session events, defined by the Session.SessionListener 
interface. The ChatActivity class implements this interface, and overrides its methods, such as
the `onConnected(Session session)` method:

    @Override
    public void onConnected(Session session) {
        Log.i(LOG_TAG, "Session Connected");
    }

The Session.SessionListener interface also defined methods for handling other session-related
events, which we will look at in the following sections.

Finally, the `connect(token)` method of the Session object connects the app to the OpenTok session.
You must connect before sending or receiving audio-video streams in the session (or before
interacting with the session in any way).

## 4: Publishing an audio video stream to the session

The code for this section is added in the step-4 branch of the repo.

First, let's test the code in this branch:

1. Find the test.html file in the root of the project. You will use the test.html file to
   connect to the OpenTok session and view the audio-video stream published by the Android app:

   * Edit the test.html file and set the `sessionCredentialsUrl` variable to match the
     `ksessionCredentialsUrl` property used in the iOS app.

   * Add the test.html file to a web server. (You cannot run WebRTC videos in web pages loaded
     from the desktop.)

   * In a browser, load the test.html file from the web server.

2. Run the Android app. The Android app publishes an audio-video stream to the session and the
   the web client app subscribes to the stream.

Now lets look at the Android code. In addition to initializing and connecting to the session, the
`onSessionConnectionDataReady()` method calls the `initializePublisher()` method:

      private void initializePublisher() {
          mPublisher = new Publisher(this);
          mPublisher.setPublisherListener(this);
          mPublisher.setCameraListener(this);
          mPublisher.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
                  BaseVideoRenderer.STYLE_VIDEO_FILL);
          mPublisherViewContainer.addView(mPublisher.getView());
      }

The Publisher object is defined in the OpenTok Android SDK. A Publisher object acquires
an audio and video stream from the device's microphone and camera. These can then be published
to the OpenTok session as an audio-video stream.

The Publisher class is a subclass of the PublisherKit class, also defined in the OpenTok Android
SDK. The PublisherKit class lets you define custom video drivers (capturers and renderers). The
Publisher class uses the device's camera as as the video source, and it implements a pre-built
video capturer and renderer.

The ChatActivity object sets itself to implement the PublisherKit.PublisherListener interface.
As such it implements method of that interface to handle publisher-related events:

      mPublisher.setPublisherListener(this);

The following code sets the Publisher to scale the video to fill the entire area of the
renderer, with cropping as needed:

      mPublisher.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
              BaseVideoRenderer.STYLE_VIDEO_FILL);

The 'getView()` method of the Publisher object returns a View object that displays
a view of the camera. This code displays that view in the app:

      mPublisherViewContainer.addView(mPublisher.getView());

The `mPublisherViewContainer` object is a FrameLayout object set to the `publisher_container`
view defined in the main layout XML file.

Upon successfully connecting to the OpenTok session (see the previous section), the
`onConnected(Session session)` method is called. In this branch of the repo, this method
includes a call to the `publish(publisherKit)` method of the Session object:

    @Override
    public void onConnected(Session session) {
        Log.i(LOG_TAG, "Session Connected");

        if (mPublisher != null) {
            mSession.publish(mPublisher);
        }
    }

The `publish(publisherKit)` method of the Session object publishes an audio-video stream to
the OpenTok session.

Upon successfully publishing the stream, the implementation of the
`onStreamCreated(publisherKit, stream)`  method (defined in the PublisherKit.PublisherListener
interface) is called:

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
        Log.i(LOG_TAG, "Publisher Stream Created");
    }

If the publisher stops sending its stream to the session, the implementation of the
`onStreamDestroyed(publisherKit, stream)` method is called:

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
        Log.i(LOG_TAG, "Publisher Stream Destroyed");
    }

## 5: Subscribing to another client's audio-video stream

The code for this section is added in the step-4 branch of the repo.

First, let's test the code in this branch:

1. Find the test.html file in the root of the project. You will use the test.html file to
   connect to the OpenTok session and view the audio-video stream published by the Android app:

   * Edit the test.html file and set the `sessionCredentialsUrl` variable to match the
     `ksessionCredentialsUrl` property used in the iOS app.

   * Add the test.html file to a web server. (You cannot run WebRTC videos in web pages loaded
     from the desktop.)

   * In a browser, load the test.html file from the web server.

2. Run the Android app. The Android app subscribes to the audio-video stream published by the
   web page.

The `onStreamReceived(Session session, Stream stream)` method (defined in the
Session.SessionListener interface) is called when a new stream is created in the session.
The app implements this method with the following:

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.i(LOG_TAG, "Stream Received");

        if (mSubscriber == null) {
            mSubscriber = new Subscriber(this, stream);
            mSubscriber.setSubscriberListener(this);
            mSubscriber.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
                    BaseVideoRenderer.STYLE_VIDEO_FILL);
            mSession.subscribe(mSubscriber);
        }
    }

The method is passed a Session and Stream object, which are both defined by the OpenTok Android
SDK. The Stream object represents the stream that another client is publishing. Although this app
assumes that only one other client is connecting to the session and publishing, the method checks
to see if the app is already subscribing to a stream (if the `mSubscriber` property is null).
If not, the method initializes an Subscriber object (`mSubscriber`), used to subscribe to the
stream, passing in the OTStream object to the constructor function. It also sets the ChatActivity
object as the implementor of the SubscriberKit.SubscriberListener interface. This interface defines
methods that handle events related to the subscriber.

The Subscriber class is also defined in the OpenTok Android SDK. It is a subclass of SubscriberKit,
which lets you define a custom video renderer. The Subscriber object implements a built-in video
renderer.

The following code sets the Subscriber to scale the video to fill the entire area of the
renderer, with cropping as needed:

      mSubscriber.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
              BaseVideoRenderer.STYLE_VIDEO_FILL)

The app then calls the `subscribe(SubscriberKit)` method of the Session object to have the app
subscribe to the stream.

When the app starts receiving the subscribed stream, the implementation of the
`onConnected(subscriberKit)` method (defined by the SubscriberKit.SubscriberListener interface)
is called:

    @Override
    public void onConnected(SubscriberKit subscriberKit) {
        Log.i(LOG_TAG, "Subscriber Connected");

        mSubscriberViewContainer.addView(mSubscriber.getView());
    }

It adds view of the subscriber stream (returned by the `getView()` method of the Subscriber object)
as a subview of the `mSubscriberViewContainer` View object.

If the subscriber's stream is dropped from the session (perhaps the client chose to stop publishing
or to disconnect from the session), the implementation of the
`Session.SessionListener.onStreamDropped(session, stream)` method is called:

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(LOG_TAG, "Stream Dropped");

        if (mSubscriber != null) {
            mSubscriber = null;
            mSubscriberViewContainer.removeAllViews();
        }
    }

## signaling.step-1

The code for this section is added in the signaling.step-1 branch of the repo.

The OpenTok signaling API lets clients send text messages to other clients connected to the
OpenTok session. You can send a signal message to a specific client, or you can send
a message to every client connected to the session.

In this branch, the following code is added to the `initializeSession()` method:

    mSession.setSignalListener(this);

This sets the ChatActivity object as the implementor of the SubscriberKit.SignalListener interface. This interface defines the `onSignalReceived(session, type, data, connection)` methods. This method
is called when the client receives a signal from the session:

    @Override
    public void onSignalReceived(Session session, String type, String data, Connection connection) {
        Toast toast = Toast.makeText(this, data, Toast.LENGTH_LONG);
        toast.show();
    }

This app uses an android.widget.Toast object to display received signals.

In the `onConnected(session)` method, the following code sends a signal when the app connects to the
session:

    mSession.sendSignal("", "Hello, Signaling!");

This signal is sent to all clients connected to the session. The method has two parameters:

* `type` (String) -- An optional parameter that can be used as a filter for types of signals.

* `data` (String) -- The data to send with the signal.

## signaling.step-2

The code for this section is added in the signaling.step-2 branch of the repo.

In this branch, the following code is added to the `initializeSession()` method:

    mSendButton = (Button)findViewById(R.id.send_button);
    mMessageEditText = (EditText)findViewById(R.id.message_edit_text);

    // Attach handlers to UI
    mSendButton.setOnClickListener(this);

The main layout XML file adds a Button and and EditText element to the main view. This code adds
properties to reference these objects. It also sets the ChatActivity object as the implementor of
the View.OnClickListener interface. This interface defines the `onClick(View v)` method.

In the `onConnected(Session session)` method (called when the app connects to the OpenTok session)
the following line of code is added in this branch:

    enableMessageViews();

The `enableMessageViews()` method enables the Message text field and the Send button:

    private void enableMessageViews() {
        mMessageEditText.setEnabled(true);
        mSendButton.setEnabled(true);
    }

The `onClick(View v)` method is called when the clicks the Send button:

    @Override
    public void onClick(View v) {
        if (v.equals(mSendButton)) {
            sendMessage();
        }
    }

The `sendMessage()` method sends the text chat message (defined in the Message text field)
to the OpenTok session:

    private void sendMessage() {
        disableMessageViews();
        mSession.sendSignal(SIGNAL_TYPE_MESSAGE, mMessageEditText.getText().toString());
        mMessageEditText.setText("");
        enableMessageViews();
    }

Note that in this branch, the `type` of the signal is set to `SIGNAL_TYPE_MESSAGE`
(a string defined as "message"). The `onSignalReceived()` method checks to see if the
signal received is of this type:

    @Override
    public void onSignalReceived(Session session, String type, String data, Connection connection) {
        switch (type) {
            case SIGNAL_TYPE_MESSAGE:
                showMessage(data);
                break;
        }
    }

## signaling.step-3

The code for this section is added in the signaling.step-3 branch of the repo.

First, let's test the code in this branch:

1. Find the test.html file in the root of the project. You will use the test.html file to
   connect to the OpenTok session and view the audio-video stream published by the Android app:

   * Edit the test.html file and set the `sessionCredentialsUrl` variable to match the
     `ksessionCredentialsUrl` property used in the iOS app.

   * Add the test.html file to a web server. (You cannot run WebRTC videos in web pages loaded
     from the desktop.)

   * In a browser, load the test.html file from the web server.

2. Run the Android app. Enter some chat message in the Message text field, then click the Send
   button. The web page displays the text message sent by the Android app. You can also send a
   message from the web page to the Android app.

Instead of using a Toast object to display received signals, the code in this branch uses an
android.widget.ListView object. This lets the app display more than one message at a time.
This branch adds the following code to the `onCreate()` method:

    mMessageHistoryListView = (ListView)findViewById(R.id.message_history_list_view);

    // Attach data source to message history
    mMessageHistory = new ChatMessageAdapter(this);
    mMessageHistoryListView.setAdapter(mMessageHistory);

This branch adds code that differentiates between signals (text chat messages) sent from the local
Android client and those sent from other clients connected to the session. The
`onSignalReceived(session, type, data, connection)` method checks the Connection object for
the received signal with the Connection object returned by `mSession.getConnection()`:

    @Override
    public void onSignalReceived(Session session, String type, String data, Connection connection) {
    boolean remote = !connection.equals(mSession.getConnection());
        switch (type) {
            case SIGNAL_TYPE_MESSAGE:
                showMessage(data);
                showMessage(data, remote);
                break;
        }
    }

The Connection object of the received signal represents the connection to the session for the client
that sent the signal. This will only match the Connection object returned by
`mSession.getConnection()` if the signal was sent by the local client.

The `showMessage(messageData, remote)` method has a new second parameter: remote. This is set
to `true` if the message was sent another client (and `false` if it was sent by the local
Android client):

    private void showMessage(String messageData, boolean remote) {
        ChatMessage message = ChatMessage.fromData(messageData);
        message.setRemote(remote);
        mMessageHistory.add(message);
     }

The `ChatMessage.fromData()` converts the message data (the data in the received signal)
into a ChatMessage object. The mMessageHistoryListView uses the mMessageHistory object as
the adaptor for the data in the list view. The mMessageHistory property is an
android.widget.ArrayAdapter object. This tutorial focuses on the OpenTok Android SDK API. For more
information on the Android classes used in this text chat implementation, see the docs for the
following:

* [ArrayAdaptor] [2]
* [ListView] [3]

Other resources
---------------

See the following:

* [API reference] [4] -- Provides details on the OpenTok iOS Android API
* [Tutorials] [5] -- Includes conceptual information and code samples for all OpenTok features

[1]: https://tokbox.com/opentok/libraries/client/android/
[2]: http://developer.android.com/reference/android/widget/ArrayAdapter.html
[3]: http://developer.android.com/reference/android/widget/ListView.html
[4]: https://tokbox.com/opentok/libraries/client/android/reference/
[5]: https://tokbox.com/opentok/tutorials/
