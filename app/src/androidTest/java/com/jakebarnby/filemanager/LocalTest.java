package com.jakebarnby.filemanager;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.util.Log;

import com.jakebarnby.filemanager.sources.SourceActivity;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class LocalTest {

    private static String sNewFolderName;
    private static String sTopFolderName = "Android";
    private static String sNewFileName = "newFile";

    @Rule
    public ActivityTestRule<SourceActivity> mActivityRule = new ActivityTestRule<>(SourceActivity.class);

    @BeforeClass
    public static void setUp() {
        createDummyFile();
        sNewFolderName = "AAAAAAAAAA Test " + new Random().nextInt();
    }

    @Before
    public void allowPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT >= 23) {
            UiDevice device = UiDevice.getInstance(getInstrumentation());
            UiObject allowPermissions = device.findObject(new UiSelector().text("ALLOW"));
            if (allowPermissions.exists()) {
                try {
                    allowPermissions.click();
                } catch (UiObjectNotFoundException e) {
                    Log.e("Permissions", "There is no permissions dialog to interact with ");
                }
            }
        }
    }

    @Test
    public void createFolderAtLocalRootAndDelete() {
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        onView(withText(R.string.menu_item_new_folder))
                .perform(click());

        onView(withId(R.id.txt_new_folder_name))
                .perform(typeText(sNewFolderName), closeSoftKeyboard());

        onView(withId(android.R.id.button1))
                .inRoot(isDialog())
                .perform(click());

        onView(allOf(withId(R.id.txt_item_title), withText(sNewFolderName)))
                .check(matches(isDisplayed()));

        onView(allOf(withId(R.id.txt_item_title), withText(sNewFolderName)))
                .perform(longClick());

        onView(withId(R.id.fab))
                .perform(click());
        Util.waitMillis(1000);

        onView(allOf(withId(R.id.title_view), withText(R.string.menu_item_delete)))
                .perform(click());

        onView(withId(android.R.id.button1))
                .inRoot(isDialog())
                .perform(click());

        onView(allOf(withId(R.id.txt_item_title), withText(sNewFolderName)))
                .check(doesNotExist());
    }

    @Test
    public void createFolderAtSublevelAndDelete() {
        onView(allOf(withId(R.id.txt_item_title), withText(sTopFolderName)))
                .perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        onView(withText(R.string.menu_item_new_folder))
                .perform(click());

        onView(withId(R.id.txt_new_folder_name))
                .perform(typeText(sNewFolderName), closeSoftKeyboard());

        onView(withId(android.R.id.button1))
                .inRoot(isDialog())
                .perform(click());

        onView(allOf(withId(R.id.txt_item_title), withText(sNewFolderName)))
                .check(matches(isDisplayed()));

        onView(allOf(withId(R.id.txt_item_title), withText(sNewFolderName)))
                .perform(longClick());

        onView(withId(R.id.fab))
                .perform(click());
        Util.waitMillis(1000);

        onView(allOf(withId(R.id.title_view), withText(R.string.menu_item_delete)))
                .perform(click());

        onView(withId(android.R.id.button1))
                .inRoot(isDialog())
                .perform(click());

        onView(allOf(withId(R.id.txt_item_title), withText(sNewFolderName)))
                .check(doesNotExist());
    }

    @Test
    public void copyDirectoryToLocalFromLocalAndDelete() {
        onView(allOf(withId(R.id.txt_item_title), withText(sTopFolderName)))
                .perform(click());

        onView(allOf(withId(R.id.txt_item_title), withText("data")))
                .perform(longClick());

        onView(withId(R.id.fab))
                .perform(click());
        Util.waitMillis(1000);

        onView(allOf(withId(R.id.title_view), withText(R.string.menu_item_copy)))
                .perform(click());

        onView(withText(R.string.copied))
                .check(matches(isDisplayed()));

        Espresso.pressBack();

        onView(withId(R.id.fab))
                .perform(click());

        onView(allOf(withId(R.id.title_view), withText(R.string.menu_item_paste)))
                .perform(click());

        onView(allOf(withId(R.id.txt_item_title), withText("data")))
                .check(matches(isDisplayed()));

        onView(allOf(withId(R.id.txt_item_title), withText("data")))
                .perform(longClick());

        onView(withId(R.id.fab))
                .perform(click());
        Util.waitMillis(1000);

        onView(allOf(withId(R.id.title_view), withText(R.string.menu_item_delete)))
                .perform(click());

        onView(withId(android.R.id.button1))
                .inRoot(isDialog())
                .perform(click());

        onView(allOf(withId(R.id.txt_item_title), withText("data")))
                .check(doesNotExist());
    }

    @Test
    public void copyFileToLocalFromLocalAndDelete() {
        onView(allOf(withId(R.id.txt_item_title), withText(sTopFolderName)))
                .perform(click());

        onView(allOf(withId(R.id.txt_item_title), withText(sNewFileName)))
                .perform(longClick());

        onView(withId(R.id.fab))
                .perform(click());
        Util.waitMillis(1000);

        onView(allOf(withId(R.id.title_view), withText(R.string.menu_item_copy)))
                .perform(click());

        onView(withText(R.string.copied))
                .check(matches(isDisplayed()));

        Espresso.pressBack();

        onView(withId(R.id.fab))
                .perform(click());
        Util.waitMillis(1000);

        onView(allOf(withId(R.id.title_view), withText(R.string.menu_item_paste)))
                .perform(click());

        onView(allOf(withId(R.id.txt_item_title), withText(sNewFileName)))
                .check(matches(isDisplayed()));

        onView(allOf(withId(R.id.txt_item_title), withText(sNewFileName)))
                .perform(longClick());

        onView(withId(R.id.fab))
                .perform(click());

        onView(allOf(withId(R.id.title_view), withText(R.string.menu_item_delete)))
                .perform(click());

        onView(withId(android.R.id.button1))
                .inRoot(isDialog())
                .perform(click());

        onView(allOf(withId(R.id.txt_item_title), withText(sNewFileName)))
                .check(doesNotExist());
    }

    private static void createDummyFile() {
        File file = new File(Environment.getExternalStorageDirectory().getPath()+File.separator+"Android"+File.separator+sNewFileName);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
