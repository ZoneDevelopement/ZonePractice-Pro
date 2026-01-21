# Arena Spawn Position Visual Markers

## Overview
Implemented a visual marker system for arena setup mode that displays armor stands at spawn positions. This helps administrators see exactly where players will spawn and allows for easy removal of individual FFA spawn points.

## Features

### 1. Visual Spawn Indicators
- **Armor Stand Markers**: Each spawn position is marked with an armor stand that looks like a player
- **Customized Appearance**:
  - **Player head** (Steve head) as helmet
  - Holds a diamond sword in natural holding position
  - **Red leather boots** for high visibility
  - Visible body to represent actual player size
  - Custom name label showing spawn number and instructions **above** the armor stand
  - Labels: "Spawn 1 (Right-click to remove)", "FFA Spawn #3 (Right-click to remove)", etc.
  - Non-collidable but full-size representation

### 2. Automatic Display
- **Auto-Show**: Markers automatically appear when entering arena setup mode
- **Auto-Hide**: Markers automatically disappear when:
  - Exiting setup mode
  - Arena is enabled
  - All admins leave setup mode

### 3. Real-Time Updates
- Markers update immediately when:
  - Adding new spawn positions
  - Removing spawn positions
  - Switching between setup modes

### 4. Direct Spawn Removal (FFA Arenas Only)
- **Click the armor stand**: Right-click directly on any FFA armor stand marker to remove that specific spawn
- **Works from any distance**: Perfect for flying setup - look at marker and click
- **FFA arenas only**: Standard arenas use left/right click on blocks to set positions
- **Protected**: Armor stands are invulnerable and cannot be manipulated
- **Validates mode**: Only works when in FFA Positions mode

### 5. Standard Arena Spawn Setting
- **Left-click block**: Sets Position 1 (Blue spawn)
- **Right-click block**: Sets Position 2 (Red spawn)
- **Visual markers**: Armor stands show spawn locations but are not clickable
- **Labels**: "Spawn 1" and "Spawn 2" (no removal instruction since they can't be clicked)

### 6. Armor Stand Protection
- **Invulnerable**: Cannot be destroyed by players
- **Non-Manipulable**: Cannot change equipment or pose
- **Click-Protected**: Only works in correct setup mode
- Automatically validates arena and mode before allowing removal

## Implementation Details

### New Classes

#### SpawnMarkerManager
**Location**: `/core/src/main/java/dev/nandi0813/practice/manager/arena/setup/SpawnMarkerManager.java`

**Key Methods**:
- `showMarkers(DisplayArena)` - Creates and displays all spawn markers for an arena
- `clearMarkers(DisplayArena)` - Removes all markers for a specific arena
- `updateMarkers(DisplayArena)` - Refreshes markers (clear + show)
- `findClosestFFASpawn(FFAArena, Location, double)` - Finds nearest FFA spawn within range
- `createMarker(Location, String)` - Creates a single armor stand marker

**Features**:
- Singleton pattern
- Tracks markers per arena
- Handles both standard arenas (2 spawns) and FFA arenas (up to 18 spawns)

### Modified Classes

#### ArenaSetupManager
**Changes**:
- Added `SpawnMarkerManager.getInstance().showMarkers(arena)` when starting setup
- Added `SpawnMarkerManager.getInstance().clearMarkers(arena)` when stopping setup
- Checks if other admins are still setting up before clearing markers

#### ArenaSetupListener  
**Changes**:
- `handleStandardPositions()` - Updates markers when setting spawn 1 or 2
- `handleFFAPositions()` - Enhanced with shift+right-click removal feature
  - Normal right-click: Add spawn (existing behavior)
  - Shift + right-click: Remove nearest spawn within 3 blocks (NEW)
  - Left-click: Remove last spawn (existing behavior)
- All spawn modifications now update markers in real-time

#### DisplayArena
**Changes**:
- `setEnabled()` - Clears markers when arena is enabled

### Interface Extensions

#### ArenaUtil Interface
**Added Method**: `setArmorStandItemInHand(ArmorStand, ItemStack, boolean)`
- Handles version differences between 1.8.8 and modern versions
- 1.8.8: Uses `setItemInHand()`
- Modern: Uses `setItem(EquipmentSlot)`

#### ItemMaterialUtil Interface
**Added Methods**:
- `getSword()` - Returns diamond sword ItemStack
- `getRedBlock()` - Returns red block ItemStack
  - 1.8.8: Red wool (damage value 14)
  - Modern: Red concrete

## Usage Guide

### For Administrators

#### Viewing Spawn Positions
1. Enter setup mode: `/arena setup <arena>`
2. Markers automatically appear at all configured spawn positions
3. Each marker shows:
   - **Player head** (looks like Steve)
   - **Red leather boots** (high visibility)
   - Diamond sword in hand
   - **Label above** the armor stand with spawn number
   - Full-size representation of how the player will spawn

#### Adding Spawns (Standard Arena)
1. Switch to "Positions" mode (shift + scroll wand)
2. Left-click block: Set spawn 1
3. Right-click block: Set spawn 2
4. Markers update automatically

#### Adding Spawns (FFA Arena)
1. Switch to "FFA Positions" mode
2. Right-click on a block: Add new spawn at that location
3. Marker appears immediately
4. Can add up to 18 spawns

#### Removing Specific Spawn (Direct Click) ⭐
1. In "Positions" or "FFA Positions" mode (depending on arena type)
2. **Right-click directly on the armor stand** marker you want to remove
3. Works from **any distance** - even while flying
4. That specific spawn is removed instantly
5. Perfect for precise spawn management

**Example**: Flying above your FFA arena, you see "FFA Spawn #3 (Right-click to remove)" is in a bad spot. Look at it and right-click → Removed!

#### Removing Last FFA Spawn
1. In "FFA Positions" mode
2. **Left-click** anywhere (existing feature)
3. Removes the last added spawn

#### Exiting Setup
1. Drop the wand (Q key), or
2. Disconnect from server
3. Markers are automatically cleaned up

## Technical Details

### Marker Properties
```java
armorStand.setVisible(true);         // Visible body to represent player
armorStand.setGravity(false);        // Floats in place
armorStand.setMarker(false);         // Full size, not tiny marker
armorStand.setBasePlate(false);      // No base plate
armorStand.setArms(true);            // Shows arms with sword
armorStand.setCustomNameVisible(true); // Shows name label above
armorStand.setHelmet(playerHead);    // Steve head
armorStand.setBoots(redBoots);       // Red leather boots
```

### Spawn Detection Algorithm
```java
// Finds closest spawn within 3 blocks using distance squared for efficiency
for each spawn in arena:
    distanceSq = spawn.distanceSquared(clickLocation)
    if distanceSq < (3.0 * 3.0) and distanceSq < closestDistanceSq:
        closestIndex = current spawn index
```

### Version Compatibility

#### 1.8.8
- Steve player head (skull item damage 3)
- Red leather boots (colored)
- Diamond sword in hand
- Uses `setItemInHand()`

#### Modern (1.13+)
- Player head item (default Steve)
- Red leather boots (colored)
- Diamond sword in hand
- Uses `setItem(EquipmentSlot.HAND)`

## Benefits

1. **Visual Clarity**: Administrators can immediately see all spawn positions
2. **Realistic Preview**: Armor stand looks like an actual player with head and boots
3. **Easy Identification**: Label positioned above the armor stand for clear visibility
4. **Easier Setup**: No guessing where spawns are located
5. **Direction Indicator**: Sword and body orientation shows spawn facing direction
6. **Flexible Editing**: Easy to remove specific spawns instead of only the last one
7. **No Manual Cleanup**: Markers auto-remove when setup ends or arena is enabled
8. **Multi-Admin Safe**: Markers persist until all admins leave setup mode

## Files Created
1. `/core/src/main/java/dev/nandi0813/practice/manager/arena/setup/SpawnMarkerManager.java`

## Files Modified
1. `/core/src/main/java/dev/nandi0813/practice/manager/arena/setup/ArenaSetupManager.java`
2. `/core/src/main/java/dev/nandi0813/practice/manager/arena/setup/ArenaSetupListener.java`
3. `/core/src/main/java/dev/nandi0813/practice/manager/arena/arenas/interfaces/DisplayArena.java`
4. `/core/src/main/java/dev/nandi0813/practice/module/interfaces/ArenaUtil.java`
5. `/core/src/main/java/dev/nandi0813/practice/module/interfaces/ItemMaterialUtil.java`
6. `/spigot_modern/src/main/java/dev/nandi0813/practice_modern/interfaces/ArenaUtil.java`
7. `/spigot_modern/src/main/java/dev/nandi0813/practice_modern/interfaces/ItemMaterialUtil.java`
8. `/spigot_1_8_8/src/main/java/dev/nandi0813/practice_1_8_8/interfaces/ArenaUtil.java`
9. `/spigot_1_8_8/src/main/java/dev/nandi0813/practice_1_8_8/interfaces/ItemMaterialUtil.java`

## Testing Checklist

- [ ] Enter setup mode for standard arena - verify 2 spawn markers appear
- [ ] Enter setup mode for FFA arena - verify all FFA spawn markers appear
- [ ] Verify armor stands have **player heads** (Steve head)
- [ ] Verify armor stands have **red leather boots**
- [ ] Verify **label appears above** the armor stand (not below)
- [ ] Verify armor stand body is visible (not invisible)
- [ ] **Try to punch armor stand** - verify it cannot be damaged
- [ ] **Try to manipulate armor stand** (change equipment) - verify it's blocked
- [ ] Add spawn by right-clicking block - verify marker appears
- [ ] **Right-click directly on armor stand from close** - verify spawn is removed
- [ ] **Right-click directly on armor stand while flying** - verify spawn is removed
- [ ] Right-click on block (not armor stand) - verify new spawn is added
- [ ] Left-click anywhere - verify last spawn is removed
- [ ] Try to click armor stand when NOT in setup mode - verify error message
- [ ] Try to click armor stand in wrong mode - verify error message
- [ ] Exit setup mode - verify all markers are removed
- [ ] Enable arena - verify markers are removed
- [ ] Multiple admins in setup - verify markers persist until last admin leaves
- [ ] Test on 1.8.8 server - verify player head and red boots
- [ ] Test on modern server - verify player head and red boots

## Future Enhancements
- Add configurable marker appearance (sword type, helmet color)
- Add click-to-teleport functionality
- Add spawn numbering in FFA mode
- Add distance indicators between spawns
