package com.example.queueapp.backend;

public final class FirestorePaths {

    public static final String COLLECTION_QUEUE_GLOBAL = "queue_global";
    public static final String DOC_QUEUE_STATE = "state";

    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_QUEUES = "queues";
    public static final String SUBCOLLECTION_HISTORY = "history";

    public static final String FIELD_DISPLAY_NAME = "displayName";
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_ROLE = "role";
    public static final String FIELD_CUSTOMER_NAME = "customerName";
    public static final String FIELD_IS_VIP = "isVip";
    public static final String FIELD_IS_PRIORITY = "isPriority";
    public static final String FIELD_FAVORITE_IDS = "favoriteIds";
    public static final String FIELD_ACTIVE_QUEUE_ID = "activeQueueId";

    public static final String FIELD_NOW_SERVING = "nowServing";
    public static final String FIELD_NEXT_COUNTER = "nextCounter";

    public static final String FIELD_USER_ID = "userId";
    public static final String FIELD_QUEUE_NUMBER = "queueNumber";
    public static final String FIELD_POSITION = "position";
    public static final String FIELD_ESTIMATED_MINUTES = "estimatedMinutes";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_FOOD_NAME = "foodName";
    public static final String FIELD_CREATED_AT = "createdAt";
    public static final String FIELD_DATE_LABEL = "dateLabel";

    public static final String STATUS_WAITING = "waiting";
    public static final String STATUS_SERVING = "serving";
    public static final String STATUS_CALLED = "called";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_CANCELLED = "cancelled";
    public static final String STATUS_SKIPPED = "skipped";

    private FirestorePaths() {
    }
}
