package com.harlie.xyzreader.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.harlie.xyzreader.R;

//from: http://stackoverflow.com/questions/4877153/android-application-wide-font-size-preference/12591991#12591991
class FontPreferences {
    //private final static String TAG = "LEE: <" + FontPreferences.class.getSimpleName() + ">";

    private final Context context;

    public FontPreferences(Context context) {
        this.context = context;
        //Log.v(TAG, "FontPreferences");
    }

    private SharedPreferences open() {
        //Log.v(TAG, "open");
        return PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    @SuppressWarnings("unused")
    protected SharedPreferences.Editor edit() {
        //Log.v(TAG, "edit");
        return open().edit();
    }

    public String getFontName() {
        final String fontNameKey = context.getResources().getString(R.string.font_name);
        final String defaultFontName = context.getResources().getString(R.string.pref_default_font);
        @SuppressWarnings("UnnecessaryLocalVariable") final String theName = open().getString(fontNameKey, defaultFontName);
        //Log.v(TAG, "---> getFontName: theName="+theName);
        return theName;
    }

    public String getFontSize() {
        final String fontSizeKey = context.getResources().getString(R.string.font_size);
        final String defaultFontSize = context.getResources().getString(R.string.pref_default_size);
        @SuppressWarnings("UnnecessaryLocalVariable") final String theSize = open().getString(fontSizeKey, defaultFontSize);
        //Log.v(TAG, "getFontSize: theSize="+theSize);
        return theSize;
    }

    public FontStyle getFontStyle() {
        String fontSize = getFontSize();
        for (FontStyle aStyle : FontStyle.values()) {
            if (aStyle.getTitle().equals(fontSize)) {
                //Log.v(TAG, "getFontStyle: FOUND fontSize="+fontSize);
                return aStyle;
            }
        }
        //Log.v(TAG, "getFontStyle: NOT FOUND fontSize="+fontSize);
        return FontStyle.Medium;
    }

}
