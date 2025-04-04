package com.baitap.uploadimage;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.baitap.uploadimage.service.ApiService;
import com.baitap.uploadimage.service.RetrofitClient;
import com.baitap.uploadimage.util.RealPathUtil;
import com.bumptech.glide.Glide;

import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.Manifest;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private EditText editUserName;
    private ImageView imgPreview;
    private Button btnChooseFile, btnUploadImage;
    private Uri selectedImageUri;
    private String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        checkPermission();

        btnChooseFile.setOnClickListener(v -> openGallery());
        btnUploadImage.setOnClickListener(v -> uploadImage());
    }

    private void initViews() {
        editUserName = findViewById(R.id.editUserName);
        imgPreview = findViewById(R.id.imgPreview);
        btnChooseFile = findViewById(R.id.btnChoose);
        btnUploadImage = findViewById(R.id.btnUpload);
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 100);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            Glide.with(this).load(selectedImageUri).into(imgPreview);
            imagePath = RealPathUtil.getRealPath(this, selectedImageUri);
            if (imagePath != null) {
                Log.d(TAG, "Image selected: " + imagePath);
            } else {
                Log.e(TAG, "Failed to get real path from URI");
                Toast.makeText(this, "Không thể lấy đường dẫn ảnh!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "Failed to select image");
        }
    }

    private void uploadImage() {
        if (selectedImageUri != null) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                byte[] fileBytes = new byte[inputStream.available()];
                inputStream.read(fileBytes);
                inputStream.close();

                RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), fileBytes);
                MultipartBody.Part body = MultipartBody.Part.createFormData("image", "image.jpg", requestBody);

                ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
                Call<ResponseBody> call = apiService.uploadImage(body);

                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Upload thành công!", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Upload successful: " + response.body());
                        } else {
                            Toast.makeText(MainActivity.this, "Upload thất bại!", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Upload failed: " + response.errorBody());
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(MainActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Upload error: " + t.getMessage(), t);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error reading file: " + e.getMessage(), e);
                Toast.makeText(this, "Lỗi đọc file!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Vui lòng chọn ảnh trước khi upload!", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "No image selected for upload");
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (view instanceof EditText) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                view.clearFocus();
            }
        }
        return super.dispatchTouchEvent(event);
    }
}
