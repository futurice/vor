package com.futurice.vor.view;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class CustomDrawerLayout extends DrawerLayout {

    public CustomDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /*
    * There is a problem with PhotoView's behavior with DrawerLayout.
    * This override prevents the app from crashing.
    * https://github.com/chrisbanes/PhotoView/issues/72
    */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        try {
            return super.onInterceptTouchEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
