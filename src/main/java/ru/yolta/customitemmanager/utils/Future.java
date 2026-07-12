package ru.yolta.customitemmanager.utils;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import ru.yolta.customitemmanager.CustomItemManager;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class Future {
    private static CustomItemManager plugin;

    private Future() {}

    public static void setPlugin(@NotNull CustomItemManager plugin) {
        Future.plugin = plugin;
    }

    public static <T> void runTask(@NotNull CompletableFuture<T> task, @NotNull Consumer<T> callback) {
        task.thenAcceptAsync(callback, runnable -> Bukkit.getScheduler().runTask(plugin, runnable));
    }

    public static <T> CompletableFuture<T> runAsyncTask(Supplier<T> task) {
        final CompletableFuture<T> future = new CompletableFuture<>();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                future.complete(task.get());
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });

        return future;
    }

    public static <T> CompletableFuture<T> runPartiallyAsyncTask(Consumer<T> syncTask, Supplier<T> asyncTask) {
        final CompletableFuture<T> future = new CompletableFuture<>();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                final T result = asyncTask.get();
                future.complete(result);
                Bukkit.getScheduler().runTask(plugin, () -> syncTask.accept(result));
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });

        return future;
    }
}
