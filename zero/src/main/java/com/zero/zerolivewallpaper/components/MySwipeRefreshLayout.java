package com.zero.zerolivewallpaper.components;

import android.content.Context;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;

public class MySwipeRefreshLayout extends SwipeRefreshLayout {

    private View[] mSwipeableChildren;

    public MySwipeRefreshLayout(Context context) {
        super(context);
    }

    public MySwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressWarnings("SameParameterValue")
    public void setSwipeableChildren(final int... ids) {
        assert ids != null;

        // Iterate through the ids and find the Views
        mSwipeableChildren = new View[ids.length];
        for (int i = 0; i < ids.length; i++) {
            mSwipeableChildren[i] = findViewById(ids[i]);
        }
    }

    @Override
    public boolean canChildScrollUp() {
        if (mSwipeableChildren != null && mSwipeableChildren.length > 0) {
            // Iterate through the scrollable children and check if any of them can not scroll up
            for (View view : mSwipeableChildren) {
                if (view != null && view.isShown() && !canViewScrollUp(view)) {
                    // If the view is shown, and can not scroll upwards, return false and start the
                    // gesture.
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean canViewScrollUp(View view) {
        if (view instanceof AbsListView) {
            // Pre-ICS we need to manually check the first visible item and the child view's top
            // value
            final AbsListView listView = (AbsListView) view;
            return listView.getChildCount() > 0 &&
                    (listView.getFirstVisiblePosition() > 0
                            || listView.getChildAt(0).getTop() < listView.getPaddingTop());
        } else {
            // For all other view types we just check the getScrollY() value
            return view.getScrollY() > 0;
        }
    }
}
