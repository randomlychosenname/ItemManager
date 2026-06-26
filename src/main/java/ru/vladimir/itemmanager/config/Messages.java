package ru.vladimir.itemmanager.config;

import org.jetbrains.annotations.NotNull;

public record Messages(
    @NotNull String noPermission,
    @NotNull String pluginDescription,
    @NotNull String invalidCommand,
    @NotNull String playerOnlyCommand,
    @NotNull String invalidArguments,
    @NotNull String mustHoldItem,
    @NotNull String itemRegistered,
    @NotNull String itemAlreadyRegistered,
    @NotNull String playerNotFound,
    @NotNull String itemNotFound,
    @NotNull String itemGiven,
    @NotNull String invalidAmount,
    @NotNull String itemList,
    @NotNull String pluginReloaded,
    @NotNull String itemUnregistered,
    @NotNull String pluginHelp
) {}
