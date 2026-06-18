package pl.fejzu.menu.interfaces.pagination;



import pl.fejzu.menu.interfaces.button.Button;
import pl.fejzu.menu.interfaces.iterator.SlotIterator;

import java.util.List;

public interface Pagination<T, E> {

    int getCurrentPage();

    Pagination<T, E> setPage(int page);

    int getTotalPages();

    boolean isFirst();

    boolean isLast();

    Pagination<T, E> first();

    Pagination<T, E> previous();

    Pagination<T, E> next();

    Pagination<T, E> last();

    List<Button<T, E>> getPageItems();

    Pagination<T, E> setItems(List<Button<T, E>> items);

    Pagination<T, E> setItemsPerPage(int count);

    int getItemsPerPage();

    Pagination<T, E> addToIterator(SlotIterator<T, E> iterator);
}