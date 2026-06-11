package com.example.queueapp.admin;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.queueapp.R;
import com.example.queueapp.api.ApiConfig;
import com.example.queueapp.api.ApiErrorHelper;
import com.example.queueapp.api.ApiService;
import com.example.queueapp.api.model.ApiResponse;
import com.example.queueapp.api.model.FoodModel;
import com.example.queueapp.api.model.UploadResponse;
import com.example.queueapp.util.ImageUploadHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddEditFoodActivity extends AppCompatActivity {

    private TextInputEditText etFoodName, etFoodDesc, etFoodPrice, etFoodCategory;
    private SwitchMaterial switchFoodAvailable;
    private ImageView ivFoodPreview;
    private View imagePlaceholder;
    private MaterialButton btnPickFoodImage;
    private ApiService apiService;

    private int foodId = -1;
    private boolean isEditMode = false;
    private String imageUrl = "";

    private final ActivityResultLauncher<PickVisualMediaRequest> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    uploadFoodImage(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_food);

        apiService = ApiConfig.getApiService();

        Toolbar toolbar = findViewById(R.id.toolbarAddEditFood);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        etFoodName = findViewById(R.id.etFoodName);
        etFoodDesc = findViewById(R.id.etFoodDesc);
        etFoodPrice = findViewById(R.id.etFoodPrice);
        etFoodCategory = findViewById(R.id.etFoodCategory);
        switchFoodAvailable = findViewById(R.id.switchFoodAvailable);
        ivFoodPreview = findViewById(R.id.ivFoodPreview);
        imagePlaceholder = findViewById(R.id.imagePlaceholder);
        btnPickFoodImage = findViewById(R.id.btnPickFoodImage);
        MaterialButton btnSaveFood = findViewById(R.id.btnSaveFood);

        View.OnClickListener pickImage = v -> pickImageLauncher.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
        btnPickFoodImage.setOnClickListener(pickImage);
        findViewById(R.id.cardFoodImage).setOnClickListener(pickImage);

        if (getIntent().hasExtra("food_id")) {
            isEditMode = true;
            foodId = getIntent().getIntExtra("food_id", -1);
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Edit Food");
            
            etFoodName.setText(getIntent().getStringExtra("food_name"));
            etFoodDesc.setText(getIntent().getStringExtra("food_desc"));
            double existingPrice = getIntent().getDoubleExtra("food_price", 0);
            etFoodPrice.setText(existingPrice > 0 ? String.valueOf((long) existingPrice) : "");
            etFoodCategory.setText(getIntent().getStringExtra("food_category"));
            switchFoodAvailable.setChecked(getIntent().getBooleanExtra("food_available", true));
            imageUrl = getIntent().getStringExtra("food_image");
            showPreview(imageUrl);
        }

        btnSaveFood.setOnClickListener(v -> saveFood());
    }

    private void uploadFoodImage(Uri uri) {
        btnPickFoodImage.setEnabled(false);
        Toast.makeText(this, R.string.uploading_image, Toast.LENGTH_SHORT).show();

        MultipartBody.Part part;
        try {
            part = ImageUploadHelper.createImagePart(this, uri);
        } catch (IOException e) {
            btnPickFoodImage.setEnabled(true);
            Toast.makeText(this, R.string.image_upload_failed, Toast.LENGTH_LONG).show();
            return;
        }

        apiService.uploadImage(part).enqueue(new Callback<ApiResponse<UploadResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UploadResponse>> call,
                                   Response<ApiResponse<UploadResponse>> response) {
                btnPickFoodImage.setEnabled(true);
                ApiResponse<UploadResponse> body = response.body();
                if (response.isSuccessful() && body != null && body.isSuccess()
                        && body.getData() != null && body.getData().getUrl() != null) {
                    String url = body.getData().getUrl();
                    imageUrl = url;
                    showPreview(url);
                } else {
                    Toast.makeText(AddEditFoodActivity.this,
                            ApiErrorHelper.getMessage(response, getString(R.string.image_upload_failed)),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UploadResponse>> call, Throwable t) {
                btnPickFoodImage.setEnabled(true);
                Toast.makeText(AddEditFoodActivity.this,
                        getString(R.string.network_error, t.getMessage()), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showPreview(String url) {
        if (url == null || url.trim().isEmpty()) {
            ivFoodPreview.setVisibility(View.GONE);
            imagePlaceholder.setVisibility(View.VISIBLE);
            return;
        }
        ivFoodPreview.setVisibility(View.VISIBLE);
        imagePlaceholder.setVisibility(View.GONE);
        Glide.with(this)
                .load(url.trim())
                .placeholder(R.drawable.ic_food_coffee)
                .error(R.drawable.ic_food_coffee)
                .into(ivFoodPreview);
    }

    private void saveFood() {
        String name = etFoodName.getText() != null ? etFoodName.getText().toString().trim() : "";
        String desc = etFoodDesc.getText() != null ? etFoodDesc.getText().toString().trim() : "";
        String priceStr = etFoodPrice.getText() != null ? etFoodPrice.getText().toString().trim() : "";
        String category = etFoodCategory.getText() != null ? etFoodCategory.getText().toString().trim() : "";
        boolean available = switchFoodAvailable.isChecked();

        if (TextUtils.isEmpty(name)) {
            etFoodName.setError("Name is required");
            return;
        }
        if (TextUtils.isEmpty(priceStr)) {
            etFoodPrice.setError("Price is required");
            return;
        }

        int price;
        try {
            price = Integer.parseInt(priceStr);
        } catch (NumberFormatException e) {
            etFoodPrice.setError("Invalid price");
            return;
        }

        FoodModel food = new FoodModel();
        if (isEditMode) food.setId(foodId);
        food.setName(name);
        food.setDescription(desc);
        food.setPrice(price);
        food.setCategory(category);
        food.setImageUrl(imageUrl);
        food.setAvailable(available);

        Callback<ApiResponse<FoodModel>> callback = new Callback<ApiResponse<FoodModel>>() {
            @Override
            public void onResponse(Call<ApiResponse<FoodModel>> call, Response<ApiResponse<FoodModel>> response) {
                ApiResponse<FoodModel> body = response.body();
                if (response.isSuccessful() && body != null && body.isSuccess()) {
                    Toast.makeText(AddEditFoodActivity.this, "Food saved successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String msg = ApiErrorHelper.getMessage(response, "Failed to save food");
                    Toast.makeText(AddEditFoodActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<FoodModel>> call, Throwable t) {
                Toast.makeText(AddEditFoodActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        };

        if (isEditMode) {
            apiService.updateFood(food).enqueue(callback);
        } else {
            apiService.createFood(food).enqueue(callback);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
