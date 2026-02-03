package dev.hypersystems.hyperverse.command.world;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.universe.world.World;
import dev.hypersystems.hyperverse.HyperVerse;
import dev.hypersystems.hyperverse.command.HvCommand;
import dev.hypersystems.hyperverse.util.PermissionUtil;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Deletes a world.
 * <p>
 * Usage: /world delete <name> [--confirm]
 * Permission: hyperverse.world.delete
 */
public class WorldDeleteCommand extends HvCommand {

    private static final String PERMISSION = "hyperverse.world.delete";

    private final HyperVerse hyperVerse;
    private final RequiredArg<String> nameArg;
    private final OptionalArg<String> confirmArg;

    /**
     * Creates a new WorldDeleteCommand.
     *
     * @param hyperVerse the HyperVerse instance
     */
    @SuppressWarnings("this-escape")
    public WorldDeleteCommand(@NotNull HyperVerse hyperVerse) {
        super("delete", "Delete a world permanently");
        this.hyperVerse = hyperVerse;
        this.nameArg = withRequiredArg("name", "World name", ArgTypes.STRING);
        this.confirmArg = withOptionalArg("confirm", "Use --confirm to delete", ArgTypes.STRING);
        addAliases("remove", "del", "rm");
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        // Permission check
        if (!PermissionUtil.hasPermission(ctx, PERMISSION)) {
            ctx.sender().sendMessage(error("You don't have permission to delete worlds."));
            return CompletableFuture.completedFuture(null);
        }

        String worldName = ctx.get(nameArg);
        if (worldName == null || worldName.isEmpty()) {
            ctx.sender().sendMessage(error("Please specify a world name."));
            return CompletableFuture.completedFuture(null);
        }

        // Check if world exists
        World world = hyperVerse.getWorldManager().getWorld(worldName);
        if (world == null) {
            ctx.sender().sendMessage(error("World '" + worldName + "' not found."));
            return CompletableFuture.completedFuture(null);
        }

        // Check if it's the default world
        if (world.equals(hyperVerse.getWorldManager().getDefaultWorld())) {
            ctx.sender().sendMessage(error("Cannot delete the default world."));
            return CompletableFuture.completedFuture(null);
        }

        // Check if world has players
        if (world.getPlayerCount() > 0) {
            ctx.sender().sendMessage(error("Cannot delete world with players in it."));
            ctx.sender().sendMessage(info("Teleport all players out first using /world tp <other-world>"));
            return CompletableFuture.completedFuture(null);
        }

        // Check for --confirm flag
        String confirm = ctx.get(confirmArg);
        if (!"--confirm".equalsIgnoreCase(confirm)) {
            ctx.sender().sendMessage(warning("This will PERMANENTLY delete world '" + worldName + "'!"));
            ctx.sender().sendMessage(info("Run /world delete " + worldName + " --confirm to proceed."));
            return CompletableFuture.completedFuture(null);
        }

        ctx.sender().sendMessage(info("Deleting world '" + worldName + "'..."));

        // Delete world asynchronously
        hyperVerse.getWorldManager().deleteWorld(worldName)
            .thenRun(() -> {
                ctx.sender().sendMessage(success("World '" + worldName + "' deleted successfully."));
            })
            .exceptionally(e -> {
                ctx.sender().sendMessage(error("Error: " + e.getCause().getMessage()));
                hyperVerse.getLogger().severe("Failed to delete world: " + e.getMessage());
                return null;
            });

        return CompletableFuture.completedFuture(null);
    }
}
