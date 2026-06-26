package ru.vladimir.itemmanager.config;

import org.jetbrains.annotations.NotNull;

public record Messages(
    @NotNull String noPermission,
    @NotNull String description,
    @NotNull String invalidSubCommand,
    @NotNull String playerOnlyCommand,
    @NotNull String invalidArguments,
    @NotNull String mustHoldItem,
    @NotNull String itemRegistered,
    @NotNull String itemAlreadyExists,
    @NotNull String playerNotFound,
    @NotNull String itemNotFound,
    @NotNull String itemGiven,
    @NotNull String invalidAmount,
    @NotNull String itemList,
    @NotNull String configReloaded,
    @NotNull String itemUnregistered
) {}
