package com.example.queueapp.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.queueapp.R;
import com.example.queueapp.api.ApiConfig;
import com.example.queueapp.api.ApiService;
import com.example.queueapp.api.model.ApiResponse;
import com.example.queueapp.api.model.FoodModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddEditFoodActivity extends AppCompatActivity {

    private TextInputEditText etFoodName, etFoodDesc, etFoodPrice, etFoodCategory, etFoodImage;
    private SwitchMaterial switchFoodAvailable;
    private ApiService apiService;
    
    private int foodId = -1;
    private boolean isEditMode = false;

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
        etFoodImage = findViewById(R.id.etFoodImage);
        switchFoodAvailable = findViewById(R.id.switchFoodAvailable);
        MaterialButton btnSaveFood = findViewById(R.id.btnSaveFood);

        if (getIntent().hasExtra("food_id")) {
            isEditMode = true;
            foodId = getIntent().getIntExtra("food_id", -1);
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Edit Food");
            
            etFoodName.setText(getIntent().getStringExtra("food_name"));
            etFoodDesc.setText(getIntent().getStringExtra("food_desc"));
            etFoodPrice.setText(String.valueOf(getIntent().getIntExtra("food_price", 0)));
            etFoodCategory.setText(getIntent().getStringExtra("food_category"));
            etFoodImage.setText(getIntent().getStringExtra("food_image"));
            switchFoodAvailable.setChecked(getIntent().getBooleanExtra("food_available", true));
        }

        btnSaveFood.setOnClickListener(v -> saveFood());
    }

    private void saveFood() {
        String name = etFoodName.getText() != null ? etFoodName.getText().toString().trim() : "";
        String desc = etFoodDesc.getText() != null ? etFoodDesc.getText().toString().trim() : "";
        String priceStr = etFoodPrice.getText() != null ? etFoodPrice.getText().toString().trim() : "";
        String category = etFoodCategory.getText() != null ? etFoodCategory.getText().toString().trim() : "";
        String image = etFoodImage.getText() != null ? etFoodImage.getText().toString().trim() : "";
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
        food.setImageUrl(image);
        food.setAvailable(available);

        Callback<ApiResponse<FoodModel>> callback = new Callback<ApiResponse<FoodModel>>() {
            @Override
            public void onResponse(Call<ApiResponse<FoodModel>> call, Response<ApiResponse<FoodModel>> response) {
                ApiResponse<FoodModel> body = response.body();
                if (response.isSuccessful() && body != null && body.isSuccess()) {
                    Toast.makeText(AddEditFoodActivity.this, "Food saved successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String msg = body != null && body.getMessage() != null ? body.getMessage() : "Failed to save food";
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
