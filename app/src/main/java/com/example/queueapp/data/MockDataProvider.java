package com.example.queueapp.data;

import com.example.queueapp.R;
import com.example.queueapp.model.FoodItem;
import com.example.queueapp.model.QueueHistory;

import java.util.ArrayList;
import java.util.List;

public final class MockDataProvider {

    private MockDataProvider() {
    }

    public static List<FoodItem> getAllFoods() {
        List<FoodItem> foods = new ArrayList<>();
        foods.add(new FoodItem(1, "Caramel Macchiato", "Rich espresso with caramel drizzle", 45000,
                "Beverage", "Medium", "None", R.color.food_placeholder_1, R.drawable.ic_food_coffee,
                "caramel_macchiato.jpeg"));
        foods.add(new FoodItem(2, "Avocado Toast", "Sourdough with smashed avocado & egg", 52000,
                "Breakfast", "Medium", "Mild", R.color.food_placeholder_2, R.drawable.ic_food_coffee,
                "avocado_toast.jpeg"));
        foods.add(new FoodItem(3, "Spicy Ramen Bowl", "Tonkotsu broth with chili oil", 68000,
                "Main", "High", "Hot", R.color.food_placeholder_3, R.drawable.ic_food_coffee,
                "spicy_ramen.jpeg"));
        foods.add(new FoodItem(4, "Matcha Latte", "Premium ceremonial grade matcha", 42000,
                "Beverage", "Low", "None", R.color.food_placeholder_4, R.drawable.ic_food_coffee,
                "matcha_latte.jpeg"));
        foods.add(new FoodItem(5, "Chicken Teriyaki", "Grilled chicken with teriyaki glaze", 75000,
                "Main", "High", "Mild", R.color.food_placeholder_5, R.drawable.ic_food_coffee,
                "chicken_teriyaki.jpeg"));
        foods.add(new FoodItem(6, "Blueberry Pancakes", "Fluffy stack with maple syrup", 48000,
                "Breakfast", "Medium", "None", R.color.food_placeholder_1, R.drawable.ic_food_coffee,
                "blueberry_pancakes.jpeg"));
        foods.add(new FoodItem(7, "Americano", "Double shot over ice", 35000,
                "Beverage", "Low", "None", R.color.food_placeholder_2, R.drawable.ic_food_coffee,
                "americano.jpeg"));
        foods.add(new FoodItem(8, "Beef Burger Deluxe", "Angus patty with special sauce", 89000,
                "Main", "High", "Mild", R.color.food_placeholder_3, R.drawable.ic_food_coffee, null));
        foods.add(new FoodItem(9, "Mango Smoothie Bowl", "Fresh mango with granola topping", 55000,
                "Dessert", "Medium", "None", R.color.food_placeholder_4, R.drawable.ic_food_coffee, null));
        foods.add(new FoodItem(10, "Truffle Fries", "Hand-cut fries with truffle oil", 38000,
                "Snack", "Low", "None", R.color.food_placeholder_5, R.drawable.ic_food_coffee, null));
        foods.add(new FoodItem(11, "Thai Green Curry", "Coconut curry with jasmine rice", 72000,
                "Main", "High", "Hot", R.color.food_placeholder_1, R.drawable.ic_food_coffee, null));
        foods.add(new FoodItem(12, "Chocolate Croissant", "Buttery layers with dark chocolate", 32000,
                "Dessert", "Low", "None", R.color.food_placeholder_2, R.drawable.ic_food_coffee, null));
        return foods;
    }

    public static List<QueueHistory> getQueueHistory() {
        List<QueueHistory> history = new ArrayList<>();
        history.add(new QueueHistory("May 28, 2026", "A018", "Caramel Macchiato", QueueHistory.STATUS_COMPLETED));
        history.add(new QueueHistory("May 27, 2026", "A014", "Spicy Ramen Bowl", QueueHistory.STATUS_COMPLETED));
        history.add(new QueueHistory("May 26, 2026", "A011", "Avocado Toast", QueueHistory.STATUS_CANCELLED));
        history.add(new QueueHistory("May 25, 2026", "A009", "Chicken Teriyaki", QueueHistory.STATUS_COMPLETED));
        history.add(new QueueHistory("May 24, 2026", "A007", "Matcha Latte", QueueHistory.STATUS_SERVING));
        history.add(new QueueHistory("May 23, 2026", "A005", "Americano", QueueHistory.STATUS_COMPLETED));
        history.add(new QueueHistory("May 21, 2026", "A003", "Blueberry Pancakes", QueueHistory.STATUS_COMPLETED));
        return history;
    }
}
