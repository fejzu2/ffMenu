package pl.fejzu.menu.config.serializer.serdes;

import eu.okaeri.configs.serdes.OkaeriSerdesPack;
import eu.okaeri.configs.serdes.SerdesRegistry;
import lombok.NonNull;
import pl.fejzu.menu.config.serializer.MenuConfigSerializer;
import pl.fejzu.menu.config.serializer.MenuItemConfigSerializer;

public class MenuSerdesPack implements OkaeriSerdesPack {

    @Override
    public void register(@NonNull SerdesRegistry registry) {
        registry.register(new MenuConfigSerializer());
        registry.register(new MenuItemConfigSerializer());
    }
}