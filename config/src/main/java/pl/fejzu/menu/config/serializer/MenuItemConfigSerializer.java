package pl.fejzu.menu.config.serializer;

import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;
import org.bukkit.inventory.ItemStack;
import pl.fejzu.menu.config.item.MenuItemConfig;

import java.util.HashMap;
import java.util.Map;

public class MenuItemConfigSerializer implements ObjectSerializer<MenuItemConfig> {


    @Override
    public boolean supports(@NonNull Class<?> type) {
        return MenuItemConfig.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(@NonNull MenuItemConfig itemConfig, @NonNull SerializationData data,
                          @NonNull GenericsDeclaration generics) {
        data.add("item", itemConfig.getItem());

        if (!itemConfig.getActions().isEmpty()) {
            data.addAsMap("actions", itemConfig.getActions(), String.class, Object.class);
        }
    }

    @Override
    public MenuItemConfig deserialize(@NonNull DeserializationData data,
                                      @NonNull GenericsDeclaration generics) {
        ItemStack item = data.get("item", ItemStack.class);

        Map<String, Object> actions;
        if (data.containsKey("actions")) {
            actions = new HashMap<>(data.getAsMap("actions", String.class, Object.class));
        } else {
            actions = new HashMap<>();
        }

        return new MenuItemConfig(item, actions);
    }
}