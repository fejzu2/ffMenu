package pl.fejzu.menu.interfaces.button;

import java.util.function.Consumer;

public interface Button<T, E> {

    T getIcon();

    void setIcon(T icon);

    Consumer<E> getClickHandler();

    Button<T, E> onClick(Consumer<E> handler);

    void handleClick(E event);

    default boolean hasClickHandler() {
        return getClickHandler() != null;
    }
}