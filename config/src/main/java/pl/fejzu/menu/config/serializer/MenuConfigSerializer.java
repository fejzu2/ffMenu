package pl.fejzu.menu.config.serializer;

import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;
import org.bukkit.event.inventory.InventoryType;
import pl.fejzu.menu.config.MenuConfig;
import pl.fejzu.menu.config.item.MenuItemConfig;

public class MenuConfigSerializer implements ObjectSerializer<MenuConfig> {

    @Override
    public boolean supports(@NonNull Class<?> type) {
        return MenuConfig.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(@NonNull MenuConfig config, @NonNull SerializationData data,
                          @NonNull GenericsDeclaration generics) {
        data.add("title", config.getTitle());
        data.addCollection("pattern", config.getPattern(), String.class);
        data.addAsMap("items", config.getItemConfigs(), Character.class, MenuItemConfig.class);

        if (config.getInventoryType() != InventoryType.CHEST) {
            data.add("type", config.getInventoryType());
        }
        if (config.getUpdateInterval() > 0) {
            data.add("update-interval", config.getUpdateInterval());
        }
    }

    @Override
    public MenuConfig deserialize(@NonNull DeserializationData data,
                                  @NonNull GenericsDeclaration generics) {
        MenuConfig config = new MenuConfig();

        config.setTitle(data.get("title", String.class));
        config.setPattern(data.getAsList("pattern", String.class));
        config.setItems(data.getAsMap("items", Character.class, MenuItemConfig.class));

        if (data.containsKey("type")) {
            config.setInventoryType(data.get("type", InventoryType.class));
        }
        if (data.containsKey("update-interval")) {
            config.setUpdateInterval(data.get("update-interval", Integer.class));
        }

        return config;
    }
}