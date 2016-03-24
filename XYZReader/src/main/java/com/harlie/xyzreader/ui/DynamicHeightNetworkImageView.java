package com.harlie.xyzreader.ui;

import android.content.Context;
import android.util.AttributeSet;

import com.android.volley.toolbox.NetworkImageView;

public class DynamicHeightNetworkImageView extends NetworkImageView {
    //private final static String TAG = "LEE: <" + DynamicHeightNetworkImageView.class.getSimpleName() + ">";

    private float mAspectRatio = 1.5f;

    public DynamicHeightNetworkImageView(Context context) {
        super(context);
        //Log.v(TAG, "DynamicHeightNetworkImageView");
    }

    public DynamicHeightNetworkImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //Log.v(TAG, "DynamicHeightNetworkImageView");
    }

    public DynamicHeightNetworkImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //Log.v(TAG, "DynamicHeightNetworkImageView");
    }

    public void setAspectRatio(float aspectRatio) {
        //Log.v(TAG, "setAspectRatio");
        mAspectRatio = aspectRatio;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //Log.v(TAG, "onMeasure");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = getMeasuredWidth();
        setMeasuredDimension(measuredWidth, (int) (measuredWidth / mAspectRatio));
    }
}
