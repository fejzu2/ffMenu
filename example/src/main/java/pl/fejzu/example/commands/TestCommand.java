package pl.fejzu.example.commands;

import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import eu.okaeri.injector.annotation.Inject;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.fejzu.example.configuration.PluginConfiguration;
import pl.fejzu.menu.bukkit.builder.MenuBuilder;
import pl.fejzu.menu.bukkit.button.BukkitButton;

import java.util.ArrayList;
import java.util.List;

@Command(name = "menu")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class TestCommand {

    private final PluginConfiguration pluginConfiguration;

    @Execute(name = "test")
    public void testMenu(@Context Player player) {
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

    @Execute(name = "test-config")
    public void testConfigMenu(@Context Player player) {
        MenuBuilder.fromTemplate(pluginConfiguration.mainMenu)
                .open(player);
    }

    @Execute(name = "test-pagination-config")
    public void testPaginationConfigMenu(@Context Player player) {
        List<BukkitButton> items = createExampleItems();

        MenuBuilder.fromTemplate(pluginConfiguration.paginationMenu)
                .onInit((p, ctx) -> {
                    ctx.pagination()
                            .items(items)
                            .itemsPerPage(21)
                            .displayAt('X');
                })
                .open(player);
    }

    @Execute(name = "test-pagination")
    public void testPaginationMenu(@Context Player player) {
        List<BukkitButton> items = createExampleItems();

        MenuBuilder.create()
                .title("<yellow>Pagination Example")
                .pattern(
                        "# # # # # # # # #",
                        "# X X X X X X X #",
                        "# X X X X X X X #",
                        "# X X X X X X X #",
                        "# # < # I # > # #"
                )
                .define('#', createGlassPane())
                .onInit((p, ctx) -> {
                    ctx.pagination()
                            .items(items)
                            .itemsPerPage(21)
                            .displayAt('X');

                    ctx.set('<', BukkitButton.of(createNavItem("&ePrevious Page"))
                            .onClick(e -> {
                                ctx.pagination().previous();
                            }));

                    ctx.set('>', BukkitButton.of(createNavItem("&eNext Page"))
                            .onClick(e -> {
                                ctx.pagination().next();
                            }));

                    ctx.set('I', BukkitButton.of(createInfoItem(
                            ctx.pagination().getCurrentPage() + 1,
                            ctx.pagination().getTotalPages()
                    )));
                })
                .open(player);
    }

    private List<BukkitButton> createExampleItems() {
        List<BukkitButton> buttons = new ArrayList<>();
        Material[] materials = {
                Material.DIAMOND, Material.EMERALD, Material.GOLD_INGOT,
                Material.IRON_INGOT, Material.COAL, Material.REDSTONE,
                Material.LAPIS_LAZULI, Material.AMETHYST_SHARD
        };

        for (int i = 1; i <= 50; i++) {
            Material mat = materials[i % materials.length];
            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§aItem #" + i);
            meta.setLore(List.of("§7Click to select"));
            item.setItemMeta(meta);

            int index = i;
            buttons.add(BukkitButton.of(item)
                    .onClick(e -> {
                        Player p = (Player) e.getWhoClicked();
                        p.sendMessage("§aSelected item #" + index);
                    }));
        }
        return buttons;
    }

    private ItemStack createGlassPane() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createNavItem(String name) {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name.replace("&", "§"));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createInfoItem(int current, int total) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§ePage " + current + "/" + total);
        item.setItemMeta(meta);
        return item;
    }
}