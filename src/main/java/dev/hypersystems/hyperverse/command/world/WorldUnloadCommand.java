package dev.hypersystems.hyperverse.command.world;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.universe.world.World;
import dev.hypersystems.hyperverse.HyperVerse;
import dev.hypersystems.hyperverse.command.HvCommand;
import dev.hypersystems.hyperverse.util.PermissionUtil;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Unloads a world from memory.
 * <p>
 * Usage: /world unload <name>
 * Permission: hyperverse.world.unload
 */
public class WorldUnloadCommand extends HvCommand {

    private static final String PERMISSION = "hyperverse.world.unload";

    private final HyperVerse hyperVerse;
    private final RequiredArg<String> nameArg;

    /**
     * Creates a new WorldUnloadCommand.
     *
     * @param hyperVerse the HyperVerse instance
     */
    @SuppressWarnings("this-escape")
    public WorldUnloadCommand(@NotNull HyperVerse hyperVerse) {
        super("unload", "Unload a world from memory");
        this.hyperVerse = hyperVerse;
        this.nameArg = withRequiredArg("name", "World name", ArgTypes.STRING);
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        // Permission check
        if (!PermissionUtil.hasPermission(ctx, PERMISSION)) {
            ctx.sender().sendMessage(error("You don't have permission to unload worlds."));
            return CompletableFuture.completedFuture(null);
        }

        String worldName = ctx.get(nameArg);
        if (worldName == null || worldName.isEmpty()) {
            ctx.sender().sendMessage(error("Please specify a world name."));
            return CompletableFuture.completedFuture(null);
        }

        // Check if world is loaded
        World world = hyperVerse.getWorldManager().getWorld(worldName);
        if (world == null) {
            ctx.sender().sendMessage(error("World '" + worldName + "' is not loaded."));
            return CompletableFuture.completedFuture(null);
        }

        // Check if it's the default world
        if (world.equals(hyperVerse.getWorldManager().getDefaultWorld())) {
            ctx.sender().sendMessage(error("Cannot unload the default world."));
            return CompletableFuture.completedFuture(null);
        }

        // Check if world has players
        if (world.getPlayerCount() > 0) {
            ctx.sender().sendMessage(error("Cannot unload world with players in it."));
            ctx.sender().sendMessage(info("Teleport all players out first using /world tp <other-world>"));
            return CompletableFuture.completedFuture(null);
        }

        ctx.sender().sendMessage(info("Unloading world '" + worldName + "'..."));

        // Unload world asynchronously
        hyperVerse.getWorldManager().unloadWorld(worldName)
            .thenRun(() -> {
                ctx.sender().sendMessage(success("World '" + worldName + "' unloaded successfully."));
                ctx.sender().sendMessage(info("Use /world load " + worldName + " to reload it."));
            })
            .exceptionally(e -> {
                ctx.sender().sendMessage(error("Error: " + e.getCause().getMessage()));
                hyperVerse.getLogger().severe("Failed to unload world: " + e.getMessage());
                return null;
            });

        return CompletableFuture.completedFuture(null);
    }
}
