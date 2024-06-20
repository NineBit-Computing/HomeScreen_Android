package com.example.homescreen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements TermsAndConditionsDialogFragment.TermsAndConditionsListener {

    ListAdapter listAdapter;
    ArrayList<ListData> dataArrayList;
    ListData listData;
    private static final String DATA_ARRAY_LIST_KEY = "dataArrayList";
    private static final String PREFS_NAME = "TermsPrefs";
    private static final String TERMS_ACCEPTED_KEY = "TermsAccepted";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_);

        // To Check if terms have been accepted or not (by default value-> false)
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean termsAccepted = preferences.getBoolean(TERMS_ACCEPTED_KEY, false);

        if (!termsAccepted) {
            showTermsAndConditionsDialog();
        }

        // Initialize or restore dataArrayList
        if (savedInstanceState != null) {
            dataArrayList = (ArrayList<ListData>) savedInstanceState.getSerializable(DATA_ARRAY_LIST_KEY);
        } else {
            dataArrayList = new ArrayList<>();
            // Populate dataArrayList with initial data if needed
            // For demonstration, adding sample data:
            int[] imageList = {
                    R.drawable.baseline_camera_alt_24, R.drawable.baseline_emergency_recording_24,
                    R.drawable.baseline_mic_24, R.drawable.baseline_file_copy_24,
                    R.drawable.baseline_camera_alt_24, R.drawable.baseline_file_copy_24,
                    R.drawable.baseline_mic_24
            };
            String[] nameList = {"IMG_10010", "Startup pitch", "Audio pitch", "Work proposal", "IMG_000111", "Survey file", "Speech"};
            String[] timeList = {"PNG file", "AVI file", "MP3 file", "DOCs file", "JPEG file", "DOCs file", "MP3 file"};
            int deleteIcon = R.drawable.baseline_delete_24;

            for (int i = 0; i < imageList.length; i++) {
                listData = new ListData(nameList[i], timeList[i], imageList[i], deleteIcon);
                dataArrayList.add(listData);
            }
        }

        // Setup ListView
        listAdapter = new ListAdapter(MainActivity.this, dataArrayList);
        ListView listView = findViewById(R.id.listview);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, "Clicked item: " + dataArrayList.get(position).getName(), Toast.LENGTH_SHORT).show();
            }
        });

        // Setup BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.cloud) {
                    Intent intent = new Intent(MainActivity.this, FileUploadScreen.class);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
    }

    // Show Terms and Conditions dialog fragment
    private void showTermsAndConditionsDialog() {
        TermsAndConditionsDialogFragment dialogFragment = new TermsAndConditionsDialogFragment();
        dialogFragment.setCancelable(false); // Make the dialog non-cancelable
        dialogFragment.show(getSupportFragmentManager(), "TermsAndConditionsDialog");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(DATA_ARRAY_LIST_KEY, dataArrayList);
    }

    @Override
    public void onTermsAccepted() {
        // Saving terms acceptance state
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(TERMS_ACCEPTED_KEY, true);
        editor.apply();
        // Continue with main activity functionality
        Toast.makeText(this, "Terms Accepted", Toast.LENGTH_SHORT).show();
    }
}
