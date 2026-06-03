package com.example.queueapp.staff.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.queueapp.R;
import com.example.queueapp.staff.adapter.StaffPerformanceAdapter;
import com.example.queueapp.staff.model.StaffPerformanceItem;

import java.util.Arrays;
import java.util.List;

public class StaffTeamPerformanceFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_staff_team_performance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView rvStaffPerformance = view.findViewById(R.id.rvStaffPerformance);
        StaffPerformanceAdapter adapter = new StaffPerformanceAdapter();
        rvStaffPerformance.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvStaffPerformance.setAdapter(adapter);

        List<StaffPerformanceItem> dummy = Arrays.asList(
                new StaffPerformanceItem("Rina", 48, 6, 1),
                new StaffPerformanceItem("Budi", 42, 7, 2),
                new StaffPerformanceItem("Siti", 39, 6, 3),
                new StaffPerformanceItem("Andi", 35, 8, 4)
        );
        adapter.setItems(dummy);
    }
}
