package com.harlie.xyzreader.ui;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.harlie.xyzreader.R;
import com.harlie.xyzreader.data.ArticleLoader;
import com.harlie.xyzreader.data.ItemsContract;
import com.harlie.xyzreader.data.UpdaterService;
import com.harlie.xyzreader.xyzReaderApplication;

/**
 * An activity representing a list of Articles. This activity has different presentations for
 * handset and tablet-size devices. On handsets, the activity presents a list of items, which when
 * touched, lead to a {@link ArticleDetailActivity} representing item details. On tablets, the
 * activity presents a grid of items as cards.
 */
public class ArticleListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private final static String TAG = "LEE: <" + ArticleListActivity.class.getSimpleName() + ">";

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);

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

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        getLoaderManager().initLoader(0, null, this);

        if (savedInstanceState == null) {
            refresh();
        }
    }

    //from: http://stackoverflow.com/questions/31662416/show-collapsingtoolbarlayout-title-only-when-collapsed
    private void configureToolbarTitleBehavior() {
        Log.v(TAG, "configureToolbarTitleBehavior");
        final String title = getResources().getString(R.string.app_name);
        final CollapsingToolbarLayout collapsingToolbarLayout = ((CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_layout));
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.toolbar_container);
        if (appBarLayout != null) {
            appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                boolean isShow = false;
                int scrollRange = -1;

                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                    if (appBarLayout != null) {
                        if (scrollRange == -1) {
                            scrollRange = appBarLayout.getTotalScrollRange();
                        }
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

    private void refresh() {
        Log.v(TAG, "refresh");
        startService(new Intent(this, UpdaterService.class));
    }

    @Override
    protected void onStart() {
        Log.v(TAG, "onStart");
        super.onStart();
        registerReceiver(mRefreshingReceiver,
                new IntentFilter(UpdaterService.BROADCAST_ACTION_STATE_CHANGE));
    }

    @Override
    protected void onStop() {
        Log.v(TAG, "onStop");
        super.onStop();
        unregisterReceiver(mRefreshingReceiver);
    }

    @Override
    protected void onDestroy() {
        Log.v(TAG, "onDestroy");
        super.onDestroy();

        mSwipeRefreshLayout = null;
        mRecyclerView = null;

        // NOTE: build uses 'preprocessor.gradle' here
        //#IFDEF 'debug'
        //xyzReaderApplication.getInstance().mustDie(this);
        //#ENDIF
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.v(TAG, "onCreateOptionsMenu");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.v(TAG, "onOptionsItemSelected");
        switch (item.getItemId()) {
            case R.id.settings: {
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;
            }
            case R.id.about: {
                String transitionName = "fancy";
                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(this, this.mRecyclerView, transitionName).toBundle();
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent, bundle);
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    private boolean mIsRefreshing = false;

    private final BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "onReceive");
            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                mIsRefreshing = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);
                updateRefreshingUI();
            }
        }
    };

    private void updateRefreshingUI() {
        Log.v(TAG, "updateRefreshingUI");
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(mIsRefreshing);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.v(TAG, "onCreateLoader");
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Log.v(TAG, "onLoadFinished");
        if (mRecyclerView != null) {
            Adapter adapter = new Adapter(cursor);
            adapter.setHasStableIds(true);
            mRecyclerView.setAdapter(adapter);
            int columnCount = getResources().getInteger(R.integer.list_column_count);
            StaggeredGridLayoutManager sglm =
                    new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
            mRecyclerView.setLayoutManager(sglm);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.v(TAG, "onLoaderReset");
        if (mRecyclerView != null) {
            mRecyclerView.setAdapter(null);
        }
    }

    private AppCompatActivity getActivity() {
        Log.v(TAG, "getActivity");
        return this;
    }

    private class Adapter extends RecyclerView.Adapter<ViewHolder> {
        private final String TAG = "LEE: <" + Adapter.class.getSimpleName() + ">";

        private final Cursor mCursor;

        public Adapter(Cursor cursor) {
            Log.v(TAG, "Adapter");
            mCursor = cursor;
        }

        @Override
        public long getItemId(int position) {
            //Log.v(TAG, "getItemId");
            mCursor.moveToPosition(position);
            return mCursor.getLong(ArticleLoader.Query._ID);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Log.v(TAG, "onCreateViewHolder");
            View view = getLayoutInflater().inflate(R.layout.list_item_article, parent, false);
            final View transitionView = view.findViewById(R.id.thumbnail);
            final String transitionName;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                transitionName = transitionView.getTransitionName();
            }
            else {
                transitionName = null;
            }
            final ViewHolder vh = new ViewHolder(view);
            final AppCompatActivity activity = getActivity();
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.v(TAG, "onClick - startActivity ArticleDetailActivity - transitionName="+transitionName);
                    Bundle bundle = new Bundle();
                    if (transitionName != null) {
                        bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, transitionView, transitionName).toBundle();
                    }
                    Intent intent = new Intent(Intent.ACTION_VIEW, ItemsContract.Items.buildItemUri(getItemId(vh.getAdapterPosition())));
                    startActivity(intent, bundle);
                }
            });
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Log.v(TAG, "onBindViewHolder");
            if (mCursor != null) {
                mCursor.moveToPosition(position);
                String title = mCursor.getString(ArticleLoader.Query.TITLE);
                holder.titleView.setText(title);
                String subtitle = DateUtils.getRelativeTimeSpanString(
                        mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                        System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                        DateUtils.FORMAT_ABBREV_ALL).toString()
                        + " " + getResources().getString(R.string.by) + " "
                        + mCursor.getString(ArticleLoader.Query.AUTHOR);
                holder.subtitleView.setText(subtitle);
                holder.thumbnailView.setImageUrl(
                        mCursor.getString(ArticleLoader.Query.THUMB_URL),
                        ImageLoaderHelper.getInstance(ArticleListActivity.this).getImageLoader());
                holder.thumbnailView.setAspectRatio(mCursor.getFloat(ArticleLoader.Query.ASPECT_RATIO));
            }
        }

        @Override
        public int getItemCount() {
            //Log.v(TAG, "getItemCount");
            return mCursor.getCount();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final static String TAG = "LEE: <" + ViewHolder.class.getSimpleName() + ">";

        public final DynamicHeightNetworkImageView thumbnailView;
        public final TextView titleView;
        public final TextView subtitleView;

        public ViewHolder(View view) {
            super(view);
            Log.v(TAG, "ViewHolder");
            thumbnailView = (DynamicHeightNetworkImageView) view.findViewById(R.id.thumbnail);
            titleView = (TextView) view.findViewById(R.id.article_title);
            subtitleView = (TextView) view.findViewById(R.id.article_subtitle);
        }
    }
}
