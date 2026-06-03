package com.example.queueapp;

import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.queueapp.adapter.FoodVerticalAdapter;
import com.example.queueapp.data.MockDataProvider;
import com.example.queueapp.model.FoodItem;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class FoodRecommendationActivity extends AppCompatActivity {

    private static final String ALL = "All";

    private FoodVerticalAdapter adapter;
    private RecyclerView rvRecommendations;
    private View emptyState;
    private TextView tvSurpriseResult;
    private MaterialButton btnSurpriseMe;

    private String selectedBudget = ALL;
    private String selectedCategory = ALL;
    private String selectedSpicy = ALL;

    private final List<FoodItem> allFoods = MockDataProvider.getAllFoods();
    private final Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_recommendation);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvRecommendations = findViewById(R.id.rvRecommendations);
        emptyState = findViewById(R.id.emptyState);
        tvSurpriseResult = findViewById(R.id.tvSurpriseResult);
        btnSurpriseMe = findViewById(R.id.btnSurpriseMe);

        adapter = new FoodVerticalAdapter((item, isFavorite) -> {
            int message = isFavorite ? R.string.added_to_favorites : R.string.removed_from_favorites;
            Snackbar.make(rvRecommendations, message, Snackbar.LENGTH_SHORT).show();
        });
        rvRecommendations.setLayoutManager(new LinearLayoutManager(this));
        rvRecommendations.setAdapter(adapter);

        setupChipGroup(findViewById(R.id.chipGroupBudget),
                new String[]{ALL, "Low", "Medium", "High"}, value -> {
                    selectedBudget = value;
                    applyFilters();
                });
        setupChipGroup(findViewById(R.id.chipGroupCategory),
                new String[]{ALL, "Beverage", "Breakfast", "Main", "Dessert", "Snack"}, value -> {
                    selectedCategory = value;
                    applyFilters();
                });
        setupChipGroup(findViewById(R.id.chipGroupSpicy),
                new String[]{ALL, "None", "Mild", "Hot"}, value -> {
                    selectedSpicy = value;
                    applyFilters();
                });

        btnSurpriseMe.setOnClickListener(v -> showSurprise());
        applyFilters();
    }

    private interface ChipSelectionListener {
        void onSelected(String value);
    }

    private void setupChipGroup(ChipGroup group, String[] options, ChipSelectionListener listener) {
        for (int i = 0; i < options.length; i++) {
            String option = options[i];
            Chip chip = new Chip(this);
            chip.setText(option);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.accent);
            chip.setId(View.generateViewId());
            if (ALL.equals(option)) {
                chip.setChecked(true);
            }
            group.addView(chip);
        }
        group.setOnCheckedStateChangeListener((chipGroup, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                return;
            }
            Chip selected = chipGroup.findViewById(checkedIds.get(0));
            if (selected != null) {
                listener.onSelected(selected.getText().toString());
            }
        });
    }

    private void applyFilters() {
        List<FoodItem> filtered = new ArrayList<>();
        for (FoodItem item : allFoods) {
            if (!ALL.equals(selectedBudget) && !item.getBudget().equals(selectedBudget)) {
                continue;
            }
            if (!ALL.equals(selectedCategory) && !item.getCategory().equals(selectedCategory)) {
                continue;
            }
            if (!ALL.equals(selectedSpicy) && !item.getSpicyLevel().equals(selectedSpicy)) {
                continue;
            }
            filtered.add(item);
        }
        adapter.setItems(filtered);
        emptyState.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        rvRecommendations.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void showSurprise() {
        if (allFoods.isEmpty()) {
            return;
        }
        FoodItem surprise = allFoods.get(random.nextInt(allFoods.size()));
        tvSurpriseResult.setVisibility(View.VISIBLE);
        tvSurpriseResult.setText(getString(R.string.surprise_message, surprise.getName()));
        tvSurpriseResult.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
        btnSurpriseMe.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_up));
        Snackbar.make(rvRecommendations, getString(R.string.surprise_message, surprise.getName()),
                Snackbar.LENGTH_LONG).show();
    }
}
