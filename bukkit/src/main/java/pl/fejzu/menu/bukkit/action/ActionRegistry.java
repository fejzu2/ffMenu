package pl.fejzu.menu.bukkit.action;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import pl.fejzu.menu.bukkit.builder.context.MenuContext;
import pl.fejzu.menu.interfaces.template.ItemTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ActionRegistry {

    @Getter
    private static final ActionRegistry instance = new ActionRegistry();

    private final Map<String, MenuAction> actions = new HashMap<>();

    private ActionRegistry() {
        registerDefaults();
    }

    private void registerDefaults() {
        register("close", (player, event, context, data) -> {
            if (data.getBool("close")) {
                context.close();
            }
        });
        register("sound", (player, event, context, data) -> {
            String soundName = data.getString("sound");
            if (soundName == null) return;
            try {
                NamespacedKey key = NamespacedKey.minecraft(soundName.toLowerCase());
                Sound sound = Registry.SOUNDS.get(key);

                if (sound != null) {
                    float volume = getFloatValue(data.getAction("sound-volume"), 1.0f);
                    float pitch = getFloatValue(data.getAction("sound-pitch"), 1.0f);
                    player.playSound(player.getLocation(), sound, volume, pitch);
                }
            } catch (Exception ignored) {}
        });
        register("command", (player, event, context, data) -> {
            String command = data.getString("command");
            if (command != null) {
                player.performCommand(command.replace("{player}", player.getName()));
            }
        });
        register("console", (player, event, context, data) -> {
            String command = data.getString("console");
            if (command != null) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        command.replace("{player}", player.getName()));
            }
        });
        register("commands", (player, event, context, data) -> {
            Object commandsObj = data.getAction("commands");
            if (commandsObj instanceof Iterable<?> commands) {
                for (Object cmd : commands) {
                    if (cmd instanceof String str) {
                        player.performCommand(str.replace("{player}", player.getName()));
                    }
                }
            }
        });
        register("console-commands", (player, event, context, data) -> {
            Object commandsObj = data.getAction("console-commands");
            if (commandsObj instanceof Iterable<?> commands) {
                for (Object cmd : commands) {
                    if (cmd instanceof String str) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                str.replace("{player}", player.getName()));
                    }
                }
            }
        });
        register("set-property", (player, event, context, data) -> {
            Object propsObj = data.getAction("set-property");
            if (propsObj instanceof Map<?, ?> props) {
                props.forEach((key, value) -> {
                    if (key instanceof String keyStr) {
                        context.setProperty(keyStr, value);
                    }
                });
            }
        });
        register("next-page", (player, event, context, data) -> {
            if (data.getBool("next-page")) {
                context.pagination().next();
            }
        });
        register("previous-page", (player, event, context, data) -> {
            if (data.getBool("previous-page")) {
                context.pagination().previous();
            }
        });
    }

    public void register(String id, MenuAction action) {
        actions.put(id.toLowerCase(), action);
    }

    public void unregister(String id) {
        actions.remove(id.toLowerCase());
    }

    public Optional<MenuAction> get(String id) {
        return Optional.ofNullable(actions.get(id.toLowerCase()));
    }

    public void executeAll(Player player, InventoryClickEvent event,
                           MenuContext context, ItemTemplate itemTemplate) {
        for (String key : itemTemplate.getActionKeys()) {
            get(key).ifPresent(action -> action.execute(player, event, context, itemTemplate));
        }
    }

    private static String getMapString(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        return value instanceof String ? (String) value : defaultValue;
    }

    private static double getMapNumber(Map<String, Object> map, String key, double defaultValue) {
        Object value = map.get(key);
        return value instanceof Number ? ((Number) value).doubleValue() : defaultValue;
    }

    private static float getFloatValue(Object value, float defaultValue) {
        if (value instanceof Number number) {
            return number.floatValue();
        }
        return defaultValue;
    }
}