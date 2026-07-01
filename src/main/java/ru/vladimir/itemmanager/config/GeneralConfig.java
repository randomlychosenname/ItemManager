package ru.vladimir.itemmanager.config;

import org.jetbrains.annotations.NotNull;
import ru.vladimir.itemmanager.utils.Logger;

public record GeneralConfig(
    @NotNull Logger.LogLevel loggingLevel
) {}
