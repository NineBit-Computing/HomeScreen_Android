package com.example.homescreen;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
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
    private LinearLayout uploadFileSection;
    private LinearLayout previewSection;
    private ImageView previewImage;
    private TextView ml_response_text;
    private Uri fileUri;
    private String filePath;

    private ImageView backButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e( "onCreate: ", "onCreate of file upload screen" );
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_upload_screen);
        initViews();
        setButtonListeners();
    }

    private void initViews() {
        uploadFileSection = findViewById(R.id.uploadFileSection);
        previewSection = findViewById(R.id.previewSection);
        previewImage = findViewById(R.id.previewImage);
        ml_response_text = findViewById(R.id.ml_response_text);
        backButton = findViewById(R.id.back_button);

    }
    private void setButtonListeners() {
        Button chooseFileButton = findViewById(R.id.outlinedButton);
        chooseFileButton.setOnClickListener(v -> showFileChooser());

        Button uploadButton = findViewById(R.id.fileUploadBtn);
        uploadButton.setOnClickListener(v -> startUpload());

        Button cancelButton = findViewById(R.id.cancelBtn);
        cancelButton.setOnClickListener(v -> showCancelConfirmationDialog());

        backButton.setOnClickListener(v -> navigateBackToFileSelection());

    }

    //Method to Select the image file to be uploaded(in jpeg or in png)
    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/jpeg", "image/png"});          // Specify allowed MIME types
        startActivityForResult(Intent.createChooser(intent, "Select a file"), FILE_SELECT_CODE);

    }
    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {       //Handles the result from the file chooser
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_SELECT_CODE && resultCode == RESULT_OK && data != null) {
            fileUri = data.getData();
            if (fileUri != null) {
                handleFilePreview(fileUri);
            } else {
                Log.e("FileUpload", "File URI is null");

            }
        } else {
            Log.e("FileUpload", "File selection failed or cancelled");

        }
    }


    //Method to Preview The selected image file , opens an input stream to read the selected file , decodes it into bitmap and displays in image view
    @SuppressLint("SetTextI18n")
    private void handleFilePreview(Uri uri) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            if (inputStream != null) {
                Toast.makeText(getApplicationContext(), R.string.preview_success_label, Toast.LENGTH_SHORT).show();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                previewImage.setImageBitmap(bitmap);
                previewSection.setVisibility(View.VISIBLE);
                uploadFileSection.setVisibility(View.GONE);
            } else {
                Log.e("FileUpload", "Input stream is null");

            }
        } catch (Exception e) {
            Log.e("FileUpload", "Error handling file preview", e);

        }
    }


//    Methods for Navigation Back button, Show Cancel Confirmation, Cancel Upload
    private void navigateBackToFileSelection() {
        previewSection.setVisibility(View.GONE);
        uploadFileSection.setVisibility(View.VISIBLE);
        ml_response_text.setText("");
        ml_response_text.setVisibility(View.GONE);
    }
    private void showCancelConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.dialog_box_label)
                .setPositiveButton("Yes", (dialog, which) -> cancelUpload())
                .setNegativeButton("No", null)
                .show();
    }
    private void cancelUpload() {  // Method for Cancel the Browse Image
        previewSection.setVisibility(View.GONE);
        uploadFileSection.setVisibility(View.VISIBLE);
        ml_response_text.setText("");
        ml_response_text.setVisibility(View.GONE);
    }


    // 1. startUpload() method -> Uploading the image file (Which includes 2 Methods-> Getting the image file Path and using that image file path to Generate Signed URL)
    private void startUpload() {
        if (fileUri != null) {
             getPathFromUri(this, fileUri, this::generateSignedUrl);
        } else {
            Log.e("FileUpload", "No file selected");
        }
    }

    //2. getPathFromUri() method --> Getting the path of the Image File
    @SuppressLint("SetTextI18n")
    private void getPathFromUri(Context context, Uri uri, OnPathRetrievedListener listener) {
        new Thread(() -> {
            String filePath = null;
            try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
                if (inputStream != null) {
                    String fileExtension = getFileExtension(context, uri);                                            // Determining the file extension dynamically
                    if (fileExtension == null || fileExtension.isEmpty()) {
                        fileExtension = "";                                                                          // If the MIME Type is not recognised do not set any extension
                    } else {
                        fileExtension = "." + fileExtension;
                    }
                    File tempFile = File.createTempFile("temp_image", fileExtension, context.getCacheDir());   //Create a temporary file with the appropriate extension
                    try (OutputStream outputStream = new FileOutputStream(tempFile)) {
                        byte[] buffer = new byte[1024];
                        int read;
                        while ((read = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, read);
                        }
                        filePath = tempFile.getAbsolutePath();
                    } catch (IOException e) {
                        Log.e("FileUpload", "Error writing to temp file", e);
                    }
                } else {
                    Log.e("FileUpload", "Input stream is null");
                }
            } catch (IOException e) {
                Log.e("FileUpload", "Error opening input stream", e);
            }
            String finalFilePath = filePath;
            runOnUiThread(()-> listener.OnPathRetrivedListener(finalFilePath));
        }).start();
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
    private void generateSignedUrl(String filePath) {
        this.filePath = filePath; // Store the file path for later use
        if (filePath != null && !filePath.isEmpty()) {
            File file = new File(filePath);
            if (file.exists()) {
                OkHttpClient client = new OkHttpClient.Builder()
                        .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
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
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onFailure(okhttp3.Call call, IOException e) {
                        Log.e("GenerateSignedUrl", "ERROR: Failed to make request", e);
                        System.out.println("Error while getting signed url: " + e.getMessage());
                    }

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResponse(okhttp3.Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String responseBody = response.body().string();
                            System.out.println("Signed URL generated successfully: " + responseBody);
                            try {
                                JSONObject jsonObject = new JSONObject(responseBody);
                                String message = jsonObject.getString("message");
                                uploadToS3(message); // Only pass the signed URL
                            } catch (JSONException e) {
                                Log.e("GenerateSignedUrl", "ERROR: Failed to parse JSON response", e);
                            }
                        } else {
                            String responseBody = response.body().string();
                            Log.e("GenerateSignedUrl", "ERROR: " + response.code() + " " + response.message() + " " + responseBody);
                        }
                    }
                });
            }
        }
    }

    //4. uploadToS3() method--> After generating Signed URL it will be uploaded to the Server(put). For Checking the response generated by ML Model
    private void uploadToS3(String signedUrl) {
        if (filePath == null) {
            Log.e("Upload", "File path is null");
            return;
        }

        File file = new File(filePath);
        if (!file.exists()) {
            Log.e("Upload", "File does not exist");
            return;
        }

        OkHttpClient client = new OkHttpClient();

        String mediaType = getContentResolver().getType(fileUri); // Ensure fileUri is still accessible
        RequestBody requestBody = RequestBody.create(file, MediaType.parse(mediaType));

        Request request = new Request.Builder()
                .url(signedUrl)
                .put(requestBody)
                .addHeader("Content-Type", mediaType)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.e("Upload", "Error Occurred While Uploading", e);
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.i("Upload", "Successfully Uploaded");
                    getMLResponse(file.getName());
                } else {
                    Log.e("Upload", "ERROR: " + response.message());
                }
            }
        });
    }



    //5. getMLResponse() method--> After Uploading it ML Model will give the response of the Image file and display on View through API call
    private void getMLResponse(String fileName) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://apis-dev.ninebit.in/py/v1/classify-image";
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("object", fileName);
            jsonBody.put("model", "model1.h5");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json")); //Converts the jsonBody into a RequestBody object  for sending  an HTTP request.
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody) //post request
                .build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.e("ML Response", "Error While Fetching the Response", e);
                runOnUiThread(() -> {
                    ml_response_text.setText("Failed to fetch ML response");
                    ml_response_text.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        runOnUiThread(() -> showRunOutput(jsonResponse));
                    } catch (JSONException e) {
                        Log.e("ML Response", "Failed to parse JSON response", e);
                        runOnUiThread(() -> {
                            ml_response_text.setText("Failed to parse ML response");
                            ml_response_text.setVisibility(View.VISIBLE);
                        });
                    }
                } else {
                    Log.e("ML Response", "ERROR: " + response.code() + " " + response.message());
                    runOnUiThread(() -> {
                        ml_response_text.setText("Failed to get ML response: " + response.code());
                        ml_response_text.setVisibility(View.VISIBLE);
                    });
                }
            }
        });
    }

    private void showRunOutput(JSONObject data) {
        try {
            String responseBody = data.getJSONObject("message").getString("body");
            String statusCode = data.getJSONObject("message").getString("statusCode");
            String outputString;

            if (!statusCode.equals("SUCCESS")) {
                outputString = "Error: " + statusCode;
            } else {
                JSONObject dRes = new JSONObject(responseBody).getJSONObject("d_res");
                String emotion = dRes.getString("emotion");
                outputString = "Emotion: " + emotion;
            }
            runOnUiThread(() -> {
                ml_response_text.setText(outputString);
                ml_response_text.setVisibility(View.VISIBLE);
            });
        } catch (JSONException e) {
            Log.e("ML Response", "Error parsing JSON", e);
            runOnUiThread(() -> {
                ml_response_text.setText("Failed to parse JSON response");
                ml_response_text.setVisibility(View.VISIBLE);
            });
        }
    }


    interface OnPathRetrievedListener{
        void OnPathRetrivedListener(String filepath);
    }
}
