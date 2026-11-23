# Overview

## What the game is

Demon Lord is a first-person 2.5D dungeon crawler game built with Java and LWJGL (OpenGL 3.3).
The world is tile-based, everything lives on a fixed 2D grid. From the
player's perspective it looks like a proper 3D corridor but the underlying logic is all
Two Dimensional.

currently it's just a mishmash of features that can hopefully coalesce into fun experience.

---

## Entry point and startup

`Main.java` does two things before the game even starts:

1. **Native extraction**: LWJGL needs platform-specific native libraries (DLLs on Windows,
   SOs on Linux, DYLIBs on macOS) to exist as real files on disk, not just inside a JAR.
   `extractNativeLibraries()` detects the OS, copies the correct files out of the JAR into a
   temp directory, and sets `org.lwjgl.librarypath` so LWJGL can find them. This is why the
   JAR artifacts bundle flat native files under `libs/native/`.

2. **Hands off to Game**: Creates a `Game` instance, registers a shutdown hook for cleanup,
   and calls `game.run()`.

`Game.init()` then does the actual setup in order:
- Create the GLFW `Window` (800x600, GL context)
- Load `LevelManager` from `levels.json`
- Init audio (OpenAL, fails gracefully if unavailable)
- Load base tile definitions into `TileRegistry`
- Register the `"reach"` objective condition factory into `Registries.CONDITIONS` (objectives are a bit of a work in progress)
- Create the `Player`
- Init `AchievementsManager`, unlock `"open_game"` achievement (achievements are also a WIP at the moment)
- Enter `MainMenuState`

---

## The game loop

```
Game.run()
  └── init()
  └── loop()
        while running and window not closed:
          deltaTime = elapsed since last frame (seconds, as float)
          currentState.update(deltaTime)
          AudioSystem.update(deltaTime)
          render()  →  glClear → currentState.render() → versionOverlay.render()
          window.update()  →  swap buffers, poll GLFW events
  └── cleanup()
```

No fixed timestep — delta time is passed through to everything. The version overlay renders
on top of every state (it's drawn in `Game.render()`, not inside individual states).

`cleanup()` is called both at the end of `loop()` and by the shutdown hook, so it runs
twice on a normal exit. Not a problem in practice but worth knowing.

---

## State machine

```
MainMenuState
  ├── Start Game  ──→  LevelSelectState
  ├── Achievements ──→ AchievementsState ──→ (back) MainMenuState
  └── Exit

LevelSelectState
  └── Select level ──→ reloadLevel() ──→ PlayingState

PlayingState
  ├── Complete all objectives + step on exit tile ──→ LevelCompleteState
  └── HP reaches 0 ──→ PlayerDefeatedState

LevelCompleteState
  ├── Enter (next level exists) ──→ reloadLevel() ──→ PlayingState
  └── Enter (no next level)    ──→ MainMenuState

PlayerDefeatedState
  ├── Enter (retry) ──→ reloadLevel() ──→ PlayingState
  └── Escape        ──→ MainMenuState
```

State transitions go through `Game.changeState()`, which calls `exit()` on the old state,
`enter()` on the new one, and resets the input edge/gate detectors so key presses from the
previous state don't bleed through.

`reloadLevel()` loads the map JSON, rebuilds the `Renderer`, and resets player position and
HP to full. It's called any time you start or restart a level.

---

## Major systems

| System | What it does | Doc |
|---|---|---|
| Map / Tiles | JSON map loading, tile grid, tile types, item/door data | `map-and-tiles.md` |
| Rendering | 3D scene (walls, floor, enemies, items), 2D overlays | `rendering.md` |
| UI Overlays | HP, timer, inventory, menus, death/complete screens | `ui-overlays.md` |
| AI / Behavior Trees | Enemy decision-making via JSON-driven behavior trees | `ai.md` |
| AI Sensors & Conditions | Vision, hearing, suspicion; BT condition types | `ai-sensors-conditions.md` |
| Pathfinding | A* on tile grid, patrol waypoints, random movement | `pathfinding.md` |
| World Entities | Player, enemies, combat, enemy manager update loop | `world-entities.md` |
| Inventory & Items | Item slots, stacking, pickup, item definitions | `inventory-and-items.md` |
| Objectives & Events | EventBus, objective conditions, level completion logic | `objectives-and-events.md` |
| Physics & Input | Tile collision, keyboard handling, input debouncing | `physics-and-input.md` |
| Audio | OpenAL music and SFX, sounds.json | `audio.md` |
| Achievements | Achievement data, persistence, unlock flow | `achievements.md` |
| Config & Build | Constants, version string, platform JARs, build scripts | `config-and-build.md` |

---

## Key data flow during gameplay

This is what happens each frame while `PlayingState` is active:

```
PlayingState.update(dt)
  ├── InputGate / InputEdge checks (debounce)
  ├── KeyboardInput → move player (WASD) + rotate (arrow keys / AD)
  ├── CollisionHandler → clamp player position away from walls
  ├── Tile under player:
  │     ├── hasItem?  → auto-add to Inventory, clear tile
  │     ├── E key + openable? → toggle tile open/closed (door)
  │     └── endsLevel + all mandatory objectives complete? → LevelCompleteState
  ├── F key → melee attack (0.5s cooldown, 1.5-tile range, 100° arc)
  │     └── EnemyEntity.takeDamage() → ENEMY_KILLED event if dead
  ├── EnemyManager.update(dt)
  │     ├── Each enemy: BehaviorController tick → sensors → AI decision → movement
  │     ├── Enemy-player contact → tryAttack() → PLAYER_TOOK_DAMAGE event
  │     ├── Enemy spots player → ENEMY_SPOTTED_PLAYER event (transition only)
  │     └── removeIf dead enemies
  ├── player.isAlive()? → false → PlayerDefeatedState
  ├── ObjectiveManager → check conditions, queue completion popups
  └── TimerService.update(dt) → level + cumulative elapsed time
```

Events posted during update (e.g. `PLAYER_TOOK_DAMAGE`, `ENEMY_KILLED`) are picked up by
listeners registered in `PlayingState.enter()` — these drive audio cues, toast messages,
and achievement unlocks.

---

## Known issues / gotchas

- **`Game.cleanup()` called twice** on normal exit (end of loop + shutdown hook). Harmless
  but something to be aware of if you ever add cleanup that shouldn't run twice.

- **Objectives registry** — `Registries.CONDITIONS` only has `"reach"` registered. Any
  objective in a map JSON using a different condition type will silently fail to instantiate.

- **`ScanAction`** unsafely casts `IAIAgent` to `EnemyEntity` — will throw `ClassCastException`
  if anything other than `EnemyEntity` ever runs a scan action.

- **`BaseEnemy.hasLineOfSight()`** is a stub that throws `UnsupportedOperationException`.
  It's never called in practice (LOS lives in `WorldAdapter`) but it's a trap if you forget.

- **Duplicate objective IDs** in `level1/map.json` — both use `"reach.specialTile"`. Won't
  cause a crash but means the objective manager can't distinguish them.

- **Scan timer mid-run** — when the reactive Selector interrupts a scan action and it
  re-enters later, the timer resumes from where it left off rather than resetting. Minor
  visual weirdness.
