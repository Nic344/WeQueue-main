package com.example.queueapp.fragment;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.queueapp.MainActivity;
import com.example.queueapp.R;
import com.example.queueapp.data.AppSession;
import com.example.queueapp.model.QueueTicket;
import com.example.queueapp.util.SystemUiHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

public class QueueFragment extends Fragment {

    private AppSession session;
    private View queueContent;
    private View emptyQueueState;
    private TextView tvTicketNumber;
    private TextView tvPosition;
    private TextView tvTicketWait;
    private SwipeRefreshLayout swipeRefresh;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_queue, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        session = AppSession.getInstance();

        queueContent = view.findViewById(R.id.queueContent);
        emptyQueueState = view.findViewById(R.id.emptyQueueState);
        tvTicketNumber = view.findViewById(R.id.tvTicketNumber);
        tvPosition = view.findViewById(R.id.tvPosition);
        tvTicketWait = view.findViewById(R.id.tvTicketWait);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        MaterialButton btnRefreshQueue = view.findViewById(R.id.btnRefreshQueue);
        MaterialButton btnCancelQueue = view.findViewById(R.id.btnCancelQueue);
        MaterialButton btnTakeQueueEmpty = view.findViewById(R.id.btnTakeQueueEmpty);

        View queueAppBar = view.findViewById(R.id.queueAppBar);
        int horizontalPadding = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 16f, getResources().getDisplayMetrics());
        SystemUiHelper.applyHeaderInsets(queueAppBar, horizontalPadding, 0);

        swipeRefresh.setColorSchemeResources(R.color.primary);
        swipeRefresh.setOnRefreshListener(() -> {
            if (session.hasActiveTicket()) {
                session.refreshQueue();
                refreshUi();
                Snackbar.make(view, R.string.queue_refreshed, Snackbar.LENGTH_SHORT).show();
            }
            swipeRefresh.setRefreshing(false);
        });

        btnRefreshQueue.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up));
            session.refreshQueue();
            refreshUi();
            Snackbar.make(view, R.string.queue_refreshed, Snackbar.LENGTH_SHORT).show();
        });

        btnCancelQueue.setOnClickListener(v -> showCancelQueueDialog(view));

        btnTakeQueueEmpty.setOnClickListener(v -> {
            if (requireActivity() instanceof MainActivity) {
                ((MainActivity) requireActivity()).navigateTo(MainActivity.NAV_HOME);
            }
        });

        refreshUi();
        updateStatusBarForQueueState();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!(requireActivity() instanceof MainActivity)) {
            return;
        }
        updateStatusBarForQueueState();
    }

    private void updateStatusBarForQueueState() {
        if (!(requireActivity() instanceof MainActivity)) {
            return;
        }
        if (session != null && session.hasActiveTicket()) {
            SystemUiHelper.setQueueScreenStatusBar(requireActivity());
        } else {
            SystemUiHelper.setQueueEmptyStatusBar(requireActivity());
        }
    }

    private void showCancelQueueDialog(View anchor) {
        if (!session.hasActiveTicket()) {
            return;
        }
        QueueTicket ticket = session.getActiveTicket();
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(
                requireContext(), R.style.ThemeOverlay_QueueApp_AlertDialog)
                .setTitle(R.string.cancel_queue_title)
                .setMessage(getString(R.string.cancel_queue_message, ticket.getQueueNumber()))
                .setNegativeButton(R.string.keep_queue, null)
                .setPositiveButton(R.string.yes_cancel, (d, w) -> {
                    session.cancelQueue();
                    refreshUi();
                    Snackbar.make(anchor, R.string.queue_cancelled, Snackbar.LENGTH_SHORT).show();
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
        if (session == null || queueContent == null) {
            return;
        }
        if (session.hasActiveTicket()) {
            queueContent.setVisibility(View.VISIBLE);
            emptyQueueState.setVisibility(View.GONE);
            QueueTicket ticket = session.getActiveTicket();
            tvTicketNumber.setText(ticket.getQueueNumber());
            tvPosition.setText(getString(R.string.position_format, ticket.getPosition()));
            tvTicketWait.setText(getString(R.string.minutes, ticket.getEstimatedMinutes()));
            queueContent.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in));
        } else {
            queueContent.setVisibility(View.GONE);
            emptyQueueState.setVisibility(View.VISIBLE);
        }
        updateStatusBarForQueueState();
    }
}
