package dev.hypersystems.hyperverse.command.world;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import dev.hypersystems.hyperverse.HyperVerse;
import dev.hypersystems.hyperverse.command.HvCommand;
import dev.hypersystems.hyperverse.util.PermissionUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/**
 * Creates a new world.
 * <p>
 * Usage: /world create <name> [template]
 * Permission: hyperverse.world.create
 */
public class WorldCreateCommand extends HvCommand {

    private static final String PERMISSION = "hyperverse.world.create";
    private static final Pattern VALID_NAME = Pattern.compile("^[a-zA-Z0-9_-]{1,32}$");
    private static final Set<String> VALID_TEMPLATES = Set.of("void", "flat", "default");

    private final HyperVerse hyperVerse;
    private final RequiredArg<String> nameArg;
    private final OptionalArg<String> templateArg;

    /**
     * Creates a new WorldCreateCommand.
     *
     * @param hyperVerse the HyperVerse instance
     */
    @SuppressWarnings("this-escape")
    public WorldCreateCommand(@NotNull HyperVerse hyperVerse) {
        super("create", "Create a new world");
        this.hyperVerse = hyperVerse;
        this.nameArg = withRequiredArg("name", "World name", ArgTypes.STRING);
        this.templateArg = withOptionalArg("template", "World template (void, flat)", ArgTypes.STRING);
        addAliases("new", "add");
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        // Permission check
        if (!PermissionUtil.hasPermission(ctx, PERMISSION)) {
            ctx.sender().sendMessage(error("You don't have permission to create worlds."));
            return CompletableFuture.completedFuture(null);
        }

        String worldName = ctx.get(nameArg);
        if (worldName == null || worldName.isEmpty()) {
            ctx.sender().sendMessage(error("Please specify a world name."));
            return CompletableFuture.completedFuture(null);
        }

        // Validate world name
        if (!VALID_NAME.matcher(worldName).matches()) {
            ctx.sender().sendMessage(error("Invalid world name!"));
            ctx.sender().sendMessage(info("Use only letters, numbers, underscores, and dashes (1-32 chars)."));
            return CompletableFuture.completedFuture(null);
        }

        // Check if world already exists
        if (hyperVerse.getWorldManager().getWorld(worldName) != null) {
            ctx.sender().sendMessage(error("World '" + worldName + "' already exists."));
            return CompletableFuture.completedFuture(null);
        }

        // Get template (default to void)
        String template = ctx.get(templateArg);
        if (template == null || template.isEmpty()) {
            template = "void";
        }
        template = template.toLowerCase();

        // Validate template
        if (!VALID_TEMPLATES.contains(template)) {
            ctx.sender().sendMessage(error("Invalid template '" + template + "'."));
            ctx.sender().sendMessage(info("Valid templates: void, flat"));
            return CompletableFuture.completedFuture(null);
        }

        final String finalTemplate = template;

        ctx.sender().sendMessage(info("Creating world '" + worldName + "' with template '" + template + "'..."));

        // Create world asynchronously
        hyperVerse.getWorldManager().createWorld(worldName, finalTemplate)
            .thenAccept(world -> {
                if (world != null) {
                    ctx.sender().sendMessage(success("World '" + worldName + "' created successfully!"));
                    ctx.sender().sendMessage(info("Use /world tp " + worldName + " to teleport."));
                } else {
                    ctx.sender().sendMessage(error("Failed to create world '" + worldName + "'."));
                }
            })
            .exceptionally(e -> {
                ctx.sender().sendMessage(error("Error: " + e.getCause().getMessage()));
                hyperVerse.getLogger().severe("Failed to create world: " + e.getMessage());
                return null;
            });

        return CompletableFuture.completedFuture(null);
    }
}
