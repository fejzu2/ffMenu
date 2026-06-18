package pl.fejzu.menu.bukkit.impl;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import pl.fejzu.menu.interfaces.InventoryContents;
import pl.fejzu.menu.interfaces.button.Button;
import pl.fejzu.menu.interfaces.iterator.SlotIterator;
import pl.fejzu.menu.interfaces.pagination.Pagination;
import pl.fejzu.menu.slot.SlotPosition;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Getter
public class BukkitInventoryContents implements InventoryContents<ItemStack, InventoryClickEvent> {

    private final int rows;
    private final int columns;
    private final UUID playerUuid;
    private final Button<ItemStack, InventoryClickEvent>[][] contents;
    private final Pagination<ItemStack, InventoryClickEvent> pagination;
    private final Map<String, SlotIterator<ItemStack, InventoryClickEvent>> iterators;
    private final Map<String, Object> properties;
    private final java.util.Set<SlotPosition> dirtySlots = new java.util.HashSet<>();
    private Player cachedPlayer;
    private Inventory menuInventory;

    @SuppressWarnings("unchecked")
    public BukkitInventoryContents(int rows, int columns, UUID playerUuid) {
        this.rows = rows;
        this.columns = columns;
        this.playerUuid = playerUuid;
        this.contents = new Button[rows][columns];
        this.pagination = new BukkitPagination();
        this.iterators = new HashMap<>();
        this.properties = new HashMap<>();
    }

    @Override
    public Pagination<ItemStack, InventoryClickEvent> pagination() {
        return pagination;
    }

    @Override
    public Optional<SlotIterator<ItemStack, InventoryClickEvent>> getIterator(String id) {
        return Optional.ofNullable(iterators.get(id));
    }

    @Override
    public SlotIterator<ItemStack, InventoryClickEvent> createIterator(String id, SlotPosition startPosition) {
        BukkitSlotIterator iterator = new BukkitSlotIterator(rows, columns, startPosition);
        iterator.setContents(contents);
        iterators.put(id, iterator);
        return iterator;
    }

    @Override
    public Optional<Button<ItemStack, InventoryClickEvent>> get(SlotPosition position) {
        if (!isValidPosition(position)) {
            return Optional.empty();
        }
        return Optional.ofNullable(contents[position.row()][position.column()]);
    }

    @Override
    public Button<ItemStack, InventoryClickEvent>[][] all() {
        return contents;
    }

    @Override
    public Optional<SlotPosition> firstEmpty() {
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                if (contents[row][column] == null) {
                    return Optional.of(SlotPosition.of(row, column));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public InventoryContents<ItemStack, InventoryClickEvent> set(SlotPosition position, Button<ItemStack, InventoryClickEvent> button) {
        if (!isValidPosition(position)) {
            return this;
        }

        contents[position.row()][position.column()] = button;
        dirtySlots.add(position);
        updateSlot(position, button != null ? button.getIcon() : null);
        return this;
    }

    public void flushDirtySlots(Inventory inventory) {
        if (dirtySlots.isEmpty()) {
            return;
        }

        for (SlotPosition pos : dirtySlots) {
            Button<ItemStack, InventoryClickEvent> button = contents[pos.row()][pos.column()];
            int slot = pos.toSlot(columns);
            if (slot < inventory.getSize()) {
                inventory.setItem(slot, button != null ? button.getIcon() : null);
            }
        }
        dirtySlots.clear();
    }

    public void markAllDirty() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                dirtySlots.add(SlotPosition.of(row, col));
            }
        }
    }

    public Player getCachedPlayer() {
        if (playerUuid == null) {
            return null;
        }
        if (cachedPlayer == null || !cachedPlayer.isOnline()) {
            cachedPlayer = Bukkit.getPlayer(playerUuid);
        }
        return cachedPlayer;
    }

    public void invalidatePlayerCache() {
        cachedPlayer = null;
        menuInventory = null;
    }

    public void setMenuInventory(Inventory inventory) {
        this.menuInventory = inventory;
    }

    @Override
    public InventoryContents<ItemStack, InventoryClickEvent> add(Button<ItemStack, InventoryClickEvent> button) {
        firstEmpty().ifPresent(pos -> set(pos, button));
        return this;
    }

    @Override
    public InventoryContents<ItemStack, InventoryClickEvent> fill(Button<ItemStack, InventoryClickEvent> button) {
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                set(SlotPosition.of(row, column), button);
            }
        }
        return this;
    }

    @Override
    public InventoryContents<ItemStack, InventoryClickEvent> fillRow(int row, Button<ItemStack, InventoryClickEvent> button) {
        if (row < 0 || row >= rows) {
            return this;
        }

        for (int column = 0; column < columns; column++) {
            set(SlotPosition.of(row, column), button);
        }
        return this;
    }

    @Override
    public InventoryContents<ItemStack, InventoryClickEvent> fillColumn(int column, Button<ItemStack, InventoryClickEvent> button) {
        if (column < 0 || column >= columns) {
            return this;
        }

        for (int row = 0; row < rows; row++) {
            set(SlotPosition.of(row, column), button);
        }
        return this;
    }

    @Override
    public InventoryContents<ItemStack, InventoryClickEvent> fillBorders(Button<ItemStack, InventoryClickEvent> button) {
        fillRow(0, button);
        fillRow(rows - 1, button);
        fillColumn(0, button);
        fillColumn(columns - 1, button);
        return this;
    }

    @Override
    public InventoryContents<ItemStack, InventoryClickEvent> fillRect(SlotPosition from, SlotPosition to, Button<ItemStack, InventoryClickEvent> button) {
        int minRow = Math.min(from.row(), to.row());
        int maxRow = Math.max(from.row(), to.row());
        int minCol = Math.min(from.column(), to.column());
        int maxCol = Math.max(from.column(), to.column());

        for (int row = minRow; row <= maxRow; row++) {
            for (int column = minCol; column <= maxCol; column++) {
                if (row == minRow || row == maxRow || column == minCol || column == maxCol) {
                    set(SlotPosition.of(row, column), button);
                }
            }
        }
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> Optional<V> getProperty(String key) {
        return Optional.ofNullable((V) properties.get(key));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V getProperty(String key, V defaultValue) {
        return (V) properties.getOrDefault(key, defaultValue);
    }

    @Override
    public InventoryContents<ItemStack, InventoryClickEvent> setProperty(String key, Object value) {
        properties.put(key, value);
        return this;
    }

    private boolean isValidPosition(SlotPosition position) {
        return position.row() >= 0 && position.row() < rows
                && position.column() >= 0 && position.column() < columns;
    }

    private void updateSlot(SlotPosition position, ItemStack item) {
        Player player = getCachedPlayer();
        if (player == null) {
            return;
        }
        if (menuInventory == null) {
            return;
        }

        Inventory topInventory = player.getOpenInventory().getTopInventory();

        if (!topInventory.equals(menuInventory)) {
            return;
        }

        int expectedSize = rows * columns;
        if (topInventory.getSize() != expectedSize) {
            return;
        }

        int slot = position.toSlot(columns);
        if (slot < topInventory.getSize()) {
            topInventory.setItem(slot, item);
        }
    }

}