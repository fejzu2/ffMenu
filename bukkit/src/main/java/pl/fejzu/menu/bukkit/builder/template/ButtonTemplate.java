package pl.fejzu.menu.bukkit.builder.template;

import lombok.Getter;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

@Getter
public class ButtonTemplate {

    private final ItemStack icon;
    private final Consumer<InventoryClickEvent> onClick;

    public ButtonTemplate(ItemStack icon) {
        this(icon, null);
    }

    public ButtonTemplate(ItemStack icon, Consumer<InventoryClickEvent> onClick) {
        this.icon = icon;
        this.onClick = onClick;
    }
}