---
applyTo: '**'
---

# Unlucky - AI Coding Assistant Instructions

## Project Overview
Unlucky is a LibGDX-based Android RPG game centered around RNG (random number generator) mechanics. The player ventures through worlds, battles monsters, and collects items in a turn-based combat system where everything is influenced by randomness.

## Architecture & Structure

### Multi-Platform LibGDX Structure
- **`core/`** - Main game logic (shared across platforms)
- **`android/`** - Android-specific launcher and assets
- **`desktop/`** - Desktop launcher (builds to runnable .jar)
- **Assets location**: All game assets are in `android/assets/` (textures, music, maps, data files)

### Core Package Organization (`core/src/com/unlucky/`)
- **`main/`** - `Unlucky.java` is the main game class managing screens and global state
- **`screen/`** - LibGDX screen implementations (MenuScreen, GameScreen, etc.)
- **`battle/`** - Turn-based combat system (Move, Moveset, SpecialMove, StatusEffect)
- **`entity/`** - Player and Enemy classes with stats and behaviors
- **`map/`** - World/Level system, tile-based maps (TileMap, GameMap, Level, World)
- **`inventory/`** - Item system, equipment, shop mechanics
- **`resource/`** - ResourceManager handles all asset loading via AssetManager
- **`save/`** - JSON-based save system with Base64 encoding

### Key Game Systems

#### Combat System
Turn-based with 4 randomly generated moves per turn, color-coded by type:
- Red = Attack moves, Blue = Magic moves, Yellow = Accurate moves, Green = Healing/buff moves
- Special moves provide bonus effects and are player-selectable
- Status effects system for buffs/debuffs

#### Map System
- 3 worlds with 10+ levels each, defined in `assets/maps/worlds.json`
- Tile-based maps stored as text files (`w{world}_l{level}.txt`)
- Star tile = level completion objective
- Weather system affects gameplay

#### Item & Economy System  
- 100+ items with rarity levels (rare0-rare4) in `assets/items/items.json`
- Shop system with separate `shopitems.json`
- Equipment enchanting system to upgrade items
- Item drops from monsters based on level/rarity

## Development Workflows

### Building & Testing
```bash
# Android build and run (use existing task or run-android.bat)
./gradlew :android:assembleDebug
adb install -r android/build/outputs/apk/debug/android-debug.apk
adb shell am start -n com.unlucky.main/com.unlucky.main.AndroidLauncher

# Desktop build
./gradlew :desktop:build
# Output: desktop/build/libs/desktop-1.0.jar
```

### Asset Management
- **Texture Atlas**: All sprites packed into `textures.atlas` + `textures.png`
- **Audio**: Music (.ogg) in `music/`, SFX in `sfx/`
- **Data Files**: JSON for items, moves, worlds configuration
- **Maps**: Text-based tile definitions in `maps/`

### Save System Details
- Save file: `save.json` (Base64 encoded) in game directory
- Handled by `Save.java` class with `PlayerAccessor` for serialization
- Must maintain save compatibility when modifying player data structures

## Code Conventions & Patterns

### LibGDX Patterns
- Screen-based architecture with `Unlucky.java` as central game coordinator
- ResourceManager singleton pattern for asset access
- SpriteBatch rendering with proper disposal
- Stage/Actor UI system for menus and interfaces

### Game-Specific Conventions
- All dimensions use game units (V_WIDTH=200, V_HEIGHT=120, V_SCALE=6)
- Entity stats: hp, exp, accuracy, damage, level
- Color-coded move types (0=red, 1=blue, 2=yellow, 3=green)
- JSON data loading patterns in ResourceManager
- Tile indices and sprite atlas coordinates hardcoded by design

### Data File Patterns
- JSON arrays with rarity levels (`rare0`, `rare1`, etc.)
- Level progression: `minLevel`/`maxLevel` ranges for item drops
- World/level organization: `w{world}_l{level}` naming convention

## Testing & Debugging
- FPS counter available in settings (`player.settings.showFps`)
- GLProfiler integration for rendering performance analysis
- Logcat output capture: `adb logcat -d > logcat.txt`
- Desktop version creates save.json in same directory as jar

## Platform Considerations
- **Android**: Primary target platform, uses Android-specific file handling
- **Desktop**: Secondary platform for development/testing, different save location
- **Asset sharing**: Android assets folder shared across platforms
- **Resolution scaling**: Fixed game dimensions with scaling factor