package dev.hypersystems.hyperverse.command;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;

/**
 * Base command class for HyperVerse commands.
 * <p>
 * Provides common utilities for message formatting.
 */
public abstract class HvCommand extends AbstractCommand {

    // HyperVerse brand colors
    protected static final Color COLOR_PRIMARY = new Color(0x00FFFF);    // Cyan
    protected static final Color COLOR_SUCCESS = new Color(0x55FF55);    // Green
    protected static final Color COLOR_ERROR = new Color(0xFF5555);      // Red
    protected static final Color COLOR_WARNING = new Color(0xFFFF55);    // Yellow
    protected static final Color COLOR_INFO = new Color(0xAAAAAA);       // Gray
    protected static final Color COLOR_ACCENT = new Color(0xFFAA00);     // Gold
    protected static final Color COLOR_WHITE = new Color(0xFFFFFF);      // White

    /**
     * Creates a new HvCommand.
     *
     * @param name        the command name
     * @param description the command description
     */
    protected HvCommand(@NotNull String name, @NotNull String description) {
        super(name, description);
    }

    /**
     * Creates the HyperVerse message prefix.
     *
     * @return the prefix message
     */
    protected Message prefix() {
        return Message.raw("[").color(COLOR_INFO)
            .insert(Message.raw("HyperVerse").color(COLOR_PRIMARY))
            .insert(Message.raw("] ").color(COLOR_INFO));
    }

    /**
     * Sends a success message.
     *
     * @param text the message text
     * @return the formatted message
     */
    protected Message success(@NotNull String text) {
        return prefix().insert(Message.raw(text).color(COLOR_SUCCESS));
    }

    /**
     * Sends an error message.
     *
     * @param text the message text
     * @return the formatted message
     */
    protected Message error(@NotNull String text) {
        return prefix().insert(Message.raw(text).color(COLOR_ERROR));
    }

    /**
     * Sends a warning message.
     *
     * @param text the message text
     * @return the formatted message
     */
    protected Message warning(@NotNull String text) {
        return prefix().insert(Message.raw(text).color(COLOR_WARNING));
    }

    /**
     * Sends an info message.
     *
     * @param text the message text
     * @return the formatted message
     */
    protected Message info(@NotNull String text) {
        return prefix().insert(Message.raw(text).color(COLOR_INFO));
    }
}
