package pl.fejzu.menu.bukkit.button;



import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import pl.fejzu.menu.interfaces.button.Button;

import java.util.Objects;
import java.util.function.Consumer;

public class BukkitButton implements Button<ItemStack, InventoryClickEvent> {

    private ItemStack icon;
    private Consumer<InventoryClickEvent> clickHandler;

    private BukkitButton(ItemStack icon) {
        this.icon = validateIcon(icon);
    }

    public static BukkitButton of(ItemStack icon) {
        return new BukkitButton(icon);
    }

    public static BukkitButton of(Material material) {
        return new BukkitButton(new ItemStack(material));
    }

    public static BukkitButton empty() {
        return new BukkitButton(new ItemStack(Material.AIR));
    }

    @Override
    public ItemStack getIcon() {
        return icon;
    }

    @Override
    public void setIcon(ItemStack icon) {
        this.icon = validateIcon(icon);
    }

    @Override
    public Consumer<InventoryClickEvent> getClickHandler() {
        return clickHandler;
    }

    @Override
    public BukkitButton onClick(Consumer<InventoryClickEvent> handler) {
        this.clickHandler = handler;
        return this;
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        if (clickHandler != null) {
            clickHandler.accept(event);
        }
    }

    private ItemStack validateIcon(ItemStack icon) {
        return Objects.requireNonNull(icon, "Icon cannot be null");
    }
}