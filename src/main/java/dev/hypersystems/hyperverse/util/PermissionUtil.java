package dev.hypersystems.hyperverse.util;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Utility for permission checking with HyperPerms integration.
 * <p>
 * Uses reflection to avoid hard dependency on HyperPerms.
 * When HyperPerms is not available, defaults to allowing access.
 */
public final class PermissionUtil {

    private static final Logger LOGGER = Logger.getLogger("HyperVerse");

    private static boolean available = false;
    private static Object hyperPermsInstance = null;
    private static Method hasPermissionMethod = null;
    private static String initError = null;

    private PermissionUtil() {}

    /**
     * Initializes the HyperPerms integration.
     */
    public static void init() {
        try {
            // Try to load HyperPerms via HyperPermsBootstrap
            Class<?> bootstrapClass = Class.forName("com.hyperperms.HyperPermsBootstrap");
            Method getInstanceMethod = bootstrapClass.getMethod("getInstance");
            hyperPermsInstance = getInstanceMethod.invoke(null);

            if (hyperPermsInstance == null) {
                initError = "HyperPermsBootstrap.getInstance() returned null";
                available = false;
                LOGGER.warning("HyperPerms bootstrap returned null - permissions disabled");
                return;
            }

            Class<?> instanceClass = hyperPermsInstance.getClass();
            LOGGER.info("HyperPerms instance class: " + instanceClass.getName());

            // Get the hasPermission method
            hasPermissionMethod = instanceClass.getMethod("hasPermission", UUID.class, String.class);

            available = true;
            LOGGER.info("HyperPerms integration enabled successfully");

        } catch (ClassNotFoundException e) {
            available = false;
            initError = "HyperPerms not found";
            LOGGER.info("HyperPerms not found - all players will have full access");
        } catch (NoSuchMethodException e) {
            available = false;
            initError = "Method not found: " + e.getMessage();
            LOGGER.warning("HyperPerms API mismatch: " + e.getMessage() + " - defaulting to allow all");
        } catch (Exception e) {
            available = false;
            initError = e.getClass().getSimpleName() + ": " + e.getMessage();
            LOGGER.warning("Failed to initialize HyperPerms integration: " + e.getMessage() + " - defaulting to allow all");
        }
    }

    /**
     * Checks if HyperPerms is available.
     *
     * @return true if available
     */
    public static boolean isAvailable() {
        return available;
    }

    /**
     * Gets initialization error message if any.
     *
     * @return error message or null
     */
    public static String getInitError() {
        return initError;
    }

    /**
     * Checks if a player has a permission via command context.
     * <p>
     * If the sender is a player, their permissions are checked via HyperPerms.
     * If the sender is console or the sender type cannot be determined, permission is granted.
     * <p>
     * Note: Console detection is done by checking if we can get a PlayerRef, NOT by
     * string matching on sender.toString() which could match usernames containing "console".
     *
     * @param ctx        the command context
     * @param permission the permission to check
     * @return true if has permission or check cannot be performed
     */
    public static boolean hasPermission(@NotNull CommandContext ctx, @NotNull String permission) {
        // Try to get player UUID from sender using reflection
        // If we can successfully get a PlayerRef with a UUID, this is a player
        // Otherwise, assume it's console or another non-player sender (allow by default)
        try {
            Object sender = ctx.sender();

            // Try to get PlayerRef from the sender
            // Player command senders typically have a getPlayerRef() method
            Method getPlayerRef = sender.getClass().getMethod("getPlayerRef");
            Object playerRef = getPlayerRef.invoke(sender);

            if (playerRef != null) {
                // Successfully got a PlayerRef - this is a player, check their permissions
                Method getUuid = playerRef.getClass().getMethod("getUuid");
                UUID uuid = (UUID) getUuid.invoke(playerRef);
                if (uuid != null) {
                    return hasPermission(uuid, permission);
                }
            }

            // PlayerRef is null - sender exists but isn't a player (likely console)
            // Console always has permission
            return true;

        } catch (NoSuchMethodException e) {
            // Sender doesn't have getPlayerRef() method - not a player sender
            // This is typically console or a system sender - allow by default
            return true;
        } catch (Exception e) {
            // Other reflection errors - fail open (allow) for safety
            // This ensures commands work even if reflection fails
            LOGGER.fine("Could not determine sender type for permission check: " + e.getMessage());
            return true;
        }
    }

    /**
     * Checks if a player has a permission.
     *
     * @param playerUuid the player's UUID
     * @param permission the permission to check
     * @return true if has permission or check cannot be performed
     */
    public static boolean hasPermission(@NotNull UUID playerUuid, @NotNull String permission) {
        // If HyperPerms not available, allow by default
        if (!available || hyperPermsInstance == null || hasPermissionMethod == null) {
            return true;
        }

        try {
            Object result = hasPermissionMethod.invoke(hyperPermsInstance, playerUuid, permission);

            if (result instanceof Boolean) {
                return (Boolean) result;
            }

            // Unexpected result type, allow by default
            return true;

        } catch (Exception e) {
            // Any error in permission check = allow (fail-open)
            LOGGER.warning("Exception checking " + permission + " for " + playerUuid + ": " + e.getMessage());
            return true;
        }
    }

    /**
     * Checks if a player has any of the specified permissions.
     *
     * @param playerUuid  the player's UUID
     * @param permissions the permissions to check
     * @return true if has any permission
     */
    public static boolean hasAnyPermission(@NotNull UUID playerUuid, @NotNull String... permissions) {
        for (String permission : permissions) {
            if (hasPermission(playerUuid, permission)) {
                return true;
            }
        }
        return false;
    }
}
