package com.example.homescreen;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
public class FileUploadScreen extends AppCompatActivity {
    private static final int FILE_SELECT_CODE = 0;
    private ProgressBar uploadProgress;
    private TextView uploadStatus;
    private LinearLayout uploadFileSection;
    private LinearLayout previewSection;
    private LinearLayout uploadProgressSection;
    private ImageView previewImage;
    private TextView uploadProgressPercentage;
    private TextView ml_response_text;
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
        ml_response_text = findViewById(R.id.ml_response_text);
        chooseFileButton.setOnClickListener(v -> showFileChooser());
        Button uploadButton = findViewById(R.id.fileUploadBtn);
        Button cancelButton = findViewById(R.id.cancelBtn);
        uploadButton.setOnClickListener(v -> startUpload());
        cancelButton.setOnClickListener(v -> cancelUpload());

        setProgressValue(0);
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
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
                uploadProgressSection.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cancelUpload() {
        previewSection.setVisibility(View.GONE);
        uploadFileSection.setVisibility(View.VISIBLE);
        uploadProgressSection.setVisibility(View.GONE);
    }

    private void setProgressValue(final int progress) {
        runOnUiThread(() -> {
            uploadProgress.setProgress(progress);
            uploadProgressPercentage.setText(progress + "%");
        });

        new Thread(() -> {
            try {
                Thread.sleep(1000);
                if (progress < 100) {
                    setProgressValue(progress + 10);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Uploading the file function (Which includes getting File Path and using that file path to Generate Signed URL)
    private void startUpload() {
        if (fileUri != null) {
            String filePath = getPathFromUri(this, fileUri);
            if (filePath != null && !filePath.isEmpty()) {
                File file = new File(filePath);
                if (file.exists()) {
                    generateSignedUrl(file);
                } else {
                    Log.e("FileUpload", "File not found: " + filePath);
                }
            } else {
                Log.e("FileUpload", "Failed to retrieve file path from URI: " + fileUri);
            }
        } else {
            Log.e("FileUpload", "No file selected");
        }
    }

    //Getting the path of the Image File
    private String getPathFromUri(Context context, Uri uri) {
        String filePath = null;
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            if (inputStream != null) {
                File tempFile = File.createTempFile("temp_image", null, context.getCacheDir());
                try (OutputStream outputStream = new FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, read);
                    }
                    filePath = tempFile.getAbsolutePath();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filePath;
    }


    //After Getting the path generating signed URL by calling method generateSignedUrl
    private void generateSignedUrl(File file) {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        String fileName = file.getName();
        String url = "https://apis-dev.ninebit.in/presigned/putObject/" + fileName;

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName, RequestBody.create(file, MediaType.parse("application/octet-stream")))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.e("GenerateSignedUrl", "ERROR: Failed to make request", e);
                runOnUiThread(() -> uploadStatus.setText("Failed to generate signed URL"));
            }
            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d("GenerateSignedUrl", "Response body: " + responseBody);
                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        String message = jsonObject.getString("message");   // Retrieving the URL from the "message" key
                        uploadToS3(file, message);                                // Passing the URL to the next step
                    } catch (JSONException e) {
                        Log.e("GenerateSignedUrl", "ERROR: Failed to parse JSON response", e);
                        runOnUiThread(() -> uploadStatus.setText("Failed to parse signed URL response"));
                    }
                } else {
                    String responseBody = response.body().string();
                    Log.e("GenerateSignedUrl", "ERROR: " + response.code() + " " + response.message() + " " + responseBody);
                    runOnUiThread(() -> uploadStatus.setText("Failed to generate signed URL"));
                }
            }

        });
    }


    //After generating Signed URL it will be uploaded to the server
    private void uploadToS3(File file, String signedUrl) {
        OkHttpClient client = new OkHttpClient();

        String mediaType = getContentResolver().getType(fileUri);
        RequestBody requestBody = RequestBody.create(file, MediaType.parse(mediaType));

        Request request = new Request.Builder()
                .url(signedUrl)
                .put(requestBody)
                .addHeader("Content-Type", mediaType)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.e("Upload", "ERROR", e);
                runOnUiThread(() -> uploadStatus.setText("Upload Failed"));
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.i("Upload", "SUCCESS");
                    runOnUiThread(() -> uploadStatus.setText("Upload Successful"));
                    getMLresponse(file.getName());
                } else {
                    Log.e("Upload", "ERROR: " + response.message());
                    runOnUiThread(() -> uploadStatus.setText("Upload Failed"));
                }
            }
        });
    }


    //After Uploading it will give the ML response and display on View through API call
    private void getMLresponse(String fileName) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://apis-dev.ninebit.in/py/v1/classify-image";

        String json = " {\n" +
                "    \"object\" : \"" + fileName + "\",\n" +
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
                        JSONObject dRes = body.getJSONObject("dRes");
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
