package ir.smartlab.permissions;

import android.app.Activity;
import android.os.Bundle;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Logger.LogLevel;
import com.google.android.gms.analytics.Tracker;

public class BaseActivity extends Activity {

	// TODO: Replace with your google analytics tracker code
    private final String GA_TRACKING_CODE = "XX-XXXXXXXX-XX";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GoogleAnalytics.getInstance(this).getLogger().setLogLevel(LogLevel.VERBOSE);

        Tracker t = GoogleAnalytics.getInstance(this).newTracker(GA_TRACKING_CODE);
        t.setScreenName(this.getClass().getName());
        t.send(new HitBuilders.AppViewBuilder().build());
        t.setScreenName(null);
    }

}
