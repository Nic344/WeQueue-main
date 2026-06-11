package com.example.queueapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.queueapp.api.model.UserListResponse;
import com.example.queueapp.api.model.UserModel;
import com.example.queueapp.data.Resource;
import com.example.queueapp.data.repository.UserRepository;

/** ViewModel for the admin "Manage Users" screen. */
public class AdminUsersViewModel extends ViewModel {

    private final UserRepository repository = new UserRepository();

    private final MutableLiveData<Resource<UserListResponse>> users = new MutableLiveData<>();
    private final MutableLiveData<Resource<UserModel>> roleResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<Object>> deleteResult = new MutableLiveData<>();

    public LiveData<Resource<UserListResponse>> getUsers() {
        return users;
    }

    public LiveData<Resource<UserModel>> getRoleResult() {
        return roleResult;
    }

    public LiveData<Resource<Object>> getDeleteResult() {
        return deleteResult;
    }

    public void loadUsers() {
        repository.getAllUsers(users);
    }

    public void updateRole(int userId, String role) {
        repository.updateUserRole(userId, role, roleResult);
    }

    public void deleteUser(int userId) {
        repository.deleteUser(userId, deleteResult);
    }
}
