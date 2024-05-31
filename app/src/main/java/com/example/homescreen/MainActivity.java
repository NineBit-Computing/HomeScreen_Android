package com.example.homescreen;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListAdapter listAdapter;
    ArrayList<ListData> dataArrayList;
    ListData listData;
    private static final String DATA_ARRAY_LIST_KEY = "dataArrayList";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_);

        if (savedInstanceState != null) {
            dataArrayList = (ArrayList<ListData>) savedInstanceState.getSerializable(DATA_ARRAY_LIST_KEY);
        } else {
            dataArrayList = new ArrayList<>();
            int[] imageList = {R.drawable.baseline_camera_alt_24, R.drawable.baseline_emergency_recording_24, R.drawable.baseline_mic_24, R.drawable.baseline_file_copy_24, R.drawable.baseline_camera_alt_24, R.drawable.baseline_file_copy_24, R.drawable.baseline_mic_24};
            String[] nameList = {"IMG_10010", "Startup pitch", "Audio pitch", "Work proposal", "IMG_000111", "Survey file", "Speech"};
            String[] timeList = {"PNG file", "AVI file", "MP3 file", "DOCs file", "JPEG file", "DOCs file", "MP3 file"};
            int deleteIcon = R.drawable.baseline_delete_24;

            for (int i = 0; i < imageList.length; i++) {
                listData = new ListData(nameList[i], timeList[i], imageList[i], deleteIcon);
                dataArrayList.add(listData);
            }
        }

        listAdapter = new ListAdapter(MainActivity.this, dataArrayList);
        ListView listView = findViewById(R.id.listview);
        listView.setAdapter(listAdapter);
        listView.setClickable(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, "Clicked item: " + dataArrayList.get(position).getName(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(DATA_ARRAY_LIST_KEY, dataArrayList);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.keyboardHidden == Configuration.KEYBOARDHIDDEN_YES) {
            Toast.makeText(this, "Keyboard available", Toast.LENGTH_SHORT).show();
        } else if (newConfig.keyboardHidden == Configuration.KEYBOARDHIDDEN_NO) {
            Toast.makeText(this, "No keyboard", Toast.LENGTH_SHORT).show();
        }
    }
}
