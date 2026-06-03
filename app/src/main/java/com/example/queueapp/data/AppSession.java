package com.example.queueapp.data;

import android.content.Context;

import com.example.queueapp.auth.RoleManager;
import com.example.queueapp.backend.BackendCallback;
import com.example.queueapp.backend.auth.FirebaseAuthRepository;
import com.example.queueapp.backend.history.HistoryBackendRepository;
import com.example.queueapp.backend.queue.QueueBackendRepository;
import com.example.queueapp.backend.queue.QueueStateSnapshot;
import com.example.queueapp.backend.user.UserBackendRepository;
import com.example.queueapp.backend.user.UserProfile;
import com.example.queueapp.model.FoodItem;
import com.example.queueapp.model.QueueHistory;
import com.example.queueapp.model.QueueTicket;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class AppSession {

    private static AppSession instance;

    private final Set<Integer> favoriteIds = new HashSet<>();
    private final FirebaseAuthRepository authRepository = new FirebaseAuthRepository();
    private final UserBackendRepository userRepository = new UserBackendRepository();
    private final QueueBackendRepository queueRepository = new QueueBackendRepository();
    private final HistoryBackendRepository historyRepository = new HistoryBackendRepository();

    private QueueTicket activeTicket;
    private String activeQueueId;
    private String nowServing = "A015";
    private String userQueue = "A020";
    private int remainingPeople = 5;
    private int estimatedWaitMinutes = 15;
    private int nextQueueCounter = 21;
    private boolean loggedIn;
    private String userName = "Devin";
    private String userEmail = "devin@wequeue.app";
    private String currentUid;
    private boolean backendReady;

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
        queueRepository.ensureGlobalQueueState(() -> backendReady = true);
        FirebaseUser user = authRepository.getCurrentUser();
        if (user != null) {
            currentUid = user.getUid();
            loggedIn = true;
            userEmail = user.getEmail() != null ? user.getEmail() : userEmail;
            syncFromBackend(null);
        }
    }

    public FirebaseAuthRepository getAuthRepository() {
        return authRepository;
    }

    public UserBackendRepository getUserRepository() {
        return userRepository;
    }

    public boolean isLoggedIn() {
        return loggedIn && authRepository.isSignedIn();
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

    public void onLoginSuccess(FirebaseUser user, String displayName, Runnable onComplete) {
        currentUid = user.getUid();
        loggedIn = true;
        userEmail = user.getEmail() != null ? user.getEmail() : userEmail;
        userName = displayName;

        userRepository.ensureUserDocument(currentUid, displayName, userEmail,
                new BackendCallback<UserProfile>() {
                    @Override
                    public void onSuccess(UserProfile profile) {
                        applyUserProfile(profile);
                        syncFromBackend(onComplete);
                    }

                    @Override
                    public void onError(String message) {
                        syncFromBackend(onComplete);
                    }
                });
    }

    public void restoreFromFirebase(FirebaseUser user, Runnable onComplete) {
        currentUid = user.getUid();
        loggedIn = true;
        userEmail = user.getEmail() != null ? user.getEmail() : userEmail;
        userRepository.loadProfile(currentUid, new BackendCallback<UserProfile>() {
            @Override
            public void onSuccess(UserProfile profile) {
                applyUserProfile(profile);
                syncFromBackend(onComplete);
            }

            @Override
            public void onError(String message) {
                syncFromBackend(onComplete);
            }
        });
    }

    public void refreshFromBackendIfNeeded() {
        if (isLoggedIn()) {
            syncFromBackend(null);
        }
    }

    public void syncFromBackend(Runnable onComplete) {
        if (!authRepository.isSignedIn() || currentUid == null) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        if (!RoleManager.getInstance().isCustomer()) {
            userRepository.loadProfile(currentUid, new BackendCallback<UserProfile>() {
                @Override
                public void onSuccess(UserProfile profile) {
                    applyUserProfile(profile);
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }

                @Override
                public void onError(String message) {
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            });
            return;
        }

        userRepository.loadProfile(currentUid, new BackendCallback<UserProfile>() {
            @Override
            public void onSuccess(UserProfile profile) {
                applyUserProfile(profile);
                queueRepository.syncQueueState(currentUid, activeQueueId,
                        new BackendCallback<QueueStateSnapshot>() {
                            @Override
                            public void onSuccess(QueueStateSnapshot snapshot) {
                                applyQueueSnapshot(snapshot);
                                if (onComplete != null) {
                                    onComplete.run();
                                }
                            }

                            @Override
                            public void onError(String message) {
                                if (onComplete != null) {
                                    onComplete.run();
                                }
                            }
                        });
            }

            @Override
            public void onError(String message) {
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
    }

    public QueueTicket takeNewQueue() {
        QueueTicket localTicket = takeNewQueueLocal();
        if (!canUseBackend()) {
            return localTicket;
        }

        queueRepository.takeQueue(currentUid, userName, new BackendCallback<QueueStateSnapshot>() {
            @Override
            public void onSuccess(QueueStateSnapshot snapshot) {
                applyQueueSnapshot(snapshot);
            }

            @Override
            public void onError(String message) {
                // Keep optimistic local ticket when backend fails.
            }
        });
        return localTicket;
    }

    public void refreshQueue() {
        if (!hasActiveTicket()) {
            return;
        }
        if (!canUseBackend() || activeQueueId == null) {
            refreshQueueLocal();
            return;
        }

        queueRepository.syncQueueState(currentUid, activeQueueId,
                new BackendCallback<QueueStateSnapshot>() {
                    @Override
                    public void onSuccess(QueueStateSnapshot snapshot) {
                        applyQueueSnapshot(snapshot);
                    }

                    @Override
                    public void onError(String message) {
                        refreshQueueLocal();
                    }
                });
    }

    public void cancelQueue() {
        if (!canUseBackend() || activeQueueId == null) {
            cancelQueueLocal();
            return;
        }

        String queueNumber = activeTicket != null ? activeTicket.getQueueNumber() : userQueue;
        queueRepository.cancelQueue(currentUid, activeQueueId, queueNumber,
                new BackendCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        cancelQueueLocal();
                    }

                    @Override
                    public void onError(String message) {
                        cancelQueueLocal();
                    }
                });
    }

    public void loadQueueHistory(BackendCallback<List<QueueHistory>> callback) {
        if (!canUseBackend()) {
            callback.onSuccess(MockDataProvider.getQueueHistory());
            return;
        }
        historyRepository.loadHistory(currentUid, new BackendCallback<List<QueueHistory>>() {
            @Override
            public void onSuccess(List<QueueHistory> history) {
                if (history == null || history.isEmpty()) {
                    callback.onSuccess(MockDataProvider.getQueueHistory());
                } else {
                    callback.onSuccess(history);
                }
            }

            @Override
            public void onError(String message) {
                callback.onSuccess(MockDataProvider.getQueueHistory());
            }
        });
    }

    public boolean isFavorite(int foodId) {
        return favoriteIds.contains(foodId);
    }

    public boolean toggleFavorite(int foodId) {
        if (favoriteIds.contains(foodId)) {
            favoriteIds.remove(foodId);
            persistFavorites();
            return false;
        }
        favoriteIds.add(foodId);
        persistFavorites();
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
        authRepository.signOut();
        loggedIn = false;
        activeTicket = null;
        activeQueueId = null;
        currentUid = null;
        RoleManager.getInstance().clear();
    }

    private boolean canUseBackend() {
        return backendReady && loggedIn && currentUid != null && authRepository.isSignedIn();
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
        activeQueueId = null;
        remainingPeople = 0;
        estimatedWaitMinutes = 0;
        userQueue = "-";
    }

    private void applyUserProfile(UserProfile profile) {
        if (profile.getDisplayName() != null && !profile.getDisplayName().isEmpty()) {
            userName = profile.getDisplayName();
        }
        if (profile.getEmail() != null && !profile.getEmail().isEmpty()) {
            userEmail = profile.getEmail();
        }
        RoleManager.getInstance().setRole(profile.getRole());
        favoriteIds.clear();
        favoriteIds.addAll(profile.getFavoriteIds());
        activeQueueId = profile.getActiveQueueId();
    }

    private void applyQueueSnapshot(QueueStateSnapshot snapshot) {
        nowServing = snapshot.getNowServing();
        if (snapshot.getActiveQueueId() != null) {
            activeQueueId = snapshot.getActiveQueueId();
        }
        if (snapshot.hasActiveTicket()) {
            activeTicket = snapshot.getActiveTicket();
            userQueue = snapshot.getUserQueue();
            remainingPeople = snapshot.getRemainingPeople();
            estimatedWaitMinutes = snapshot.getEstimatedWaitMinutes();
        } else {
            cancelQueueLocal();
        }
    }

    private void persistFavorites() {
        if (!canUseBackend()) {
            return;
        }
        userRepository.saveFavoriteIds(currentUid, new java.util.ArrayList<>(favoriteIds));
    }
}
