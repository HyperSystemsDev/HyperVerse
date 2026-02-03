package dev.hypersystems.hyperverse;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Bootstrap class for HyperVerse.
 * <p>
 * This class provides the entry point for loading the plugin. When the Hytale
 * server API is available, this should be called from the main plugin class
 * that implements Hytale's plugin interface.
 * <p>
 * Example integration:
 * <pre>
 * public class HyperVersePlugin implements HytalePlugin {
 *     private HyperVerse hyperVerse;
 *
 *     public void onEnable() {
 *         hyperVerse = HyperVerseBootstrap.create(getDataFolder().toPath(), getLogger());
 *         hyperVerse.enable();
 *     }
 *
 *     public void onDisable() {
 *         if (hyperVerse != null) {
 *             hyperVerse.disable();
 *         }
 *     }
 * }
 * </pre>
 */
public final class HyperVerseBootstrap {

    private static HyperVerse instance;
    private static Object plugin; // Platform-specific plugin instance

    private HyperVerseBootstrap() {}

    /**
     * Gets the running HyperVerse instance.
     *
     * @return the instance, or null if not initialized
     */
    public static HyperVerse getInstance() {
        return instance;
    }

    /**
     * Sets the running HyperVerse instance.
     * Should only be called by the platform plugin.
     *
     * @param hyperVerse the instance
     */
    public static void setInstance(HyperVerse hyperVerse) {
        instance = hyperVerse;
    }

    /**
     * Gets the platform-specific plugin instance.
     *
     * @return the plugin instance, or null if not initialized
     */
    public static Object getPlugin() {
        return plugin;
    }

    /**
     * Sets the platform-specific plugin instance.
     * Should only be called by the platform plugin.
     *
     * @param pluginInstance the plugin instance
     */
    public static void setPlugin(Object pluginInstance) {
        plugin = pluginInstance;
    }

    /**
     * Creates a new HyperVerse instance.
     *
     * @param dataDirectory the plugin data directory
     * @param logger        the parent logger
     * @return the HyperVerse instance
     */
    public static HyperVerse create(Path dataDirectory, Logger logger) {
        return new HyperVerse(dataDirectory, logger);
    }

    /**
     * Standalone entry point for testing.
     * <p>
     * This allows running HyperVerse outside of the Hytale server environment
     * for testing and development purposes.
     *
     * @param args command line arguments (optional: data directory path)
     */
    public static void main(String[] args) {
        // Configure logging
        Logger logger = Logger.getLogger("HyperVerse");
        logger.setLevel(Level.ALL);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);

        // Determine data directory
        Path dataDir;
        if (args.length > 0) {
            dataDir = Paths.get(args[0]);
        } else {
            dataDir = Paths.get(".", "hyperverse-data");
        }

        logger.info("Starting HyperVerse in standalone mode...");
        logger.info("Data directory: " + dataDir.toAbsolutePath());

        // Create and enable
        HyperVerse hyperVerse = create(dataDir, logger);

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down...");
            hyperVerse.disable();
        }));

        try {
            hyperVerse.enable();
            logger.info("HyperVerse is running. Press Ctrl+C to stop.");

            // Keep the main thread alive
            Thread.currentThread().join();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to start HyperVerse", e);
            hyperVerse.disable();
            System.exit(1);
        }
    }
}
