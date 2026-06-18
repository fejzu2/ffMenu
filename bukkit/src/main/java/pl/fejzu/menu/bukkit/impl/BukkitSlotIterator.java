package pl.fejzu.menu.bukkit.impl;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import pl.fejzu.menu.interfaces.button.Button;
import pl.fejzu.menu.interfaces.iterator.SlotIterator;
import pl.fejzu.menu.slot.SlotPosition;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Getter
@Setter
public class BukkitSlotIterator implements SlotIterator<ItemStack, InventoryClickEvent> {

    private final int rows;
    private final int columns;
    private final Set<SlotPosition> blacklisted = new HashSet<>();

    private int row;
    private int column;
    private boolean allowOverride;
    private boolean started;
    private boolean ended;

    private Button<ItemStack, InventoryClickEvent>[][] contents;

    @SuppressWarnings("unchecked")
    public BukkitSlotIterator(int rows, int columns, int startRow, int startColumn) {
        this.rows = rows;
        this.columns = columns;
        this.row = startRow;
        this.column = startColumn;
        this.contents = new Button[rows][columns];
    }

    public BukkitSlotIterator(int rows, int columns, SlotPosition startPosition) {
        this(rows, columns, startPosition.row(), startPosition.column());
    }

    @Override
    public Optional<Button<ItemStack, InventoryClickEvent>> get() {
        return Optional.ofNullable(contents[row][column]);
    }

    @Override
    public SlotIterator<ItemStack, InventoryClickEvent> set(Button<ItemStack, InventoryClickEvent> button) {
        if (allowOverride || contents[row][column] == null) {
            contents[row][column] = button;
        }
        return this;
    }

    @Override
    public SlotIterator<ItemStack, InventoryClickEvent> next() {
        if (!started) {
            started = true;
            if (!isBlacklisted(row, column)) {
                return this;
            }
        }

        do {
            if (column < columns - 1) {
                column++;
            } else if (row < rows - 1) {
                row++;
                column = 0;
            } else {
                ended = true;
                return this;
            }
        } while (isBlacklisted(row, column));

        return this;
    }

    @Override
    public SlotIterator<ItemStack, InventoryClickEvent> previous() {
        do {
            if (column > 0) {
                column--;
            } else if (row > 0) {
                row--;
                column = columns - 1;
            } else {
                return this;
            }
        } while (isBlacklisted(row, column));

        return this;
    }

    @Override
    public SlotIterator<ItemStack, InventoryClickEvent> blacklist(SlotPosition position) {
        blacklisted.add(position);
        return this;
    }

    @Override
    public SlotPosition getPosition() {
        return SlotPosition.of(row, column);
    }

    @Override
    public SlotIterator<ItemStack, InventoryClickEvent> setPosition(SlotPosition position) {
        this.row = position.row();
        this.column = position.column();
        return this;
    }

    @Override
    public boolean hasStarted() {
        return started;
    }

    @Override
    public boolean hasEnded() {
        return ended;
    }

    @Override
    public boolean allowsOverride() {
        return allowOverride;
    }

    @Override
    public SlotIterator<ItemStack, InventoryClickEvent> setAllowOverride(boolean allow) {
        this.allowOverride = allow;
        return this;
    }

    private boolean isBlacklisted(int row, int column) {
        return blacklisted.contains(SlotPosition.of(row, column));
    }
}