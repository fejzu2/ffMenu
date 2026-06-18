package pl.fejzu.menu.bukkit.menu;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import pl.fejzu.menu.bukkit.builder.MenuBuilder;
import pl.fejzu.menu.bukkit.builder.context.MenuContext;
import pl.fejzu.menu.bukkit.builder.template.ButtonTemplate;
import pl.fejzu.menu.bukkit.button.BukkitButton;
import pl.fejzu.menu.bukkit.impl.BukkitInventoryContents;
import pl.fejzu.menu.bukkit.service.MenuService;
import pl.fejzu.menu.bukkit.task.MenuUpdateTask;

import pl.fejzu.menu.interfaces.button.Button;
import pl.fejzu.menu.slot.SlotPosition;
import pl.fejzu.menu.utils.PatternsUtil;

import java.util.*;

@Getter
public class BukkitMenu {

    private final MenuBuilder builder;
    private final MenuService service;
    private final int rows;
    private final int columns;

    private MenuUpdateTask updateTask;
    private boolean closed = false;

    private BukkitInventoryContents personalContents;

    private final boolean shared;
    private boolean closeable;
    private Inventory sharedInventory;
    private BukkitInventoryContents sharedContents;
    private final Set<UUID> viewers = new HashSet<>();

    public BukkitMenu(MenuBuilder builder, MenuService service) {
        this.builder = builder;
        this.service = service;
        this.shared = builder.isShared();
        this.closeable = builder.isCloseable();

        if (builder.getInventoryType() == org.bukkit.event.inventory.InventoryType.CHEST) {
            int[] size = PatternsUtil.calculateSize(builder.getPattern());
            this.rows = size[0];
            this.columns = size[1];
        } else {
            this.rows = builder.getPattern().size();
            this.columns = builder.getPattern().isEmpty() ? 0 :
                builder.getPattern().get(0).split(" ").length;
        }
    }

    public void open(Player player) {
        if (shared) {
            openShared(player);
        } else {
            openPersonal(player);
        }
    }

    public void open(Player... players) {
        if (!shared) {
            for (Player player : players) {
                openPersonal(player);
            }
            return;
        }

        for (Player player : players) {
            openShared(player);
        }
    }

    private void openPersonal(Player player) {
        closed = false;
        BukkitInventoryContents contents = new BukkitInventoryContents(rows, columns, player.getUniqueId());
        applyTemplates(contents);
        MenuContext context = new MenuContext(contents, builder, player);
        applyItemTemplates(contents, context);
        builder.getOnInit().accept(player, context);

        Component titleWithPlaceholders = builder.getTitleWithPlaceholders(player, context);

        Inventory inventory;
        if (builder.getInventoryType() == org.bukkit.event.inventory.InventoryType.CHEST) {
            inventory = Bukkit.createInventory(null, rows * columns, titleWithPlaceholders);
        } else {
            inventory = Bukkit.createInventory(null, builder.getInventoryType(), titleWithPlaceholders);
        }
        fillInventory(inventory, contents);

        contents.setMenuInventory(inventory);

        this.personalContents = contents;

        player.openInventory(inventory);

        service.setMenu(player, this);
        service.setContents(player, contents);

        startUpdateTask(player);
    }

    private void openShared(Player player) {

        if (sharedInventory == null) {
            initializeSharedInventory(player);
        }

        viewers.add(player.getUniqueId());

        MenuContext context = new MenuContext(sharedContents, builder, player);
        builder.getOnInit().accept(player, context);

        service.setMenu(player, this);
        service.setContents(player, sharedContents);

        player.openInventory(sharedInventory);

        if (viewers.size() == 1 && builder.getUpdateInterval() > 0) {
            startSharedUpdateTask();
        }
    }

    private void initializeSharedInventory(Player firstPlayer) {
        closed = false;
        sharedContents = new BukkitInventoryContents(rows, columns, null);
        applyTemplates(sharedContents);

        MenuContext context = new MenuContext(sharedContents, builder, firstPlayer);
        applyItemTemplates(sharedContents, context);

        Component titleWithPlaceholders = builder.getTitleWithPlaceholders(firstPlayer, context);

        if (builder.getInventoryType() == org.bukkit.event.inventory.InventoryType.CHEST) {
            sharedInventory = Bukkit.createInventory(null, rows * columns, titleWithPlaceholders);
        } else {
            sharedInventory = Bukkit.createInventory(null, builder.getInventoryType(), titleWithPlaceholders);
        }
        fillInventory(sharedInventory, sharedContents);

        sharedContents.setMenuInventory(sharedInventory);
    }

    public void update(Player player) {
        if (closed) return;

        if (shared) {
            updateShared(player);
        } else {
            updatePersonal(player);
        }
    }

    private void updatePersonal(Player player) {
        if (service.getMenu(player).orElse(null) != this) {
            stopUpdateTask();
            return;
        }

        if (personalContents == null) {
            return;
        }

        Inventory topInventory = player.getOpenInventory().getTopInventory();

        Inventory expected = personalContents.getMenuInventory();
        if (!topInventory.equals(expected)) {
            return;
        }

        MenuContext context = new MenuContext(personalContents, builder, player);
        builder.getOnUpdate().accept(player, context);

        personalContents.flushDirtySlots(topInventory);
    }

    private void updateShared(Player player) {
        if (sharedContents == null || sharedInventory == null) return;

        MenuContext context = new MenuContext(sharedContents, builder, player);
        builder.getOnUpdate().accept(player, context);

        sharedContents.flushDirtySlots(sharedInventory);
    }

    public void close(Player player) {
        if (shared) {
            closeShared(player);
        } else {
            closePersonal(player);
        }
    }

    private void closePersonal(Player player) {
        if (closed) return;
        closed = true;
        stopUpdateTask();
        builder.getOnClose().accept(player);
        service.getContents(player).ifPresent(contents -> {
            if (contents instanceof BukkitInventoryContents bukkitContents) {
                bukkitContents.invalidatePlayerCache();
            }
        });
        service.setMenu(player, null);
        service.setContents(player, null);
        this.personalContents = null;
    }

    private void closeShared(Player player) {
        viewers.remove(player.getUniqueId());
        builder.getOnClose().accept(player);
        service.setMenu(player, null);
        service.setContents(player, null);
        if (viewers.isEmpty()) {
            closed = true;
            stopUpdateTask();
            if (sharedContents != null) {
                sharedContents.invalidatePlayerCache();
            }
            sharedInventory = null;
            sharedContents = null;
        }
    }

    public void closeAll() {
        if (shared) {
            Set<UUID> viewersCopy = new HashSet<>(viewers);
            for (UUID uuid : viewersCopy) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    player.closeInventory();
                }
            }
        } else {
            closed = true;
            stopUpdateTask();
        }
    }

    public void reopen(Player player) {
        close(player);
        open(player);
    }

    public boolean isCloseable() {
        return closeable;
    }

    public void setCloseable(boolean closeable) {
        this.closeable = closeable;
    }

    public boolean shouldCancelClick() {
        return builder.isCancelClick();
    }

    public Component getTitle() {
        return builder.getTitle();
    }

    public Inventory getInventory() {
        return sharedInventory;
    }

    public void setItem(int slot, ItemStack item) {
        if (!shared || sharedInventory == null) {
            return;
        }

        if (slot >= 0 && slot < sharedInventory.getSize()) {
            sharedInventory.setItem(slot, item);
            int row = slot / columns;
            int col = slot % columns;
            if (sharedContents != null) {
                sharedContents.set(SlotPosition.of(row, col), item != null ? BukkitButton.of(item) : null);
            }
        }
    }

    public void setItem(char key, BukkitButton button) {
        if (!shared || sharedContents == null || sharedInventory == null) {
            return;
        }

        List<SlotPosition> positions = PatternsUtil.getPositionsFromSymbol(builder.getPattern(), key);
        for (SlotPosition pos : positions) {
            sharedContents.set(pos, button);
            int slot = pos.toSlot(columns);
            if (slot < sharedInventory.getSize()) {
                sharedInventory.setItem(slot, button != null ? button.getIcon() : null);
            }
        }
    }

    public Set<Player> getViewers() {
        Set<Player> result = new HashSet<>();
        for (UUID uuid : viewers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                result.add(player);
            }
        }
        return result;
    }

    public boolean isShared() {
        return shared;
    }

    public BukkitInventoryContents getSharedContents() {
        return sharedContents;
    }

    private void applyTemplates(BukkitInventoryContents contents) {
        for (Map.Entry<Character, ButtonTemplate> entry : builder.getTemplates().entrySet()) {
            char symbol = entry.getKey();
            ButtonTemplate template = entry.getValue();

            List<SlotPosition> positions = PatternsUtil.getPositionsFromSymbol(builder.getPattern(), symbol);
            for (SlotPosition pos : positions) {
                BukkitButton button = BukkitButton.of(template.getIcon());
                if (template.getOnClick() != null) {
                    button.onClick(template.getOnClick());
                }
                contents.set(pos, button);
            }
        }
    }

    private void applyItemTemplates(BukkitInventoryContents contents, MenuContext context) {
        for (Map.Entry<Character, ?> entry : builder.getItemTemplates().entrySet()) {
            char symbol = entry.getKey();

            List<SlotPosition> positions = PatternsUtil.getPositionsFromSymbol(builder.getPattern(), symbol);
            for (SlotPosition pos : positions) {
                BukkitButton button = builder.createButtonFromTemplate(symbol, context);
                if (button != null) {
                    contents.set(pos, button);
                }
            }
        }
    }

    private void fillInventory(Inventory inventory, BukkitInventoryContents contents) {
        Button<ItemStack, InventoryClickEvent>[][] buttons = contents.all();

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                Button<ItemStack, InventoryClickEvent> button = buttons[row][col];
                int slot = row * columns + col;
                inventory.setItem(slot, button != null ? button.getIcon() : null);
            }
        }
    }

    private void startUpdateTask(Player player) {
        if (builder.getUpdateInterval() > 0) {
            updateTask = new MenuUpdateTask(this, player);
            updateTask.runTaskTimer(service.getPlugin(), builder.getUpdateInterval(), builder.getUpdateInterval());
        }
    }

    private void startSharedUpdateTask() {
        if (builder.getUpdateInterval() > 0) {
            updateTask = new SharedMenuUpdateTask(this);
            updateTask.runTaskTimer(service.getPlugin(), builder.getUpdateInterval(), builder.getUpdateInterval());
        }
    }

    private void stopUpdateTask() {
        if (updateTask != null && !updateTask.isCancelled()) {
            updateTask.cancel();
            updateTask = null;
        }
    }

    private static class SharedMenuUpdateTask extends MenuUpdateTask {
        private final BukkitMenu sharedMenu;

        public SharedMenuUpdateTask(BukkitMenu menu) {
            super(menu, null);
            this.sharedMenu = menu;
        }

        @Override
        public void run() {
            Set<Player> currentViewers = sharedMenu.getViewers();
            if (currentViewers.isEmpty()) {
                cancel();
                return;
            }
            try {
                Player firstViewer = currentViewers.iterator().next();
                sharedMenu.updateShared(firstViewer);
            } catch (Exception e) {
                e.printStackTrace();
                cancel();
                sharedMenu.closeAll();
            }
        }
    }
}
