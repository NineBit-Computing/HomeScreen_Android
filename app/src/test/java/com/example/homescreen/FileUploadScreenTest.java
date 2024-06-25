//package com.example.homescreen;
//
//import android.app.Activity;
//import android.content.Intent;
//import android.net.Uri;
//
//import androidx.test.core.app.ApplicationProvider;
//
//import com.example.homescreen.FileUploadScreen;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.robolectric.Robolectric;
//import org.robolectric.RobolectricTestRunner;
//import org.robolectric.annotation.Config;
//import org.robolectric.shadows.ShadowActivity;
//import org.robolectric.shadows.ShadowSystemClock;
//import org.robolectric.Shadows;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//
//@RunWith(RobolectricTestRunner.class)
//@Config(sdk = 28, manifest = Config.NONE, resourceDir = "src/main/res")
//public class FileUploadScreenTest {
//
//    private FileUploadScreen activity;
//
//    @Test
//    public void testShowFileChooser() {
//        activity.findViewById(R.id.outlinedButton).performClick();
//        Intent expectedIntent = new Intent(Intent.ACTION_GET_CONTENT);
//        expectedIntent.setType("image/*");
//        expectedIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/jpeg", "image/png"});
//        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
//        Intent actualIntent = shadowActivity.getNextStartedActivity();
//        assertEquals(expectedIntent.getAction(), actualIntent.getAction());
//        assertEquals(expectedIntent.getType(), actualIntent.getType());
//        assertEquals(expectedIntent.getStringArrayExtra(Intent.EXTRA_MIME_TYPES), actualIntent.getStringArrayExtra(Intent.EXTRA_MIME_TYPES));
//    }
//
//
//
//}
