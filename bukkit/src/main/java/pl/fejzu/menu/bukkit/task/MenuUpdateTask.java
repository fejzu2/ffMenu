package pl.fejzu.menu.bukkit.task;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pl.fejzu.menu.bukkit.menu.BukkitMenu;

public class MenuUpdateTask extends BukkitRunnable {

    private final BukkitMenu menu;
    private final Player player;

    public MenuUpdateTask(BukkitMenu menu, Player player) {
        this.menu = menu;
        this.player = player;
    }

    @Override
    public void run() {
        if (!player.isOnline()) {
            cancel();
            return;
        }

        try {
            menu.update(player);
        } catch (Exception e) {
            e.printStackTrace();
            cancel();
            player.closeInventory();
        }
    }
}