package dev.hypersystems.hyperverse.command.world;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import dev.hypersystems.hyperverse.HyperVerse;
import dev.hypersystems.hyperverse.command.HvCommand;
import dev.hypersystems.hyperverse.util.PermissionUtil;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Loads a world from disk.
 * <p>
 * Usage: /world load <name>
 * Permission: hyperverse.world.load
 */
public class WorldLoadCommand extends HvCommand {

    private static final String PERMISSION = "hyperverse.world.load";

    private final HyperVerse hyperVerse;
    private final RequiredArg<String> nameArg;

    /**
     * Creates a new WorldLoadCommand.
     *
     * @param hyperVerse the HyperVerse instance
     */
    @SuppressWarnings("this-escape")
    public WorldLoadCommand(@NotNull HyperVerse hyperVerse) {
        super("load", "Load a world from disk");
        this.hyperVerse = hyperVerse;
        this.nameArg = withRequiredArg("name", "World name", ArgTypes.STRING);
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        // Permission check
        if (!PermissionUtil.hasPermission(ctx, PERMISSION)) {
            ctx.sender().sendMessage(error("You don't have permission to load worlds."));
            return CompletableFuture.completedFuture(null);
        }

        String worldName = ctx.get(nameArg);
        if (worldName == null || worldName.isEmpty()) {
            ctx.sender().sendMessage(error("Please specify a world name."));
            return CompletableFuture.completedFuture(null);
        }

        // Check if world is already loaded
        if (hyperVerse.getWorldManager().getWorld(worldName) != null) {
            ctx.sender().sendMessage(warning("World '" + worldName + "' is already loaded."));
            return CompletableFuture.completedFuture(null);
        }

        // Check if world exists on disk
        if (!hyperVerse.getWorldManager().isWorldLoadable(worldName)) {
            ctx.sender().sendMessage(error("World '" + worldName + "' does not exist on disk."));
            return CompletableFuture.completedFuture(null);
        }

        ctx.sender().sendMessage(info("Loading world '" + worldName + "'..."));

        // Load world asynchronously
        hyperVerse.getWorldManager().loadWorld(worldName)
            .thenAccept(world -> {
                if (world != null) {
                    ctx.sender().sendMessage(success("World '" + worldName + "' loaded successfully!"));
                } else {
                    ctx.sender().sendMessage(error("Failed to load world '" + worldName + "'."));
                }
            })
            .exceptionally(e -> {
                ctx.sender().sendMessage(error("Error: " + e.getCause().getMessage()));
                hyperVerse.getLogger().severe("Failed to load world: " + e.getMessage());
                return null;
            });

        return CompletableFuture.completedFuture(null);
    }
}
