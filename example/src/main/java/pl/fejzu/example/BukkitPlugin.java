package pl.fejzu.example;

import dev.rollczi.litecommands.bukkit.LiteBukkitFactory;
import dev.rollczi.litecommands.bukkit.LiteBukkitMessages;
import dev.rollczi.litecommands.schematic.Schematic;
import eu.okaeri.configs.serdes.OkaeriSerdesPack;
import eu.okaeri.configs.serdes.commons.SerdesCommons;
import eu.okaeri.configs.yaml.bukkit.serdes.SerdesBukkit;
import lombok.Getter;
import lombok.NonNull;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import pl.fejzu.example.commands.TestCommand;
import pl.fejzu.example.configuration.PluginConfiguration;
import pl.fejzu.menu.bukkit.MenuAPI;
import pl.fejzu.menu.config.serializer.serdes.MenuSerdesPack;

import pl.ffcode.platform.bukkit.PlatformBukkit;
import pl.ffcode.platform.bukkit.config.PlatformConfig;
import pl.ffcode.platform.bukkit.config.create.ConfigurationCreator;
import pl.ffcode.platform.bukkit.config.resolvers.ConfigurationResolver;
import pl.ffcode.platform.module.ModuleService;

public final class BukkitPlugin extends PlatformBukkit implements PlatformConfig
{

    @Getter private static BukkitPlugin instance;
    @Getter private final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    @Override
    public void load(@NonNull ModuleService moduleService) {
        instance = this;
    }

    @Override
    public void enable(@NonNull ModuleService moduleService) {
        moduleService.setDebug(false);
        MenuAPI.init(this);

        moduleService.registerModule(ConfigurationCreator.class);
        moduleService.registerResolver(ConfigurationResolver.class);
        moduleService.registerModule(PluginConfiguration.class);

        LiteBukkitFactory.builder()
                .settings(settings -> settings
                        .fallbackPrefix("ffmenu-test")
                        .nativePermissions(false)
                )
                .commands(
                        createInstance(TestCommand.class)
                )
                .message(LiteBukkitMessages.PLAYER_ONLY, "<color:#ff2e3c>☹ <red>Komenda tylko z poziomu gracza!")
                .message(LiteBukkitMessages.PLAYER_NOT_FOUND, input -> "<color:#ff2e3c>☹ <red>Gracz <dark_red>" + input + " <red>noe został odnaleziony!")
                .missingPermission((invocation, missingPermissions, chain) -> {
                    String permissions = missingPermissions.asJoinedText();
                    CommandSender sender = invocation.sender();
                    sender.sendMessage(MINI_MESSAGE.deserialize("<color:#ff2e3c>☹ <red>Nie posiadasz do tego permisji! <dark-gray>(<dark_red>" + permissions + "<dark_gray>)"));
                })
                .invalidUsage((invocation, result, chain) -> {
                    CommandSender sender = invocation.sender();
                    Schematic schematic = result.getSchematic();

                    if (schematic.isOnlyFirst()) {
                        sender.sendMessage(MINI_MESSAGE.deserialize("<color:#ff2e3c>☹ <red>Poprawne użycie: <dark_red>" + schematic.first()));
                        return;
                    }
                    sender.sendMessage(MINI_MESSAGE.deserialize("<color:#ff2e3c>☹ <red>Poprawne użycie:"));
                    for (String scheme : schematic.all()) {
                        sender.sendMessage(MINI_MESSAGE.deserialize("<dark_gray>- <dark_red>" + scheme));
                    }
                })

                .build();
    }

    @Override
    public void disable() {
        MenuAPI.disable();
    }

    @Override
    public OkaeriSerdesPack getConfigSerdesPack() {
        return registry -> {
            registry.register(new SerdesCommons());
            registry.register(new SerdesBukkit());
            registry.register(new MenuSerdesPack());
        };
    }
}