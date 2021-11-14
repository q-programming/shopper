package pl.qprogramming.shopper.watch.config;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

@Getter
public enum EventType {
    LOADING_STARTED("pl.qprogramming.shopper.watch.config.loader.started"),
    LOADING_FINISHED("pl.qprogramming.shopper.watch.config.loader.finished"),
    UNKNOWN("pl.qprogramming.shopper.watch.config.n/a");

    private static final Map<String, EventType> BY_CODE = new HashMap<>();

    static {
        for (EventType eType : values()) {
            BY_CODE.put(eType.code, eType);
        }
    }

    private final String code;

    EventType(String code) {
        this.code = code;
    }

    public static EventType getType(String type) {
        return BY_CODE.computeIfAbsent(type, s -> {
            Log.e("EventType", "Unknown type of Event " + type);
            return UNKNOWN;
        });
    }

}
