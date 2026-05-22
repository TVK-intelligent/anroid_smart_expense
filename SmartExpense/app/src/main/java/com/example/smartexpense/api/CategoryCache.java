package com.example.smartexpense.api;

import com.example.smartexpense.models.Category;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryCache {
    private static final Map<Integer, Category> categoriesMap = new HashMap<>();

    public static synchronized List<Category> getCategories() {
        return new ArrayList<>(categoriesMap.values());
    }

    public static synchronized void setCategories(List<Category> categories) {
        categoriesMap.clear();
        if (categories != null) {
            for (Category c : categories) {
                if (c.getCategoryId() != null) {
                    categoriesMap.put(c.getCategoryId(), c);
                }
            }
        }
    }

    public static synchronized Category getCategory(Integer id) {
        return categoriesMap.get(id);
    }

    public static synchronized String getCategoryName(Integer id) {
        Category c = categoriesMap.get(id);
        return c != null ? c.getName() : "Khác";
    }

    public static synchronized String getCategoryIcon(Integer id) {
        Category c = categoriesMap.get(id);
        return c != null ? c.getIcon() : "other";
    }

    public static int getIconResource(String iconName) {
        if (iconName == null) return android.R.drawable.ic_menu_today;
        
        switch (iconName.toLowerCase().trim()) {
            case "salary":
            case "lương":
            case "thu nhập":
                return android.R.drawable.ic_input_add;
            case "food":
            case "ăn uống":
                return android.R.drawable.ic_menu_compass;
            case "transport":
            case "di chuyển":
                return android.R.drawable.ic_menu_directions;
            case "bill":
            case "hóa đơn":
                return android.R.drawable.ic_menu_myplaces;
            case "shopping":
            case "mua sắm":
                return android.R.drawable.ic_menu_gallery;
            case "leisure":
            case "giải trí":
                return android.R.drawable.ic_menu_slideshow;
            default:
                return android.R.drawable.ic_menu_today;
        }
    }
}
