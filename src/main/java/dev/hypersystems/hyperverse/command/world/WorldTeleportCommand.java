package dev.hypersystems.hyperverse.command.world;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.spawn.ISpawnProvider;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hypersystems.hyperverse.HyperVerse;
import dev.hypersystems.hyperverse.util.PermissionUtil;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;

/**
 * Teleports a player to a world.
 * <p>
 * Usage: /world tp <name> [player]
 * Permission: hyperverse.world.teleport (self)
 * Permission: hyperverse.world.teleport.other (others)
 */
public class WorldTeleportCommand extends AbstractPlayerCommand {

    private static final String PERMISSION_SELF = "hyperverse.world.teleport";
    private static final String PERMISSION_OTHER = "hyperverse.world.teleport.other";

    // Colors
    protected static final Color COLOR_PRIMARY = new Color(0x00FFFF);
    protected static final Color COLOR_SUCCESS = new Color(0x55FF55);
    protected static final Color COLOR_ERROR = new Color(0xFF5555);
    protected static final Color COLOR_WARNING = new Color(0xFFFF55);
    protected static final Color COLOR_INFO = new Color(0xAAAAAA);

    private final HyperVerse hyperVerse;
    private final RequiredArg<String> nameArg;
    private final OptionalArg<String> playerArg;

    /**
     * Creates a new WorldTeleportCommand.
     *
     * @param hyperVerse the HyperVerse instance
     */
    @SuppressWarnings("this-escape")
    public WorldTeleportCommand(@NotNull HyperVerse hyperVerse) {
        super("tp", "Teleport to a world");
        this.hyperVerse = hyperVerse;
        this.nameArg = withRequiredArg("name", "World name", ArgTypes.STRING);
        this.playerArg = withOptionalArg("player", "Target player", ArgTypes.STRING);
        addAliases("teleport", "goto", "go");
    }

    @Override
    protected void execute(@NotNull CommandContext ctx,
                          @NotNull Store<EntityStore> store,
                          @NotNull Ref<EntityStore> ref,
                          @NotNull PlayerRef playerRef,
                          @NotNull World currentWorld) {

        // Get arguments
        String worldName = ctx.get(nameArg);
        String targetPlayer = ctx.get(playerArg);

        if (worldName == null || worldName.isEmpty()) {
            ctx.sender().sendMessage(error("Please specify a world name."));
            return;
        }

        // Determine if teleporting self or other
        boolean teleportingSelf = targetPlayer == null || targetPlayer.isEmpty();

        // Permission check
        String requiredPermission = teleportingSelf ? PERMISSION_SELF : PERMISSION_OTHER;
        if (!PermissionUtil.hasPermission(playerRef.getUuid(), requiredPermission)) {
            ctx.sender().sendMessage(error("You don't have permission to teleport" +
                (teleportingSelf ? "." : " other players.")));
            return;
        }

        // Get target world
        World targetWorld = hyperVerse.getWorldManager().getWorld(worldName);
        if (targetWorld == null) {
            ctx.sender().sendMessage(error("World '" + worldName + "' not found."));
            return;
        }

        if (teleportingSelf) {
            // Teleport self
            teleportPlayer(ctx, store, ref, playerRef, targetWorld, currentWorld);
        } else {
            // Find target player and teleport them
            ctx.sender().sendMessage(error("Teleporting other players is not yet implemented."));
            // TODO: Implement player lookup and teleport
        }
    }

    private void teleportPlayer(CommandContext ctx, Store<EntityStore> store, Ref<EntityStore> ref,
                                 PlayerRef playerRef, World targetWorld, World currentWorld) {
        // Check if already in target world
        if (currentWorld.equals(targetWorld)) {
            ctx.sender().sendMessage(warning("You are already in world '" + targetWorld.getName() + "'."));
            return;
        }

        // Get spawn transform from WorldConfig's spawn provider
        Transform spawnTransform = getSpawnTransform(targetWorld, playerRef);

        ctx.sender().sendMessage(info("Teleporting to world '" + targetWorld.getName() + "'..."));

        // For cross-world teleport, we need to:
        // 1. Remove player from current world
        // 2. Add player to target world at spawn position
        // Using World.addPlayer() handles this properly
        currentWorld.execute(() -> {
            // Remove from current world's store
            Holder<EntityStore> holder = playerRef.removeFromStore();

            // Add to target world with spawn transform
            // Note: addPlayer is async and handles the teleport properly
            targetWorld.addPlayer(playerRef, spawnTransform)
                .thenAccept(result -> {
                    // Send success message AFTER teleport completes
                    ctx.sender().sendMessage(success("Teleported to world '" + targetWorld.getName() + "'!"));
                })
                .exceptionally(e -> {
                    ctx.sender().sendMessage(error("Failed to teleport: " + e.getMessage()));
                    hyperVerse.getLogger().warning("Teleport failed for " + playerRef.getUsername() + ": " + e.getMessage());
                    return null;
                });
        });
    }

    /**
     * Gets the spawn transform for a world.
     * Uses the WorldConfig spawn provider if available, otherwise falls back to default.
     */
    private Transform getSpawnTransform(World world, PlayerRef playerRef) {
        ISpawnProvider spawnProvider = world.getWorldConfig().getSpawnProvider();

        if (spawnProvider != null) {
            // Use the spawn provider to get position for this player
            return spawnProvider.getSpawnPoint(world, playerRef.getUuid());
        }

        // Fallback to a safe default spawn position
        // Y=64 is a reasonable default for most worlds
        return new Transform(0, 64, 0);
    }

    // Message helpers

    private Message prefix() {
        return Message.raw("[").color(COLOR_INFO)
            .insert(Message.raw("HyperVerse").color(COLOR_PRIMARY))
            .insert(Message.raw("] ").color(COLOR_INFO));
    }

    private Message success(String text) {
        return prefix().insert(Message.raw(text).color(COLOR_SUCCESS));
    }

    private Message error(String text) {
        return prefix().insert(Message.raw(text).color(COLOR_ERROR));
    }

    private Message warning(String text) {
        return prefix().insert(Message.raw(text).color(COLOR_WARNING));
    }

    private Message info(String text) {
        return prefix().insert(Message.raw(text).color(COLOR_INFO));
    }
}
