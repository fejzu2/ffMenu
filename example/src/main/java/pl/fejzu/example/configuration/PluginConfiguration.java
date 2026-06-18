package pl.fejzu.example.configuration;

import eu.okaeri.configs.OkaeriConfig;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import pl.fejzu.menu.config.MenuConfig;
import pl.fejzu.menu.config.item.MenuItemConfig;
import pl.ffcode.platform.bukkit.config.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration(child = "config")
public class PluginConfiguration extends OkaeriConfig
{
    public MenuConfig mainMenu = new MenuConfig(
            "&6Main Menu",
            List.of(
                    "# # # # # # # # #",
                    "# . S . K . W . #",
                    "# # # # C # # # #"
            ),
            Map.of(
                    '#', new MenuItemConfig(createGlassPane()),
                    'S', new MenuItemConfig(
                            createItem(Material.EMERALD, "&aShop"),
                            Map.of("sound", "ui_button_click", "open-menu", "shop")
                    ),
                    'C', new MenuItemConfig(
                            createItem(Material.BARRIER, "&cClose"),
                            Map.of("sound", "ui_button_click", "close", true)
                    )
            )
    );

    public MenuConfig paginationMenu = new MenuConfig(
            "&ePagination Menu (Config)",
            List.of(
                    "# # # # # # # # #",
                    "# X X X X X X X #",
                    "# X X X X X X X #",
                    "# X X X X X X X #",
                    "# # < # I # > # #"
            ),
            Map.of(
                    '#', new MenuItemConfig(createGlassPane()),
                    '<', new MenuItemConfig(
                            createItem(Material.ARROW, "&ePrevious Page"),
                            Map.of("previous-page", true)
                    ),
                    'I', new MenuItemConfig(
                            createItem(Material.PAPER, "&ePage {current_page}/{total_pages}")
                    ),
                    '>', new MenuItemConfig(
                            createItem(Material.ARROW, "&eNext Page"),
                            Map.of("next-page", true)
                    )
            )
    );

    private ItemStack createGlassPane() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name.replace("&", "§"));
        item.setItemMeta(meta);
        return item;
    }
}
