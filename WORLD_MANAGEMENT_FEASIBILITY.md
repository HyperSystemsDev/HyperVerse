# World Management Mod - Feasibility Analysis

**Date:** 2026-02-02
**Priority Score:** 8.3/10
**Feasibility Score:** 9.5/10 - HIGHLY FEASIBLE

---

## Executive Summary

Building a comprehensive World Management mod for Hytale is **highly feasible**. The decompiled source reveals an extremely mature and well-architected world management system that already provides most of the core infrastructure needed. Many "MVP features" you identified already exist in the game's core systems, making this more of an **enhancement/unification project** than building from scratch.

---

## Existing Infrastructure Analysis

### Universe Class (`com.hypixel.hytale.server.core.universe.Universe`)

The `Universe` class is a singleton that manages all worlds and provides:

| Method | Description | Status |
|--------|-------------|--------|
| `addWorld(name)` | Create a new world with default config | Already Exists |
| `addWorld(name, generatorType, chunkStorageType)` | Create with specific generator | Already Exists |
| `makeWorld(name, path, config, start)` | Full control over world creation | Already Exists |
| `loadWorld(name)` | Load existing world from disk | Already Exists |
| `removeWorld(name)` | Remove/unload a world | Already Exists |
| `getWorld(name)` / `getWorld(uuid)` | Get world by name or UUID | Already Exists |
| `getDefaultWorld()` | Get the server's default world | Already Exists |
| `getWorlds()` | Get all loaded worlds | Already Exists |
| `isWorldLoadable(name)` | Check if world exists on disk | Already Exists |

**World Storage Location:** `universe/worlds/<worldName>/`

### World Gen Providers (Templates)

Built-in world generation providers exist:

| Provider | Class | Description |
|----------|-------|-------------|
| **Void** | `VoidWorldGenProvider` | Empty world, configurable environment/tint |
| **Flat** | `FlatWorldGenProvider` | Configurable layers with BlockType per layer |
| **Dummy** | `DummyWorldGenProvider` | Placeholder/test world |

All implement `IWorldGenProvider` interface with CODEC registration system for easy extension.

### WorldConfig (`com.hypixel.hytale.server.core.universe.world.WorldConfig`)

**Per-World Settings Already Available:**

| Setting | Type | Description |
|---------|------|-------------|
| `DisplayName` | String | Player-facing world name |
| `Seed` | Long | World generation seed |
| `SpawnProvider` | ISpawnProvider | Spawn point management |
| `WorldGen` | IWorldGenProvider | World generator selection |
| `IsTicking` | Boolean | Chunk ticking toggle |
| `IsBlockTicking` | Boolean | Block ticking toggle |
| `IsPvpEnabled` | Boolean | PvP toggle |
| `IsFallDamageEnabled` | Boolean | Fall damage toggle |
| `IsGameTimePaused` | Boolean | Pause day/night cycle |
| `GameTime` | Instant | Current world time |
| `ForcedWeather` | String | Force specific weather |
| `GameMode` | GameMode | Default gamemode for world |
| `IsSpawningNPC` | Boolean | NPC spawning toggle |
| `IsAllNPCFrozen` | Boolean | Freeze all NPCs |
| `GameplayConfig` | String | Reference gameplay config |
| `IsSavingPlayers` | Boolean | Save player data in world |
| `IsSavingChunks` | Boolean | Save chunk data to disk |
| `IsUnloadingChunks` | Boolean | Chunk unload behavior |
| `DeleteOnRemove` | Boolean | Auto-delete when removed |
| `DeleteOnUniverseStart` | Boolean | Auto-delete on restart |
| `ChunkConfig.PregenerateRegion` | Box2D | Pre-generate area |
| `ChunkConfig.KeepLoadedRegion` | Box2D | Never unload area |

### Existing Commands

**WorldConfig Commands:**
- `WorldConfigSetSpawnCommand` - Set world spawn
- `WorldConfigSetPvpCommand` - Toggle PvP
- `WorldConfigPauseTimeCommand` - Pause time
- `WorldConfigSeedCommand` - View/set seed
- `WorldPauseCommand` - Pause world

**Teleportation (builtin TeleportPlugin):**
- `/teleport world <name>` - Teleport to world spawn
- `/teleport <player>` - Teleport to player
- `/teleport <x> <y> <z>` - Teleport to coordinates
- `/teleport top` - Teleport to highest block
- `/teleport back` / `/teleport forward` - History navigation
- `/teleport home` - Teleport to home
- `/spawn` - Teleport to spawn
- `/warp set <name>` - Create warp
- `/warp <name>` - Go to warp
- `/warp list` - List warps
- `/warp remove <name>` - Remove warp

### Spawn Providers

| Provider | Class | Description |
|----------|-------|-------------|
| **Global** | `GlobalSpawnProvider` | Single spawn for all players |
| **Individual** | `IndividualSpawnProvider` | Per-player spawn points |
| **FitToHeightMap** | `FitToHeightMapSpawnProvider` | Auto-adjust Y to terrain |

### Events System

| Event | Description |
|-------|-------------|
| `AddWorldEvent` | Fired when world is created/loaded |
| `RemoveWorldEvent` | Fired when world is removed |
| `AllWorldsLoadedEvent` | Fired when all worlds are ready |
| `StartWorldEvent` | Fired when world starts |
| `PrepareUniverseEvent` | Before universe initializes |

---

## MVP Features Assessment

### World Operations

| Feature | Status | Implementation Effort |
|---------|--------|----------------------|
| Create world (with templates) | Built-in | Wrap existing API |
| Delete world | Built-in | Wrap existing API |
| Clone/copy world | Not built-in | Medium - filesystem copy + UUID regeneration |
| Import external worlds | Partially | Low - use `loadWorld()` after copying files |
| Unload/load worlds | Built-in | Wrap existing API |

### Per-World Settings

| Feature | Status | Implementation Effort |
|---------|--------|----------------------|
| Spawn point configuration | Built-in | Commands exist |
| Gamemode override | Built-in | `WorldConfig.setGameMode()` |
| Difficulty settings | Partial | Via GameplayConfig reference |
| Weather control | Built-in | `WorldConfig.setForcedWeather()` |
| Time control | Built-in | `WorldConfig.setGameTime/setGameTimePaused()` |
| PvP toggle | Built-in | `WorldConfig.setPvpEnabled()` |

### Teleportation

| Feature | Status | Implementation Effort |
|---------|--------|----------------------|
| `/world <name>` | Built-in | `TeleportWorldCommand` exists |
| `/worlds` list | Not built-in | Low - iterate `Universe.getWorlds()` |
| Per-world spawn | Built-in | Via SpawnProvider |

### Templates

| Template | Status | Implementation Effort |
|----------|--------|----------------------|
| Void world | Built-in | `VoidWorldGenProvider` |
| Flat world | Built-in | `FlatWorldGenProvider` |
| Custom presets | Not built-in | Medium - extend `IWorldGenProvider` |

---

## World Gen V2 Compatibility Assessment

### Architecture Analysis

The current architecture is **highly modular** and **interface-driven**:

```java
// World gen is abstracted behind interface
IWorldGenProvider.CODEC.register("Flat", FlatWorldGenProvider.class, ...);
IWorldGenProvider.CODEC.register("Void", VoidWorldGenProvider.class, ...);

// WorldConfig stores provider, not implementation
private IWorldGenProvider worldGenProvider = IWorldGenProvider.CODEC.getDefault();
```

### Future-Proofing Strategy

1. **Interface-based design:** Your mod should work with `IWorldGenProvider` interface, not implementations
2. **CODEC system:** Hytale uses a CODEC registration system - new world gen types get registered the same way
3. **No direct coupling:** World management (create/delete/load) is separate from world generation
4. **Configuration agnostic:** `WorldConfig` stores the provider reference, not generator internals

### Risk Assessment

| Risk | Level | Mitigation |
|------|-------|------------|
| New worldgen types | Low | Use `IWorldGenProvider` interface |
| API changes | Low | Core Universe/World APIs are stable |
| Config format changes | Medium | `WorldConfig.CODEC` handles versioning |
| New spawn mechanics | Low | `ISpawnProvider` is also interface-based |

**Recommendation:** Code against interfaces (`IWorldGenProvider`, `ISpawnProvider`, `IChunkStorageProvider`). Avoid depending on specific implementations like `FlatWorldGenProvider` internals.

---

## Differentiation Opportunities

### 1. Integration with Permissions (HyperPerms)

```java
// Per-world access control hook points:
AddWorldEvent -> Check permission to create worlds
RemoveWorldEvent -> Check permission to delete worlds
AddPlayerToWorldEvent -> Check per-world access permissions
```

The event system makes this integration trivial.

### 2. World Templates

Extend beyond built-in Flat/Void:
- **Skyblock** - Void with small island prefab
- **Hub World** - Pre-built spawn area + void
- **Resource World** - Full worldgen with auto-regeneration flag

### 3. Performance Focus

Leverage existing config options:
- `ChunkConfig.keepLoadedRegion` - Keep spawn always loaded
- `ChunkConfig.pregenerateRegion` - Pre-generate on creation
- `WorldConfig.isUnloadingChunks` - Memory management

### 4. Advanced Features (Post-MVP)

- World instancing (clone with `DeleteOnRemove=true`)
- World linking/portals
- World backup scheduling (builds on existing `Universe.runBackup()`)
- Per-world resource packs

---

## Implementation Recommendations

### Phase 1: Core Commands (2-3 days)

1. `/world create <name> [template]` - Wrap `Universe.addWorld()`
2. `/world delete <name>` - Wrap `Universe.removeWorld()`
3. `/world list` - Iterate `Universe.getWorlds()`
4. `/world info <name>` - Display `WorldConfig` settings
5. `/world tp <name>` - Already exists, alias wrapper

### Phase 2: Configuration Commands (2-3 days)

1. `/worldconfig <world> spawn set` - Enhance existing
2. `/worldconfig <world> gamemode <mode>`
3. `/worldconfig <world> pvp <on/off>`
4. `/worldconfig <world> weather <type>`
5. `/worldconfig <world> time <value>`

### Phase 3: Templates (1-2 days)

1. Create template config format
2. Implement custom `IWorldGenProvider` for presets
3. Register templates via CODEC system

### Phase 4: Advanced (ongoing)

1. World cloning
2. World importing
3. Permission integration hooks

---

## Technical Risks

| Risk | Severity | Mitigation |
|------|----------|------------|
| Thread safety | Medium | World operations are designed for async - use `CompletableFuture` properly |
| Event ordering | Low | Events have priority ordering built-in |
| Config persistence | Low | Use `WorldConfig.markChanged()` + built-in save system |
| Memory leaks | Low | Rely on built-in unload mechanics |

---

## Conclusion

**Feasibility: EXCELLENT**

This project is essentially a **command-line/API wrapper** around an already sophisticated world management system. The heavy lifting (world creation, chunk storage, world generation, teleportation, spawns) is already done.

Your mod's value-add:
1. **Unified interface** - Single `/world` command family
2. **User-friendly** - Hide complexity behind simple commands
3. **Templates** - Pre-built world configurations
4. **Integration** - Tie into HyperPerms for access control
5. **Future-proof** - Interface-based design survives worldgen v2

**Estimated Total Development Time:** 1-2 weeks for full MVP

The codebase quality is high and well-documented in the decompiled source. This is a green light project.
