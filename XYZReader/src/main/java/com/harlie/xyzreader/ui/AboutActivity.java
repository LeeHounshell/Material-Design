package com.harlie.xyzreader.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.harlie.xyzreader.R;

public class AboutActivity extends Activity {
    private final static String TAG = "LEE: <" + AboutActivity.class.getSimpleName() + ">";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        getTheme().applyStyle(new FontPreferences(this).getFontStyle().getResId(), true);
    }
}
