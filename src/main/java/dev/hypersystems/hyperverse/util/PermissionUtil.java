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
     *
     * @param ctx        the command context
     * @param permission the permission to check
     * @return true if has permission or check cannot be performed
     */
    public static boolean hasPermission(@NotNull CommandContext ctx, @NotNull String permission) {
        // Try to get player UUID from sender
        try {
            Object sender = ctx.sender();
            // Check if sender is a player by looking for getUuid method
            Method getPlayerRef = sender.getClass().getMethod("getPlayerRef");
            Object playerRef = getPlayerRef.invoke(sender);
            if (playerRef != null) {
                Method getUuid = playerRef.getClass().getMethod("getUuid");
                UUID uuid = (UUID) getUuid.invoke(playerRef);
                return hasPermission(uuid, permission);
            }
        } catch (Exception e) {
            // Not a player or reflection failed, check if console
            try {
                String senderName = ctx.sender().toString().toLowerCase();
                if (senderName.contains("console") || senderName.contains("server")) {
                    return true; // Console always has permission
                }
            } catch (Exception ignored) {}
        }

        // Default to allow if we can't determine the sender type
        return true;
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
