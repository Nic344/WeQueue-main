package com.example.queueapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.queueapp.adapter.SearchFoodAdapter;
import com.example.queueapp.api.ApiConfig;
import com.example.queueapp.api.ApiErrorHelper;
import com.example.queueapp.api.ApiService;
import com.example.queueapp.api.model.ApiResponse;
import com.example.queueapp.api.model.FoodListResponse;
import com.example.queueapp.api.model.FoodModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    public static final String EXTRA_QUERY = "extra_query";
    private static final long SEARCH_DEBOUNCE_MS = 500L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private ApiService apiService;
    private SearchFoodAdapter adapter;
    private RecyclerView rvSearchResults;
    private View emptySearchState;
    private ProgressBar progressSearch;
    private Call<ApiResponse<FoodListResponse>> activeCall;
    private String latestQuery = "";

    private final Runnable searchRunnable = () -> search(latestQuery);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        apiService = ApiConfig.getApiService();

        SearchView searchView = findViewById(R.id.searchView);
        rvSearchResults = findViewById(R.id.rvSearchResults);
        emptySearchState = findViewById(R.id.emptySearchState);
        progressSearch = findViewById(R.id.progressSearch);

        adapter = new SearchFoodAdapter();
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        rvSearchResults.setAdapter(adapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                queueSearch(query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                queueSearch(newText);
                return true;
            }
        });

        String initialQuery = getIntent().getStringExtra(EXTRA_QUERY);
        if (initialQuery != null && !initialQuery.trim().isEmpty()) {
            searchView.setQuery(initialQuery, false);
            queueSearch(initialQuery);
        } else {
            showEmpty(false);
        }
    }

    private void queueSearch(String query) {
        latestQuery = query != null ? query.trim() : "";
        handler.removeCallbacks(searchRunnable);
        handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_MS);
    }

    private void search(String query) {
        if (activeCall != null) {
            activeCall.cancel();
        }
        if (query == null || query.trim().isEmpty()) {
            adapter.setItems(Collections.emptyList());
            setLoading(false);
            showEmpty(false);
            return;
        }
        setLoading(true);
        activeCall = apiService.searchFoods(query.trim());
        activeCall.enqueue(new Callback<ApiResponse<FoodListResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<FoodListResponse>> call,
                                   Response<ApiResponse<FoodListResponse>> response) {
                if (call.isCanceled()) {
                    return;
                }
                ApiResponse<FoodListResponse> body = response.body();
                if (response.isSuccessful() && body != null && body.isSuccess() && body.getData() != null) {
                    List<FoodModel> foods = body.getData().getFoods();
                    adapter.setItems(foods != null ? foods : Collections.emptyList());
                    showEmpty(adapter.getItemCount() == 0);
                } else {
                    adapter.setItems(Collections.emptyList());
                    showEmpty(true);
                    showRetry(response);
                }
                setLoading(false);
            }

            @Override
            public void onFailure(Call<ApiResponse<FoodListResponse>> call, Throwable t) {
                if (call.isCanceled()) {
                    return;
                }
                adapter.setItems(Collections.emptyList());
                showEmpty(true);
                setLoading(false);
                Snackbar.make(rvSearchResults, getString(R.string.network_error, t.getMessage()),
                                Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry, v -> search(latestQuery))
                        .show();
            }
        });
    }

    private void setLoading(boolean loading) {
        progressSearch.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showEmpty(boolean show) {
        emptySearchState.setVisibility(show ? View.VISIBLE : View.GONE);
        rvSearchResults.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showRetry(Response<?> response) {
        Snackbar.make(rvSearchResults,
                        ApiErrorHelper.getMessage(response, getString(R.string.load_failed,
                                getString(R.string.no_results))),
                        Snackbar.LENGTH_LONG)
                .setAction(R.string.retry, v -> search(latestQuery))
                .show();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(searchRunnable);
        if (activeCall != null) {
            activeCall.cancel();
        }
        super.onDestroy();
    }
}
