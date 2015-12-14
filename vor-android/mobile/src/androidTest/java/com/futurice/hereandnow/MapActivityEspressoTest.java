package com.futurice.hereandnow;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import com.futurice.hereandnow.activity.MapActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by Luís Ramalho on 10/11/15.
 * <luis.ramalho@futurice.com>
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MapActivityEspressoTest {

    @Rule
    public ActivityTestRule<MapActivity> mActivityRule = new ActivityTestRule(MapActivity.class);

    @Test
    public void showsTheBlueprint() {
        onView(withId(R.id.futumap)).check(matches(isDisplayed()));
    }
}
