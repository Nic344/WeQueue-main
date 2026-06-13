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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.queueapp.MainActivity;
import com.example.queueapp.R;
import com.example.queueapp.SearchActivity;
import com.example.queueapp.adapter.PopularFoodAdapter;
import com.example.queueapp.api.model.FavoriteModel;
import com.example.queueapp.api.model.FoodModel;
import com.example.queueapp.api.model.QueueStatusResponse;
import com.example.queueapp.api.model.UserModel;
import com.example.queueapp.data.AppSession;
import com.example.queueapp.data.SessionManager;
import com.example.queueapp.util.SystemUiHelper;
import com.example.queueapp.viewmodel.HomeViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;
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

    private int pendingToggleFoodId = -1;
    private boolean pendingTogglePrevState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

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

        observeViewModel();
        view.post(this::loadAllData);
    }

    private void observeViewModel() {
        viewModel.getQueueStatus().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null || resource.isLoading()) {
                return;
            }
            if (resource.isSuccess() && resource.data != null) {
                QueueStatusResponse status = resource.data;
                tvNowServing.setText(nonEmpty(status.getNowServing(), "—"));
                tvRemaining.setText(getString(R.string.people, status.getRemaining()));
                tvEstimatedWait.setText(getString(R.string.minutes, status.getEstimatedWaitMinutes()));
                coreContentLoaded = true;
            } else if (resource.isError()) {
                queueError(resource.message);
            }
            finishLoad();
        });

        viewModel.getMyQueue().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null || resource.isLoading()) {
                return;
            }
            if (resource.isSuccess() && resource.data != null
                    && resource.data.isHasActiveQueue() && resource.data.getQueue() != null) {
                tvYourQueue.setText(resource.data.getQueue().getQueueNumber());
            } else {
                tvYourQueue.setText("—");
            }
            finishLoad();
        });

        viewModel.getFavorites().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null || resource.isLoading()) {
                return;
            }
            if (resource.isSuccess() && resource.data != null) {
                Set<Integer> ids = new HashSet<>();
                List<FavoriteModel> favorites = resource.data.getFavorites();
                if (favorites != null) {
                    for (FavoriteModel favorite : favorites) {
                        ids.add(favorite.getId());
                    }
                }
                foodAdapter.setFavoriteIds(ids);
            }
            finishLoad();
        });

        viewModel.getPopularFoods().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null || resource.isLoading()) {
                return;
            }
            if (resource.isSuccess() && resource.data != null) {
                popularFoods.clear();
                if (resource.data.getFoods() != null) {
                    popularFoods.addAll(resource.data.getFoods());
                }
                foodAdapter.setItems(popularFoods);
                if (!popularFoods.isEmpty()) {
                    coreContentLoaded = true;
                }
            } else if (resource.isError()) {
                queueError(resource.message);
            }
            finishLoad();
        });

        viewModel.getFavoriteToggle().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null || resource.isLoading() || pendingToggleFoodId == -1) {
                return;
            }
            if (resource.isError()) {

                foodAdapter.setFavorite(pendingToggleFoodId, pendingTogglePrevState);
                Snackbar.make(requireView(),
                        resource.message != null ? resource.message
                                : getString(R.string.load_failed, getString(R.string.favorites)),
                        Snackbar.LENGTH_LONG).show();
            }
            pendingToggleFoodId = -1;
        });
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
        skeletonContainer.setVisibility(View.VISIBLE);

        viewModel.loadQueueStatus();
        viewModel.loadPopularFoods();
        viewModel.loadFavorites();
        viewModel.loadMyQueue();
    }

    private void queueError(@Nullable String message) {
        if (pendingErrorMessage == null && message != null) {
            pendingErrorMessage = message;
        }
    }

    private void toggleFavorite(View anchor, FoodModel food, boolean currentlyFavorite) {

        pendingToggleFoodId = food.getId();
        pendingTogglePrevState = currentlyFavorite;
        foodAdapter.setFavorite(food.getId(), !currentlyFavorite);
        Snackbar.make(anchor,
                currentlyFavorite ? R.string.removed_from_favorites : R.string.added_to_favorites,
                Snackbar.LENGTH_SHORT).show();
        viewModel.toggleFavorite(food.getId(), currentlyFavorite);
    }

    private void showRandomFoodDialog(View anchor) {
        if (popularFoods.isEmpty()) {
            Snackbar.make(anchor, R.string.no_results, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.retry, v -> viewModel.loadPopularFoods())
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
                        .setAction(R.string.retry, v -> loadAllData())
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
