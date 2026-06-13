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
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.queueapp.R;
import com.example.queueapp.api.model.FoodModel;
import com.example.queueapp.util.ImageUploadHelper;
import com.example.queueapp.viewmodel.AddEditFoodViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;

import okhttp3.MultipartBody;

public class AddEditFoodActivity extends AppCompatActivity {

    private TextInputEditText etFoodName, etFoodDesc, etFoodPrice, etFoodCategory;
    private SwitchMaterial switchFoodAvailable;
    private ImageView ivFoodPreview;
    private View imagePlaceholder;
    private MaterialButton btnPickFoodImage;
    private MaterialButton btnSaveFood;
    private AddEditFoodViewModel viewModel;

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

        viewModel = new ViewModelProvider(this).get(AddEditFoodViewModel.class);

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
        btnSaveFood = findViewById(R.id.btnSaveFood);

        observeViewModel();

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
        Toast.makeText(this, R.string.uploading_image, Toast.LENGTH_SHORT).show();
        MultipartBody.Part part;
        try {
            part = ImageUploadHelper.createImagePart(this, uri);
        } catch (IOException e) {
            Toast.makeText(this, R.string.image_upload_failed, Toast.LENGTH_LONG).show();
            return;
        }
        viewModel.uploadImage(part);
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

    private void observeViewModel() {
        viewModel.getSaveResult().observe(this, resource -> {
            if (resource == null) {
                return;
            }
            btnSaveFood.setEnabled(!resource.isLoading());
            if (resource.isSuccess()) {
                Toast.makeText(this, "Food saved successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else if (resource.isError()) {
                Toast.makeText(this,
                        resource.message != null ? resource.message : "Failed to save food",
                        Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getUploadResult().observe(this, resource -> {
            if (resource == null) {
                return;
            }
            btnPickFoodImage.setEnabled(!resource.isLoading());
            if (resource.isSuccess() && resource.data != null && resource.data.getUrl() != null) {
                imageUrl = resource.data.getUrl();
                showPreview(imageUrl);
            } else if (resource.isError()) {
                Toast.makeText(this,
                        resource.message != null ? resource.message : getString(R.string.image_upload_failed),
                        Toast.LENGTH_LONG).show();
            }
        });
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

        viewModel.saveFood(food, isEditMode);
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
