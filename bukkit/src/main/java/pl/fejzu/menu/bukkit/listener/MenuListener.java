package pl.fejzu.menu.bukkit.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import pl.fejzu.menu.bukkit.impl.BukkitInventoryContents;
import pl.fejzu.menu.bukkit.menu.BukkitMenu;
import pl.fejzu.menu.bukkit.service.MenuService;
import pl.fejzu.menu.slot.SlotPosition;


import java.util.HashMap;

public class MenuListener implements Listener {

    private final MenuService service;

    public MenuListener(MenuService service) {
        this.service = service;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        service.getMenu(player).ifPresent(menu -> {
            Inventory clickedInventory = event.getClickedInventory();
            if (clickedInventory == null) {
                if (menu.shouldCancelClick()) {
                    event.setCancelled(true);
                }
                return;
            }
            Inventory topInventory = player.getOpenInventory().getTopInventory();
            if (clickedInventory.equals(topInventory)) {
                int slot = event.getSlot();
                int columns = menu.getColumns();
                int row = slot / columns;
                int column = slot % columns;
                if (row < 0 || column < 0 || row >= menu.getRows() || column >= menu.getColumns()) {
                    return;
                }
                service.getContents(player).flatMap(contents -> contents.get(SlotPosition.of(row, column))).ifPresent(button -> {
                    button.handleClick(event);
                });
            }
            for (var handler : menu.getBuilder().getClickHandlers()) {
                handler.accept(event);
            }

            if (menu.shouldCancelClick()) {
                event.setCancelled(true);
            }
        });
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        service.getMenu(player).ifPresent(menu -> {
            boolean affectsTopInventory = event.getRawSlots().stream()
                    .anyMatch(slot -> slot < player.getOpenInventory().getTopInventory().getSize());
            for (var handler : menu.getBuilder().getDragHandlers()) {
                handler.accept(event);
            }

            if (affectsTopInventory && menu.shouldCancelClick()) {
                event.setCancelled(true);
            }
        });
    }


    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        service.getMenu(player).ifPresent(menu -> {
            Inventory top = event.getView().getTopInventory();
            Inventory expected;
            if (menu.isShared()) {
                expected = menu.getInventory();
            } else {
                expected = service.getContents(player)
                        .filter(c -> c instanceof BukkitInventoryContents)
                        .map(c -> ((BukkitInventoryContents) c).getMenuInventory())
                        .orElse(null);
            }

            if (!top.equals(expected)) {
                return;
            }
            for (var handler : menu.getBuilder().getCloseHandlers()) {
                handler.accept(event);
            }

            if (menu.isCloseable()) {
                menu.close(player);
                if (!menu.isShared()) {
                    top.clear();
                }
            } else {
                Bukkit.getScheduler().runTask(service.getPlugin(), () -> player.openInventory(top));
            }
        });
    }




    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        service.getMenu(player).ifPresent(menu -> {
            menu.close(player);
        });
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPluginDisable(PluginDisableEvent event) {
        if (!event.getPlugin().equals(service.getPlugin())) {
            return;
        }
        java.util.Set<BukkitMenu> processedSharedMenus = new java.util.HashSet<>();

        new HashMap<>(service.getOpenMenus()).forEach((uuid, menu) -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                if (menu.isShared()) {
                    if (!processedSharedMenus.contains(menu)) {
                        processedSharedMenus.add(menu);
                        menu.closeAll();
                    }
                } else {
                    Inventory topInventory = player.getOpenInventory().getTopInventory();
                    topInventory.clear();
                    menu.close(player);
                    player.closeInventory();
                }
            }
        });
        service.getOpenMenus().clear();
        service.getContents().clear();
    }
}