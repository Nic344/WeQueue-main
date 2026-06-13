package com.example.queueapp.data;

import android.content.Context;

import com.example.queueapp.api.model.UserModel;
import com.example.queueapp.auth.RoleManager;
import com.example.queueapp.auth.UserRole;
import com.example.queueapp.backend.BackendCallback;
import com.example.queueapp.model.FoodItem;
import com.example.queueapp.model.QueueHistory;
import com.example.queueapp.model.QueueTicket;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class AppSession {

    private static AppSession instance;

    private final Set<Integer> favoriteIds = new HashSet<>();

    private QueueTicket activeTicket;
    private String nowServing = "A015";
    private String userQueue = "-";
    private int remainingPeople = 0;
    private int estimatedWaitMinutes = 0;
    private int nextQueueCounter = 22;
    private boolean loggedIn;
    private String userName = "";
    private String userEmail = "";
    private int userId;

    private AppSession() {
        favoriteIds.add(1);
        favoriteIds.add(4);
    }

    public static synchronized AppSession getInstance() {
        if (instance == null) {
            instance = new AppSession();
        }
        return instance;
    }

    public void init(Context context) {
        SessionManager.getInstance().init(context);
        restoreFromSession();
    }

    public void restoreFromSession() {
        SessionManager sessionManager = SessionManager.getInstance();
        if (!sessionManager.isLoggedIn()) {
            loggedIn = false;
            return;
        }
        UserModel user = sessionManager.getUser();
        if (user != null) {
            applyUser(user);
        }
        loggedIn = true;
    }

    public void applyUser(UserModel user) {
        if (user == null) {
            return;
        }
        userId = user.getId();
        userName = user.getName() != null ? user.getName() : "";
        userEmail = user.getEmail() != null ? user.getEmail() : "";
        loggedIn = true;
        RoleManager.getInstance().setRole(user.getRole() != null ? user.getRole() : UserRole.CUSTOMER);
    }

    public boolean isLoggedIn() {
        return loggedIn && SessionManager.getInstance().isLoggedIn();
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public int getUserId() {
        return userId;
    }

    public String getNowServing() {
        return nowServing;
    }

    public String getUserQueue() {
        return userQueue;
    }

    public int getRemainingPeople() {
        return remainingPeople;
    }

    public int getEstimatedWaitMinutes() {
        return estimatedWaitMinutes;
    }

    public boolean hasActiveTicket() {
        return activeTicket != null;
    }

    public QueueTicket getActiveTicket() {
        return activeTicket;
    }

    public void refreshFromBackendIfNeeded() {

    }

    public QueueTicket takeNewQueue() {
        return takeNewQueueLocal();
    }

    public void refreshQueue() {
        refreshQueueLocal();
    }

    public void cancelQueue() {
        cancelQueueLocal();
    }

    public void loadQueueHistory(BackendCallback<List<QueueHistory>> callback) {
        callback.onSuccess(MockDataProvider.getQueueHistory());
    }

    public boolean isFavorite(int foodId) {
        return favoriteIds.contains(foodId);
    }

    public boolean toggleFavorite(int foodId) {
        if (favoriteIds.contains(foodId)) {
            favoriteIds.remove(foodId);
            return false;
        }
        favoriteIds.add(foodId);
        return true;
    }

    public List<FoodItem> getFavoriteFoods() {
        List<FoodItem> all = MockDataProvider.getAllFoods();
        List<FoodItem> favorites = new java.util.ArrayList<>();
        for (FoodItem item : all) {
            if (favoriteIds.contains(item.getId())) {
                favorites.add(item);
            }
        }
        return favorites;
    }

    public void resetSession() {
        SessionManager.getInstance().clearSession();
        loggedIn = false;
        activeTicket = null;
        userQueue = "-";
        remainingPeople = 0;
        estimatedWaitMinutes = 0;
        RoleManager.getInstance().clear();
    }

    private QueueTicket takeNewQueueLocal() {
        String number = String.format("A%03d", nextQueueCounter++);
        int position = 3 + (int) (Math.random() * 8);
        int wait = position * 3;
        activeTicket = new QueueTicket(number, position, wait, nowServing);
        userQueue = number;
        remainingPeople = position;
        estimatedWaitMinutes = wait;
        return activeTicket;
    }

    private void refreshQueueLocal() {
        if (activeTicket == null) {
            return;
        }
        if (remainingPeople > 1) {
            remainingPeople--;
            estimatedWaitMinutes = Math.max(3, estimatedWaitMinutes - 3);
        }
        int servingNum = Integer.parseInt(nowServing.substring(1));
        if (servingNum < nextQueueCounter - 1) {
            nowServing = String.format("A%03d", servingNum + 1);
        }
        activeTicket = new QueueTicket(
                activeTicket.getQueueNumber(),
                remainingPeople,
                estimatedWaitMinutes,
                nowServing
        );
    }

    private void cancelQueueLocal() {
        activeTicket = null;
        remainingPeople = 0;
        estimatedWaitMinutes = 0;
        userQueue = "-";
    }
}
