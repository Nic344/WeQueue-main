package com.example.queueapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.queueapp.FoodRecommendationActivity;
import com.example.queueapp.R;
import com.example.queueapp.adapter.FoodVerticalAdapter;
import com.example.queueapp.data.AppSession;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

public class FavoritesFragment extends Fragment {

    private FoodVerticalAdapter adapter;
    private View emptyState;
    private RecyclerView rvFavorites;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvFavorites = view.findViewById(R.id.rvFavorites);
        emptyState = view.findViewById(R.id.emptyFavoritesState);
        MaterialButton btnBrowse = emptyState.findViewById(R.id.btnBrowseFoods);

        adapter = new FoodVerticalAdapter((item, isFavorite) -> {
            Snackbar.make(view, R.string.removed_from_favorites, Snackbar.LENGTH_SHORT).show();
            loadFavorites();
        });
        rvFavorites.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvFavorites.setAdapter(adapter);

        btnBrowse.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), FoodRecommendationActivity.class)));

        loadFavorites();
    }

    private void loadFavorites() {
        boolean hasFavorites = !AppSession.getInstance().getFavoriteFoods().isEmpty();
        emptyState.setVisibility(hasFavorites ? View.GONE : View.VISIBLE);
        rvFavorites.setVisibility(hasFavorites ? View.VISIBLE : View.GONE);
        adapter.setItems(AppSession.getInstance().getFavoriteFoods());
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavorites();
    }
}
