/*
 *
 *  * Apache License
 *  *
 *  * Copyright [2017] Sinyuk
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.sinyuk.fanfou.ui.activities;


import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.sinyuk.fanfou.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class BottomNavigationTest {

    @Rule
    public ActivityTestRule<HomeActivity> mActivityTestRule = new ActivityTestRule<>(HomeActivity.class);

    @Test
    public void bottomNavigationTest() {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction textView = onView(
                allOf(withId(R.id.actionBarTitle), withText("主页"),
                        childAtPosition(
                                allOf(withId(R.id.titleView),
                                        childAtPosition(
                                                withId(R.id.actionBarSwitcher),
                                                0)),
                                0),
                        isDisplayed()));
        textView.check(matches(withText("主页")));

        ViewInteraction colorTabView = onView(
                allOf(childAtPosition(
                        childAtPosition(
                                withId(R.id.tabLayout),
                                0),
                        1),
                        isDisplayed()));
        colorTabView.perform(click());

        ViewInteraction imageView = onView(
                allOf(withId(R.id.endButton),
                        childAtPosition(
                                allOf(withId(R.id.endButtonSwitcher),
                                        childAtPosition(
                                                withId(R.id.actionBar),
                                                2)),
                                0),
                        isDisplayed()));
        imageView.check(matches(isDisplayed()));

        ViewInteraction colorTabView2 = onView(
                allOf(childAtPosition(
                        childAtPosition(
                                withId(R.id.tabLayout),
                                0),
                        2),
                        isDisplayed()));
        colorTabView2.perform(click());

        ViewInteraction textView2 = onView(
                allOf(withId(R.id.actionBarTitle), withText("消息"),
                        childAtPosition(
                                allOf(withId(R.id.titleView),
                                        childAtPosition(
                                                withId(R.id.actionBarSwitcher),
                                                0)),
                                0),
                        isDisplayed()));
        textView2.check(matches(withText("消息")));

        ViewInteraction colorTabView3 = onView(
                allOf(childAtPosition(
                        childAtPosition(
                                withId(R.id.tabLayout),
                                0),
                        3),
                        isDisplayed()));
        colorTabView3.perform(click());

        ViewInteraction textView3 = onView(
                allOf(withId(R.id.actionBarTitle), withText("私信"),
                        childAtPosition(
                                allOf(withId(R.id.titleView),
                                        childAtPosition(
                                                withId(R.id.actionBarSwitcher),
                                                0)),
                                0),
                        isDisplayed()));
        textView3.check(matches(withText("私信")));

        ViewInteraction colorTabView4 = onView(
                allOf(childAtPosition(
                        childAtPosition(
                                withId(R.id.tabLayout),
                                0),
                        0),
                        isDisplayed()));
        colorTabView4.perform(click());

        ViewInteraction textView4 = onView(
                allOf(withId(R.id.actionBarTitle), withText("主页"),
                        childAtPosition(
                                allOf(withId(R.id.titleView),
                                        childAtPosition(
                                                withId(R.id.actionBarSwitcher),
                                                0)),
                                0),
                        isDisplayed()));
        textView4.check(matches(withText("主页")));

    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
