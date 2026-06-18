package pl.fejzu.menu.bukkit.impl;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import pl.fejzu.menu.interfaces.button.Button;
import pl.fejzu.menu.interfaces.iterator.SlotIterator;
import pl.fejzu.menu.interfaces.pagination.Pagination;


import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class BukkitPagination implements Pagination<ItemStack, InventoryClickEvent> {

    private int currentPage;
    private int itemsPerPage = 5;
    private List<Button<ItemStack, InventoryClickEvent>> items = new ArrayList<>();

    @Override
    public int getCurrentPage() {
        return currentPage;
    }

    @Override
    public Pagination<ItemStack, InventoryClickEvent> setPage(int page) {
        this.currentPage = Math.max(0, Math.min(page, getTotalPages() - 1));
        return this;
    }

    @Override
    public int getTotalPages() {
        return Math.max(1, (int) Math.ceil((double) items.size() / itemsPerPage));
    }

    @Override
    public boolean isFirst() {
        return currentPage == 0;
    }

    @Override
    public boolean isLast() {
        return currentPage >= getTotalPages() - 1;
    }

    @Override
    public Pagination<ItemStack, InventoryClickEvent> first() {
        this.currentPage = 0;
        return this;
    }

    @Override
    public Pagination<ItemStack, InventoryClickEvent> previous() {
        if (!isFirst()) {
            currentPage--;
        }
        return this;
    }

    @Override
    public Pagination<ItemStack, InventoryClickEvent> next() {
        if (!isLast()) {
            currentPage++;
        }
        return this;
    }

    @Override
    public Pagination<ItemStack, InventoryClickEvent> last() {
        this.currentPage = getTotalPages() - 1;
        return this;
    }

    @Override
    public List<Button<ItemStack, InventoryClickEvent>> getPageItems() {
        int start = currentPage * itemsPerPage;
        int end = Math.min(start + itemsPerPage, items.size());

        if (start >= items.size()) {
            return new ArrayList<>();
        }

        return new ArrayList<>(items.subList(start, end));
    }

    @Override
    public Pagination<ItemStack, InventoryClickEvent> setItems(List<Button<ItemStack, InventoryClickEvent>> items) {
        this.items = new ArrayList<>(items);
        return this;
    }

    @Override
    public Pagination<ItemStack, InventoryClickEvent> setItemsPerPage(int count) {
        this.itemsPerPage = Math.max(1, count);
        return this;
    }

    @Override
    public int getItemsPerPage() {
        return itemsPerPage;
    }

    @Override
    public Pagination<ItemStack, InventoryClickEvent> addToIterator(SlotIterator<ItemStack, InventoryClickEvent> iterator) {
        for (Button<ItemStack, InventoryClickEvent> item : getPageItems()) {
            if (iterator.hasEnded()) {
                break;
            }
            iterator.next().set(item);
        }
        return this;
    }
}