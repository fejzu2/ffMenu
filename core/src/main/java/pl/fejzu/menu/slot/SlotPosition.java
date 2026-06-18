package pl.fejzu.menu.slot;

public record SlotPosition(int row, int column) {

    public static SlotPosition of(int row, int column) {
        return new SlotPosition(row, column);
    }

    public static SlotPosition fromSlot(int slot, int columns) {
        return new SlotPosition(slot / columns, slot % columns);
    }

    public int toSlot(int columns) {
        return row * columns + column;
    }

    public int toSlot() {
        return toSlot(9);
    }
}