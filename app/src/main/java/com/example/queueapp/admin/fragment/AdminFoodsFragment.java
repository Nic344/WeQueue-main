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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.queueapp.R;
import com.example.queueapp.admin.AddEditFoodActivity;
import com.example.queueapp.admin.adapter.FoodAdminAdapter;
import com.example.queueapp.api.model.FoodModel;
import com.example.queueapp.viewmodel.AdminFoodsViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class AdminFoodsFragment extends Fragment implements FoodAdminAdapter.OnFoodAdminClickListener {

    private SwipeRefreshLayout swipeRefreshAdminFoods;
    private FoodAdminAdapter adapter;
    private AdminFoodsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_foods, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AdminFoodsViewModel.class);

        swipeRefreshAdminFoods = view.findViewById(R.id.swipeRefreshAdminFoods);
        RecyclerView rvAdminFoods = view.findViewById(R.id.rvAdminFoods);
        SearchView searchAdminFoods = view.findViewById(R.id.searchAdminFoods);
        FloatingActionButton fabAddFood = view.findViewById(R.id.fabAddFood);

        rvAdminFoods.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new FoodAdminAdapter(this);
        rvAdminFoods.setAdapter(adapter);

        swipeRefreshAdminFoods.setOnRefreshListener(() -> viewModel.refresh());

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

        fabAddFood.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddEditFoodActivity.class)));

        observeViewModel();
        viewModel.loadFoodsIfNeeded();
    }

    private void observeViewModel() {
        viewModel.getFoods().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) {
                return;
            }
            swipeRefreshAdminFoods.setRefreshing(resource.isLoading());
            if (resource.isSuccess() && resource.data != null) {
                adapter.setItems(resource.data);
            } else if (resource.isError()) {
                Toast.makeText(requireContext(),
                        resource.message != null ? resource.message : "Failed to load foods",
                        Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getDeleteResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null || resource.isLoading()) {
                return;
            }
            if (resource.isSuccess()) {
                Toast.makeText(requireContext(), "Food deleted", Toast.LENGTH_SHORT).show();
                viewModel.refresh();
            } else if (resource.isError()) {
                Toast.makeText(requireContext(),
                        resource.message != null ? resource.message : "Failed to delete food",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        viewModel.refresh();
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
        viewModel.deleteFood(food);
    }
}
