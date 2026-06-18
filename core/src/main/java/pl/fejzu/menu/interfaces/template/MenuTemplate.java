package pl.fejzu.menu.interfaces.template;

import java.util.List;
import java.util.Map;

public interface MenuTemplate {

    String getTitle();

    List<String> getPattern();

    Map<Character, ItemTemplate> getItems();

    default boolean isCloseable() {
        return true;
    }

    default boolean isCancelClick() {
        return true;
    }

    default int getUpdateInterval() {
        return -1;
    }

    default Object getInventoryType() {
        return null;
    }
}