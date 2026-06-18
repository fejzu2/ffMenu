package pl.fejzu.menu.utils;

import lombok.experimental.UtilityClass;
import pl.fejzu.menu.slot.SlotPosition;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@UtilityClass
public final class PatternsUtil {

    private static final int MAX_COLUMNS = 9;
    private static final int MAX_ROWS = 6;

    public static List<SlotPosition> getPositionsFromSymbol(List<String> pattern, char symbol) {
        return findBySymbol(pattern, symbol, pos -> pos);
    }

    public static List<Integer> getSlotsFromSymbol(List<String> pattern, char symbol) {
        return findBySymbol(pattern, symbol, PatternsUtil::getSlotFromPosition);
    }

    public static List<SlotPosition> findAllPositionsByChar(List<String> pattern, char symbol) {
        return getPositionsFromSymbol(pattern, symbol);
    }

    public static List<Integer> findAllSlotsByChar(List<String> pattern, char symbol) {
        return getSlotsFromSymbol(pattern, symbol);
    }

    private static <T> List<T> findBySymbol(List<String> pattern, char symbol, Function<SlotPosition, T> mapper) {
        List<T> results = new ArrayList<>();

        for (int row = 0; row < pattern.size(); row++) {
            String[] chars = pattern.get(row).split(" ");

            for (int column = 0; column < Math.min(chars.length, MAX_COLUMNS); column++) {
                if (!chars[column].isEmpty() && chars[column].charAt(0) == symbol) {
                    results.add(mapper.apply(new SlotPosition(row, column)));
                }
            }
        }

        return results;
    }

    public static SlotPosition getPositionFromSlot(int slot) {
        return new SlotPosition(slot / MAX_COLUMNS, slot % MAX_COLUMNS);
    }

    public static int getRowFromSlot(int slot) {
        return slot / MAX_COLUMNS;
    }

    public static int getColumnFromSlot(int slot) {
        return slot % MAX_COLUMNS;
    }

    public static int getSlotFromPosition(SlotPosition position) {
        return position.row() * MAX_COLUMNS + position.column();
    }

    public static boolean isValidPattern(List<String> pattern) {
        if (pattern == null || pattern.isEmpty() || pattern.size() > MAX_ROWS) {
            return false;
        }

        for (String line : pattern) {
            if (!line.matches("^\\S( \\S)*$")) {
                return false;
            }
            if (line.split(" ").length > MAX_COLUMNS) {
                return false;
            }
        }

        return true;
    }

    public static int[] calculateSize(List<String> pattern) {
        return new int[]{ Math.min(MAX_ROWS, pattern.size()), MAX_COLUMNS };
    }
}