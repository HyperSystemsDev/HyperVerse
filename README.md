# HyperVerse

**Comprehensive World Management for Hytale Servers**

HyperVerse brings Multiverse-like functionality to Hytale, allowing server administrators to create, manage, and configure multiple worlds with ease.

## Features

- **World Management** - Create, delete, load, and unload worlds
- **World Templates** - Void, Flat, and custom world presets
- **Per-World Settings** - Gamemode, PvP, time, weather, spawn configuration
- **Easy Teleportation** - `/world tp <name>` to jump between worlds
- **Permission Integration** - Granular permission nodes for all features
- **Future-Proof** - Designed to work with World Gen V2

## Commands

### World Management
| Command | Description | Permission |
|---------|-------------|------------|
| `/world create <name> [template]` | Create a new world | `hyperverse.world.create` |
| `/world delete <name>` | Delete a world | `hyperverse.world.delete` |
| `/world list` | List all worlds | `hyperverse.world.list` |
| `/world info <name>` | View world information | `hyperverse.world.info` |
| `/world tp <name> [player]` | Teleport to world | `hyperverse.world.teleport` |
| `/world load <name>` | Load an unloaded world | `hyperverse.world.load` |
| `/world unload <name>` | Unload a world | `hyperverse.world.unload` |

### World Configuration
| Command | Description | Permission |
|---------|-------------|------------|
| `/worldconfig <world> spawn set` | Set world spawn | `hyperverse.worldconfig.spawn` |
| `/worldconfig <world> gamemode <mode>` | Set default gamemode | `hyperverse.worldconfig.gamemode` |
| `/worldconfig <world> pvp <on\|off>` | Toggle PvP | `hyperverse.worldconfig.pvp` |
| `/worldconfig <world> time <value>` | Set world time | `hyperverse.worldconfig.time` |
| `/worldconfig <world> weather <type>` | Set weather | `hyperverse.worldconfig.weather` |

## Templates

| Template | Description |
|----------|-------------|
| `void` | Empty void world |
| `flat` | Flat world with grass layer |
| `flat:stone` | Flat world with stone |

## Installation

1. Build the plugin: `./gradlew shadowJar`
2. Copy `build/libs/HyperVerse-*.jar` to your Hytale `mods/` folder
3. Restart the server

## Requirements

- Hytale Server (latest)
- Java 25+ (Temurin recommended)
- Gradle 9.3+

## Building

### Prerequisites

1. Create the `libs/` directory and add the Hytale server JAR:
   ```bash
   mkdir -p libs
   # Copy HytaleServer.jar to libs/
   ```

2. (Optional) For HyperPerms integration, also add HyperPerms:
   ```bash
   # Copy HyperPerms.jar to libs/
   ```

### Build Commands

```bash
# Build the shadow JAR (fat JAR with dependencies)
./gradlew shadowJar

# Clean build
./gradlew clean shadowJar

# Dev build (version set to 0.0.0)
./gradlew buildDev
```

### Output

The compiled JAR will be at:
```
build/libs/HyperVerse-1.0.0.jar
```

### Deploy

Copy the JAR to your Hytale mods folder:
```bash
cp build/libs/HyperVerse-*.jar ~/Documents/Hytale/mods/
```

## License

MIT License - See LICENSE file

## Links

- [Discord](https://discord.gg/SNPjyfkYPc)
- [GitHub](https://github.com/HyperSystemsDev/HyperVerse)
