package com.futurice.vor.interfaces;

/**
 * http://stackoverflow.com/questions/21914808/how-to-handle-asynctasks-in-actionbaractivity-fragments-when-viewpager-is-used/24386516#24386516
 */
public interface FragmentLifecycle {
    void onResumeFragment();
    void onPauseFragment();
}
