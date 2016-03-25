package com.harlie.xyzreader.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;

import com.harlie.xyzreader.R;
import com.harlie.xyzreader.data.ArticleLoader;
import com.harlie.xyzreader.data.ItemsContract;
import com.harlie.xyzreader.xyzReaderApplication;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private final static String TAG = "LEE: <" + ArticleDetailActivity.class.getSimpleName() + ">";

    private long mSelectedItemId;
    private int mSelectedItemUpButtonFloor = Integer.MAX_VALUE;
    private int mTopInset;
    private long mStartId;

    private Cursor mCursor;
    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;
    private View mUpButtonContainer;
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

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                mStartId = ItemsContract.Items.getItemId(getIntent().getData());
                mSelectedItemId = mStartId;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        setContentView(R.layout.activity_article_detail);

        getTheme().applyStyle(new FontPreferences(this).getFontStyle().getResId(), true);

        getLoaderManager().initLoader(0, null, this);

        mUpButtonContainer = findViewById(R.id.up_container);

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

        mPagerAdapter = new MyPagerAdapter(getFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        if (mPager != null) {
            mPager.setAdapter(mPagerAdapter);
            mPager.setPageMargin((int) TypedValue
                    .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
            mPager.setPageMarginDrawable(new ColorDrawable(0x22000000));

            mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                @Override
                public void onPageScrollStateChanged(int state) {
                    super.onPageScrollStateChanged(state);
                    if (mUpButton != null) {
                        mUpButton.animate()
                                .alpha((state == ViewPager.SCROLL_STATE_IDLE) ? 1f : 0f)
                                .setDuration(300);
                    }
                }

                @Override
                public void onPageSelected(int position) {
                    if (mCursor != null) {
                        mCursor.moveToPosition(position);
                        mSelectedItemId = mCursor.getLong(ArticleLoader.Query._ID);
                        updateUpButtonPosition();
                    }
                }
            });
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mUpButtonContainer != null) {
                mUpButtonContainer.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                            view.onApplyWindowInsets(windowInsets);
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                            mTopInset = windowInsets.getSystemWindowInsetTop();
                        }
                        if (mUpButtonContainer != null) {
                            mUpButtonContainer.setTranslationY(mTopInset);
                            updateUpButtonPosition();
                        }
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

        mCursor = null;
        mPager = null;
        mPagerAdapter = null;
        mUpButtonContainer = null;
        mUpButton = null;

        // NOTE: build uses 'preprocessor.gradle' here
        //#IFDEF 'debug'
        //xyzReaderApplication.getInstance().mustDie(this);
        //#ENDIF
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.v(TAG, "onOptionsItemSelected");
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.v(TAG, "onCreateLoader");
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Log.v(TAG, "onLoadFinished");
        mCursor = cursor;
        if (mPagerAdapter != null) {
            mPagerAdapter.notifyDataSetChanged();
        }

        // Select the start ID
        if (mCursor != null && mStartId > 0) {
            mCursor.moveToFirst();
            // TODO: optimize
            while (!mCursor.isAfterLast()) {
                if (mCursor.getLong(ArticleLoader.Query._ID) == mStartId) {
                    final int position = mCursor.getPosition();
                    mPager.setCurrentItem(position, false);
                    break;
                }
                mCursor.moveToNext();
            }
            mStartId = 0;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        Log.v(TAG, "onLoaderReset");
        mCursor = null;
        if (mPagerAdapter != null) {
            mPagerAdapter.notifyDataSetChanged();
        }
    }

    public void onUpButtonFloorChanged(long itemId, ArticleDetailFragment fragment) {
        Log.v(TAG, "onUpButtonFloorChanged");
        if (itemId == mSelectedItemId) {
            mSelectedItemUpButtonFloor = fragment.getUpButtonFloor();
            updateUpButtonPosition();
        }
    }

    private void updateUpButtonPosition() {
        Log.v(TAG, "updateUpButtonPosition");
        if (mUpButton != null) {
            int upButtonNormalBottom = mTopInset + mUpButton.getHeight();
            mUpButton.setTranslationY(Math.min(mSelectedItemUpButtonFloor - upButtonNormalBottom, 0));
        }
    }

    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        private final String TAG = "LEE: <" + MyPagerAdapter.class.getSimpleName() + ">";

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
            Log.v(TAG, "MyPagerAdapter");
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            Log.v(TAG, "setPrimaryItem");
            super.setPrimaryItem(container, position, object);
            ArticleDetailFragment fragment = (ArticleDetailFragment) object;
            if (fragment != null) {
                mSelectedItemUpButtonFloor = fragment.getUpButtonFloor();
                updateUpButtonPosition();
            }
        }

        @Override
        public Fragment getItem(int position) {
            //Log.v(TAG, "getItem");
            if (mCursor != null) {
                mCursor.moveToPosition(position);
                return ArticleDetailFragment.newInstance(mCursor.getLong(ArticleLoader.Query._ID));
            }
            return ArticleDetailFragment.newInstance(0);
        }

        @Override
        public int getCount() {
            //Log.v(TAG, "getCount");
            return (mCursor != null) ? mCursor.getCount() : 0;
        }
    }

}
