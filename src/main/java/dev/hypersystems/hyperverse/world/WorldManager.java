package dev.hypersystems.hyperverse.world;

import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.spawn.GlobalSpawnProvider;
import dev.hypersystems.hyperverse.HyperVerse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Manages worlds through the Hytale Universe API.
 * <p>
 * Provides a wrapper around the native Universe world management
 * with additional features for HyperVerse.
 */
public class WorldManager {

    private final HyperVerse hyperVerse;
    private final Logger logger;

    // Track worlds managed by HyperVerse
    private final Map<String, WorldMetadata> worldMetadata = new ConcurrentHashMap<>();

    /**
     * Creates a new WorldManager.
     *
     * @param hyperVerse the HyperVerse instance
     */
    public WorldManager(@NotNull HyperVerse hyperVerse) {
        this.hyperVerse = hyperVerse;
        this.logger = hyperVerse.getLogger();
    }

    /**
     * Initializes the world manager.
     */
    public void initialize() {
        logger.info("WorldManager initialized");

        // Load any existing worlds into our tracking
        for (World world : getWorlds()) {
            worldMetadata.computeIfAbsent(world.getName(), name -> new WorldMetadata(name));
        }
    }

    /**
     * Shuts down the world manager.
     */
    public void shutdown() {
        logger.info("WorldManager shutting down...");
        worldMetadata.clear();
    }

    // ==================== World Operations ====================

    /**
     * Creates a new world.
     *
     * @param name     the world name
     * @param template the world template (void, flat, etc.)
     * @return a future containing the created world
     */
    public CompletableFuture<World> createWorld(@NotNull String name, @NotNull String template) {
        // Check if world already exists
        if (getWorld(name) != null) {
            return CompletableFuture.failedFuture(
                new IllegalStateException("World '" + name + "' already exists"));
        }

        logger.info("Creating world '" + name + "' with template '" + template + "'");

        try {
            // Map template to Hytale generator type
            String generatorType = mapTemplateToGeneratorType(template);

            // Create world through Universe API with generator type
            // Using the deprecated but functional addWorld(name, generatorType, chunkStorageType)
            return Universe.get().addWorld(name, generatorType, null)
                .thenApply(world -> {
                    if (world != null) {
                        // Configure spawn provider based on template
                        configureWorldForTemplate(world, template);

                        // Track the new world
                        worldMetadata.put(name, new WorldMetadata(name, template));
                        logger.info("World '" + name + "' created successfully with generator '" + generatorType + "'");
                    }
                    return world;
                })
                .exceptionally(e -> {
                    logger.severe("Failed to create world '" + name + "': " + e.getMessage());
                    throw new RuntimeException("Failed to create world", e);
                });
        } catch (Exception e) {
            logger.severe("Failed to create world '" + name + "': " + e.getMessage());
            return CompletableFuture.failedFuture(new RuntimeException("Failed to create world", e));
        }
    }

    /**
     * Maps a HyperVerse template name to Hytale's generator type ID.
     *
     * @param template the template name (void, flat, default)
     * @return the Hytale generator type ID
     */
    private String mapTemplateToGeneratorType(@NotNull String template) {
        return switch (template.toLowerCase()) {
            case "void" -> "Void";
            case "flat" -> "Flat";
            default -> null; // null = use Hytale's default generator
        };
    }

    /**
     * Configures world settings based on template after creation.
     *
     * @param world    the created world
     * @param template the template used
     */
    private void configureWorldForTemplate(@NotNull World world, @NotNull String template) {
        WorldConfig config = world.getWorldConfig();

        // Set spawn provider if not already set by the generator
        if (config.getSpawnProvider() == null) {
            Transform defaultSpawn = getDefaultSpawnForTemplate(template);
            config.setSpawnProvider(new GlobalSpawnProvider(defaultSpawn));
            logger.info("Configured spawn provider for world '" + world.getName() + "' at " + defaultSpawn);
        }

        // Mark config as changed so it gets saved
        config.markChanged();
    }

    /**
     * Gets the default spawn transform for a template.
     *
     * @param template the template name
     * @return the default spawn transform
     */
    private Transform getDefaultSpawnForTemplate(@NotNull String template) {
        return switch (template.toLowerCase()) {
            case "void" -> new Transform(0, 1, 0);  // Void worlds spawn at Y=1
            case "flat" -> new Transform(0, 81, 0); // Flat worlds spawn above the terrain (Y=80)
            default -> new Transform(0, 64, 0);    // Default spawn
        };
    }

    /**
     * Deletes a world permanently.
     * <p>
     * This will remove the world from memory AND delete its data from disk.
     *
     * @param name the world name
     * @return a future that completes when the world is deleted
     */
    public CompletableFuture<Void> deleteWorld(@NotNull String name) {
        World world = getWorld(name);
        if (world == null) {
            return CompletableFuture.failedFuture(
                new IllegalStateException("World '" + name + "' does not exist"));
        }

        // Check if world has players
        if (world.getPlayerCount() > 0) {
            return CompletableFuture.failedFuture(
                new IllegalStateException("Cannot delete world '" + name + "' with players in it"));
        }

        logger.info("Deleting world '" + name + "' (including disk data)");

        return CompletableFuture.runAsync(() -> {
            try {
                // Set deleteOnRemove to true so Universe.removeWorld() deletes the data
                world.getWorldConfig().setDeleteOnRemove(true);

                Universe.get().removeWorld(name);
                worldMetadata.remove(name);
                logger.info("World '" + name + "' deleted successfully");
            } catch (Exception e) {
                logger.severe("Failed to delete world '" + name + "': " + e.getMessage());
                throw new RuntimeException("Failed to delete world", e);
            }
        });
    }

    /**
     * Loads an existing world from disk.
     *
     * @param name the world name
     * @return a future containing the loaded world
     */
    public CompletableFuture<World> loadWorld(@NotNull String name) {
        // Check if already loaded
        World existing = getWorld(name);
        if (existing != null) {
            logger.info("World '" + name + "' is already loaded");
            return CompletableFuture.completedFuture(existing);
        }

        // Check if world exists on disk
        if (!isWorldLoadable(name)) {
            return CompletableFuture.failedFuture(
                new IllegalStateException("World '" + name + "' does not exist on disk"));
        }

        logger.info("Loading world '" + name + "'");

        try {
            // Load world through Universe API - returns CompletableFuture<World>
            return Universe.get().loadWorld(name)
                .thenApply(world -> {
                    if (world != null) {
                        worldMetadata.computeIfAbsent(name, n -> new WorldMetadata(n));
                        logger.info("World '" + name + "' loaded successfully");
                    }
                    return world;
                })
                .exceptionally(e -> {
                    logger.severe("Failed to load world '" + name + "': " + e.getMessage());
                    throw new RuntimeException("Failed to load world", e);
                });
        } catch (Exception e) {
            logger.severe("Failed to load world '" + name + "': " + e.getMessage());
            return CompletableFuture.failedFuture(new RuntimeException("Failed to load world", e));
        }
    }

    /**
     * Unloads a world from memory without deleting its data.
     * <p>
     * The world can be reloaded later using {@link #loadWorld(String)}.
     * NOTE: The Hytale Universe API does not have a dedicated "unload" method.
     * This uses removeWorld() with deleteOnRemove=false to preserve world data.
     *
     * @param name the world name
     * @return a future that completes when the world is unloaded
     */
    public CompletableFuture<Void> unloadWorld(@NotNull String name) {
        World world = getWorld(name);
        if (world == null) {
            return CompletableFuture.failedFuture(
                new IllegalStateException("World '" + name + "' is not loaded"));
        }

        // Check if world has players
        if (world.getPlayerCount() > 0) {
            return CompletableFuture.failedFuture(
                new IllegalStateException("Cannot unload world '" + name + "' with players in it"));
        }

        logger.info("Unloading world '" + name + "' (preserving disk data)");

        return CompletableFuture.runAsync(() -> {
            try {
                // IMPORTANT: Set deleteOnRemove to false to preserve world data on disk
                // The Hytale API's removeWorld() checks this flag before deleting
                world.getWorldConfig().setDeleteOnRemove(false);

                Universe.get().removeWorld(name);

                // Note: We keep the metadata in case the world is reloaded
                // The metadata will be refreshed on reload anyway
                logger.info("World '" + name + "' unloaded successfully (data preserved on disk)");
            } catch (Exception e) {
                logger.severe("Failed to unload world '" + name + "': " + e.getMessage());
                throw new RuntimeException("Failed to unload world", e);
            }
        });
    }

    // ==================== World Queries ====================

    /**
     * Gets a world by name.
     *
     * @param name the world name
     * @return the world, or null if not found
     */
    @Nullable
    public World getWorld(@NotNull String name) {
        return Universe.get().getWorld(name);
    }

    /**
     * Gets all loaded worlds.
     *
     * @return collection of loaded worlds
     */
    @NotNull
    public Collection<World> getWorlds() {
        return Universe.get().getWorlds().values();
    }

    /**
     * Gets the world names map.
     *
     * @return map of world names to worlds
     */
    @NotNull
    public Map<String, World> getWorldMap() {
        return Universe.get().getWorlds();
    }

    /**
     * Checks if a world exists on disk and can be loaded.
     *
     * @param name the world name
     * @return true if the world can be loaded
     */
    public boolean isWorldLoadable(@NotNull String name) {
        return Universe.get().isWorldLoadable(name);
    }

    /**
     * Gets the default world.
     *
     * @return the default world
     */
    @NotNull
    public World getDefaultWorld() {
        return Universe.get().getDefaultWorld();
    }

    // ==================== World Metadata ====================

    /**
     * Gets metadata for a world.
     *
     * @param name the world name
     * @return the metadata, or null if not tracked
     */
    @Nullable
    public WorldMetadata getWorldMetadata(@NotNull String name) {
        return worldMetadata.get(name);
    }

    /**
     * Simple metadata class for tracking HyperVerse-specific world info.
     */
    public static class WorldMetadata {
        private final String name;
        private final String template;
        private final long createdAt;

        public WorldMetadata(@NotNull String name) {
            this(name, "default");
        }

        public WorldMetadata(@NotNull String name, @NotNull String template) {
            this.name = name;
            this.template = template;
            this.createdAt = System.currentTimeMillis();
        }

        public String getName() {
            return name;
        }

        public String getTemplate() {
            return template;
        }

        public long getCreatedAt() {
            return createdAt;
        }
    }
}
