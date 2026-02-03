package dev.hypersystems.hyperverse.command.world;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.universe.world.World;
import dev.hypersystems.hyperverse.HyperVerse;
import dev.hypersystems.hyperverse.command.HvCommand;
import dev.hypersystems.hyperverse.util.PermissionUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * Lists all loaded worlds.
 * <p>
 * Usage: /world list
 * Permission: hyperverse.world.list
 */
public class WorldListCommand extends HvCommand {

    private static final String PERMISSION = "hyperverse.world.list";

    private final HyperVerse hyperVerse;

    /**
     * Creates a new WorldListCommand.
     *
     * @param hyperVerse the HyperVerse instance
     */
    public WorldListCommand(@NotNull HyperVerse hyperVerse) {
        super("list", "List all loaded worlds");
        this.hyperVerse = hyperVerse;
        addAliases("ls", "l");
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        // Permission check
        if (!PermissionUtil.hasPermission(ctx, PERMISSION)) {
            ctx.sender().sendMessage(error("You don't have permission to use this command."));
            return CompletableFuture.completedFuture(null);
        }

        Collection<World> worlds = hyperVerse.getWorldManager().getWorlds();

        if (worlds.isEmpty()) {
            ctx.sender().sendMessage(warning("No worlds are currently loaded."));
            return CompletableFuture.completedFuture(null);
        }

        // Header
        ctx.sender().sendMessage(Message.raw(""));
        ctx.sender().sendMessage(
            Message.raw("=== ").color(COLOR_INFO)
                .insert(Message.raw("Loaded Worlds").color(COLOR_PRIMARY).bold(true))
                .insert(Message.raw(" (" + worlds.size() + ") ===").color(COLOR_INFO))
        );
        ctx.sender().sendMessage(Message.raw(""));

        // List each world
        World defaultWorld = hyperVerse.getWorldManager().getDefaultWorld();

        for (World world : worlds) {
            String worldName = world.getName();
            int playerCount = world.getPlayerCount();
            boolean isDefault = world.equals(defaultWorld);

            Message worldLine = Message.raw("  ");

            // World name with default indicator
            if (isDefault) {
                worldLine = worldLine.insert(Message.raw("* ").color(COLOR_ACCENT));
            }
            worldLine = worldLine.insert(Message.raw(worldName).color(COLOR_SUCCESS));

            // Player count
            worldLine = worldLine.insert(
                Message.raw(" - ").color(COLOR_INFO)
                    .insert(Message.raw(String.valueOf(playerCount)).color(COLOR_WHITE))
                    .insert(Message.raw(" player" + (playerCount == 1 ? "" : "s")).color(COLOR_INFO))
            );

            ctx.sender().sendMessage(worldLine);
        }

        ctx.sender().sendMessage(Message.raw(""));
        ctx.sender().sendMessage(
            Message.raw("* = default world").color(COLOR_INFO).italic(true)
        );

        return CompletableFuture.completedFuture(null);
    }
}
