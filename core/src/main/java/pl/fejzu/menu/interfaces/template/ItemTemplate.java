package pl.fejzu.menu.interfaces.template;

import java.util.Map;
import java.util.Set;

public interface ItemTemplate {

    Object getItem();

    Map<String, Object> getActions();

    default Set<String> getActionKeys() {
        return getActions().keySet();
    }

    default Object getAction(String key) {
        return getActions().get(key);
    }

    default <T> T getAction(String key, Class<T> type) {
        Object value = getActions().get(key);
        if (value == null) return null;
        return type.cast(value);
    }

    @SuppressWarnings("unchecked")
    default <T> T getAction(String key, T defaultValue) {
        Object value = getActions().get(key);
        return value != null ? (T) value : defaultValue;
    }

    default String getString(String key) {
        return getAction(key, String.class);
    }

    default boolean getBool(String key) {
        return getAction(key, false);
    }

    default int getInt(String key) {
        return getAction(key, 0);
    }
}