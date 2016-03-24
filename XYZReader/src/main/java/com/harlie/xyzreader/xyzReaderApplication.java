package com.harlie.xyzreader;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.StandardExceptionParser;
import com.google.android.gms.analytics.Tracker;
// NOTE: build uses 'preprocessor.gradle' here
//#IFDEF 'debug'
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
//#ENDIF


//from: http://www.androidhive.info/2015/08/android-integrating-google-analytics-v4/
@SuppressWarnings("unused")
public class xyzReaderApplication extends Application {
    private final static String TAG = "LEE: <" + xyzReaderApplication.class.getSimpleName() + ">";

    private static Context sContext;
    private static xyzReaderApplication mInstance;

    // NOTE: build uses 'preprocessor.gradle' here
    //#IFDEF 'debug'
    private RefWatcher refWatcher;
    //#ENDIF

    public void onCreate() {
        Log.v(TAG, "===> onCreate <===");
        xyzReaderApplication.mInstance = this;
        super.onCreate();
        xyzReaderApplication.sContext = getApplicationContext();

        // NOTE: build uses 'preprocessor.gradle' here
        //#IFDEF 'debug'
        refWatcher = LeakCanary.install(this);
        //#ENDIF

        AnalyticsTrackers.initialize(this);
        AnalyticsTrackers.getInstance().get(AnalyticsTrackers.Target.APP);
    }

    public static Context getAppContext() {
        Log.v(TAG, "getAppContext");
        return xyzReaderApplication.sContext;
    }

    public static synchronized xyzReaderApplication getInstance() {
        Log.v(TAG, "getInstance");
        return xyzReaderApplication.mInstance;
    }

    public synchronized Tracker getGoogleAnalyticsTracker() {
        Log.v(TAG, "getGoogleAnalyticsTracker");
        AnalyticsTrackers analyticsTrackers = AnalyticsTrackers.getInstance();
        return analyticsTrackers.get(AnalyticsTrackers.Target.APP);
    }

    /***
     * Tracking screen view
     *
     * @param screenName screen name to be displayed on GA dashboard
     */
    public void trackScreenView(String screenName) {
        Log.v(TAG, "trackScreenView");
        Tracker t = getGoogleAnalyticsTracker();

        // Set screen name.
        t.setScreenName(screenName);

        // Send a screen view.
        t.send(new HitBuilders.ScreenViewBuilder().build());

        GoogleAnalytics.getInstance(this).dispatchLocalHits();
    }

    /***
     * Tracking exception
     *
     * @param e exception to be tracked
     */
    public void trackException(Exception e) {
        Log.v(TAG, "trackException");
        if (e != null) {
            Tracker t = getGoogleAnalyticsTracker();

            t.send(new HitBuilders.ExceptionBuilder()
                    .setDescription(
                            new StandardExceptionParser(this, null)
                                    .getDescription(Thread.currentThread().getName(), e))
                    .setFatal(false)
                    .build()
            );
        }
    }

    /***
     * Tracking event
     *
     * @param category event category
     * @param action   action of the event
     * @param label    label
     */
    public void trackEvent(String category, String action, String label) {
        Log.v(TAG, "trackEvent");
        Tracker t = getGoogleAnalyticsTracker();

        // Build and send an Event.
        t.send(new HitBuilders.EventBuilder().setCategory(category).setAction(action).setLabel(label).build());
    }

    // --------------------------------------------------------------------------------
    // NOTE: build uses 'preprocessor.gradle' here
    //#IFDEF 'debug'

    public static RefWatcher getRefWatcher(Context context) {
        Log.v(TAG, "getRefWatcher");
        xyzReaderApplication application = (xyzReaderApplication) context.getApplicationContext();
        return application.refWatcher;
    }

    /* from: http://stackoverflow.com/questions/33654503/how-to-use-leak-canary */
    public void mustDie(Object object) {
        Log.v(TAG, "mustDie");
        if (refWatcher != null) {
            refWatcher.watch(object);
        }
    }

    //#ENDIF
    // --------------------------------------------------------------------------------

}
