package com.example.queueapp.fragment;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.queueapp.MainActivity;
import com.example.queueapp.R;
import com.example.queueapp.api.model.MyQueueResponse;
import com.example.queueapp.api.model.QueueModel;
import com.example.queueapp.data.Resource;
import com.example.queueapp.util.QueueNotifier;
import com.example.queueapp.util.SystemUiHelper;
import com.example.queueapp.viewmodel.QueueViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

public class QueueFragment extends Fragment {

    private QueueViewModel viewModel;
    private View queueContent;
    private View emptyQueueState;
    private TextView tvTicketNumber;
    private TextView tvPosition;
    private TextView tvTicketWait;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressQueue;
    private MaterialButton btnRefreshQueue;
    private MaterialButton btnCancelQueue;
    private MaterialButton btnTakeQueueEmpty;
    private QueueModel activeQueue;

    private static final long AUTO_REFRESH_INTERVAL = 20_000L;
    private final Handler autoRefreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable autoRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (isAdded()) {
                viewModel.loadMyQueue();
                autoRefreshHandler.postDelayed(this, AUTO_REFRESH_INTERVAL);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_queue, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(QueueViewModel.class);

        queueContent = view.findViewById(R.id.queueContent);
        emptyQueueState = view.findViewById(R.id.emptyQueueState);
        tvTicketNumber = view.findViewById(R.id.tvTicketNumber);
        tvPosition = view.findViewById(R.id.tvPosition);
        tvTicketWait = view.findViewById(R.id.tvTicketWait);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        progressQueue = view.findViewById(R.id.progressQueue);
        btnRefreshQueue = view.findViewById(R.id.btnRefreshQueue);
        btnCancelQueue = view.findViewById(R.id.btnCancelQueue);
        btnTakeQueueEmpty = view.findViewById(R.id.btnTakeQueueEmpty);

        View queueAppBar = view.findViewById(R.id.queueAppBar);
        int horizontalPadding = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 16f, getResources().getDisplayMetrics());
        SystemUiHelper.applyHeaderInsets(queueAppBar, horizontalPadding, 0);

        swipeRefresh.setColorSchemeResources(R.color.primary);
        swipeRefresh.setOnRefreshListener(() -> viewModel.loadMyQueue());

        btnRefreshQueue.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up));
            setLoading(true);
            viewModel.loadMyQueue();
        });
        btnCancelQueue.setOnClickListener(v -> showCancelQueueDialog());
        btnTakeQueueEmpty.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up));
            setLoading(true);
            viewModel.takeQueue();
        });

        observeViewModel();
        setLoading(true);
        viewModel.loadMyQueue();
    }

    private void observeViewModel() {
        viewModel.getMyQueue().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null || resource.isLoading()) {
                return;
            }
            setLoading(false);
            swipeRefresh.setRefreshing(false);
            if (resource.isSuccess() && resource.data != null) {
                MyQueueResponse data = resource.data;
                activeQueue = data.isHasActiveQueue() ? data.getQueue() : null;
                QueueNotifier.evaluate(requireContext(), activeQueue);
                refreshUi();
            } else if (resource.isError()) {
                showRetry(resource.message, () -> {
                    setLoading(true);
                    viewModel.loadMyQueue();
                });
            }
        });

        viewModel.getTakeResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null || resource.isLoading()) {
                return;
            }
            setLoading(false);
            if (resource.isSuccess() && resource.data != null) {
                activeQueue = resource.data;
                refreshUi();
                Snackbar.make(requireView(),
                        getString(R.string.queue_taken, activeQueue.getQueueNumber()),
                        Snackbar.LENGTH_LONG).show();
            } else if (resource.isError()) {
                showRetry(resource.message, () -> {
                    setLoading(true);
                    viewModel.takeQueue();
                });
            }
        });

        viewModel.getCancelResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null || resource.isLoading()) {
                return;
            }
            setLoading(false);
            if (resource.isSuccess()) {
                activeQueue = null;
                refreshUi();
                Snackbar.make(requireView(), R.string.queue_cancelled, Snackbar.LENGTH_SHORT).show();
            } else if (resource.isError()) {
                showRetry(resource.message, () -> {
                    setLoading(true);
                    viewModel.cancelQueue();
                });
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateStatusBarForQueueState();
        autoRefreshHandler.postDelayed(autoRefreshRunnable, AUTO_REFRESH_INTERVAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        autoRefreshHandler.removeCallbacks(autoRefreshRunnable);
    }

    private void showCancelQueueDialog() {
        if (activeQueue == null) {
            return;
        }
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(
                requireContext(), R.style.ThemeOverlay_QueueApp_AlertDialog)
                .setTitle(R.string.cancel_queue_title)
                .setMessage(getString(R.string.cancel_queue_message, activeQueue.getQueueNumber()))
                .setNegativeButton(R.string.keep_queue, null)
                .setPositiveButton(R.string.yes_cancel, (d, w) -> {
                    setLoading(true);
                    viewModel.cancelQueue();
                });

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            Button keepButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE);
            Button cancelButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);

            if (keepButton != null) {
                keepButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
                keepButton.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
                keepButton.setBackgroundResource(R.drawable.bg_dialog_button_outlined_orange);
            }
            if (cancelButton != null) {
                cancelButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                cancelButton.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
                cancelButton.setBackgroundTintList(
                        ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.cancel_red)));
            }
        });
        dialog.show();
    }

    public void refreshUi() {
        if (queueContent == null) {
            return;
        }
        if (activeQueue != null) {
            queueContent.setVisibility(View.VISIBLE);
            emptyQueueState.setVisibility(View.GONE);
            tvTicketNumber.setText(activeQueue.getQueueNumber());
            tvPosition.setText(getString(R.string.position_format, activeQueue.getPosition()));
            tvTicketWait.setText(getString(R.string.minutes, activeQueue.getEstimatedWait()));
            queueContent.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in));
        } else {
            queueContent.setVisibility(View.GONE);
            emptyQueueState.setVisibility(View.VISIBLE);
        }
        updateStatusBarForQueueState();
    }

    private void setLoading(boolean loading) {
        if (progressQueue != null) {
            progressQueue.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
        if (btnRefreshQueue != null) {
            btnRefreshQueue.setEnabled(!loading);
        }
        if (btnCancelQueue != null) {
            btnCancelQueue.setEnabled(!loading);
        }
        if (btnTakeQueueEmpty != null) {
            btnTakeQueueEmpty.setEnabled(!loading);
        }
    }

    private void showRetry(@Nullable String message, Runnable retry) {
        if (!isAdded()) {
            return;
        }
        swipeRefresh.setRefreshing(false);
        String text = message != null ? message : getString(R.string.action_failed, "");
        Snackbar.make(requireView(), text, Snackbar.LENGTH_LONG)
                .setAction(R.string.retry, v -> retry.run())
                .show();
    }

    private void updateStatusBarForQueueState() {
        if (!(getActivity() instanceof MainActivity)) {
            return;
        }
        if (activeQueue != null) {
            SystemUiHelper.setQueueScreenStatusBar(requireActivity());
        } else {
            SystemUiHelper.setQueueEmptyStatusBar(requireActivity());
        }
    }
}
