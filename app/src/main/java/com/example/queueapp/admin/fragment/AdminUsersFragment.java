package com.example.queueapp.admin.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.queueapp.R;
import com.example.queueapp.admin.adapter.UserAdminAdapter;
import com.example.queueapp.api.model.UserModel;
import com.example.queueapp.data.SessionManager;
import com.example.queueapp.viewmodel.AdminUsersViewModel;

import java.util.List;

/** Admin "Manage Users" screen (MVVM). */
public class AdminUsersFragment extends Fragment implements UserAdminAdapter.OnUserAdminClickListener {

    private SwipeRefreshLayout swipeRefreshAdminUsers;
    private UserAdminAdapter adapter;
    private AdminUsersViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AdminUsersViewModel.class);

        swipeRefreshAdminUsers = view.findViewById(R.id.swipeRefreshAdminUsers);
        RecyclerView rvAdminUsers = view.findViewById(R.id.rvAdminUsers);
        SearchView searchAdminUsers = view.findViewById(R.id.searchAdminUsers);

        rvAdminUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new UserAdminAdapter(this);
        rvAdminUsers.setAdapter(adapter);

        swipeRefreshAdminUsers.setOnRefreshListener(() -> viewModel.loadUsers());

        searchAdminUsers.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });

        observeViewModel();
        viewModel.loadUsers();
    }

    private void observeViewModel() {
        viewModel.getUsers().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) {
                return;
            }
            swipeRefreshAdminUsers.setRefreshing(resource.isLoading());
            if (resource.isSuccess() && resource.data != null) {
                List<UserModel> users = resource.data.getUsers();
                if (users != null) {
                    adapter.setItems(users);
                }
            } else if (resource.isError()) {
                Toast.makeText(requireContext(),
                        resource.message != null ? resource.message : "Failed to load users",
                        Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getRoleResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null || resource.isLoading()) {
                return;
            }
            if (resource.isSuccess()) {
                Toast.makeText(requireContext(), "Role updated", Toast.LENGTH_SHORT).show();
                viewModel.loadUsers();
            } else if (resource.isError()) {
                Toast.makeText(requireContext(),
                        resource.message != null ? resource.message : "Update failed",
                        Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getDeleteResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null || resource.isLoading()) {
                return;
            }
            if (resource.isSuccess()) {
                Toast.makeText(requireContext(), "User deleted", Toast.LENGTH_SHORT).show();
                viewModel.loadUsers();
            } else if (resource.isError()) {
                Toast.makeText(requireContext(),
                        resource.message != null ? resource.message : "Delete failed",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onChangeRoleClick(UserModel user, String newRole) {
        if (user.getId() == SessionManager.getInstance().getUser().getId()) {
            Toast.makeText(requireContext(), "Cannot change your own role", Toast.LENGTH_SHORT).show();
            return;
        }
        viewModel.updateRole(user.getId(), newRole);
    }

    @Override
    public void onDeleteUserClick(UserModel user) {
        if (user.getId() == SessionManager.getInstance().getUser().getId()) {
            Toast.makeText(requireContext(), "Cannot delete your own account", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.admin_delete_user)
                .setMessage(getString(R.string.admin_delete_user_confirm,
                        user.getName() != null ? user.getName() : user.getEmail()))
                .setPositiveButton(R.string.yes, (d, w) -> viewModel.deleteUser(user.getId()))
                .setNegativeButton(R.string.no, null)
                .show();
    }
}
