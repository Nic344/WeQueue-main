package com.example.queueapp.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Outline;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.queueapp.R;
import com.example.queueapp.model.FoodItem;

import java.io.IOException;
import java.io.InputStream;

public final class FoodImageHelper {

    private static final String ASSETS_DIR = "images/";

    private FoodImageHelper() {
    }

    public static void loadFoodImage(@NonNull Context context, @NonNull ImageView imageView,
                                     @Nullable View imageContainer, @NonNull FoodItem item) {
        if (item.getImageAssetName() != null) {
            Bitmap bitmap = loadBitmapFromAssets(context, item.getImageAssetName());
            if (bitmap != null) {
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setImageBitmap(bitmap);
                if (imageContainer != null) {
                    imageContainer.setBackgroundResource(android.R.color.transparent);
                }
                return;
            }
        }
        applyPlaceholder(context, imageView, imageContainer, item);
    }

    public static void loadFoodThumbnail(@NonNull Context context, @NonNull ImageView imageView,
                                         @NonNull FoodItem item) {
        loadFoodImage(context, imageView, null, item);
    }

    public static void loadHistoryThumbnail(@NonNull Context context, @NonNull ImageView imageView,
                                            @NonNull String foodName) {
        String asset = assetNameForFood(foodName);
        if (asset != null) {
            Bitmap bitmap = loadBitmapFromAssets(context, asset);
            if (bitmap != null) {
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setImageBitmap(bitmap);
                imageView.setVisibility(View.VISIBLE);
                return;
            }
        }
        imageView.setImageResource(R.drawable.ic_food_coffee);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setVisibility(View.VISIBLE);
    }

    @Nullable
    public static String assetNameForFood(@NonNull String foodName) {
        switch (foodName) {
            case "Caramel Macchiato":
                return "caramel_macchiato.jpeg";
            case "Avocado Toast":
                return "avocado_toast.jpeg";
            case "Spicy Ramen Bowl":
                return "spicy_ramen.jpeg";
            case "Matcha Latte":
                return "matcha_latte.jpeg";
            case "Americano":
            case "Iced Americano":
                return "americano.jpeg";
            case "Blueberry Pancakes":
                return "blueberry_pancakes.jpeg";
            case "Chicken Teriyaki":
                return "chicken_teriyaki.jpeg";
            default:
                return null;
        }
    }

    public static void applyRoundedCorners(@NonNull ImageView imageView, float radiusPx) {
        imageView.setClipToOutline(true);
        imageView.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radiusPx);
            }
        });
    }

    @Nullable
    private static Bitmap loadBitmapFromAssets(@NonNull Context context, @NonNull String fileName) {
        try (InputStream stream = context.getAssets().open(ASSETS_DIR + fileName)) {
            return BitmapFactory.decodeStream(stream);
        } catch (IOException ignored) {
            return null;
        }
    }

    private static void applyPlaceholder(@NonNull Context context, @NonNull ImageView imageView,
                                         @Nullable View imageContainer, @NonNull FoodItem item) {
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setImageResource(item.getIconRes());
        if (imageContainer != null) {
            imageContainer.setBackgroundResource(item.getPlaceholderColorRes());
        }
    }
}
