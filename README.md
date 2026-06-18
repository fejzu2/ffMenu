# ffMenu

![License](https://img.shields.io/github/license/fejzu/ffmenu?style=for-the-badge&color=blue)
![Java](https://img.shields.io/badge/java-21+-blue.svg?style=for-the-badge)
![Paper](https://img.shields.io/badge/platform-paper%201.21+-blue.svg?style=for-the-badge)

Lightweight, pattern-based GUI framework for Minecraft (Paper) with fluent API, built-in pagination, and YAML configuration support.

## Features

- **Pattern-Based Layout**: Define menu structure using intuitive character patterns
- **Fluent Builder API**: Clean, chainable methods for menu creation
- **Smart Pagination**: Built-in pagination with simple API and YAML support
- **Shared Menus**: Multi-player inventory support for minigames, auctions, etc.
- **Performance Optimized**: Dirty flag system, intelligent updates, ~80-90% fewer operations
- **Auto-Update**: Automatic inventory refresh at configurable intervals
- **Action System**: Configurable actions from YAML (sounds, commands, pagination, etc.)
- **Template System**: Core interfaces for custom implementations (YAML, database, etc.)
- **Modular Design**: Core, Bukkit, and Config modules - use only what you need
- **Global API**: Single initialization, use anywhere without passing services

## Requirements

- **Paper 1.21+** (or compatible forks like Purpur)
- **Java 21+**

## Installation

### Bukkit Module (Core functionality)

**Maven:**
```xml
<repositories>
    <repository>
        <id>fejzu-repo</id>
        <url>https://repo.fejzu.pl/releases</url>
    </repository>
</repositories>
```
```xml
<dependency>
    <groupId>pl.fejzu</groupId>
    <artifactId>ffmenu-bukkit</artifactId>
    <version>1.6-BETA</version>
</dependency>
```

**Gradle (Kotlin DSL):**
```kotlin
repositories {
    maven("https://repo.fejzu.pl/releases")
}
```
```kotlin
dependencies {
    implementation("pl.fejzu:ffmenu-bukkit:0.3-BETA")
}
```

### Config Module (YAML configuration support)

Includes Bukkit module + okaeri-configs integration.

**Maven:**
```xml
<dependency>
    <groupId>pl.fejzu</groupId>
    <artifactId>ffmenu-config</artifactId>
    <version>0.2-BETA</version>
</dependency>
```

**Gradle (Kotlin DSL):**
```kotlin
dependencies {
    implementation("pl.fejzu:ffmenu-config:0.3-BETA")
}
```

## Quick Start

### Plugin Setup
```java
import pl.fejzu.menu.bukkit.MenuAPI;

public class MyPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        MenuAPI.init(this);
    }

    @Override
    public void onDisable() {
        MenuAPI.disable();
    }
}
```

### Simple Menu
```java
import pl.fejzu.menu.bukkit.builder.MenuBuilder;
import pl.fejzu.menu.bukkit.button.BukkitButton;

public class SimpleMenuExample {

    public static void open(Player player) {
        MenuBuilder.create()
            .title("<green>My First Menu")
            .pattern(
                "# # # # # # # # #",
                "# . . . X . . . #",
                "# # # # # # # # #"
            )
            .define('#', new ItemStack(Material.GRAY_STAINED_GLASS_PANE))
            .onInit((p, ctx) -> {
                ctx.set('X', BukkitButton.of(Material.DIAMOND)
                    .onClick(e -> {
                        p.sendMessage("You clicked the diamond!");
                    }));
            })
            .open(player);
    }
}
```

## Pattern System

Patterns define your menu layout using characters separated by spaces:
```java
.pattern(
    "# # # # # # # # #",   // Row 0: all borders
    "# X X X X X X X #",   // Row 1: X = item slots
    "# X X X X X X X #",   // Row 2: X = item slots
    "# X X X X X X X #",   // Row 3: X = item slots
    "# # # < . > # # #"    // Row 4: navigation
)
```

- Each character represents a slot
- Characters are separated by spaces
- `.` represents empty slots
- Use any character as a symbol identifier
- Define static items with `.define(char, ItemStack)`
- Set dynamic items in `.onInit()` or `.onUpdate()`

## Menu Examples

### Dynamic Content
```java
MenuBuilder.create()
    .title("<yellow>Player Stats")
    .pattern(
        "# # # # # # # # #",
        "# . H . L . C . #",
        "# # # # # # # # #"
    )
    .define('#', new ItemStack(Material.BLACK_STAINED_GLASS_PANE))
    .onInit((player, ctx) -> {
        ctx.set('H', BukkitButton.of(new ItemBuilder(Material.RED_DYE)
            .name("&cHealth: " + (int) player.getHealth())
            .build()));

        ctx.set('L', BukkitButton.of(new ItemBuilder(Material.EXPERIENCE_BOTTLE)
            .name("&aLevel: " + player.getLevel())
            .build()));

        ctx.set('C', BukkitButton.of(Material.BARRIER)
            .onClick(e -> ctx.close()));
    })
    .open(player);
```

### Auto-Updating Menu
```java
MenuBuilder.create()
    .title("<gold>Live Stats")
    .pattern(
        "# # # # # # # # #",
        "# . . T . . . . #",
        "# # # # # # # # #"
    )
    .updateEvery(20) // Update every second (20 ticks)
    .onInit((player, ctx) -> {
        ctx.setProperty("startTime", System.currentTimeMillis());
    })
    .onUpdate((player, ctx) -> {
        long elapsed = System.currentTimeMillis() - ctx.getProperty("startTime", 0L);
        int seconds = (int) (elapsed / 1000);

        ctx.set('T', BukkitButton.of(new ItemBuilder(Material.CLOCK)
            .name("&eTime: " + seconds + "s")
            .build()));
    })
    .open(player);
```

### Non-Closeable Menu
```java
MenuBuilder.create()
    .title("<red>Confirm Action")
    .pattern(
        "# # # # # # # # #",
        "# . Y . . . N . #",
        "# # # # # # # # #"
    )
    .closeable(false)
    .onInit((player, ctx) -> {
        ctx.set('Y', BukkitButton.of(Material.LIME_WOOL)
            .onClick(e -> {
                player.sendMessage("Confirmed!");
                ctx.close();
            }));

        ctx.set('N', BukkitButton.of(Material.RED_WOOL)
            .onClick(e -> {
                player.sendMessage("Cancelled!");
                ctx.close();
            }));
    })
    .open(player);
```

### Shared Menu (Multi-Player)

Create menus that multiple players can view simultaneously - perfect for minigames, auctions, or any shared UI:

```java
// Create shared menu (don't open yet)
BukkitMenu menu = MenuBuilder.create()
    .title("&8Game - Player1 vs Player2")
    .pattern(
        "# # # # # # # # #",
        "# A B # C # D E #",
        "# # # # # # # # #"
    )
    .shared()                    // Enable shared mode
    .closeable(false)            // Players can't close during game
    .define('#', filler)
    .updateEvery(2)              // Update every 2 ticks
    .onInit((player, ctx) -> {
        ctx.set('A', playerHead1);
        ctx.set('E', playerHead2);
    })
    .onUpdate((player, ctx) -> {
        // Update countdown timer, etc.
    })
    .build();

// Open for multiple players at once
menu.open(player1, player2);

// Dynamic updates from scheduled task
Bukkit.getScheduler().runTaskTimer(plugin, () -> {
    menu.setItem(11, newItem);           // Update by slot
    menu.setItem('C', countdownButton);  // Update by pattern key

    // Or access inventory directly
    menu.getInventory().setItem(13, timerItem);
}, 0L, 2L);

// After game ends
menu.setCloseable(true);    // Allow closing
menu.closeAll();            // Close for all viewers
```

**Shared Menu API:**

| Method | Description |
|--------|-------------|
| `.shared()` | Enable shared inventory mode (in builder) |
| `.open(Player...)` | Open for multiple players |
| `.close(Player)` | Close for one player |
| `.closeAll()` | Close for all viewers |
| `.getInventory()` | Get the Bukkit Inventory |
| `.setItem(int, ItemStack)` | Update slot directly |
| `.setItem(char, BukkitButton)` | Update by pattern key |
| `.setCloseable(boolean)` | Dynamically change closeable state |
| `.isCloseable()` | Check if closeable |
| `.getViewers()` | Get Set of players viewing |
| `.isShared()` | Check if shared mode |

### Event Listeners

Add custom handlers for inventory events directly in the builder:

```java
MenuBuilder.create()
    .title("&6Event Example")
    .pattern(
        "# # # # # # # # #",
        "# . . . X . . . #",
        "# # # # # # # # #"
    )
    .define('#', new ItemStack(Material.GRAY_STAINED_GLASS_PANE))
    .listener(InventoryClickEvent.class, event -> {
        Player player = (Player) event.getWhoClicked();
        player.sendMessage("You clicked slot: " + event.getSlot());
    })
    .listener(InventoryDragEvent.class, event -> {
        // Handle drag events
        event.setCancelled(true);
    })
    .listener(InventoryCloseEvent.class, event -> {
        Player player = (Player) event.getPlayer();
        player.sendMessage("Menu closed!");
    })
    .open(player);
```

**Supported Events:**

| Event Class | Description |
|-------------|-------------|
| `InventoryClickEvent.class` | Fired on any click in the menu |
| `InventoryDragEvent.class` | Fired when items are dragged |
| `InventoryCloseEvent.class` | Fired when menu is closed |

Multiple handlers can be added for each event type - they will be called in order.

## Pagination

ffMenu provides multiple ways to handle pagination - choose what fits your needs best.

### Simple Pagination (Recommended)

Clean, simple API inspired by triumph-gui:

```java
public void openShop(Player player, List<ShopItem> items) {
    List<BukkitButton> buttons = items.stream()
        .map(item -> BukkitButton.of(item.getIcon())
            .onClick(e -> buyItem(player, item)))
        .toList();

    MenuBuilder.create()
        .title("<yellow>Shop")
        .pattern(
            "# # # # # # # # #",
            "# X X X X X X X #",
            "# X X X X X X X #",
            "# X X X X X X X #",
            "# # < # I # > # #"
        )
        .define('#', new ItemStack(Material.GRAY_STAINED_GLASS_PANE))
        .onInit((p, ctx) -> {
            // Setup pagination
            ctx.pagination()
                .items(buttons)
                .itemsPerPage(21)
                .displayAt('X');

            // Previous button - simple!
            ctx.set('<', BukkitButton.of(Material.ARROW)
                .onClick(e -> ctx.pagination().previous()));

            // Next button - simple!
            ctx.set('>', BukkitButton.of(Material.ARROW)
                .onClick(e -> ctx.pagination().next()));

            // Optional: Info button
            ctx.set('I', BukkitButton.of(new ItemBuilder(Material.PAPER)
                .name("&ePage " + (ctx.pagination().getCurrentPage() + 1) +
                      "/" + ctx.pagination().getTotalPages())
                .build()));
        })
        .open(player);
}
```

### Pagination with YAML Configuration

Define pagination buttons directly in your config:

**Java:**
```java
MenuBuilder.fromTemplate(config.shopMenu)
    .onInit((p, ctx) -> {
        ctx.pagination()
            .items(createShopItems())
            .itemsPerPage(21)
            .displayAt('X');
    })
    .open(player);
```

**YAML:**
```yaml
shop-menu:
  title: "&eShop"
  pattern:
    - "# # # # # # # # #"
    - "# X X X X X X X #"
    - "# X X X X X X X #"
    - "# X X X X X X X #"
    - "# # < # I # > # #"
  items:
    '#':
      item:
        material: GRAY_STAINED_GLASS_PANE
        name: " "
    '<':
      item:
        material: ARROW
        name: "&ePrevious Page"
      actions:
        previous-page: true  # Built-in action!
    '>':
      item:
        material: ARROW
        name: "&eNext Page"
      actions:
        next-page: true      # Built-in action!
    'I':
      item:
        material: PAPER
        name: "&ePage Info"
```

### Opening on a Specific Page

Use `.goToPage(int)` to open the menu already positioned at a given page (0-indexed):

```java
public void openShop(Player player, List<BukkitButton> buttons, int startPage) {
    MenuBuilder.create()
        .title("<yellow>Shop")
        .pattern(
            "# # # # # # # # #",
            "# X X X X X X X #",
            "# X X X X X X X #",
            "# X X X X X X X #",
            "# # < # I # > # #"
        )
        .define('#', new ItemStack(Material.GRAY_STAINED_GLASS_PANE))
        .onInit((p, ctx) -> {
            ctx.pagination()
                .items(buttons)
                .itemsPerPage(21)
                .displayAt('X')
                .goToPage(startPage); // open on desired page

            ctx.set('<', BukkitButton.of(Material.ARROW)
                .onClick(e -> ctx.pagination().previous()));
            ctx.set('>', BukkitButton.of(Material.ARROW)
                .onClick(e -> ctx.pagination().next()));
        })
        .open(player);
}
```

> **Note:** Call `.goToPage()` **after** `.displayAt()` so the correct page items are rendered immediately.

### Advanced: Custom Page Change Handler

Need to update other elements when page changes? Use the callback parameter:

```java
ctx.pagination()
    .items(buttons)
    .itemsPerPage(21)
    .displayAt('X');

ctx.set('<', BukkitButton.of(Material.ARROW)
    .onClick(e -> {
        ctx.pagination().previous();
        // Update page info after page change
        updatePageInfo(ctx);
    }));

ctx.set('>', BukkitButton.of(Material.ARROW)
    .onClick(e -> {
        ctx.pagination().next();
        // Update page info after page change
        updatePageInfo(ctx);
    }));
```

---

# Template System

The Template System provides core interfaces (`MenuTemplate`, `ItemTemplate`) that allow you to define menus from any data source - YAML files, databases, or programmatically.

## Core Interfaces

### MenuTemplate
```java
public interface MenuTemplate {
    String getTitle();
    List<String> getPattern();
    Map<Character, ItemTemplate> getItems();
    
    default boolean isCloseable() { return true; }
    default boolean isCancelClick() { return true; }
    default int getUpdateInterval() { return -1; }
}
```

### ItemTemplate
```java
public interface ItemTemplate {
    Object getItem();
    Map<String, Object> getActions();
    
    default String getString(String key) { ... }
    default boolean getBool(String key) { ... }
    default int getInt(String key) { ... }
}
```

## Using Templates

### From Config Module (YAML)
```java
// MenuConfig implements MenuTemplate
MenuBuilder.fromTemplate(config.shopMenu)
    .onInit((player, ctx) -> {
        // Add dynamic content
    })
    .open(player);
```

### Custom Implementation (e.g., Database)
```java
public class DatabaseMenuTemplate implements MenuTemplate {
    
    private final String menuId;
    private final Database database;

    public DatabaseMenuTemplate(String menuId, Database database) {
        this.menuId = menuId;
        this.database = database;
    }

    @Override
    public String getTitle() {
        return database.getMenuTitle(menuId);
    }

    @Override
    public List<String> getPattern() {
        return database.getMenuPattern(menuId);
    }

    @Override
    public Map<Character, ItemTemplate> getItems() {
        return database.getMenuItems(menuId);
    }
}

// Usage
MenuBuilder.fromTemplate(new DatabaseMenuTemplate("shop", database))
    .open(player);
```

---

# Action System

The Action System allows you to define item behaviors in configuration. Actions are executed when a player clicks on an item.

## Built-in Actions

### close

Closes the menu when clicked.

**YAML:**
```yaml
actions:
  close: true
```

**Java equivalent:**
```java
ctx.set('X', BukkitButton.of(Material.BARRIER)
    .onClick(e -> ctx.close()));
```

---

### sound

Plays a sound to the player.

**YAML:**
```yaml
actions:
  sound: "ui_button_click"
  sound-volume: 1.0  # Optional, default: 1.0
  sound-pitch: 1.0   # Optional, default: 1.0
```

**Examples:**
```yaml
# Simple click sound
actions:
  sound: "ui_button_click"

# Level up sound with custom pitch
actions:
  sound: "entity_player_levelup"
  sound-volume: 0.5
  sound-pitch: 1.5
```

**Java equivalent:**
```java
ctx.set('X', BukkitButton.of(Material.DIAMOND)
    .onClick(e -> {
        Player p = (Player) e.getWhoClicked();
        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
    }));
```

---

### command / commands

Executes command(s) as the player.

**YAML:**
```yaml
actions:
  command: "spawn"
  
# Or multiple commands
actions:
  commands:
    - "spawn"
    - "kit starter"
```

---

### console / console-commands

Executes command(s) as the console.

**YAML:**
```yaml
actions:
  console: "give %player% diamond 64"
  
# Or multiple commands
actions:
  console-commands:
    - "give %player% diamond 64"
    - "eco give %player% 1000"
```

---

### set-property

Sets properties in the menu context.

**YAML:**
```yaml
actions:
  set-property:
    selected: true
    count: 5
```

---

### next-page

Navigates to the next page in a paginated menu.

**YAML:**
```yaml
actions:
  next-page: true
```

**Java equivalent:**
```java
ctx.set('>', BukkitButton.of(Material.ARROW)
    .onClick(e -> ctx.pagination().next()));
```

---

### previous-page

Navigates to the previous page in a paginated menu.

**YAML:**
```yaml
actions:
  previous-page: true
```

**Java equivalent:**
```java
ctx.set('<', BukkitButton.of(Material.ARROW)
    .onClick(e -> ctx.pagination().previous()));
```

---

## Combining Actions
```yaml
items:
  'B':
    item:
      material: DIAMOND
      name: "&bBuy Diamond"
    actions:
      sound: "entity_player_levelup"
      console: "eco take %player% 100"
      close: true
```

---

## Custom Actions

### Global Actions

Register actions that work across all menus:
```java
@Override
public void onEnable() {
    MenuAPI.init(this);
    
    ActionRegistry registry = ActionRegistry.getInstance();

    // give-money action
    registry.register("give-money", (player, event, context, data) -> {
        int amount = data.getInt("give-money");
        if (amount > 0) {
            EconomyManager.give(player, amount);
            player.sendMessage("§aYou received $" + amount + "!");
        }
    });

    // open-menu action
    registry.register("open-menu", (player, event, context, data) -> {
        String menuId = data.getString("open-menu");
        if (menuId != null) {
            context.close();
            Bukkit.getScheduler().runTaskLater(plugin, 
                () -> menuManager.open(menuId, player), 1L);
        }
    });
}
```

**Usage in YAML:**
```yaml
items:
  'R':
    item:
      material: EMERALD
      name: "&aReward"
    actions:
      give-money: 500
      sound: "entity_player_levelup"
      close: true
```

# Config Module Usage

## Setting Up
```java
import eu.okaeri.configs.ConfigManager;
import pl.fejzu.menu.config.serializer.MenuSerdesPack;

public class MyPlugin extends JavaPlugin {

    private PluginConfig config;

    @Override
    public void onEnable() {
        MenuAPI.init(this);
        
        this.config = ConfigManager.create(PluginConfig.class, cfg -> {
            cfg.withConfigurer(new YamlSnakeYamlConfigurer());
            cfg.withSerdesPack(new MenuSerdesPack());
            cfg.withSerdesPack(new SerdesBukkit());
            cfg.withBindFile(new File(getDataFolder(), "config.yml"));
            cfg.saveDefaults();
            cfg.load(true);
        });
    }
}
```

## Configuration Class
```java
import eu.okaeri.configs.OkaeriConfig;
import pl.fejzu.menu.config.MenuConfig;
import pl.fejzu.menu.config.item.MenuItemConfig;

public class PluginConfig extends OkaeriConfig {

    public MenuConfig mainMenu = new MenuConfig(
        "&6Main Menu",
        List.of(
            "# # # # # # # # #",
            "# . S . K . W . #",
            "# # # # C # # # #"
        ),
        Map.of(
            '#', new MenuItemConfig(createGlassPane()),
            'S', new MenuItemConfig(
                createItem(Material.EMERALD, "&aShop"),
                Map.of("sound", "ui_button_click", "open-menu", "shop")
            ),
            'C', new MenuItemConfig(
                createItem(Material.BARRIER, "&cClose"),
                Map.of("sound", "ui_button_click", "close", true)
            )
        )
    );

    public MenuConfig shopMenu = new MenuConfig(
        "&aShop",
        List.of(
            "# # # # # # # # #",
            "# X X X X X X X #",
            "# X X X X X X X #",
            "# # < # B # > # #"
        ),
        Map.of(
            '#', new MenuItemConfig(createGlassPane()),
            '<', new MenuItemConfig(
                createItem(Material.ARROW, "&ePrevious"),
                Map.of("sound", "ui_button_click")
            ),
            '>', new MenuItemConfig(
                createItem(Material.ARROW, "&eNext"),
                Map.of("sound", "ui_button_click")
            ),
            'B', new MenuItemConfig(
                createItem(Material.DARK_OAK_DOOR, "&cBack"),
                Map.of("sound", "ui_button_click", "open-menu", "main")
            )
        )
    );
    
    private ItemStack createGlassPane() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name.replace("&", "§"));
        item.setItemMeta(meta);
        return item;
    }
}
```

## Generated YAML
```yaml
main-menu:
  title: "&6Main Menu"
  pattern:
    - "# # # # # # # # #"
    - "# . S . K . W . #"
    - "# # # # C # # # #"
  items:
    '#':
      item:
        material: GRAY_STAINED_GLASS_PANE
        name: " "
    'S':
      item:
        material: EMERALD
        name: "&aShop"
      actions:
        sound: "ui_button_click"
        open-menu: "shop"
    'C':
      item:
        material: BARRIER
        name: "&cClose"
      actions:
        sound: "ui_button_click"
        close: true
```

## Opening Menus

### Simple Menu
```java
public void openMainMenu(Player player) {
    MenuBuilder.fromTemplate(config.mainMenu)
        .open(player);
}
```

### Menu with Dynamic Content
```java
public void openShopMenu(Player player) {
    List<BukkitButton> shopItems = createShopButtons();

    MenuBuilder.fromTemplate(config.shopMenu)
        .onInit((p, ctx) -> {
            ctx.pagination()
                .items(shopItems)
                .itemsPerPage(14)
                .displayAt('X')
                .prevButton('<', new ItemStack(Material.ARROW), () -> openShopMenu(p))
                .nextButton('>', new ItemStack(Material.ARROW), () -> openShopMenu(p));
        })
        .open(player);
}
```

### Menu with Custom Actions
```java
public void openRewardMenu(Player player) {
    MenuBuilder.fromTemplate(config.rewardMenu)
        .action("claim-reward", (p, event, ctx, data) -> {
            String rewardId = data.getString("claim-reward");
            RewardManager.claim(p, rewardId);
        })
        .action("check-permission", (p, event, ctx, data) -> {
            String perm = data.getString("check-permission");
            if (!p.hasPermission(perm)) {
                event.setCancelled(true);
                p.sendMessage("§cNo permission!");
            }
        })
        .open(player);
}
```

---

## API Reference

### MenuBuilder Methods

| Method | Description |
|--------|-------------|
| `.create()` | Create new empty builder |
| `.fromTemplate(MenuTemplate)` | Create builder from template |
| `.title(String)` | Set menu title (supports color codes) |
| `.title(Component)` | Set menu title as Adventure Component |
| `.pattern(String...)` | Define menu layout |
| `.define(char, ItemStack)` | Pre-define static item for symbol |
| `.define(char, ItemStack, Consumer)` | Pre-define item with click handler |
| `.action(String, ConfigActionHandler)` | Register menu-specific action |
| `.closeable(boolean)` | Allow/prevent closing with ESC |
| `.cancelClick(boolean)` | Cancel inventory click events |
| `.updateEvery(int)` | Auto-update interval in ticks |
| `.shared()` | Enable shared inventory for multi-player |
| `.onInit(BiConsumer)` | Called when menu opens |
| `.onUpdate(BiConsumer)` | Called on each update tick |
| `.onClose(Consumer)` | Called when menu closes |
| `.listener(Class, Consumer)` | Add event handler (Click/Drag/Close) |
| `.build()` | Build BukkitMenu instance |
| `.open(Player)` | Build and open for player |

### BukkitMenu Methods

| Method | Description |
|--------|-------------|
| `.open(Player)` | Open menu for single player |
| `.open(Player...)` | Open menu for multiple players (shared mode) |
| `.close(Player)` | Close menu for specific player |
| `.closeAll()` | Close menu for all viewers |
| `.reopen(Player)` | Close and reopen for player |
| `.getInventory()` | Get Bukkit Inventory (shared mode) |
| `.setItem(int, ItemStack)` | Update slot directly (shared mode) |
| `.setItem(char, BukkitButton)` | Update by pattern key (shared mode) |
| `.setCloseable(boolean)` | Dynamically change closeable state |
| `.isCloseable()` | Check if menu can be closed |
| `.getViewers()` | Get players viewing menu |
| `.isShared()` | Check if shared mode enabled |

### MenuContext Methods

| Method | Description |
|--------|-------------|
| `.set(char, BukkitButton)` | Set button at symbol positions |
| `.set(char, ItemStack)` | Set item at symbol positions |
| `.set(char, ItemStack, Consumer)` | Set item with click handler |
| `.setAll(char, List<BukkitButton>)` | Set multiple buttons at symbol |
| `.fill(ItemStack)` | Fill all slots |
| `.fillBorders(ItemStack)` | Fill border slots |
| `.setProperty(String, Object)` | Store state |
| `.getProperty(String, T)` | Retrieve state with default |
| `.pagination()` | Get pagination context |
| `.getPlayer()` | Get viewing player |
| `.close()` | Close menu |

### PaginationContext Methods

| Method | Description |
|--------|-------------|
| `.items(List<BukkitButton>)` | Set paginated items |
| `.itemsPerPage(int)` | Set items per page |
| `.displayAt(char)` | Display items at symbol positions |
| `.next()` | Go to next page and refresh display |
| `.previous()` | Go to previous page and refresh display |
| `.goToPage(int)` | Jump to specific page (0-indexed) and refresh display |
| `.prevButton(char, ItemStack, Runnable)` | Add previous page button (legacy) |
| `.nextButton(char, ItemStack, Runnable)` | Add next page button (legacy) |
| `.getCurrentPage()` | Get current page (0-indexed) |
| `.getTotalPages()` | Get total page count |
| `.isFirst()` | Check if on first page |
| `.isLast()` | Check if on last page |

### Built-in Actions

| Action | Type | Description |
|--------|------|-------------|
| `close` | `boolean` | Close menu |
| `sound` | `string` | Play sound |
| `sound-volume` | `float` | Sound volume (0.0-1.0) |
| `sound-pitch` | `float` | Sound pitch (0.5-2.0) |
| `command` | `string` | Execute as player |
| `commands` | `list<string>` | Execute multiple as player |
| `console` | `string` | Execute as console |
| `console-commands` | `list<string>` | Execute multiple as console |
| `set-property` | `map` | Set context properties |
| `next-page` | `boolean` | Navigate to next page |
| `previous-page` | `boolean` | Navigate to previous page |

---

## Performance

ffMenu is built with performance in mind. Here are the key optimizations:

### Dirty Flag System
Only slots that have changed are updated in the inventory, reducing unnecessary operations by ~80-90%.

```java
// Before: Updates all 54 slots every tick
// After: Only updates changed slots (often 0-5 slots)
```

### Cached Resources
- **Player lookup**: Cached per menu instead of HashMap lookup on every set()
- **PaginationContext**: Reused instance instead of creating new objects
- **Display positions**: Stored once, reused on page changes

### Optimized Pagination
- Single loop instead of two (clear + set)
- 50% fewer operations when changing pages
- Automatic intelligent refresh

### Performance Metrics

**Typical menu with auto-update (20 ticks) and pagination:**

| Operation | Before Optimization | After Optimization | Improvement |
|-----------|--------------------|--------------------|-------------|
| Update task | 54 slot updates | 0-5 slot updates | **~90%** |
| Page change | 42 operations | 21 operations | **50%** |
| Player lookups | Per operation | Once per menu | **~95%** |
| Object allocations | Per call | Cached | **~80%** |

**Total performance gain: ~80-90% fewer operations for typical use cases**

### Best Practices

1. **Use auto-update wisely**: Only enable `.updateEvery()` when you need dynamic content
2. **Batch operations**: When possible, set multiple items before opening the menu
3. **Reuse buttons**: Create button instances once and reuse them
4. **Simple pagination**: Use the new `.next()`/`.previous()` API for cleaner code

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.