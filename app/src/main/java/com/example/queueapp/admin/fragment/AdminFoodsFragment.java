package com.example.queueapp.admin.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.queueapp.R;
import com.example.queueapp.admin.AddEditFoodActivity;
import com.example.queueapp.admin.adapter.FoodAdminAdapter;
import com.example.queueapp.api.ApiConfig;
import com.example.queueapp.api.ApiService;
import com.example.queueapp.api.model.ApiResponse;
import com.example.queueapp.api.model.FoodIdRequest;
import com.example.queueapp.api.model.FoodListResponse;
import com.example.queueapp.api.model.FoodModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminFoodsFragment extends Fragment implements FoodAdminAdapter.OnFoodAdminClickListener {

    private ApiService apiService;
    private SwipeRefreshLayout swipeRefreshAdminFoods;
    private RecyclerView rvAdminFoods;
    private FoodAdminAdapter adapter;
    private List<FoodModel> allFoods = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_foods, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        apiService = ApiConfig.getApiService();
        
        swipeRefreshAdminFoods = view.findViewById(R.id.swipeRefreshAdminFoods);
        rvAdminFoods = view.findViewById(R.id.rvAdminFoods);
        SearchView searchAdminFoods = view.findViewById(R.id.searchAdminFoods);
        FloatingActionButton fabAddFood = view.findViewById(R.id.fabAddFood);
        
        rvAdminFoods.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new FoodAdminAdapter(this);
        rvAdminFoods.setAdapter(adapter);
        
        swipeRefreshAdminFoods.setOnRefreshListener(this::loadFoods);
        
        searchAdminFoods.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        
        fabAddFood.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddEditFoodActivity.class);
            startActivity(intent);
        });
        
        loadFoods();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        loadFoods();
    }

    private void loadFoods() {
        swipeRefreshAdminFoods.setRefreshing(true);
        apiService.getFoodList().enqueue(new Callback<ApiResponse<FoodListResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<FoodListResponse>> call, Response<ApiResponse<FoodListResponse>> response) {
                if (!isAdded()) return;
                swipeRefreshAdminFoods.setRefreshing(false);
                ApiResponse<FoodListResponse> body = response.body();
                if (response.isSuccessful() && body != null && body.isSuccess() && body.getData() != null) {
                    allFoods = body.getData().getFoods();
                    adapter.setItems(allFoods);
                } else {
                    Toast.makeText(requireContext(), "Failed to load foods", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<FoodListResponse>> call, Throwable t) {
                if (!isAdded()) return;
                swipeRefreshAdminFoods.setRefreshing(false);
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEditClick(FoodModel food) {
        Intent intent = new Intent(requireContext(), AddEditFoodActivity.class);
        intent.putExtra("food_id", food.getId());
        intent.putExtra("food_name", food.getName());
        intent.putExtra("food_desc", food.getDescription());
        intent.putExtra("food_price", food.getPrice());
        intent.putExtra("food_category", food.getCategory());
        intent.putExtra("food_image", food.getImageUrl());
        intent.putExtra("food_available", food.isAvailable());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(FoodModel food) {
        FoodIdRequest req = new FoodIdRequest(food.getId());
        apiService.deleteFood(req).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (!isAdded()) return;
                ApiResponse<Object> body = response.body();
                if (response.isSuccessful() && body != null && body.isSuccess()) {
                    Toast.makeText(requireContext(), "Food deleted", Toast.LENGTH_SHORT).show();
                    loadFoods();
                } else {
                    Toast.makeText(requireContext(), "Failed to delete food", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
