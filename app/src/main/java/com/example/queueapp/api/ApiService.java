package com.example.queueapp.api;

import com.example.queueapp.api.model.ApiResponse;
import com.example.queueapp.api.model.FavoriteListResponse;
import com.example.queueapp.api.model.FoodIdRequest;
import com.example.queueapp.api.model.FoodListResponse;
import com.example.queueapp.api.model.FoodModel;
import com.example.queueapp.api.model.LoginRequest;
import com.example.queueapp.api.model.LoginResponse;
import com.example.queueapp.api.model.MyQueueResponse;
import com.example.queueapp.api.model.ProfileResponse;
import com.example.queueapp.api.model.QueueHistoryListResponse;
import com.example.queueapp.api.model.QueueModel;
import com.example.queueapp.api.model.QueueStatusResponse;
import com.example.queueapp.api.model.RegisterRequest;
import com.example.queueapp.api.model.StaffAllQueuesResponse;
import com.example.queueapp.api.model.StaffDashboardResponse;
import com.example.queueapp.api.model.StaffQueueItem;
import com.example.queueapp.api.model.TakeQueueRequest;
import com.example.queueapp.api.model.UpdateProfileRequest;
import com.example.queueapp.api.model.UploadResponse;
import com.example.queueapp.api.model.UserListResponse;

import com.google.gson.JsonObject;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ApiService {

    // Auth
    @POST("auth/register.php")
    Call<ApiResponse<LoginResponse>> register(@Body RegisterRequest request);

    @POST("auth/login.php")
    Call<ApiResponse<LoginResponse>> login(@Body LoginRequest request);

    @POST("auth/logout.php")
    Call<ApiResponse<Object>> logout();

    // Queue
    @GET("queue/status.php")
    Call<ApiResponse<QueueStatusResponse>> getQueueStatus();

    @POST("queue/take.php")
    Call<ApiResponse<QueueModel>> takeQueue(@Body TakeQueueRequest request);

    @GET("queue/my.php")
    Call<ApiResponse<MyQueueResponse>> getMyQueue();

    @PUT("queue/cancel.php")
    Call<ApiResponse<QueueModel>> cancelQueue();

    @GET("queue/history.php")
    Call<ApiResponse<QueueHistoryListResponse>> getQueueHistory();

    // Foods
    @GET("foods/popular.php")
    Call<ApiResponse<FoodListResponse>> getPopularFoods();

    @GET("foods/search.php")
    Call<ApiResponse<FoodListResponse>> searchFoods(@Query("q") String query);

    @GET("foods/list.php")
    Call<ApiResponse<FoodListResponse>> getFoodList();

    @POST("foods/create.php")
    Call<ApiResponse<FoodModel>> createFood(@Body FoodModel food);

    @PUT("foods/update.php")
    Call<ApiResponse<FoodModel>> updateFood(@Body FoodModel food);

    @HTTP(method = "DELETE", path = "foods/delete.php", hasBody = true)
    Call<ApiResponse<Object>> deleteFood(@Body FoodIdRequest request);

    // Favorites
    @GET("favorites/list.php")
    Call<ApiResponse<FavoriteListResponse>> getFavorites();

    @POST("favorites/add.php")
    Call<ApiResponse<Object>> addFavorite(@Body FoodIdRequest request);

    @HTTP(method = "DELETE", path = "favorites/remove.php", hasBody = true)
    Call<ApiResponse<Object>> removeFavorite(@Body FoodIdRequest request);

    // User
    @GET("user/profile.php")
    Call<ApiResponse<ProfileResponse>> getProfile();

    @PUT("user/update.php")
    Call<ApiResponse<ProfileResponse>> updateProfile(@Body UpdateProfileRequest request);

    @PUT("user/change-password.php")
    Call<ApiResponse<Object>> changePassword(@Body com.example.queueapp.api.model.ChangePasswordRequest request);

    // Upload (multipart)
    @Multipart
    @POST("upload/image.php")
    Call<ApiResponse<UploadResponse>> uploadImage(@Part MultipartBody.Part file);

    // Staff
    @GET("staff/dashboard.php")
    Call<ApiResponse<StaffDashboardResponse>> getStaffDashboard();

    @POST("staff/call-next.php")
    Call<ApiResponse<StaffQueueItem>> callNext();

    @PUT("staff/complete.php")
    Call<ApiResponse<StaffQueueItem>> completeQueue(@Body JsonObject body);

    @PUT("staff/skip.php")
    Call<ApiResponse<StaffQueueItem>> skipQueue(@Body JsonObject body);

    @PUT("staff/cancel-queue.php")
    Call<ApiResponse<StaffQueueItem>> staffCancelQueue(@Body JsonObject body);

    @GET("staff/all-queues.php")
    Call<ApiResponse<StaffAllQueuesResponse>> getAllQueues(
            @Query("status") String status,
            @Query("date") String date,
            @Query("page") int page,
            @Query("limit") int limit);

    // Admin
    @GET("admin/users.php")
    Call<ApiResponse<UserListResponse>> getAllUsers();
    
    @PUT("admin/users/update-role.php")
    Call<ApiResponse<com.example.queueapp.api.model.UserModel>> updateUserRole(@Body JsonObject body);

    @HTTP(method = "DELETE", path = "admin/users/delete.php", hasBody = true)
    Call<ApiResponse<Object>> deleteUser(@Body JsonObject body);
}
