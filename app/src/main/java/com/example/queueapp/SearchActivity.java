package com.example.queueapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.queueapp.adapter.SearchFoodAdapter;
import com.example.queueapp.api.model.FoodModel;
import com.example.queueapp.viewmodel.SearchViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.Collections;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    public static final String EXTRA_QUERY = "extra_query";
    private static final long SEARCH_DEBOUNCE_MS = 500L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private SearchViewModel viewModel;
    private SearchFoodAdapter adapter;
    private RecyclerView rvSearchResults;
    private View emptySearchState;
    private ProgressBar progressSearch;
    private String latestQuery = "";

    private final Runnable searchRunnable = () -> doSearch(latestQuery);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        SearchView searchView = findViewById(R.id.searchView);
        rvSearchResults = findViewById(R.id.rvSearchResults);
        emptySearchState = findViewById(R.id.emptySearchState);
        progressSearch = findViewById(R.id.progressSearch);

        adapter = new SearchFoodAdapter();
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        rvSearchResults.setAdapter(adapter);

        observeViewModel();

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

    private void observeViewModel() {
        viewModel.getResults().observe(this, resource -> {
            if (resource == null) {
                return;
            }
            setLoading(resource.isLoading());
            if (resource.isSuccess()) {
                List<FoodModel> foods = resource.data != null ? resource.data.getFoods() : null;
                adapter.setItems(foods != null ? foods : Collections.emptyList());
                showEmpty(adapter.getItemCount() == 0);
            } else if (resource.isError()) {
                adapter.setItems(Collections.emptyList());
                showEmpty(true);
                Snackbar.make(rvSearchResults,
                                resource.message != null ? resource.message
                                        : getString(R.string.load_failed, getString(R.string.no_results)),
                                Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry, v -> doSearch(latestQuery))
                        .show();
            }
        });
    }

    private void queueSearch(String query) {
        latestQuery = query != null ? query.trim() : "";
        handler.removeCallbacks(searchRunnable);
        handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_MS);
    }

    private void doSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            adapter.setItems(Collections.emptyList());
            setLoading(false);
            showEmpty(false);
            return;
        }
        viewModel.search(query.trim());
    }

    private void setLoading(boolean loading) {
        progressSearch.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showEmpty(boolean show) {
        emptySearchState.setVisibility(show ? View.VISIBLE : View.GONE);
        rvSearchResults.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(searchRunnable);
        super.onDestroy();
    }
}
