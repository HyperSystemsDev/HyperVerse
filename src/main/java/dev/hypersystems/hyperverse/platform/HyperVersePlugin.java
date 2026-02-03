package dev.hypersystems.hyperverse.platform;

import dev.hypersystems.hyperverse.HyperVerse;
import dev.hypersystems.hyperverse.HyperVerseBootstrap;
import dev.hypersystems.hyperverse.command.WorldCommand;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import java.util.logging.Level;

/**
 * Main Hytale plugin class for HyperVerse.
 * <p>
 * This class integrates HyperVerse with the Hytale server by:
 * <ul>
 *   <li>Managing the plugin lifecycle</li>
 *   <li>Registering commands and event listeners</li>
 *   <li>Providing world management functionality</li>
 * </ul>
 */
public class HyperVersePlugin extends JavaPlugin {

    private HyperVerse hyperVerse;

    /**
     * Creates a new HyperVersePlugin instance.
     * Called by the Hytale plugin loader.
     *
     * @param init the plugin initialization data
     */
    public HyperVersePlugin(JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        // Initialize HyperVerse core
        hyperVerse = new HyperVerse(getDataDirectory(), java.util.logging.Logger.getLogger("HyperVerse"));

        // Register the global instance for API access
        HyperVerseBootstrap.setInstance(hyperVerse);
        HyperVerseBootstrap.setPlugin(this);

        getLogger().at(Level.INFO).log("HyperVerse setup complete");
    }

    @Override
    protected void start() {
        // Enable HyperVerse core
        hyperVerse.enable();

        // Register commands
        registerCommands();

        // TODO: Register event listeners
        // registerEventListeners();

        getLogger().at(Level.INFO).log("HyperVerse v%s enabled!", getManifest().getVersion());
    }

    /**
     * Registers commands with Hytale.
     */
    private void registerCommands() {
        try {
            getCommandRegistry().registerCommand(new WorldCommand(hyperVerse));

            getLogger().at(Level.INFO).log("Registered commands: /world");
        } catch (Exception e) {
            getLogger().at(Level.SEVERE).withCause(e).log("Failed to register commands");
        }
    }

    @Override
    protected void shutdown() {
        // Disable HyperVerse core
        if (hyperVerse != null) {
            hyperVerse.disable();
        }

        // Clear the global instances
        HyperVerseBootstrap.setInstance(null);
        HyperVerseBootstrap.setPlugin(null);

        getLogger().at(Level.INFO).log("HyperVerse disabled");
    }

    /**
     * Gets the HyperVerse instance.
     *
     * @return the HyperVerse instance
     */
    public HyperVerse getHyperVerse() {
        return hyperVerse;
    }
}
