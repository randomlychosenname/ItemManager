package ru.yolta.itemmanager.config;

import org.jetbrains.annotations.NotNull;
import ru.yolta.itemmanager.utils.Logger;

public record GeneralConfig(
    @NotNull Logger.LogLevel loggingLevel
) {}
