package dev.hypersystems.hyperverse;

import dev.hypersystems.hyperverse.util.PermissionUtil;
import dev.hypersystems.hyperverse.world.WorldManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class for HyperVerse.
 * <p>
 * This is the core logic class for the world management plugin.
 * It is platform-independent and handles all world management operations.
 */
public final class HyperVerse {

    public static final String VERSION = BuildInfo.VERSION;

    private static volatile HyperVerse instance;

    private final Path dataDirectory;
    private final Logger logger;

    // Core components
    private WorldManager worldManager;
    // private TemplateRegistry templateRegistry;
    // private HyperVerseConfig config;

    // State
    private volatile boolean enabled = false;

    /**
     * Creates a new HyperVerse instance.
     *
     * @param dataDirectory the plugin data directory
     * @param logger        the parent logger
     */
    public HyperVerse(@NotNull Path dataDirectory, @NotNull Logger logger) {
        this.dataDirectory = dataDirectory;
        this.logger = logger;
    }

    /**
     * Gets the plugin instance.
     *
     * @return the instance, or null if not enabled
     */
    @Nullable
    public static HyperVerse getInstance() {
        return instance;
    }

    /**
     * Enables the plugin.
     */
    public void enable() {
        if (enabled) {
            return;
        }

        long startTime = System.currentTimeMillis();
        instance = this;

        try {
            logger.info("Enabling HyperVerse...");

            // Create data directory if it doesn't exist
            if (!dataDirectory.toFile().exists()) {
                dataDirectory.toFile().mkdirs();
            }

            // Initialize HyperPerms integration
            PermissionUtil.init();
            if (PermissionUtil.isAvailable()) {
                logger.info("HyperPerms integration enabled");
            } else {
                logger.info("HyperPerms not found - all players have full access");
            }

            // TODO: Initialize configuration
            // config = new HyperVerseConfig(dataDirectory);
            // config.load();

            // Initialize world manager
            worldManager = new WorldManager(this);
            worldManager.initialize();

            // TODO: Initialize template registry
            // templateRegistry = new TemplateRegistry(dataDirectory);

            enabled = true;
            long elapsed = System.currentTimeMillis() - startTime;
            logger.info(String.format("HyperVerse enabled in %dms", elapsed));

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to enable HyperVerse", e);
            disable();
            throw new RuntimeException("Failed to enable HyperVerse", e);
        }
    }

    /**
     * Disables the plugin.
     */
    public void disable() {
        if (!enabled && instance == null) {
            return;
        }

        logger.info("Disabling HyperVerse...");

        // Cleanup resources
        if (worldManager != null) {
            worldManager.shutdown();
            worldManager = null;
        }

        enabled = false;
        instance = null;
        logger.info("HyperVerse disabled");
    }

    /**
     * Reloads the plugin configuration.
     */
    public void reload() {
        logger.info("Reloading HyperVerse...");

        // TODO: Reload configuration
        // if (config != null) {
        //     config.reload();
        // }

        logger.info("HyperVerse reloaded");
    }

    // ==================== Accessors ====================

    /**
     * Gets the plugin data directory.
     *
     * @return the data directory path
     */
    @NotNull
    public Path getDataDirectory() {
        return dataDirectory;
    }

    /**
     * Gets the logger.
     *
     * @return the logger
     */
    @NotNull
    public Logger getLogger() {
        return logger;
    }

    /**
     * Gets the plugin version.
     *
     * @return the current plugin version
     */
    @NotNull
    public String getVersion() {
        return VERSION;
    }

    /**
     * Checks if the plugin is enabled.
     *
     * @return true if enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    // ==================== World Management API ====================

    /**
     * Gets the world manager.
     *
     * @return the world manager
     */
    @NotNull
    public WorldManager getWorldManager() {
        if (worldManager == null) {
            throw new IllegalStateException("HyperVerse is not enabled");
        }
        return worldManager;
    }

    // ==================== Future API Methods ====================

    // TODO: Template registry API
    // public TemplateRegistry getTemplateRegistry() { return templateRegistry; }

    // TODO: Configuration API
    // public HyperVerseConfig getConfig() { return config; }
}
