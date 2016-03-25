package com.harlie.xyzreader.ui;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.widget.ImageView;

import com.harlie.xyzreader.R;

// NOTE: build uses 'preprocessor.gradle' here
//#IFDEF 'debug'
//import com.harlie.xyzreader.xyzReaderApplication;
//#ENDIF

public class AboutActivity extends AppCompatActivity {
    private final static String TAG = "LEE: <" + AboutActivity.class.getSimpleName() + ">";

    private int mTopInset;
    private View mUpButton;

    // NOTE: normally I would not over-ride onBackPressed like this.
    // because I am using Transitions, if the screen is rotated before transitioning back to ArticleListActivity
    // then the Toolbar is somehow destroyed. I'm not sure why. But if I run the Activity directly then it works ok.
    @Override
    public void onBackPressed() {
        Log.v(TAG, "onBackPressed");
        //super.onBackPressed();
        String transitionName = "fancy";
        Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(this, mUpButton, transitionName).toBundle();
        Intent intent = new Intent(this, ArticleListActivity.class);
        startActivity(intent, bundle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            // restore our old session..
            onRestoreInstanceState(savedInstanceState);
        }

        setContentView(R.layout.activity_about);

        getTheme().applyStyle(new FontPreferences(this).getFontStyle().getResId(), true);

        configureToolbarTitleBehavior();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayShowTitleEnabled(false);
            }
        }

        // get the image view
        ImageView imageView = (ImageView) findViewById(R.id.LeeHounshellImage);

        // set the ontouch listener
        if (imageView != null) {
            imageView.setOnTouchListener(
                    new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(
                                View v,
                                MotionEvent event
                        ) {
                            Log.v(TAG, "onTouch()");
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN: {
                                    ImageView view = (ImageView) v;
                                    // overlay is black with transparency of 0x77 (119)
                                    view.getDrawable().setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
                                    view.invalidate();
                                    break;
                                }
                                case MotionEvent.ACTION_UP:
                                case MotionEvent.ACTION_CANCEL: {
                                    Log.v(TAG, "view Linked-In");
                                    ImageView view = (ImageView) v;
                                    // clear the overlay
                                    view.getDrawable().clearColorFilter();
                                    view.invalidate();
                                    // show my Linked-In profile
                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_VIEW);
                                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                                    intent.setData(Uri.parse("http://www.linkedin.com/pub/lee-hounshell/2/674/852"));
                                    startActivity(intent);
                                    break;
                                }
                            }
                            return true;
                        }
                    }
            );
        }

        final View upButtonContainer = findViewById(R.id.up_container);

        mUpButton = findViewById(R.id.action_up);
        if (mUpButton != null) {
            mUpButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.v(TAG, "onClick");
                    onBackPressed();
                }
            });
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (upButtonContainer != null) {
                upButtonContainer.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                            view.onApplyWindowInsets(windowInsets);
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                            mTopInset = windowInsets.getSystemWindowInsetTop();
                        }
                        upButtonContainer.setTranslationY(mTopInset);
                        return windowInsets;
                    }
                });
            }
        }
    }

    @Override
    protected void onDestroy() {
        Log.v(TAG, "onDestroy");
        super.onDestroy();

        mUpButton = null;

        // NOTE: build uses 'preprocessor.gradle' here
        //#IFDEF 'debug'
        //xyzReaderApplication.getInstance().mustDie(this);
        //#ENDIF
    }

    //from: http://stackoverflow.com/questions/31662416/show-collapsingtoolbarlayout-title-only-when-collapsed
    private void configureToolbarTitleBehavior() {
        Log.v(TAG, "configureToolbarTitleBehavior");
        final String title = getResources().getString(R.string.action_about);
        final CollapsingToolbarLayout collapsingToolbarLayout = ((CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_layout));
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.toolbar_container);
        if (appBarLayout != null) {
            appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                boolean isShow = false;
                int scrollRange = -1;

                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                    if (scrollRange == -1) {
                        scrollRange = appBarLayout.getTotalScrollRange();
                    }
                    if (collapsingToolbarLayout != null) {
                        if (scrollRange + verticalOffset == 0) {
                            collapsingToolbarLayout.setTitle(title);
                            isShow = true;
                        } else if (isShow) {
                            collapsingToolbarLayout.setTitle("");
                            isShow = false;
                        }
                    }
                }
            });
        }
    }

}
