//package com.example.homescreen;
//
//import static androidx.test.espresso.Espresso.onView;
//import static androidx.test.espresso.action.ViewActions.click;
//import static androidx.test.espresso.assertion.ViewAssertions.matches;
//import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
//import static androidx.test.espresso.matcher.ViewMatchers.withId;
//import androidx.test.espresso.intent.rule.IntentsTestRule;
//import androidx.test.ext.junit.runners.AndroidJUnit4;
//import androidx.test.platform.app.InstrumentationRegistry;
//
//import org.junit.Before;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//@RunWith(AndroidJUnit4.class)
//public class FileUploadScreenTest {
//
//    @Rule
//    public IntentsTestRule<FileUploadScreen> activityTestRule = new IntentsTestRule<>(FileUploadScreen.class);
//
//    @Before
//    public void disableAnimations() {
//        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
//                "settings put global transition_animation_scale 0.0"
//        );
//        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
//                "settings put global window_animation_scale 0.0"
//        );
//        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
//                "settings put global animator_duration_scale 0.0"
//        );
//    }
//
//
//    @Test
//    public void check_terms_conditions(){
//        onView(withId(R.id.BtnNxt)).perform(click());
//    }
//
//    @Test
//    public void upload_icon_check(){
//        onView(withId(R.id.cloud)).perform(click());//click to cloud icon to show the upload file page
//        onView(withId(R.id.uploadFileSection)).check(matches(isDisplayed()));
//        onView(withId(R.id.outlinedButton)).check(matches(isDisplayed()));
//    }
//    @Test
//    public void testFileSelectionAndPreview() {
//        onView(withId(R.id.outlinedButton)).perform(click()); // Click on choose file button
//        // Perform action to select file using Espresso Intents
//        // Check if preview section becomes visible
//        // Check if upload button becomes visible
//        onView(withId(R.id.previewSection)).check(matches(isDisplayed()));
//        onView(withId(R.id.fileUploadBtn)).check(matches(isDisplayed()));
//    }
//
//
//    @Test
//    public void testUploadFile() {
//        // Click on upload button
//        onView(withId(R.id.fileUploadBtn)).perform(click());
//        // Assert if progress bar becomes visible
//        onView(withId(R.id.uploadProgressBar)).check(matches(isDisplayed()));
//
//        // Check if response handling is correct
//    }
//
//
//    @Test
//    public void testRunModel() {
//        // Click on run model button
//        // Assert if model run progress bar becomes visible
//        // Check if ML response is displayed correctly
//    }
//}
