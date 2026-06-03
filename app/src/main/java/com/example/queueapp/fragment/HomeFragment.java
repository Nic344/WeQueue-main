package com.example.queueapp.fragment;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
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

import com.example.queueapp.FoodRecommendationActivity;
import com.example.queueapp.MainActivity;
import com.example.queueapp.R;
import com.example.queueapp.adapter.FoodHorizontalAdapter;
import com.example.queueapp.data.AppSession;
import com.example.queueapp.data.MockDataProvider;
import com.example.queueapp.util.SystemUiHelper;
import com.google.android.material.snackbar.Snackbar;

public class HomeFragment extends Fragment {

    private AppSession session;
    private TextView tvGreeting;
    private TextView tvNowServing;
    private TextView tvYourQueue;
    private TextView tvRemaining;
    private TextView tvEstimatedWait;
    private SwipeRefreshLayout swipeRefresh;
    private View skeletonContainer;
    private FoodHorizontalAdapter foodAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        session = AppSession.getInstance();

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

        tvGreeting.setText(getString(R.string.good_afternoon, session.getUserName()));
        updateQueueStatus();

        int primaryColor = ContextCompat.getColor(requireContext(), R.color.primary);
        setupQuickAction(view.findViewById(R.id.actionTakeQueue), R.drawable.ic_queue, R.string.take_queue,
                primaryColor, v -> takeQueueWithAnimation(v));
        setupQuickAction(view.findViewById(R.id.actionRecommendation), R.drawable.ic_food_coffee,
                R.string.food_recommendation, primaryColor,
                v -> startActivity(new Intent(requireContext(), FoodRecommendationActivity.class)));
        setupQuickAction(view.findViewById(R.id.actionFavorites), R.drawable.ic_favorite_filled,
                R.string.favorites, primaryColor,
                v -> navigateMain(MainActivity.NAV_FAVORITES));
        setupQuickAction(view.findViewById(R.id.actionHistory), R.drawable.ic_history, R.string.history,
                primaryColor, v -> navigateMain(MainActivity.NAV_HISTORY));

        foodAdapter = new FoodHorizontalAdapter((item, isFavorite) -> {
            int msg = isFavorite ? R.string.added_to_favorites : R.string.removed_from_favorites;
            Snackbar.make(view, msg, Snackbar.LENGTH_SHORT).show();
        });
        rvPopularFoods.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvPopularFoods.setAdapter(foodAdapter);

        showSkeletonThenLoad();

        swipeRefresh.setColorSchemeColors(primaryColor);
        swipeRefresh.setOnRefreshListener(() -> {
            updateQueueStatus();
            session.refreshQueue();
            foodAdapter.setItems(MockDataProvider.getAllFoods());
            swipeRefresh.setRefreshing(false);
            Snackbar.make(view, R.string.queue_refreshed, Snackbar.LENGTH_SHORT).show();
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            String query = etSearch.getText() != null ? etSearch.getText().toString().trim() : "";
            if (!query.isEmpty()) {
                Snackbar.make(view, getString(R.string.search_tapped, query), Snackbar.LENGTH_SHORT).show();
            }
            return true;
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

    private void showSkeletonThenLoad() {
        skeletonContainer.setVisibility(View.VISIBLE);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isAdded()) {
                return;
            }
            skeletonContainer.setVisibility(View.GONE);
            foodAdapter.setItems(MockDataProvider.getAllFoods());
        }, 800);
    }

    private void takeQueueWithAnimation(View view) {
        view.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up));
        session.takeNewQueue();
        updateQueueStatus();
        Snackbar.make(view, getString(R.string.queue_taken, session.getUserQueue()), Snackbar.LENGTH_LONG).show();
        navigateMain(MainActivity.NAV_QUEUE);
    }

    private void updateQueueStatus() {
        tvNowServing.setText(session.getNowServing());
        tvYourQueue.setText(session.getUserQueue());
        tvRemaining.setText(getString(R.string.people, session.getRemainingPeople()));
        tvEstimatedWait.setText(getString(R.string.minutes, session.getEstimatedWaitMinutes()));
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
        if (tvGreeting != null) {
            updateQueueStatus();
        }
    }

}
