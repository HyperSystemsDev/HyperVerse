# Reference Files

Decompiled Hytale server source files for reference while building HyperVerse.

## Directory Structure

```
references/
├── universe/          # Universe management (world container)
├── world/             # World class and configuration
├── worldgen/          # World generation providers
├── spawn/             # Spawn point providers
├── teleport/          # Teleport plugin reference
├── commands/          # Command implementation examples
├── events/            # World-related events
└── plugin/            # Plugin infrastructure
```

## Key Files

### Universe Management
| File | Description |
|------|-------------|
| `universe/Universe.java` | Singleton that manages all worlds - create, load, remove, get worlds |

### World System
| File | Description |
|------|-------------|
| `world/World.java` | World class - threading, chunks, entities, players |
| `world/WorldConfig.java` | Per-world configuration - spawn, gamemode, pvp, time, weather |

### World Generation
| File | Description |
|------|-------------|
| `worldgen/IWorldGenProvider.java` | Interface for world generators |
| `worldgen/IWorldGen.java` | World generation interface |
| `worldgen/VoidWorldGenProvider.java` | Void world template |
| `worldgen/FlatWorldGenProvider.java` | Flat world with layers |
| `worldgen/DummyWorldGenProvider.java` | Test/placeholder world |

### Spawn Providers
| File | Description |
|------|-------------|
| `spawn/ISpawnProvider.java` | Interface for spawn logic |
| `spawn/GlobalSpawnProvider.java` | Single spawn for all players |
| `spawn/IndividualSpawnProvider.java` | Per-player spawn points |
| `spawn/FitToHeightMapSpawnProvider.java` | Auto-adjust Y to terrain |

### Teleportation
| File | Description |
|------|-------------|
| `teleport/TeleportPlugin.java` | Built-in teleport plugin - commands, warps |
| `teleport/Warp.java` | Warp point data class |
| `teleport/WarpListPage.java` | Warp list pagination |

### Command Examples
| File | Description |
|------|-------------|
| `commands/TeleportCommand.java` | Parent teleport command |
| `commands/TeleportWorldCommand.java` | Teleport to world spawn |
| `commands/SpawnCommand.java` | Teleport to spawn point |
| `commands/WorldConfigCommand.java` | Parent worldconfig command |
| `commands/WorldConfigSetSpawnCommand.java` | Set world spawn |
| `commands/WorldConfigSetPvpCommand.java` | Toggle PvP |
| `commands/WorldConfigSeedCommand.java` | View/set seed |
| `commands/WorldPauseCommand.java` | Pause world |

### Events
| File | Description |
|------|-------------|
| `events/AddWorldEvent.java` | Fired when world is created/loaded |
| `events/RemoveWorldEvent.java` | Fired when world is removed |
| `events/StartWorldEvent.java` | Fired when world starts |
| `events/WorldEvent.java` | Base world event class |
| `events/AddPlayerToWorldEvent.java` | Player joins world |
| `events/DrainPlayerFromWorldEvent.java` | Player leaving world |
| `events/PrepareUniverseEvent.java` | Universe initialization |

### Plugin Infrastructure
| File | Description |
|------|-------------|
| `plugin/JavaPlugin.java` | Base plugin class to extend |
| `plugin/JavaPluginInit.java` | Plugin initialization interface |
| `plugin/AbstractCommand.java` | Base command class |
| `plugin/AbstractPlayerCommand.java` | Player-only command base |
| `plugin/CommandRegistry.java` | Command registration system |

## Usage Patterns

### Creating a World
```java
// From Universe.java
Universe.get().addWorld("myworld")
    .thenAccept(world -> {
        // World created successfully
    });

// With specific generator
Universe.get().addWorld("void_world",
    IWorldGenProvider.CODEC.parse("Void").result().get(),
    IChunkStorageProvider.CODEC.getDefault());
```

### Modifying World Config
```java
// From WorldConfig.java
WorldConfig config = world.getWorldConfig();
config.setPvpEnabled(false);
config.setGameMode(GameMode.CREATIVE);
config.setForcedWeather("clear");
config.markChanged(); // Important: triggers save
```

### Registering Commands
```java
// From TeleportPlugin.java pattern
CommandRegistry.get().register(new WorldCommand());
```

### Listening to Events
```java
// From event system
HytaleServer.get().getEventBus()
    .subscribe(AddWorldEvent.class, event -> {
        // Handle world creation
    });
```

## Notes

- These are decompiled sources for **reference only**
- Do not copy code directly - use as API documentation
- Check for null returns on async operations
- Always call `markChanged()` after WorldConfig modifications
- World operations return `CompletableFuture` - handle async properly
