package pl.fejzu.menu.interfaces;


import pl.fejzu.menu.interfaces.button.Button;
import pl.fejzu.menu.interfaces.iterator.SlotIterator;
import pl.fejzu.menu.interfaces.pagination.Pagination;
import pl.fejzu.menu.slot.SlotPosition;

import java.util.Optional;

public interface InventoryContents<T, E> {

    Pagination<T, E> pagination();

    Optional<SlotIterator<T, E>> getIterator(String id);

    SlotIterator<T, E> createIterator(String id, SlotPosition startPosition);

    default SlotIterator<T, E> createIterator(String id, int row, int column) {
        return createIterator(id, SlotPosition.of(row, column));
    }

    Optional<Button<T, E>> get(SlotPosition position);

    default Optional<Button<T, E>> get(int row, int column) {
        return get(SlotPosition.of(row, column));
    }

    Button<T, E>[][] all();

    Optional<SlotPosition> firstEmpty();

    InventoryContents<T, E> set(SlotPosition position, Button<T, E> button);

    default InventoryContents<T, E> set(int row, int column, Button<T, E> button) {
        return set(SlotPosition.of(row, column), button);
    }

    InventoryContents<T, E> add(Button<T, E> button);

    InventoryContents<T, E> fill(Button<T, E> button);

    InventoryContents<T, E> fillRow(int row, Button<T, E> button);

    InventoryContents<T, E> fillColumn(int column, Button<T, E> button);

    InventoryContents<T, E> fillBorders(Button<T, E> button);

    InventoryContents<T, E> fillRect(SlotPosition from, SlotPosition to, Button<T, E> button);

    default InventoryContents<T, E> fillRect(int fromRow, int fromCol, int toRow, int toCol, Button<T, E> button) {
        return fillRect(SlotPosition.of(fromRow, fromCol), SlotPosition.of(toRow, toCol), button);
    }

    <V> Optional<V> getProperty(String key);

    <V> V getProperty(String key, V defaultValue);

    InventoryContents<T, E> setProperty(String key, Object value);
}