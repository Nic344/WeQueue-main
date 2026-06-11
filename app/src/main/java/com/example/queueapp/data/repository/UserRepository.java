package com.example.queueapp.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.example.queueapp.api.model.ChangePasswordRequest;
import com.example.queueapp.api.model.ProfileResponse;
import com.example.queueapp.api.model.UpdateProfileRequest;
import com.example.queueapp.api.model.UploadResponse;
import com.example.queueapp.api.model.UserListResponse;
import com.example.queueapp.api.model.UserModel;
import com.example.queueapp.data.Resource;

import com.google.gson.JsonObject;

import okhttp3.MultipartBody;

/** Repository for user/profile operations and admin user management. */
public class UserRepository extends BaseRepository {

    public void getProfile(MutableLiveData<Resource<ProfileResponse>> target) {
        enqueue(api.getProfile(), target, "Failed to load profile");
    }

    public void updateProfile(String name, String email, String profilePicture,
                              MutableLiveData<Resource<ProfileResponse>> target) {
        enqueue(api.updateProfile(new UpdateProfileRequest(name, email, profilePicture)),
                target, "Failed to update profile");
    }

    public void changePassword(String current, String newPass,
                               MutableLiveData<Resource<Object>> target) {
        enqueue(api.changePassword(new ChangePasswordRequest(current, newPass)),
                target, "Failed to change password");
    }

    public void uploadImage(MultipartBody.Part part, MutableLiveData<Resource<UploadResponse>> target) {
        enqueue(api.uploadImage(part), target, "Failed to upload image");
    }

    // --- Admin user management ---

    public void getAllUsers(MutableLiveData<Resource<UserListResponse>> target) {
        enqueue(api.getAllUsers(), target, "Failed to load users");
    }

    public void updateUserRole(int userId, String role, MutableLiveData<Resource<UserModel>> target) {
        JsonObject body = new JsonObject();
        body.addProperty("user_id", userId);
        body.addProperty("role", role);
        enqueue(api.updateUserRole(body), target, "Failed to update role");
    }

    public void deleteUser(int userId, MutableLiveData<Resource<Object>> target) {
        JsonObject body = new JsonObject();
        body.addProperty("user_id", userId);
        enqueue(api.deleteUser(body), target, "Failed to delete user");
    }
}
