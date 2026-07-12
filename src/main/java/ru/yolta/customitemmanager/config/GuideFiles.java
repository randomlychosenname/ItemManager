package ru.yolta.customitemmanager.config;

import org.jetbrains.annotations.NotNull;
import ru.yolta.customitemmanager.CustomItemManager;
import ru.yolta.customitemmanager.utils.Logger;

import java.io.File;

final class GuideFiles {

    private static final String GUIDES_FOLDER_NAME = "guides";
    private static final String ITEMS_GUIDE_FILE_NAME = GUIDES_FOLDER_NAME + "/items.md";
    private static final String PLACEHOLDERS_GUIDE_FILE_NAME = GUIDES_FOLDER_NAME + "/placeholders.md";

    private GuideFiles() {}

    static void overwriteGuides(@NotNull CustomItemManager plugin, @NotNull ConfigManager manager) {
        final File folder = new File(plugin.getDataFolder(), GUIDES_FOLDER_NAME);

        if (!folder.exists()) {
            final boolean result = folder.mkdirs();

            if (!result) {
                Logger.error(GuideFiles.class, "Failed to create a folder for guides.");
                return;
            }
        }

        manager.saveFile(ITEMS_GUIDE_FILE_NAME);
        manager.saveFile(PLACEHOLDERS_GUIDE_FILE_NAME);
    }
}
