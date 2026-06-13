package com.example.queueapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.queueapp.R;
import com.example.queueapp.SearchActivity;
import com.example.queueapp.adapter.FavoriteAdapter;
import com.example.queueapp.api.model.FavoriteModel;
import com.example.queueapp.viewmodel.FavoritesViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Collections;
import java.util.List;

public class FavoritesFragment extends Fragment {

    private FavoritesViewModel viewModel;
    private FavoriteAdapter adapter;
    private View emptyState;
    private RecyclerView rvFavorites;
    private ProgressBar progressFavorites;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(FavoritesViewModel.class);

        rvFavorites = view.findViewById(R.id.rvFavorites);
        emptyState = view.findViewById(R.id.emptyFavoritesState);
        progressFavorites = view.findViewById(R.id.progressFavorites);
        MaterialButton btnBrowse = emptyState.findViewById(R.id.btnBrowseFoods);

        adapter = new FavoriteAdapter((favorite, position) -> removeFavorite(view, favorite, position));
        rvFavorites.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvFavorites.setAdapter(adapter);

        btnBrowse.setOnClickListener(v -> startActivity(new Intent(requireContext(), SearchActivity.class)));

        observeViewModel();
        viewModel.loadFavorites();
    }

    private void observeViewModel() {
        viewModel.getFavorites().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) {
                return;
            }
            setLoading(resource.isLoading());
            if (resource.isSuccess()) {
                List<FavoriteModel> favorites = resource.data != null
                        ? resource.data.getFavorites() : null;
                adapter.setItems(favorites != null ? favorites : Collections.emptyList());
                updateEmptyState();
            } else if (resource.isError()) {
                adapter.setItems(Collections.emptyList());
                updateEmptyState();
                showRetry(resource.message, () -> viewModel.loadFavorites());
            }
        });

        viewModel.getRemoveResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null || resource.isLoading()) {
                return;
            }
            if (resource.isSuccess()) {
                Snackbar.make(requireView(), R.string.removed_from_favorites, Snackbar.LENGTH_SHORT).show();
                viewModel.loadFavorites();
            } else if (resource.isError()) {
                showRetry(resource.message, null);
            }
        });
    }

    private void removeFavorite(View anchor, FavoriteModel favorite, int position) {
        if (position == RecyclerView.NO_POSITION) {
            return;
        }
        viewModel.removeFavorite(favorite.getId());
    }

    private void updateEmptyState() {
        boolean empty = adapter.isEmpty();
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvFavorites.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void setLoading(boolean loading) {
        progressFavorites.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (loading) {
            rvFavorites.setVisibility(View.GONE);
            emptyState.setVisibility(View.GONE);
        }
    }

    private void showRetry(@Nullable String message, @Nullable Runnable retry) {
        String text = message != null ? message
                : getString(R.string.load_failed, getString(R.string.favorites));
        Snackbar bar = Snackbar.make(requireView(), text, Snackbar.LENGTH_LONG);
        if (retry != null) {
            bar.setAction(R.string.retry, v -> retry.run());
        }
        bar.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.loadFavorites();
        }
    }
}
