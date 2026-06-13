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
        return c != null ? localizeCategoryName(c.getName()) : localizeCategoryName("Khác");
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
                return android.R.drawable.ic_input_add;
            case "bonus":
            case "thưởng":
                return android.R.drawable.ic_menu_send;
            case "investment":
            case "đầu tư":
                return android.R.drawable.ic_menu_sort_by_size;
            case "other_income":
            case "thu nhập khác":
            case "thu nhập":
                return android.R.drawable.ic_menu_add;
            case "food":
            case "ăn uống":
                return android.R.drawable.ic_menu_compass;
            case "transport":
            case "di chuyển":
                return android.R.drawable.ic_menu_directions;
            case "home":
            case "nhà cửa":
                return com.example.smartexpense.R.drawable.ic_home;
            case "bill":
            case "hóa đơn":
                return com.example.smartexpense.R.drawable.ic_calendar;
            case "shopping":
            case "mua sắm":
                return android.R.drawable.ic_menu_gallery;
            case "leisure":
            case "giải trí":
                return android.R.drawable.ic_menu_slideshow;
            case "health":
            case "sức khỏe":
                return com.example.smartexpense.R.drawable.ic_favorite;
            default:
                return android.R.drawable.ic_menu_today;
        }
    }

    public static String localizeCategoryName(String name) {
        if (name == null) return "Other";
        String lang = com.example.smartexpense.utils.LocaleHelper.getAppLanguage();
        if ("vi".equals(lang)) {
            switch (name.toLowerCase().trim()) {
                case "salary": return "Lương";
                case "bonus": return "Thưởng";
                case "investment": return "Đầu tư";
                case "other_income": return "Thu nhập khác";
                case "food": return "Ăn uống";
                case "transport": return "Di chuyển";
                case "home": return "Nhà cửa";
                case "bill": return "Hóa đơn";
                case "shopping": return "Mua sắm";
                case "leisure": return "Giải trí";
                case "health": return "Sức khỏe";
                case "other": return "Khác";
                case "khác": return "Khác";
                default: return name;
            }
        } else {
            switch (name.toLowerCase().trim()) {
                case "lương": return "Salary";
                case "thưởng": return "Bonus";
                case "đầu tư": return "Investment";
                case "thu nhập khác": return "Other Income";
                case "ăn uống": return "Food & Beverage";
                case "di chuyển": return "Transportation";
                case "đi lại": return "Transportation";
                case "nhà cửa": return "Housing/Home";
                case "hóa đơn": return "Bills & Utilities";
                case "mua sắm": return "Shopping";
                case "giải trí": return "Leisure & Entertainment";
                case "sức khỏe": return "Health & Fitness";
                case "khác": return "Other";
                case "other": return "Other";
                default: return name;
            }
        }
    }
}
