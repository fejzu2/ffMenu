package pl.fejzu.menu.bukkit.builder.context;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import pl.fejzu.menu.bukkit.builder.MenuBuilder;
import pl.fejzu.menu.bukkit.button.BukkitButton;
import pl.fejzu.menu.bukkit.impl.BukkitInventoryContents;
import pl.fejzu.menu.interfaces.template.ItemTemplate;
import pl.fejzu.menu.slot.SlotPosition;
import pl.fejzu.menu.utils.PatternsUtil;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Getter
public class MenuContext {

    private final BukkitInventoryContents contents;
    private final MenuBuilder builder;
    private final Player player;
    private PaginationContext cachedPaginationContext;

    public MenuContext(BukkitInventoryContents contents, MenuBuilder builder, Player player) {
        this.contents = contents;
        this.builder = builder;
        this.player = player;
    }

    public MenuContext set(char symbol, ItemStack icon) {
        getPositions(symbol).forEach(pos ->
                contents.set(pos, BukkitButton.of(icon))
        );
        return this;
    }

    public MenuContext set(char symbol, ItemStack icon, Consumer<InventoryClickEvent> onClick) {
        getPositions(symbol).forEach(pos ->
                contents.set(pos, BukkitButton.of(icon).onClick(onClick))
        );
        return this;
    }

    public MenuContext set(char symbol, BukkitButton button) {
        getPositions(symbol).forEach(pos -> contents.set(pos, button));
        return this;
    }

    public MenuContext set(SlotPosition position, BukkitButton button) {
        contents.set(position, button);
        return this;
    }

    public MenuContext setAll(char symbol, List<BukkitButton> buttons) {
        List<SlotPosition> positions = getPositions(symbol);
        for (int i = 0; i < Math.min(positions.size(), buttons.size()); i++) {
            contents.set(positions.get(i), buttons.get(i));
        }
        return this;
    }

    public MenuContext fillBorders(ItemStack icon) {
        contents.fillBorders(BukkitButton.of(icon));
        return this;
    }

    public MenuContext fill(ItemStack icon) {
        contents.fill(BukkitButton.of(icon));
        return this;
    }

    public MenuContext setProperty(String key, Object value) {
        contents.setProperty(key, value);
        return this;
    }

    public <T> T getProperty(String key, T defaultValue) {
        return contents.getProperty(key, defaultValue);
    }

    public PaginationContext pagination() {
        if (cachedPaginationContext == null) {
            cachedPaginationContext = new PaginationContext(contents.pagination(), this);
        }
        return cachedPaginationContext;
    }

    public MenuContext refreshItemTemplates() {
        for (Map.Entry<Character, ItemTemplate> entry : builder.getItemTemplates().entrySet()) {
            char symbol = entry.getKey();
            for (SlotPosition pos : getPositions(symbol)) {
                BukkitButton button = builder.createButtonFromTemplate(symbol, this);
                if (button != null) {
                    contents.set(pos, button);
                }
            }
        }
        return this;
    }

    public void close() {
        player.closeInventory();
    }

    private List<SlotPosition> getPositions(char symbol) {
        return PatternsUtil.getPositionsFromSymbol(builder.getPattern(), symbol);
    }
}