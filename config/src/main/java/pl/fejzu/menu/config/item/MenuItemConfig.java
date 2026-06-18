package pl.fejzu.menu.config.item;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;
import pl.fejzu.menu.interfaces.template.ItemTemplate;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemConfig implements ItemTemplate {

    private ItemStack item;
    private Map<String, Object> actions = new HashMap<>();

    public MenuItemConfig(ItemStack item) {
        this.item = item;
        this.actions = new HashMap<>();
    }

    @Override
    public Object getItem() {
        return item;
    }

    @Override
    public Map<String, Object> getActions() {
        return actions;
    }
}