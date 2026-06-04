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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.queueapp.R;
import com.example.queueapp.SearchActivity;
import com.example.queueapp.adapter.FavoriteAdapter;
import com.example.queueapp.api.ApiConfig;
import com.example.queueapp.api.ApiErrorHelper;
import com.example.queueapp.api.ApiService;
import com.example.queueapp.api.model.ApiResponse;
import com.example.queueapp.api.model.FavoriteListResponse;
import com.example.queueapp.api.model.FavoriteModel;
import com.example.queueapp.api.model.FoodIdRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoritesFragment extends Fragment {

    private ApiService apiService;
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
        apiService = ApiConfig.getApiService();
        rvFavorites = view.findViewById(R.id.rvFavorites);
        emptyState = view.findViewById(R.id.emptyFavoritesState);
        progressFavorites = view.findViewById(R.id.progressFavorites);
        MaterialButton btnBrowse = emptyState.findViewById(R.id.btnBrowseFoods);

        adapter = new FavoriteAdapter((favorite, position) -> removeFavorite(view, favorite, position));
        rvFavorites.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvFavorites.setAdapter(adapter);

        btnBrowse.setOnClickListener(v -> startActivity(new Intent(requireContext(), SearchActivity.class)));

        loadFavorites();
    }

    private void loadFavorites() {
        setLoading(true);
        apiService.getFavorites().enqueue(new Callback<ApiResponse<FavoriteListResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<FavoriteListResponse>> call,
                                   Response<ApiResponse<FavoriteListResponse>> response) {
                if (!isAdded()) {
                    return;
                }
                ApiResponse<FavoriteListResponse> body = response.body();
                if (response.isSuccessful() && body != null && body.isSuccess() && body.getData() != null) {
                    List<FavoriteModel> favorites = body.getData().getFavorites();
                    adapter.setItems(favorites != null ? favorites : Collections.emptyList());
                    updateEmptyState();
                } else {
                    adapter.setItems(Collections.emptyList());
                    updateEmptyState();
                    showRetry(response, this::retry);
                }
                setLoading(false);
            }

            private void retry() {
                loadFavorites();
            }

            @Override
            public void onFailure(Call<ApiResponse<FavoriteListResponse>> call, Throwable t) {
                if (!isAdded()) {
                    return;
                }
                adapter.setItems(Collections.emptyList());
                updateEmptyState();
                showRetry(t, FavoritesFragment.this::loadFavorites);
                setLoading(false);
            }
        });
    }

    private void removeFavorite(View anchor, FavoriteModel favorite, int position) {
        if (position == RecyclerView.NO_POSITION) {
            return;
        }
        apiService.removeFavorite(new FoodIdRequest(favorite.getId())).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (!isAdded()) {
                    return;
                }
                ApiResponse<Object> body = response.body();
                if (response.isSuccessful() && body != null && body.isSuccess()) {
                    adapter.removeAt(position);
                    updateEmptyState();
                    Snackbar.make(anchor, R.string.removed_from_favorites, Snackbar.LENGTH_SHORT).show();
                } else {
                    showRetry(response, () -> removeFavorite(anchor, favorite, position));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                if (!isAdded()) {
                    return;
                }
                showRetry(t, () -> removeFavorite(anchor, favorite, position));
            }
        });
    }

    private void updateEmptyState() {
        boolean empty = adapter.isEmpty();
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvFavorites.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void setLoading(boolean loading) {
        progressFavorites.setVisibility(loading ? View.VISIBLE : View.GONE);
        rvFavorites.setVisibility(loading ? View.GONE : rvFavorites.getVisibility());
        emptyState.setVisibility(loading ? View.GONE : emptyState.getVisibility());
    }

    private void showRetry(Response<?> response, Runnable retry) {
        Snackbar.make(requireView(),
                        ApiErrorHelper.getMessage(response, getString(R.string.load_failed,
                                getString(R.string.favorites))),
                        Snackbar.LENGTH_LONG)
                .setAction(R.string.retry, v -> retry.run())
                .show();
    }

    private void showRetry(Throwable t, Runnable retry) {
        Snackbar.make(requireView(), getString(R.string.network_error, t.getMessage()), Snackbar.LENGTH_LONG)
                .setAction(R.string.retry, v -> retry.run())
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (apiService != null) {
            loadFavorites();
        }
    }
}
