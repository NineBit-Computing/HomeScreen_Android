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

import org.json.JSONException;
import org.json.JSONObject;

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
    private TextView ml_response_text;


    // File URI
    private Uri fileUri;
    private String fileName;

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
        ml_response_text = findViewById(R.id.ml_response_text);



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
                      String signedUrl = "https://ninebit.s3.ap-south-1.amazonaws.com/312.jpg?Content-Type=image%2Fjpeg&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=ASIA3D7KSU77T7IBVQ5N%2F20240611%2Fap-south-1%2Fs3%2Faws4_request&X-Amz-Date=20240611T101204Z&X-Amz-Expires=600&X-Amz-Security-Token=IQoJb3JpZ2luX2VjEML%2F%2F%2F%2F%2F%2F%2F%2F%2F%2FwEaCmFwLXNvdXRoLTEiRzBFAiAwfgVxEfAp16HX%2Fd5JHyZ0Ujo6xiY5q6qEv8c4E4iO%2FwIhAIdj9IZUdA%2FZzraY1rrRVLAwsVjXiSvBYgyX%2B2S2nkKyKpIDCFsQAxoMNzY0NDU5MjYzOTk5IgxfLLBKFOH4frYNr7Eq7wIQlk82WY85w1FNcHhDZT9zn7hRVN94aToMQisMWeRFwXYthJQqxGKCLJbWXTQAOqeR6kcCA3TRpSKrCIdIt6qBqFyEy9pmxa2Jl2q0s2T7dGwEvOlc2F7VjjPdh0q5yL7tUHe4tsDOp4RBkb6%2FU%2BN0sip%2Ff2gDg%2BAIFnajRMIVGi2wKuctSTi2y9bk%2BV%2FWmNxSnc76IANv5UBwQbcgIcJuvHY3NbicSyWqKQJv6eFqZqF5FF4%2BOfrM9RiRYdw%2Fb64ugjR9jYeJJwXyYfpBvaQdJIBu4aH78kEp0iSTd1p50aHPRLvFTjhiwN%2FJ10n6vGSo2RF35nBtadJfe27vPRBUO0xLkibvHshezstIO8ddNvJQufZ7QTYSnZOV%2FGv8fIDtUkBQmvR6dAkSuVJaITWGiFOZ9SwFgl6opqqjORxd7vgEszx5Fl68%2FJMlt%2FNhM1GqLFPe92bsn9D1zwEWuwlg%2FhQixZnhqOmCzYjMednSMPTFoLMGOp0BvPzyrYOkl%2BQWztju6nBo%2F287Ssr86ilFo%2B7P9cIi4ESOrc8MO6kdfZmNOdk%2B8IddKBGhDExCSBo%2F67q3naDY90EfMh7%2FTogOd3UqKrsjdO596gteAVTShQqzIA4pApTA7be7894DboHCmyiugclG61XDtMocIxAb2aIMDQ19faO0x3LbWbFR0d31sd9uVbhLpUuXiKs1qk6a24o17A%3D%3D&X-Amz-Signature=54d29e8a4fc6fa5b7bd65f5c77f7fb3cf08352ae7c18c94dec3ebf4433d26760&X-Amz-SignedHeaders=host";
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
                    runOnUiThread(()-> uploadStatus.setText("Upload Successful"));
                    getMLresponse(fileName);
                } else {
                    Log.e("Upload", "ERROR: " + response.message());
                    runOnUiThread(()-> uploadStatus.setText("Upload Failed"));
                }
            }
        });
    }

    private void getMLresponse(String fileName) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://apis-dev.ninebit.in/py/v1/classify-image";
        String json = " {\n" +
                "    \"object\" : \"312.jpg\",\n" +
                "    \"model\": \"model1.h5\"\n" +
                "} ";
        RequestBody requestBody = RequestBody.create(json, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.e("ML Response", "ERROR", e);
                runOnUiThread(() -> uploadStatus.setText("ML Response Failed"));
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        JSONObject message = jsonResponse.getJSONObject("message");
                        JSONObject body = new JSONObject(message.getString("body"));

                        // Getting the "dRes" object
                        JSONObject dRes = body.getJSONObject("dRes");

                        // Getting the "emotion" value
                        String emotion = dRes.getString("emotion");

                        Log.i("Emotion", emotion);
                        runOnUiThread(() -> {
                            ml_response_text.setText("Emotion: " + emotion);
                            ml_response_text.setVisibility(View.VISIBLE);
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> uploadStatus.setText("ML Response: Failed (JSONException)"));
                    }
                } else {
                    Log.e("ML Response", "ERROR: " + response.message());
                    runOnUiThread(() -> uploadStatus.setText("ML Response Failed"));
                }
            }
        });
    }


}
