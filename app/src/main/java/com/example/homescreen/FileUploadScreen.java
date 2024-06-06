package com.example.homescreen;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import android.view.View;



public class FileUploadScreen extends AppCompatActivity {
    private static final int FILE_SELECT_CODE = 0;
    int progress = 0;
    private ProgressBar uploadProgress;
    private TextView uploadStatus;
    private LinearLayout uploadFileSection;
    private LinearLayout previewSection;
    private LinearLayout uploadProgressSection;
    private ImageView previewImage;
    private TextView uploadProgressPercentage;

    // File URI
    private Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_upload_screen);

        uploadFileSection = findViewById(R.id.uploadFileSection);
        previewSection = findViewById(R.id.previewSection);
        uploadProgressSection = findViewById(R.id.uploadProgressSection);

        TextView uploadTitle = findViewById(R.id.upload_title);
        ImageView uploadIcon = findViewById(R.id.upload_icon);
        TextView uploadHint = findViewById(R.id.upload_hint);
        Button chooseFileButton = findViewById(R.id.outlinedButton);
        uploadProgress = findViewById(R.id.upload_progress);
        uploadProgressPercentage = findViewById(R.id.upload_progress_percentage);
        uploadStatus = findViewById(R.id.upload_status);
        previewImage = findViewById(R.id.previewImage);

        chooseFileButton.setOnClickListener(v -> showFileChooser());

        Button uploadButton = findViewById(R.id.fileUploadBtn);
        Button cancelButton = findViewById(R.id.cancelBtn);

        uploadButton.setOnClickListener(v -> startUpload());
        cancelButton.setOnClickListener(v -> cancelUpload());

        setProgressValue(progress);
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // Change this if you want to allow other types of files
        startActivityForResult(Intent.createChooser(intent, "Select a file"), FILE_SELECT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_SELECT_CODE && resultCode == RESULT_OK && data != null) {
            fileUri = data.getData();
            if (fileUri != null) {
                handleFilePreview(fileUri);
            }
        }
    }

    private void handleFilePreview(Uri uri) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            if (inputStream != null) {
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                previewImage.setImageBitmap(bitmap);
                previewSection.setVisibility(View.VISIBLE);
                uploadFileSection.setVisibility(View.GONE);
                uploadProgressSection.setVisibility(View.GONE);  // Hide the upload progress section
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startUpload() {
        if (fileUri != null) {
            String filePath = getPathFromUri(this, fileUri);
            if (filePath != null && !filePath.isEmpty()) {
                File file = new File(filePath);
                if (file.exists()) {
                    try {
                        // Use your API endpoint for uploading
                        String signedUrl = "https://ninebit.s3.ap-south-1.amazonaws.com/IMG_20240423_174708.jpg?Content-Type=image%2Fjpeg&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=ASIA3D7KSU77YT66VCTN%2F20240606%2Fap-south-1%2Fs3%2Faws4_request&X-Amz-Date=20240606T093732Z&X-Amz-Expires=600&X-Amz-Security-Token=IQoJb3JpZ2luX2VjEEoaCmFwLXNvdXRoLTEiRjBEAiBmHyhXBT%2FpQ5fN1SmVGiJ96sf4KSHuKM3qv30HFDMhggIgW5HuRwMBeagLe92bx3oqqATOJpIqs2njyMkqziHw2%2F0qmwMI0%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2FARADGgw3NjQ0NTkyNjM5OTkiDMfHBe7e0qykENZhYSrvAsXNnzjd7JrT%2FOx5WZuCygd%2BEaOhREllTQUtTq33zhzH5t0YsM9B2vdsjzCYzzCjmt%2FRsUih%2BDVJ%2Fh0Uwy92NaomOI%2Btj%2FdklWnSFJTKdjOUXxe2LWUvR%2BSjBro1hpG88bfpsMt%2BrcjGTi6Bs65raA5r%2BeT3sILKYOoW%2FugjWN3pAml6bX1E7NfS%2B6o0chalacot1knp6ROQXr211tsjBDK9HHTQEGYfIL5A4J8mB7KtoksWyEoUc%2FTksszH5Xy%2FQKOTE41GLuS3yPPOSmpVZpT2vIURw5cmxt%2F8AeCRG2UqGN0DyxR4v%2Fl3xfvthliLC2jRp4ajGQE9W5suCiO3N30NON0gXapAQg%2FqaqWy4qfwAsLECKq3M0b6SXEYeBPyUPKgBgXub4eURgtRk0Euwg2ZqkN%2BdiSrTDi%2B3AAUfyEHKQ1AuLxIddLLGiaPGfL5s6%2BmTVGxpRBOWJPPBPzVU7Zs6EeJzGVq70FF3%2FlqlSAw24aGswY6ngGRp%2Ff3U6PMCQLg2pOeoBPkAf73mlPPg4Yh9WR67gxbUyWM3TBHcWExksmkridfMhWcok24y7F7U%2Fi9BgeJffb5rTVBiDD1dVxi1ZW%2BS8EQL1yDgC5garQnBf1ReBqJjIy9trePEAZwRFyU4RCFTyLBp60cd%2BwKO92YSZFofx2yNnL6uBWlGq8rEik46APL7LpCFXMmYCb23I9m%2BS1SkQ%3D%3D&X-Amz-Signature=4aee5595d6e96d1ef3a72024c8f3f0f29d8e3aed59286cd62ba89a7374905ebc&X-Amz-SignedHeaders=host";
                        uploadToS3(file, signedUrl);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e("FileUpload", "File not found: " + filePath   );
                }
            } else {
                Log.e("FileUpload", "Failed to retrieve file path from URI: " + fileUri);
            }
        } else {
            Log.e("FileUpload", "No file selected");
        }

//        previewSection.setVisibility(View.GONE);
//        uploadProgressSection.setVisibility(View.VISIBLE);
    }

    private void cancelUpload() {
        previewSection.setVisibility(View.GONE);
        uploadFileSection.setVisibility(View.VISIBLE);
        uploadProgressSection.setVisibility(View.VISIBLE);
    }

    private void setProgressValue(final int progress) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                uploadProgress.setProgress(progress);
                uploadProgressPercentage.setText(progress + "%");
            }
        });

        // thread is used to change the progress value
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    if (progress < 100) {
                        setProgressValue(progress + 10);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }


    private String getPathFromUri(Context context, Uri uri) {
        String filePath = null;
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                File tempFile = File.createTempFile("temp_image", null, context.getCacheDir());
                OutputStream outputStream = new FileOutputStream(tempFile);
                byte[] buffer = new byte[1024];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
                filePath = tempFile.getAbsolutePath();
                inputStream.close();
                outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filePath;
    }

    private void uploadToS3(File file, String signedUrl) throws IOException {
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("image/jpeg");
        RequestBody requestBody = RequestBody.create(file, mediaType);

        Request request = new Request.Builder()
                .url(signedUrl)
                .put(requestBody)
                .addHeader("Content-Type", "image/jpeg")
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.e("Upload", "ERROR", e);
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.i("Upload", "SUCCESS");
                } else {
                    Log.e("Upload", "ERROR: " + response.message());
                }
            }
        });
    }
}