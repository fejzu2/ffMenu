package pl.fejzu.menu.bukkit.action;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import pl.fejzu.menu.bukkit.builder.context.MenuContext;
import pl.fejzu.menu.interfaces.template.ItemTemplate;

@FunctionalInterface
public interface MenuAction {

    void execute(Player player, InventoryClickEvent event, MenuContext context, ItemTemplate data);
}