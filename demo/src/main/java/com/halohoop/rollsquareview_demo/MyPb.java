package com.halohoop.rollsquareview_demo;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ProgressBar;

/**
 * Created by Pooholah on 2017/4/24.
 */

public class MyPb extends ProgressBar {
    public MyPb(Context context) {
        super(context);
    }

    public MyPb(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyPb(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onVisibilityAggregated(boolean isVisible) {
        super.onVisibilityAggregated(isVisible);
    }

}
