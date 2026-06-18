package pl.fejzu.menu.bukkit.action;


import pl.fejzu.menu.interfaces.template.ItemTemplate;

import java.util.Set;

public interface ActionDataProvider extends ItemTemplate {

    @Override
    default Set<String> getActionKeys() {
        return getActions().keySet();
    }

    default Object getActionData(String key) {
        return getAction(key);
    }

    default <T> T getActionData(String key, Class<T> type) {
        return getAction(key, type);
    }

    @SuppressWarnings("unchecked")
    default <T> T getActionData(String key, T defaultValue) {
        return getAction(key, defaultValue);
    }
}