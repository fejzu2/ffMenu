package pl.fejzu.menu.bukkit;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import pl.fejzu.menu.bukkit.service.MenuService;

public final class MenuAPI {

    @Getter
    private static MenuService service;
    @Getter
    private static boolean initialized = false;

    private MenuAPI() {}

    public static void init(JavaPlugin plugin) {
        if (initialized) {
            throw new IllegalStateException("MenuAPI is already initialized!");
        }
        service = new MenuService(plugin);
        service.init();
        initialized = true;
    }

    public static void disable() {
        if (service != null) {
            service.closeAll();
        }
        service = null;
        initialized = false;
    }

}