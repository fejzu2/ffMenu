package pl.fejzu.menu.bukkit.builder;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import pl.fejzu.menu.bukkit.MenuAPI;
import pl.fejzu.menu.bukkit.action.ActionRegistry;
import pl.fejzu.menu.bukkit.builder.context.MenuContext;
import pl.fejzu.menu.bukkit.builder.template.ButtonTemplate;
import pl.fejzu.menu.bukkit.button.BukkitButton;
import pl.fejzu.menu.bukkit.menu.BukkitMenu;
import pl.fejzu.menu.bukkit.service.MenuService;
import pl.fejzu.menu.interfaces.pagination.Pagination;
import pl.fejzu.menu.interfaces.template.ItemTemplate;
import pl.fejzu.menu.interfaces.template.MenuTemplate;
import pl.fejzu.menu.utils.PlaceholderUtil;


import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Getter
public class MenuBuilder {

    private Component title = Component.empty();
    private final List<String> pattern = new ArrayList<>();
    private final Map<Character, ButtonTemplate> templates = new HashMap<>();
    private final Map<Character, ItemTemplate> itemTemplates = new HashMap<>();
    private boolean closeable = true;
    private boolean cancelClick = true;
    private int updateInterval = -1;
    private InventoryType inventoryType = InventoryType.CHEST;
    private boolean shared = false;

    private BiConsumer<Player, MenuContext> onInit = (p, c) -> {};
    private BiConsumer<Player, MenuContext> onUpdate = (p, c) -> {};
    private Consumer<Player> onClose = p -> {};

    private final Map<String, ConfigActionHandler> customActions = new HashMap<>();
    private final Map<String, PlaceholderProvider> placeholders = new HashMap<>();

    private final List<Consumer<InventoryClickEvent>> clickHandlers = new ArrayList<>();
    private final List<Consumer<InventoryDragEvent>> dragHandlers = new ArrayList<>();
    private final List<Consumer<InventoryCloseEvent>> closeHandlers = new ArrayList<>();

    public static MenuBuilder create() {
        return new MenuBuilder();
    }

    public static MenuBuilder fromTemplate(MenuTemplate template) {
        MenuBuilder builder = new MenuBuilder();
        builder.title(template.getTitle());
        builder.pattern(template.getPattern().toArray(new String[0]));
        builder.closeable(template.isCloseable());
        builder.cancelClick(template.isCancelClick());
        Object inventoryTypeObj = template.getInventoryType();
        if (inventoryTypeObj instanceof InventoryType inventoryType) {
            builder.inventoryType(inventoryType);
        }
        if (template.getUpdateInterval() > 0) {
            builder.updateEvery(template.getUpdateInterval());
        }
        if (template.getItems() != null) {
            template.getItems().forEach(builder.itemTemplates::put);
        }
        return builder;
    }

    public MenuBuilder title(String title) {
        this.title = LegacyComponentSerializer.legacyAmpersand().deserialize(title);
        return this;
    }

    public MenuBuilder title(Component title) {
        this.title = title;
        return this;
    }

    public MenuBuilder pattern(String... lines) {
        pattern.clear();
        pattern.addAll(Arrays.asList(lines));
        return this;
    }

    public MenuBuilder closeable(boolean closeable) {
        this.closeable = closeable;
        return this;
    }

    public MenuBuilder cancelClick(boolean cancel) {
        this.cancelClick = cancel;
        return this;
    }

    public MenuBuilder updateEvery(int ticks) {
        this.updateInterval = ticks;
        return this;
    }

    public MenuBuilder inventoryType(InventoryType inventoryType) {
        this.inventoryType = inventoryType;
        return this;
    }

    public MenuBuilder shared() {
        this.shared = true;
        return this;
    }

    public MenuBuilder define(char symbol, ItemStack icon) {
        templates.put(symbol, new ButtonTemplate(icon));
        return this;
    }

    public MenuBuilder define(char symbol, ItemStack icon, Consumer<InventoryClickEvent> onClick) {
        templates.put(symbol, new ButtonTemplate(icon, onClick));
        return this;
    }

    public MenuBuilder action(String actionId, ConfigActionHandler handler) {
        customActions.put(actionId.toLowerCase(), handler);
        return this;
    }

    public MenuBuilder placeholder(String key, PlaceholderProvider provider) {
        placeholders.put(key, provider);
        return this;
    }

    public MenuBuilder onInit(BiConsumer<Player, MenuContext> handler) {
        this.onInit = handler;
        return this;
    }

    public MenuBuilder onUpdate(BiConsumer<Player, MenuContext> handler) {
        this.onUpdate = handler;
        return this;
    }

    public MenuBuilder onClose(Consumer<Player> handler) {
        this.onClose = handler;
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T extends org.bukkit.event.Event> MenuBuilder listener(Class<T> eventClass, Consumer<T> handler) {
        if (eventClass == InventoryClickEvent.class) {
            clickHandlers.add((Consumer<InventoryClickEvent>) handler);
        } else if (eventClass == InventoryDragEvent.class) {
            dragHandlers.add((Consumer<InventoryDragEvent>) handler);
        } else if (eventClass == InventoryCloseEvent.class) {
            closeHandlers.add((Consumer<InventoryCloseEvent>) handler);
        }
        return this;
    }

    public BukkitMenu build() {
        return new BukkitMenu(this, MenuAPI.getService());
    }

    public void open(Player player) {
        build().open(player);
    }

    public BukkitMenu build(MenuService service) {
        return new BukkitMenu(this, service);
    }

    public void open(Player player, MenuService service) {
        build(service).open(player);
    }

    public BukkitButton createButtonFromTemplate(char symbol, MenuContext context) {
        ItemTemplate itemTemplate = itemTemplates.get(symbol);
        if (itemTemplate == null || itemTemplate.getItem() == null) {
            return null;
        }

        Object itemObj = itemTemplate.getItem();
        if (!(itemObj instanceof ItemStack itemStack)) {
            return null;
        }
        ItemStack finalItem = itemStack.clone();
        applyPlaceholdersToItem(finalItem, context.getPlayer(), context, itemTemplate, symbol);

        return BukkitButton.of(finalItem)
                .onClick(event -> {
                    Player player = (Player) event.getWhoClicked();
                    for (String key : itemTemplate.getActionKeys()) {
                        ConfigActionHandler customHandler = customActions.get(key.toLowerCase());
                        if (customHandler != null) {
                            customHandler.handle(player, event, context, itemTemplate);
                        }
                    }
                    ActionRegistry.getInstance().executeAll(player, event, context, itemTemplate);
                });
    }

    private void applyPlaceholdersToItem(ItemStack item, Player player, MenuContext context,
                                          ItemTemplate itemTemplate, Character symbol) {
        if (!item.hasItemMeta()) {
            return;
        }

        Map<String, Object> resolvedPlaceholders = resolvePlaceholders(player, context, itemTemplate, symbol);

        if (resolvedPlaceholders.isEmpty()) {
            return;
        }

        item.editMeta(meta -> {
            if (meta.hasDisplayName()) {
                Component displayName = meta.displayName();
                if (displayName != null) {
                    String serialized = LegacyComponentSerializer.legacySection().serialize(displayName);
                    if (serialized.indexOf('{') >= 0) {
                        String replaced = pl.fejzu.menu.utils.PlaceholderUtil.replace(serialized, resolvedPlaceholders);
                        Component newDisplayName = LegacyComponentSerializer.legacySection().deserialize(replaced)
                            .decorationIfAbsent(net.kyori.adventure.text.format.TextDecoration.ITALIC, net.kyori.adventure.text.format.TextDecoration.State.FALSE);
                        meta.displayName(newDisplayName);
                    }
                }
            }
            if (meta.hasLore()) {
                List<Component> lore = meta.lore();
                if (lore != null) {
                    boolean loreChanged = false;
                    List<Component> newLore = new ArrayList<>();
                    for (Component line : lore) {
                        String serialized = LegacyComponentSerializer.legacySection().serialize(line);
                        if (serialized.indexOf('{') >= 0) {
                            String replaced = PlaceholderUtil.replace(serialized, resolvedPlaceholders);
                            Component newLine = LegacyComponentSerializer.legacySection().deserialize(replaced)
                                .decorationIfAbsent(net.kyori.adventure.text.format.TextDecoration.ITALIC, net.kyori.adventure.text.format.TextDecoration.State.FALSE);
                            newLore.add(newLine);
                            loreChanged = true;
                        } else {
                            newLore.add(line);
                        }
                    }
                    if (loreChanged) {
                        meta.lore(newLore);
                    }
                }
            }
        });
    }

    private Map<String, Object> resolvePlaceholders(Player player, MenuContext context,
                                                     ItemTemplate itemTemplate, Character symbol) {
        Map<String, Object> resolved = new HashMap<>();

        if (context != null && context.getContents() != null) {
            Pagination<ItemStack, InventoryClickEvent> pagination = context.getContents().pagination();
            int current = pagination.getCurrentPage();
            int total = pagination.getTotalPages();
            resolved.put("current_page", current + 1);
            resolved.put("total_pages", total);
            resolved.put("next_page", Math.min(current + 2, total));
            resolved.put("previous_page", Math.max(current, 1));
            resolved.put("items_per_page", pagination.getItemsPerPage());
        }

        for (Map.Entry<String, PlaceholderProvider> entry : placeholders.entrySet()) {
            try {
                Object value = entry.getValue().provide(player, context, itemTemplate, symbol);
                resolved.put(entry.getKey(), value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return resolved;
    }

    public Component getTitleWithPlaceholders(Player player, MenuContext context) {
        String serialized = LegacyComponentSerializer.legacySection().serialize(title);

        if (serialized.indexOf('{') < 0) {
            return title;
        }

        Map<String, Object> resolvedPlaceholders = resolvePlaceholders(player, context, null, null);
        String replaced = pl.fejzu.menu.utils.PlaceholderUtil.replace(serialized, resolvedPlaceholders);
        return LegacyComponentSerializer.legacySection().deserialize(replaced);
    }

    @FunctionalInterface
    public interface ConfigActionHandler {
        void handle(Player player, InventoryClickEvent event, MenuContext context, ItemTemplate itemTemplate);
    }
}