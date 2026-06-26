package ru.vladimir.itemmanager.command.list;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import ru.vladimir.itemmanager.ItemManager;
import ru.vladimir.itemmanager.command.SubCommand;
import ru.vladimir.itemmanager.config.ConfigManager;
import ru.vladimir.itemmanager.utils.Messager;

public class ListItems implements SubCommand {

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if (args.length != 1) {
            Messager.sendMessage(sender, ConfigManager.getInstance().getMessages().invalidArguments(), Map.of("USAGE", "/itemmanager list"));
            return;
        }

        final Set<String> itemIds = ItemManager.getApi().getAllCustomItemIds();

        if (itemIds.isEmpty()) {
            Messager.sendMessage(sender, ConfigManager.getInstance().getMessages().itemList(), Map.of("ITEMS", "No custom items registered."));
            return;
        }

        final String itemList = String.join(", ", itemIds);
        
        Messager.sendMessage(sender, ConfigManager.getInstance().getMessages().itemList(), Map.of("ITEMS", itemList));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        return List.of();
    }
}
