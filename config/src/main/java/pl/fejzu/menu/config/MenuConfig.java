package pl.fejzu.menu.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.event.inventory.InventoryType;
import pl.fejzu.menu.config.item.MenuItemConfig;
import pl.fejzu.menu.interfaces.template.ItemTemplate;
import pl.fejzu.menu.interfaces.template.MenuTemplate;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuConfig implements MenuTemplate {

    private String title = "";
    private List<String> pattern = new ArrayList<>();
    private Map<Character, MenuItemConfig> items = new HashMap<>();
    private InventoryType inventoryType = InventoryType.CHEST;
    private int updateInterval = -1;

    public MenuConfig(String title, List<String> pattern, Map<Character, MenuItemConfig> items) {
        this.title = title;
        this.pattern = pattern;
        this.items = items;
    }

    public MenuConfig(String title,
                      List<String> pattern,
                      Map<Character, MenuItemConfig> items,
                      InventoryType inventoryType) {
        this.title = title;
        this.pattern = pattern;
        this.items = items;
        this.inventoryType = inventoryType;
    }


    @Override
    @SuppressWarnings("unchecked")
    public Map<Character, ItemTemplate> getItems() {
        return (Map<Character, ItemTemplate>) (Map<?, ?>) items;
    }

    public Map<Character, MenuItemConfig> getItemConfigs() {
        return items;
    }
}