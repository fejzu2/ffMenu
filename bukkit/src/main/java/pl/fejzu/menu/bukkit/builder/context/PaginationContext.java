package pl.fejzu.menu.bukkit.builder.context;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import pl.fejzu.menu.bukkit.MenuAPI;
import pl.fejzu.menu.bukkit.button.BukkitButton;

import pl.fejzu.menu.interfaces.button.Button;
import pl.fejzu.menu.interfaces.pagination.Pagination;
import pl.fejzu.menu.slot.SlotPosition;
import pl.fejzu.menu.utils.PatternsUtil;

import java.util.List;

public class PaginationContext {

    private final Pagination<ItemStack, InventoryClickEvent> pagination;
    private final MenuContext menuContext;
    private List<SlotPosition> displayPositions;

    public PaginationContext(Pagination<ItemStack, InventoryClickEvent> pagination, MenuContext menuContext) {
        this.pagination = pagination;
        this.menuContext = menuContext;
    }

    public PaginationContext items(List<BukkitButton> items) {
        pagination.setItems(items.stream()
                .map(b -> (Button<ItemStack, InventoryClickEvent>) b)
                .toList());
        return this;
    }

    public PaginationContext itemsPerPage(int count) {
        pagination.setItemsPerPage(count);
        return this;
    }

    public PaginationContext displayAt(char symbol) {
        this.displayPositions = PatternsUtil.getPositionsFromSymbol(
                menuContext.getBuilder().getPattern(), symbol
        );

        refreshDisplay(displayPositions);
        return this;
    }

    private void refreshDisplay(List<SlotPosition> positions) {
        List<Button<ItemStack, InventoryClickEvent>> pageItems = pagination.getPageItems();
        for (int i = 0; i < positions.size(); i++) {
            Button<ItemStack, InventoryClickEvent> button = i < pageItems.size() ? pageItems.get(i) : null;
            menuContext.getContents().set(positions.get(i), button);
        }
    }

    public PaginationContext nextButton(char symbol, ItemStack icon, Runnable onPageChange) {
        menuContext.set(symbol, BukkitButton.of(icon).onClick(e -> {
            e.setCancelled(true);
            if (!pagination.isLast()) {
                pagination.next();
                refreshDisplayFromStorage();
                if (onPageChange != null) {
                    Bukkit.getScheduler().runTask(MenuAPI.getService().getPlugin(), onPageChange);
                }
            }
        }));
        return this;
    }

    public PaginationContext prevButton(char symbol, ItemStack icon, Runnable onPageChange) {
        menuContext.set(symbol, BukkitButton.of(icon).onClick(e -> {
            e.setCancelled(true);
            if (!pagination.isFirst()) {
                pagination.previous();
                refreshDisplayFromStorage();
                if (onPageChange != null) {
                    Bukkit.getScheduler().runTask(MenuAPI.getService().getPlugin(), onPageChange);
                }
            }
        }));
        return this;
    }

    public int getCurrentPage() {
        return pagination.getCurrentPage();
    }

    public int getTotalPages() {
        return pagination.getTotalPages();
    }

    public boolean isFirst() {
        return pagination.isFirst();
    }

    public boolean isLast() {
        return pagination.isLast();
    }

    public void next() {
        if (!pagination.isLast()) {
            pagination.next();
            refreshDisplayFromStorage();
        }
    }

    public void previous() {
        if (!pagination.isFirst()) {
            pagination.previous();
            refreshDisplayFromStorage();
        }
    }

    public PaginationContext goToPage(int page) {
        pagination.setPage(page);
        refreshDisplayFromStorage();
        return this;
    }

    private void refreshDisplayFromStorage() {
        if (displayPositions != null) {
            refreshDisplay(displayPositions);
        }
        menuContext.refreshItemTemplates();
    }
}