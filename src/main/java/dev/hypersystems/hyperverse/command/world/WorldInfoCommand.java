package dev.hypersystems.hyperverse.command.world;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import dev.hypersystems.hyperverse.HyperVerse;
import dev.hypersystems.hyperverse.command.HvCommand;
import dev.hypersystems.hyperverse.util.PermissionUtil;
import dev.hypersystems.hyperverse.world.WorldManager;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Displays detailed information about a world.
 * <p>
 * Usage: /world info <name>
 * Permission: hyperverse.world.info
 */
public class WorldInfoCommand extends HvCommand {

    private static final String PERMISSION = "hyperverse.world.info";

    private final HyperVerse hyperVerse;
    private final RequiredArg<String> nameArg;

    /**
     * Creates a new WorldInfoCommand.
     *
     * @param hyperVerse the HyperVerse instance
     */
    @SuppressWarnings("this-escape")
    public WorldInfoCommand(@NotNull HyperVerse hyperVerse) {
        super("info", "View detailed world information");
        this.hyperVerse = hyperVerse;
        this.nameArg = withRequiredArg("name", "World name", ArgTypes.STRING);
        addAliases("i");
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        // Permission check
        if (!PermissionUtil.hasPermission(ctx, PERMISSION)) {
            ctx.sender().sendMessage(error("You don't have permission to use this command."));
            return CompletableFuture.completedFuture(null);
        }

        String worldName = ctx.get(nameArg);
        if (worldName == null || worldName.isEmpty()) {
            ctx.sender().sendMessage(error("Please specify a world name."));
            return CompletableFuture.completedFuture(null);
        }

        World world = hyperVerse.getWorldManager().getWorld(worldName);
        if (world == null) {
            ctx.sender().sendMessage(error("World '" + worldName + "' not found."));
            return CompletableFuture.completedFuture(null);
        }

        // Get world config
        WorldConfig config = world.getWorldConfig();
        WorldManager.WorldMetadata metadata = hyperVerse.getWorldManager().getWorldMetadata(worldName);
        boolean isDefault = world.equals(hyperVerse.getWorldManager().getDefaultWorld());

        // Header
        ctx.sender().sendMessage(Message.raw(""));
        ctx.sender().sendMessage(
            Message.raw("=== World: ").color(COLOR_INFO)
                .insert(Message.raw(worldName).color(COLOR_PRIMARY).bold(true))
                .insert(Message.raw(" ===").color(COLOR_INFO))
        );
        ctx.sender().sendMessage(Message.raw(""));

        // Basic info
        sendInfo(ctx, "Status", "Loaded");
        sendInfo(ctx, "Default", isDefault ? "Yes" : "No");
        sendInfo(ctx, "Players", String.valueOf(world.getPlayerCount()));

        // Config info (if available)
        if (config != null) {
            sendInfo(ctx, "Game Mode", String.valueOf(config.getGameMode()));
            sendInfo(ctx, "PvP Enabled", config.isPvpEnabled() ? "Yes" : "No");

            // Time and weather (if methods are available)
            try {
                sendInfo(ctx, "Game Time", String.valueOf(config.getGameTime()));
            } catch (Exception ignored) {
                // Method might not exist
            }

            try {
                var weather = config.getForcedWeather();
                sendInfo(ctx, "Weather", weather != null ? weather.toString() : "Natural");
            } catch (Exception ignored) {
                // Method might not exist
            }
        }

        // Metadata info
        if (metadata != null) {
            sendInfo(ctx, "Template", metadata.getTemplate());
        }

        ctx.sender().sendMessage(Message.raw(""));

        return CompletableFuture.completedFuture(null);
    }

    private void sendInfo(CommandContext ctx, String label, String value) {
        ctx.sender().sendMessage(
            Message.raw("  " + label + ": ").color(COLOR_INFO)
                .insert(Message.raw(value).color(COLOR_WHITE))
        );
    }
}
