package pl.fejzu.menu.bukkit.builder;

import org.bukkit.entity.Player;
import pl.fejzu.menu.bukkit.builder.context.MenuContext;
import pl.fejzu.menu.interfaces.template.ItemTemplate;

@FunctionalInterface
public interface PlaceholderProvider {

    Object provide(Player player, MenuContext context, ItemTemplate itemTemplate, Character symbol);
}
