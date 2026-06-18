package pl.fejzu.menu.interfaces.iterator;


import pl.fejzu.menu.interfaces.button.Button;
import pl.fejzu.menu.slot.SlotPosition;

import java.util.Optional;

public interface SlotIterator<T, E> {

    Optional<Button<T, E>> get();

    SlotIterator<T, E> set(Button<T, E> button);

    SlotIterator<T, E> next();

    SlotIterator<T, E> previous();

    SlotIterator<T, E> blacklist(SlotPosition position);

    default SlotIterator<T, E> blacklist(int row, int column) {
        return blacklist(SlotPosition.of(row, column));
    }

    SlotPosition getPosition();

    SlotIterator<T, E> setPosition(SlotPosition position);

    default SlotIterator<T, E> setPosition(int row, int column) {
        return setPosition(SlotPosition.of(row, column));
    }

    boolean hasStarted();

    boolean hasEnded();

    boolean allowsOverride();

    SlotIterator<T, E> setAllowOverride(boolean allow);
}