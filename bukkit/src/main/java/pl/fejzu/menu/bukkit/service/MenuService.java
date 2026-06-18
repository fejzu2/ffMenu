package pl.fejzu.menu.bukkit.service;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import pl.fejzu.menu.bukkit.menu.BukkitMenu;
import pl.fejzu.menu.bukkit.listener.MenuListener;
import pl.fejzu.menu.interfaces.InventoryContents;


import java.util.*;

@Getter
public class MenuService {

    private final JavaPlugin plugin;
    private final Map<UUID, BukkitMenu> openMenus = new HashMap<>();
    private final Map<UUID, InventoryContents<ItemStack, InventoryClickEvent>> contents = new HashMap<>();

    public MenuService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void init() {
        Bukkit.getPluginManager().registerEvents(new MenuListener(this), plugin);
    }

    public Optional<BukkitMenu> getMenu(Player player) {
        return Optional.ofNullable(openMenus.get(player.getUniqueId()));
    }

    public void setMenu(Player player, BukkitMenu menu) {
        if (menu == null) {
            openMenus.remove(player.getUniqueId());
        } else {
            openMenus.put(player.getUniqueId(), menu);
        }
    }

    public Optional<InventoryContents<ItemStack, InventoryClickEvent>> getContents(Player player) {
        return Optional.ofNullable(contents.get(player.getUniqueId()));
    }

    public void setContents(Player player, InventoryContents<ItemStack, InventoryClickEvent> content) {
        if (content == null) {
            contents.remove(player.getUniqueId());
        } else {
            contents.put(player.getUniqueId(), content);
        }
    }

    public void closeAll() {
        new HashMap<>(openMenus).forEach((uuid, menu) -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.closeInventory();
            }
        });
        openMenus.clear();
        contents.clear();
    }
}