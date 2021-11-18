package pl.qprogramming.shopper.watch.config;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

@Getter
public enum FontSize {
    SMALL("small"), MEDIUM("medium"), LARGE("large");

    private final String size;

    private static final Map<String, FontSize> BY_CODE = new HashMap<>();

    static {
        for (FontSize fSize : values()) {
            BY_CODE.put(fSize.size, fSize);
        }
    }

    FontSize(String size) {
        this.size = size;
    }

    public static FontSize getType(String type) {
        return BY_CODE.computeIfAbsent(type, s -> {
            Log.e("FontSize", "Unknown type of FontSize " + type);
            return MEDIUM;
        });
    }
}
