package com.example.queueapp.staff.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.queueapp.R;
import com.example.queueapp.auth.RoleNavigation;
import com.example.queueapp.data.AppSession;
import com.google.android.material.button.MaterialButton;

public class StaffProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_staff_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AppSession session = AppSession.getInstance();
        TextView tvStaffProfileName = view.findViewById(R.id.tvStaffProfileName);
        TextView tvStaffProfileEmail = view.findViewById(R.id.tvStaffProfileEmail);
        MaterialButton btnStaffLogout = view.findViewById(R.id.btnStaffLogout);

        tvStaffProfileName.setText(session.getUserName());
        tvStaffProfileEmail.setText(session.getUserEmail());

        btnStaffLogout.setOnClickListener(v -> {
            session.resetSession();
            RoleNavigation.navigateToLogin(requireContext());
            requireActivity().finish();
        });
    }
}
