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

* *signaling.step-1* -- This branch shows you how to use the OpenTok signaling API to implement
  text chat.

* *signaling.step-2* -- This branch shows you how to use the OpenTok signaling API to implement
  text chat.

* *signaling.step-3* -- This branch shows you how to use the OpenTok signaling API to implement
  text chat.

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

This app sets `this` as the listener for Session events, defined by the Session.SessionListener interface. The ChatActivity class implements this interface, and overrides its methods, such as
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

Note that in addition to initializing and connecting to the session, the `onSessionConnectionDataReady()` method calls the `initializePublisher()` method:

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


[1]: https://tokbox.com/opentok/libraries/client/android/
