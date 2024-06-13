package com.example.homescreen;

import android.annotation.SuppressLint;
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
import android.webkit.MimeTypeMap;
import android.widget.Toast;

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
        initViews();
        setButtonListeners();
        setProgressValue(0);
    }

    private void initViews() {
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
    }
    private void setButtonListeners() {
        Button chooseFileButton = findViewById(R.id.outlinedButton);
        chooseFileButton.setOnClickListener(v -> showFileChooser());

        Button uploadButton = findViewById(R.id.fileUploadBtn);
        uploadButton.setOnClickListener(v -> startUpload());

        Button cancelButton = findViewById(R.id.cancelBtn);
        cancelButton.setOnClickListener(v -> cancelUpload());

    }


    private void showFileChooser() {                                                                //Method to Select the image file to be uploaded(in jpeg or in png)
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/jpeg", "image/png"});          // Specify allowed MIME types
        startActivityForResult(Intent.createChooser(intent, "Select a file"), FILE_SELECT_CODE);

    }
    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {   //Handles the result from the file chooser
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_SELECT_CODE && resultCode == RESULT_OK && data != null) {
            fileUri = data.getData();
            if (fileUri != null) {
                handleFilePreview(fileUri);
            } else {
                Log.e("FileUpload", "File URI is null");
                uploadStatus.setText("Failed to retrieve file");
            }
        } else {
            Log.e("FileUpload", "File selection failed or cancelled");
            uploadStatus.setText("No file selected");
        }
    }

    @SuppressLint("SetTextI18n")
    private void handleFilePreview(Uri uri) {  //Method to Preview The selected image file , opens an input stream to read the selected file , decodes it into bitmap and displays in image view
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            if (inputStream != null) {
                Toast.makeText(getApplicationContext(), "Preview Successful", Toast.LENGTH_SHORT).show();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                previewImage.setImageBitmap(bitmap);
                previewSection.setVisibility(View.VISIBLE);
                uploadFileSection.setVisibility(View.GONE);
                uploadProgressSection.setVisibility(View.GONE);
            } else {
                Log.e("FileUpload", "Input stream is null");
                uploadStatus.setText("Failed to open file");
            }
        } catch (Exception e) {
            Log.e("FileUpload", "Error handling file preview", e);
            uploadStatus.setText("Error handling file preview");
        }
    }

    private void cancelUpload() {  // Method for Cancel the Browse Image
        previewSection.setVisibility(View.GONE);
        uploadFileSection.setVisibility(View.VISIBLE);
        uploadProgressSection.setVisibility(View.GONE);
    }

    @SuppressLint("SetTextI18n")
    private void setProgressValue(final int progress) { //Progress Bar component
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



    // 1. startUpload() method -> Uploading the image file (Which includes 2 Methods-> Getting the image file Path and using that image file path to Generate Signed URL)
    private void startUpload() {
        if (fileUri != null) {
            String filePath = getPathFromUri(this, fileUri);
            if (filePath != null && !filePath.isEmpty()) {
                File file = new File(filePath);
                if (file.exists()) {
                Toast.makeText(getApplicationContext(), "File Has Been Uploaded Successfully", Toast.LENGTH_SHORT).show();
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

    //2. getPathFromUri() method --> Getting the path of the Image File
    @SuppressLint("SetTextI18n")
    private String getPathFromUri(Context context, Uri uri) {
        String filePath = null;
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            if (inputStream != null) {
                String fileExtension = getFileExtension(context, uri);              // Determining the file extension dynamically
                if (fileExtension == null  || fileExtension.isEmpty()){
                    fileExtension="";                                              // If the MIME Type is not recognised do not set any extension
                }
                else {
                    fileExtension= "." + fileExtension;
                }
                File tempFile = File.createTempFile("temp_image", fileExtension, context.getCacheDir());   //Create a temporary file with the appropriate extension
                try (OutputStream outputStream = new FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, read);
                    }
                    filePath = tempFile.getAbsolutePath();
                }catch (IOException e) {
                    Log.e("FileUpload", "Error writing to temp file", e);
                    uploadStatus.setText("Error writing to temp file");
                }
            } else {
                Log.e("FileUpload", "Input stream is null");
            }
        } catch (IOException e) {
            Log.e("FileUpload", "Error opening input stream", e);
        }
        return filePath;
    }
    //Helper method to get the file extension from the URI
    private  String getFileExtension(Context context, Uri uri){
        String extension = null;
        String mimeType = context.getContentResolver().getType(uri);
        if (mimeType!= null){
            extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
        }
        return extension;
    }


    //3. generateSignedUrl() method--> After Getting the path generating signed URL by calling method (post)
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
                .post(requestBody) //post
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.e("GenerateSignedUrl", "ERROR: Failed to make request", e);
                runOnUiThread(() -> uploadStatus.setText("Failed to generate signed URL"));
            }
            @SuppressLint("SetTextI18n")
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


    //4. uploadToS3() method--> After generating Signed URL it will be uploaded to the Server(put) For Checking the response generated by ML Model
    private void uploadToS3(File file, String signedUrl) {
        OkHttpClient client = new OkHttpClient();

        String mediaType = getContentResolver().getType(fileUri);
        RequestBody requestBody = RequestBody.create(file, MediaType.parse(mediaType));

        Request request = new Request.Builder()
                .url(signedUrl)
                .put(requestBody) //put
                .addHeader("Content-Type", mediaType)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.e("Upload", "Error Occurred While Uploading", e);
                runOnUiThread(() -> uploadStatus.setText("Upload Failed"));
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.i("Upload", "Successfully Uploaded");
                    runOnUiThread(() -> uploadStatus.setText("Upload Successful"));
                    getMLResponse(file.getName());
                } else {
                    Log.e("Upload", "ERROR: " + response.message());
                    runOnUiThread(() -> uploadStatus.setText("Upload Failed"));
                }
            }
        });
    }


    //5. getMLResponse() method--> After Uploading it ML Model will give the response of the Image file and display on View through API call
    private void getMLResponse(String fileName) {
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
            @SuppressLint("SetTextI18n")
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.e("ML Response", "Error While Fetching the Response", e);
                runOnUiThread(() -> uploadStatus.setText("ML Response Failed"));
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        JSONObject message = jsonResponse.getJSONObject("message");
                        JSONObject body = new JSONObject(message.getString("body"));
                        JSONObject dRes = body.getJSONObject("d_res");
                        String emotion = dRes.getString("emotion");

                        Log.i("Emotion", emotion);
                        runOnUiThread(() -> {
                            ml_response_text.setText("Emotion: " + emotion);
                            ml_response_text.setVisibility(View.VISIBLE);
                        });
                    } catch (JSONException e) {
                        Log.e("ML Response", "Failed to parse JSON response", e);
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
