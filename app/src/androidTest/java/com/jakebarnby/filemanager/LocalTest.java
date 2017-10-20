package com.jakebarnby.filemanager;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.jakebarnby.filemanager.sources.SourceActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class LocalTest {

    private String mNewFolderName;

    @Before
    public void setUp() {
        mNewFolderName = "Test " + new Random().nextInt();
    }

    @Rule
    public ActivityTestRule<SourceActivity> mActivityRule = new ActivityTestRule<>(SourceActivity.class);

    @Test
    public void createFolderAtLocalRootTest() {
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        onView(withText(R.string.menu_item_new_folder))
                .perform(click());

        onView(withId(R.id.txt_new_folder_name))
                .perform(typeText(mNewFolderName), closeSoftKeyboard());

        onView(withText("OK"))
                .perform(click());

        //onView(allOf(withId(R.id.txt_item_title), withText(mNewFolderName)))
         //       .check(matches(isDisplayed()));
    }

    @Test
    public void createFolderAtSublevelTest() {
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        onView(allOf(withId(R.id.txt_item_title), withText("Android")))
                .perform(click());

        onView(withText(R.string.menu_item_new_folder))
                .perform(click());

        onView(withId(R.id.txt_new_folder_name))
                .perform(typeText(mNewFolderName), closeSoftKeyboard());

        onView(withText("OK"))
                .perform(click());

        //onView(allOf(withId(R.id.txt_item_title), withText(mNewFolderName)))
        //        .check(matches(isDisplayed()));
    }

    @Test
    public void copyDirectoryToLocalFromLocalSameDirectory() {
        String folderToCopy = "Android";

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        onView(allOf(withId(R.id.txt_item_title), withText(folderToCopy)))
                .perform(longClick());

        onView(withId(R.id.fab))
                .perform(click());

        onView(allOf(withId(R.id.title_view), withText(R.string.menu_item_copy)))
                .perform(click());

        onView(withText(R.string.copied))
                .check(matches(isDisplayed()));

        onView(withId(R.id.fab))
                .perform(click());

        onView(allOf(withId(R.id.title_view), withText(R.string.menu_item_paste)))
                .perform(click());

        //onView(allOf(withId(R.id.txt_item_title), withText(folderToCopy)))
         //       .check(matches(isDisplayed()));
    }
}
