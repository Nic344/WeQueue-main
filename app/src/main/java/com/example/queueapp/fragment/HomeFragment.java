package com.example.queueapp.fragment;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.queueapp.MainActivity;
import com.example.queueapp.R;
import com.example.queueapp.SearchActivity;
import com.example.queueapp.adapter.PopularFoodAdapter;
import com.example.queueapp.api.ApiConfig;
import com.example.queueapp.api.ApiErrorHelper;
import com.example.queueapp.api.ApiService;
import com.example.queueapp.api.model.ApiResponse;
import com.example.queueapp.api.model.FavoriteListResponse;
import com.example.queueapp.api.model.FavoriteModel;
import com.example.queueapp.api.model.FoodIdRequest;
import com.example.queueapp.api.model.FoodListResponse;
import com.example.queueapp.api.model.FoodModel;
import com.example.queueapp.api.model.MyQueueResponse;
import com.example.queueapp.api.model.QueueStatusResponse;
import com.example.queueapp.api.model.UserModel;
import com.example.queueapp.data.AppSession;
import com.example.queueapp.data.SessionManager;
import com.example.queueapp.util.SystemUiHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private static final int MAX_RETRIES = 2;
    private static final long RETRY_DELAY_MS = 700L;

    private ApiService apiService;
    private TextView tvGreeting;
    private TextView tvNowServing;
    private TextView tvYourQueue;
    private TextView tvRemaining;
    private TextView tvEstimatedWait;
    private SwipeRefreshLayout swipeRefresh;
    private View skeletonContainer;
    private PopularFoodAdapter foodAdapter;
    private final List<FoodModel> popularFoods = new ArrayList<>();
    private int pendingLoads;
    private boolean coreContentLoaded;
    private String pendingErrorMessage;
    private Runnable pendingErrorRetry;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        apiService = ApiConfig.getApiService();

        View headerContainer = view.findViewById(R.id.headerContainer);
        int horizontalPadding = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 16f, getResources().getDisplayMetrics());
        SystemUiHelper.applyHeaderInsets(headerContainer, horizontalPadding, 0);

        tvGreeting = view.findViewById(R.id.tvGreeting);
        tvNowServing = view.findViewById(R.id.tvNowServing);
        tvYourQueue = view.findViewById(R.id.tvYourQueue);
        tvRemaining = view.findViewById(R.id.tvRemaining);
        tvEstimatedWait = view.findViewById(R.id.tvEstimatedWait);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        skeletonContainer = view.findViewById(R.id.skeletonContainer);
        RecyclerView rvPopularFoods = view.findViewById(R.id.rvPopularFoods);
        EditText etSearch = view.findViewById(R.id.etSearch);

        tvGreeting.setText(getString(R.string.good_afternoon, getDisplayName()));

        int primaryColor = ContextCompat.getColor(requireContext(), R.color.primary);
        setupQuickAction(view.findViewById(R.id.actionTakeQueue), R.drawable.ic_queue, R.string.take_queue,
                primaryColor, v -> {
                    v.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up));
                    navigateMain(MainActivity.NAV_QUEUE);
                });
        setupQuickAction(view.findViewById(R.id.actionRecommendation), R.drawable.ic_food_coffee,
                R.string.food_recommendation, primaryColor, v -> showRandomFoodDialog(view));
        setupQuickAction(view.findViewById(R.id.actionFavorites), R.drawable.ic_favorite_filled,
                R.string.favorites, primaryColor, v -> navigateMain(MainActivity.NAV_FAVORITES));
        setupQuickAction(view.findViewById(R.id.actionHistory), R.drawable.ic_history, R.string.history,
                primaryColor, v -> navigateMain(MainActivity.NAV_HISTORY));

        foodAdapter = new PopularFoodAdapter((food, currentlyFavorite) ->
                toggleFavorite(view, food, currentlyFavorite));
        rvPopularFoods.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvPopularFoods.setNestedScrollingEnabled(false);
        rvPopularFoods.setHasFixedSize(false);
        rvPopularFoods.setAdapter(foodAdapter);

        swipeRefresh.setColorSchemeColors(primaryColor);
        swipeRefresh.setOnRefreshListener(this::loadAllData);

        etSearch.setFocusable(false);
        etSearch.setOnClickListener(v -> openSearch(null));
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                openSearch(etSearch.getText() != null ? etSearch.getText().toString().trim() : null);
                return true;
            }
            return false;
        });

        view.post(this::loadAllData);
    }

    private void setupQuickAction(View actionRoot, int iconRes, int labelRes, int iconColor,
                                  View.OnClickListener listener) {
        ImageView icon = actionRoot.findViewById(R.id.actionIcon);
        TextView label = actionRoot.findViewById(R.id.actionLabel);
        icon.setImageResource(iconRes);
        ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(iconColor));
        label.setText(labelRes);
        actionRoot.setOnClickListener(listener);
    }

    private void loadAllData() {
        pendingLoads = 4;
        coreContentLoaded = false;
        pendingErrorMessage = null;
        pendingErrorRetry = null;
        skeletonContainer.setVisibility(View.VISIBLE);

        loadQueueStatus(0);
        requireView().postDelayed(() -> loadPopularFoods(0), 150);
        requireView().postDelayed(() -> loadFavoriteIds(0), 300);
        requireView().postDelayed(() -> loadMyQueue(0), 450);
    }

    private void loadQueueStatus(int attempt) {
        apiService.getQueueStatus().enqueue(new Callback<ApiResponse<QueueStatusResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<QueueStatusResponse>> call,
                                   Response<ApiResponse<QueueStatusResponse>> response) {
                if (!isAdded()) {
                    return;
                }
                ApiResponse<QueueStatusResponse> body = response.body();
                if (response.isSuccessful() && body != null && body.isSuccess() && body.getData() != null) {
                    QueueStatusResponse status = body.getData();
                    tvNowServing.setText(nonEmpty(status.getNowServing(), "—"));
                    tvRemaining.setText(getString(R.string.people, status.getRemaining()));
                    tvEstimatedWait.setText(getString(R.string.minutes, status.getEstimatedWaitMinutes()));
                    coreContentLoaded = true;
                } else if (attempt < MAX_RETRIES) {
                    retryLater(() -> loadQueueStatus(attempt + 1));
                    return;
                } else {
                    queueError(ApiErrorHelper.getMessage(response,
                            getString(R.string.load_failed, getString(R.string.queue_status))));
                }
                finishLoad();
            }

            @Override
            public void onFailure(Call<ApiResponse<QueueStatusResponse>> call, Throwable t) {
                if (!isAdded()) {
                    return;
                }
                if (attempt < MAX_RETRIES && ApiErrorHelper.isTransientError(t)) {
                    retryLater(() -> loadQueueStatus(attempt + 1));
                    return;
                }
                queueError(ApiErrorHelper.getNetworkMessage(t,
                        getString(R.string.load_failed, getString(R.string.queue_status))));
                finishLoad();
            }
        });
    }

    private void loadMyQueue(int attempt) {
        apiService.getMyQueue().enqueue(new Callback<ApiResponse<MyQueueResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<MyQueueResponse>> call,
                                   Response<ApiResponse<MyQueueResponse>> response) {
                if (!isAdded()) {
                    return;
                }
                ApiResponse<MyQueueResponse> body = response.body();
                if (response.isSuccessful() && body != null && body.getData() != null
                        && body.getData().isHasActiveQueue() && body.getData().getQueue() != null) {
                    tvYourQueue.setText(body.getData().getQueue().getQueueNumber());
                } else {
                    tvYourQueue.setText("—");
                }
                finishLoad();
            }

            @Override
            public void onFailure(Call<ApiResponse<MyQueueResponse>> call, Throwable t) {
                if (!isAdded()) {
                    return;
                }
                if (attempt < MAX_RETRIES && ApiErrorHelper.isTransientError(t)) {
                    retryLater(() -> loadMyQueue(attempt + 1));
                    return;
                }
                tvYourQueue.setText("—");
                finishLoad();
            }
        });
    }

    private void loadFavoriteIds(int attempt) {
        apiService.getFavorites().enqueue(new Callback<ApiResponse<FavoriteListResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<FavoriteListResponse>> call,
                                   Response<ApiResponse<FavoriteListResponse>> response) {
                if (!isAdded()) {
                    return;
                }
                ApiResponse<FavoriteListResponse> body = response.body();
                if (response.isSuccessful() && body != null && body.isSuccess() && body.getData() != null) {
                    Set<Integer> ids = new HashSet<>();
                    List<FavoriteModel> favorites = body.getData().getFavorites();
                    if (favorites != null) {
                        for (FavoriteModel favorite : favorites) {
                            ids.add(favorite.getId());
                        }
                    }
                    foodAdapter.setFavoriteIds(ids);
                } else if (attempt < MAX_RETRIES) {
                    retryLater(() -> loadFavoriteIds(attempt + 1));
                    return;
                }
                finishLoad();
            }

            @Override
            public void onFailure(Call<ApiResponse<FavoriteListResponse>> call, Throwable t) {
                if (!isAdded()) {
                    return;
                }
                if (attempt < MAX_RETRIES && ApiErrorHelper.isTransientError(t)) {
                    retryLater(() -> loadFavoriteIds(attempt + 1));
                    return;
                }
                finishLoad();
            }
        });
    }

    private void loadPopularFoods(int attempt) {
        apiService.getPopularFoods().enqueue(new Callback<ApiResponse<FoodListResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<FoodListResponse>> call,
                                   Response<ApiResponse<FoodListResponse>> response) {
                if (!isAdded()) {
                    return;
                }
                ApiResponse<FoodListResponse> body = response.body();
                if (response.isSuccessful() && body != null && body.isSuccess() && body.getData() != null) {
                    popularFoods.clear();
                    if (body.getData().getFoods() != null) {
                        popularFoods.addAll(body.getData().getFoods());
                    }
                    foodAdapter.setItems(popularFoods);
                    if (!popularFoods.isEmpty()) {
                        coreContentLoaded = true;
                    }
                } else if (attempt < MAX_RETRIES) {
                    retryLater(() -> loadPopularFoods(attempt + 1));
                    return;
                } else {
                    queueError(ApiErrorHelper.getMessage(response,
                            getString(R.string.load_failed, getString(R.string.popular_foods))));
                }
                finishLoad();
            }

            @Override
            public void onFailure(Call<ApiResponse<FoodListResponse>> call, Throwable t) {
                if (!isAdded()) {
                    return;
                }
                if (attempt < MAX_RETRIES && ApiErrorHelper.isTransientError(t)) {
                    retryLater(() -> loadPopularFoods(attempt + 1));
                    return;
                }
                queueError(ApiErrorHelper.getNetworkMessage(t,
                        getString(R.string.load_failed, getString(R.string.popular_foods))));
                finishLoad();
            }
        });
    }

    private void retryLater(Runnable action) {
        requireView().postDelayed(action, RETRY_DELAY_MS);
    }

    private void queueError(String message) {
        if (pendingErrorMessage == null) {
            pendingErrorMessage = message;
            pendingErrorRetry = this::loadAllData;
        }
    }

    private void toggleFavorite(View anchor, FoodModel food, boolean currentlyFavorite) {
        Call<ApiResponse<Object>> call = currentlyFavorite
                ? apiService.removeFavorite(new FoodIdRequest(food.getId()))
                : apiService.addFavorite(new FoodIdRequest(food.getId()));
        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (!isAdded()) {
                    return;
                }
                ApiResponse<Object> body = response.body();
                if (response.isSuccessful() && body != null && body.isSuccess()) {
                    foodAdapter.setFavorite(food.getId(), !currentlyFavorite);
                    Snackbar.make(anchor,
                            currentlyFavorite ? R.string.removed_from_favorites : R.string.added_to_favorites,
                            Snackbar.LENGTH_SHORT).show();
                } else {
                    String msg = ApiErrorHelper.getMessage(response, getString(R.string.load_failed,
                            getString(R.string.favorites)));
                    Snackbar.make(anchor, msg, Snackbar.LENGTH_LONG)
                            .setAction(R.string.retry, v -> toggleFavorite(anchor, food, currentlyFavorite))
                            .show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                if (!isAdded()) {
                    return;
                }
                Snackbar.make(anchor, ApiErrorHelper.getNetworkMessage(t,
                                getString(R.string.load_failed, getString(R.string.favorites))),
                        Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry, v -> toggleFavorite(anchor, food, currentlyFavorite))
                        .show();
            }
        });
    }

    private void showRandomFoodDialog(View anchor) {
        if (popularFoods.isEmpty()) {
            Snackbar.make(anchor, R.string.no_results, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.retry, v -> loadPopularFoods(0))
                    .show();
            return;
        }
        FoodModel food = popularFoods.get(new Random().nextInt(popularFoods.size()));
        String message = String.format(Locale.getDefault(), "%s\nRp %,.0f",
                nonEmpty(food.getDescription(), ""), food.getPrice()).trim();
        new MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_QueueApp_AlertDialog)
                .setTitle(food.getName())
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void openSearch(@Nullable String query) {
        Intent intent = new Intent(requireContext(), SearchActivity.class);
        if (query != null && !query.isEmpty()) {
            intent.putExtra(SearchActivity.EXTRA_QUERY, query);
        }
        startActivity(intent);
    }

    private String getDisplayName() {
        UserModel user = SessionManager.getInstance().getUser();
        if (user != null && user.getName() != null && !user.getName().trim().isEmpty()) {
            return user.getName();
        }
        return AppSession.getInstance().getUserName();
    }

    private void finishLoad() {
        pendingLoads = Math.max(0, pendingLoads - 1);
        if (pendingLoads == 0) {
            skeletonContainer.setVisibility(View.GONE);
            swipeRefresh.setRefreshing(false);
            if (!coreContentLoaded && pendingErrorMessage != null) {
                Snackbar.make(requireView(), pendingErrorMessage, Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry, v -> {
                            if (pendingErrorRetry != null) {
                                pendingErrorRetry.run();
                            }
                        })
                        .show();
            }
        }
    }

    private String nonEmpty(@Nullable String value, String fallback) {
        return value != null && !value.trim().isEmpty() ? value : fallback;
    }

    private void navigateMain(int target) {
        if (requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).navigateTo(target);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            SystemUiHelper.setCustomerHomeStatusBar(getActivity());
        }
    }
}
