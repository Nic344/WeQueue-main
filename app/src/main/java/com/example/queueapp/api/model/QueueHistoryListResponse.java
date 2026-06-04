package com.example.queueapp.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class QueueHistoryListResponse {

    @SerializedName("history")
    private List<QueueHistoryItem> history;

    public List<QueueHistoryItem> getHistory() {
        return history;
    }

    public static class QueueHistoryItem {

        @SerializedName("id")
        private int id;

        @SerializedName("queue_number")
        private String queueNumber;

        @SerializedName("status")
        private String status;

        @SerializedName("status_label")
        private String statusLabel;

        @SerializedName("food_name")
        private String foodName;

        @SerializedName("food_image")
        private String foodImage;

        @SerializedName("date")
        private String date;

        @SerializedName("created_at")
        private String createdAt;

        public int getId() {
            return id;
        }

        public String getQueueNumber() {
            return queueNumber;
        }

        public String getStatus() {
            return status;
        }

        public String getStatusLabel() {
            return statusLabel;
        }

        public String getFoodName() {
            return foodName;
        }

        public String getFoodImage() {
            return foodImage;
        }

        public String getDate() {
            return date != null ? date : createdAt;
        }
    }
}
