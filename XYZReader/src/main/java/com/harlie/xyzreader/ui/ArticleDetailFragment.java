package com.harlie.xyzreader.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v7.graphics.Palette;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.harlie.xyzreader.R;
import com.harlie.xyzreader.data.ArticleLoader;
import com.harlie.xyzreader.xyzReaderApplication;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private final static String TAG = "LEE: <" + ArticleDetailFragment.class.getSimpleName() + ">";

    private static final String ARG_ITEM_ID = "item_id";
    private static final float PARALLAX_FACTOR = 1.25f;

    private Cursor mCursor;
    private long mItemId;
    private View mRootView;
    private int mMutedColor = 0xFF333333;
    private ObservableScrollView mScrollView;
    private DrawInsetsFrameLayout mDrawInsetsFrameLayout;
    private ColorDrawable mStatusBarColorDrawable;

    private int mTopInset;
    private View mPhotoContainerView;
    private ImageView mPhotoView;
    private int mScrollY;
    private boolean mIsCard = false;
    private int mStatusBarFullOpacityBottom;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
        Log.v(TAG, "ArticleDetailFragment");
    }

    public static ArticleDetailFragment newInstance(long itemId) {
        Log.v(TAG, "newInstance");
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }

        mIsCard = getResources().getBoolean(R.bool.detail_is_card);
        mStatusBarFullOpacityBottom = getResources().getDimensionPixelSize(
                R.dimen.detail_card_top_margin);
        setHasOptionsMenu(true);
    }

    private ArticleDetailActivity getActivityCast() {
        Log.v(TAG, "getActivityCast");
        return (ArticleDetailActivity) getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.v(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView");
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
        mDrawInsetsFrameLayout = (DrawInsetsFrameLayout)
                mRootView.findViewById(R.id.draw_insets_frame_layout);
        mDrawInsetsFrameLayout.setOnInsetsCallback(new DrawInsetsFrameLayout.OnInsetsCallback() {
            @Override
            public void onInsetsChanged(Rect insets) {
                mTopInset = insets.top;
            }
        });

        mScrollView = (ObservableScrollView) mRootView.findViewById(R.id.scrollview);
        mScrollView.setCallbacks(new ObservableScrollView.Callbacks() {
            @Override
            public void onScrollChanged() {
                mScrollY = mScrollView.getScrollY();
                getActivityCast().onUpButtonFloorChanged(mItemId, ArticleDetailFragment.this);
                mPhotoContainerView.setTranslationY((int) (mScrollY - mScrollY / PARALLAX_FACTOR));
                updateStatusBar();
            }
        });

        mPhotoView = (ImageView) mRootView.findViewById(R.id.photo);
        mPhotoContainerView = mRootView.findViewById(R.id.photo_container);

        mStatusBarColorDrawable = new ColorDrawable(0);

        mRootView.findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, "onClick");
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share)));
            }
        });

        bindViews();
        updateStatusBar();
        return mRootView;
    }

    private void updateStatusBar() {
        Log.v(TAG, "updateStatusBar");
        int color = 0;
        if (mPhotoView != null && mTopInset != 0 && mScrollY > 0) {
            float f = progress(mScrollY,
                    mStatusBarFullOpacityBottom - mTopInset * 3,
                    mStatusBarFullOpacityBottom - mTopInset);
            color = Color.argb((int) (255 * f),
                    (int) (Color.red(mMutedColor) * 0.9),
                    (int) (Color.green(mMutedColor) * 0.9),
                    (int) (Color.blue(mMutedColor) * 0.9));
        }
        mStatusBarColorDrawable.setColor(color);
        mDrawInsetsFrameLayout.setInsetBackground(mStatusBarColorDrawable);
    }

    private static float progress(float v, float min, float max) {
        Log.v(TAG, "progress");
        return constrain((v - min) / (max - min), 0, 1);
    }

    private static float constrain(float val, @SuppressWarnings("SameParameterValue") float min, @SuppressWarnings("SameParameterValue") float max) {
        Log.v(TAG, "constrain");
        if (val < min) {
            return min;
        } else if (val > max) {
            return max;
        } else {
            return val;
        }
    }

    private void bindViews() {
        Log.v(TAG, "bindViews");
        if (mRootView == null) {
            return;
        }

        TextView titleView = (TextView) mRootView.findViewById(R.id.article_title);
        TextView bylineView = (TextView) mRootView.findViewById(R.id.article_byline);
        bylineView.setMovementMethod(new LinkMovementMethod());
        TextView bodyView = (TextView) mRootView.findViewById(R.id.article_body);

        setFontTypeAndSizes(titleView, bylineView, bodyView);

        if (mCursor != null) {
            mRootView.setAlpha(0);
            mRootView.setVisibility(View.VISIBLE);
            mRootView.animate().alpha(1);
            titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
            bylineView.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by <font color='#ffffff'>"
                            + mCursor.getString(ArticleLoader.Query.AUTHOR)
                            + "</font>"));
            bodyView.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY)));
            ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
                    .get(mCursor.getString(ArticleLoader.Query.PHOTO_URL), new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                            Bitmap bitmap = imageContainer.getBitmap();
                            if (bitmap != null) {
                                Palette p = Palette.generate(bitmap, 12);
                                mMutedColor = p.getDarkMutedColor(0xFF333333);
                                try {
                                    mPhotoView.setImageBitmap(imageContainer.getBitmap());
                                }
                                catch (NullPointerException e) {
                                    Log.w(TAG, "unable to setImageBitmap - mPhotoView is null");
                                }
                                try {
                                    mRootView.findViewById(R.id.meta_bar)
                                            .setBackgroundColor(mMutedColor);
                                }
                                catch (NullPointerException e) {
                                    Log.w(TAG, "unable to setBackgroundColor - mRootView is null");
                                }
                                try {
                                    updateStatusBar();
                                }
                                catch (NullPointerException e) {
                                    Log.w(TAG, "unable to updateStatusBar - null");
                                }
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError volleyError) {

                        }
                    });
        } else {
            mRootView.setVisibility(View.GONE);
            titleView.setText("N/A");
            bylineView.setText("N/A" );
            bodyView.setText("N/A");
        }
    }

    private void setFontTypeAndSizes(TextView titleView, TextView bylineView, TextView bodyView) {
        Log.v(TAG, "setFontTypeAndSizes");
        FontPreferences fontPreferences = new FontPreferences(getActivity());
        String fontname = fontPreferences.getFontName();
        String fontsize = fontPreferences.getFontSize();
        Log.v(TAG, "--> USING FONT NAME="+fontname+", SIZE="+fontsize);
        bodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), fontname));
        getActivity().getTheme().applyStyle(fontPreferences.getFontStyle().getResId(), true);

        int[] attrs = {R.attr.font_small, R.attr.font_medium, R.attr.font_large, R.attr.font_xlarge}; // The attributes to retrieve
        TypedArray ta = getActivity().obtainStyledAttributes(fontPreferences.getFontStyle().getResId(), attrs);
        String str;
        //noinspection ResourceType
        str = ta.getString(3);
        float titleTextSize = Float.valueOf(str.substring(0, str.length()-2)); // discard the "sp" part of the style item
        //noinspection ResourceType
        str = ta.getString(0);
        float bylineTextSize = Float.valueOf(str.substring(0, str.length()-2)); // discard the "sp" part of the style item
        //noinspection ResourceType
        str = ta.getString(1);
        float bodyTextSize = Float.valueOf(str.substring(0, str.length()-2)); // discard the "sp" part of the style item
        Log.v(TAG, "=========> titleTextSize="+titleTextSize+", bylineTextSize="+bylineTextSize+", bodyTextSize="+bodyTextSize);
        ta.recycle();

        titleView.setTextSize(titleTextSize);
        bylineView.setTextSize(bylineTextSize);
        bodyView.setTextSize(bodyTextSize);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.v(TAG, "onCreateLoader");
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Log.v(TAG, "onLoadFinished");
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        bindViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        Log.v(TAG, "onLoaderReset");
        mCursor = null;
        bindViews();
    }

    public int getUpButtonFloor() {
        Log.v(TAG, "getUpButtonFloor");
        if (mPhotoContainerView == null || mPhotoView.getHeight() == 0) {
            return Integer.MAX_VALUE;
        }

        // account for parallax
        return mIsCard
                ? (int) mPhotoContainerView.getTranslationY() + mPhotoView.getHeight() - mScrollY
                : mPhotoView.getHeight() - mScrollY;
    }
}
