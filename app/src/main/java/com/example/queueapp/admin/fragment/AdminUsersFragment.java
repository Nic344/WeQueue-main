package com.example.queueapp.admin.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.queueapp.R;
import com.example.queueapp.admin.adapter.UserAdminAdapter;
import com.example.queueapp.api.ApiConfig;
import com.example.queueapp.api.ApiService;
import com.example.queueapp.api.model.ApiResponse;
import com.example.queueapp.api.model.UserModel;
import com.example.queueapp.data.SessionManager;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminUsersFragment extends Fragment implements UserAdminAdapter.OnUserAdminClickListener {

    private ApiService apiService;
    private SwipeRefreshLayout swipeRefreshAdminUsers;
    private RecyclerView rvAdminUsers;
    private UserAdminAdapter adapter;
    private List<UserModel> allUsers = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = ApiConfig.getApiService();

        swipeRefreshAdminUsers = view.findViewById(R.id.swipeRefreshAdminUsers);
        rvAdminUsers = view.findViewById(R.id.rvAdminUsers);
        SearchView searchAdminUsers = view.findViewById(R.id.searchAdminUsers);

        rvAdminUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new UserAdminAdapter(this);
        rvAdminUsers.setAdapter(adapter);

        swipeRefreshAdminUsers.setOnRefreshListener(this::loadUsers);

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

        loadUsers();
    }

    private void loadUsers() {
        swipeRefreshAdminUsers.setRefreshing(true);
        apiService.getAllUsers().enqueue(new Callback<ApiResponse<List<UserModel>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<UserModel>>> call, Response<ApiResponse<List<UserModel>>> response) {
                if (!isAdded()) return;
                swipeRefreshAdminUsers.setRefreshing(false);
                ApiResponse<List<UserModel>> body = response.body();
                if (response.isSuccessful() && body != null && body.isSuccess() && body.getData() != null) {
                    allUsers = body.getData();
                    adapter.setItems(allUsers);
                } else {
                    Toast.makeText(requireContext(), "Failed to load users", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<UserModel>>> call, Throwable t) {
                if (!isAdded()) return;
                swipeRefreshAdminUsers.setRefreshing(false);
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onChangeRoleClick(UserModel user, String newRole) {
        if (user.getId() == SessionManager.getInstance().getUser().getId()) {
            Toast.makeText(requireContext(), "Cannot change your own role", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObject body = new JsonObject();
        body.addProperty("user_id", user.getId());
        body.addProperty("role", newRole);

        apiService.updateUserRole(body).enqueue(new Callback<ApiResponse<UserModel>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserModel>> call, Response<ApiResponse<UserModel>> response) {
                if (!isAdded()) return;
                ApiResponse<UserModel> responseBody = response.body();
                if (response.isSuccessful() && responseBody != null && responseBody.isSuccess()) {
                    Toast.makeText(requireContext(), "Role updated", Toast.LENGTH_SHORT).show();
                    loadUsers();
                } else {
                    String msg = responseBody != null && responseBody.getMessage() != null ? responseBody.getMessage() : "Update failed";
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserModel>> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
