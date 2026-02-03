package dev.hypersystems.hyperverse.command;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import dev.hypersystems.hyperverse.HyperVerse;
import dev.hypersystems.hyperverse.command.world.*;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.concurrent.CompletableFuture;

/**
 * Parent command for all world management commands.
 * <p>
 * Usage: /world <subcommand> [args]
 * <p>
 * Subcommands:
 * <ul>
 *   <li>create - Create a new world</li>
 *   <li>delete - Delete a world</li>
 *   <li>list - List all worlds</li>
 *   <li>info - View world information</li>
 *   <li>tp - Teleport to a world</li>
 *   <li>load - Load an unloaded world</li>
 *   <li>unload - Unload a world</li>
 * </ul>
 */
public class WorldCommand extends AbstractCommand {

    private static final Color COLOR_PRIMARY = new Color(0x00FFFF);
    private static final Color COLOR_INFO = new Color(0xAAAAAA);
    private static final Color COLOR_SUCCESS = new Color(0x55FF55);
    private static final Color COLOR_ACCENT = new Color(0xFFAA00);

    private final HyperVerse hyperVerse;

    /**
     * Creates a new WorldCommand.
     *
     * @param hyperVerse the HyperVerse instance
     */
    @SuppressWarnings("this-escape")
    public WorldCommand(@NotNull HyperVerse hyperVerse) {
        super("world", "World management commands");
        this.hyperVerse = hyperVerse;

        // Register subcommands
        addSubCommand(new WorldCreateCommand(hyperVerse));
        addSubCommand(new WorldDeleteCommand(hyperVerse));
        addSubCommand(new WorldListCommand(hyperVerse));
        addSubCommand(new WorldInfoCommand(hyperVerse));
        addSubCommand(new WorldTeleportCommand(hyperVerse));
        addSubCommand(new WorldLoadCommand(hyperVerse));
        addSubCommand(new WorldUnloadCommand(hyperVerse));

        // Add aliases
        addAliases("w", "worlds");
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        // Show help when no subcommand is provided
        showHelp(ctx);
        return CompletableFuture.completedFuture(null);
    }

    private void showHelp(CommandContext ctx) {
        ctx.sender().sendMessage(Message.raw(""));
        ctx.sender().sendMessage(
            Message.raw("=== ").color(COLOR_INFO)
                .insert(Message.raw("HyperVerse").color(COLOR_PRIMARY).bold(true))
                .insert(Message.raw(" World Commands ===").color(COLOR_INFO))
        );
        ctx.sender().sendMessage(Message.raw(""));

        // List all subcommands with descriptions
        sendCommand(ctx, "/world create <name> [template]", "Create a new world");
        sendCommand(ctx, "/world delete <name>", "Delete a world");
        sendCommand(ctx, "/world list", "List all worlds");
        sendCommand(ctx, "/world info <name>", "View world information");
        sendCommand(ctx, "/world tp <name> [player]", "Teleport to a world");
        sendCommand(ctx, "/world load <name>", "Load an unloaded world");
        sendCommand(ctx, "/world unload <name>", "Unload a world");

        ctx.sender().sendMessage(Message.raw(""));
        ctx.sender().sendMessage(
            Message.raw("Templates: ").color(COLOR_INFO)
                .insert(Message.raw("void").color(COLOR_ACCENT))
                .insert(Message.raw(", ").color(COLOR_INFO))
                .insert(Message.raw("flat").color(COLOR_ACCENT))
        );
        ctx.sender().sendMessage(Message.raw(""));
    }

    private void sendCommand(CommandContext ctx, String command, String description) {
        ctx.sender().sendMessage(
            Message.raw(command).color(COLOR_SUCCESS)
                .insert(Message.raw(" - " + description).color(COLOR_INFO))
        );
    }
}
