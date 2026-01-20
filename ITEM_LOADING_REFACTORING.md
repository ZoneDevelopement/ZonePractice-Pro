# Item Loading Refactoring - Version-Specific Implementation

## Problem Statement

The previous `BackendUtil` class had a single implementation for loading items from configuration files. This caused
visual issues on modern Minecraft versions (1.13+):

- The `DAMAGE` value (used for item data/colors in 1.8) was being applied as actual durability damage in modern versions
- This resulted in ugly durability bars appearing on lobby items (e.g., colored wool, stained glass)
- Items looked damaged/broken in the GUI when they should appear pristine

## Solution Overview

Refactored the item loading logic into a **version-specific abstraction layer** that properly handles the differences
between legacy (1.8.8) and modern (1.13+) Minecraft versions.

## Architecture

### 1. Core Module - Interface Definition

**File:** `core/src/main/java/dev/nandi0813/practice/module/interfaces/ConfigItemProvider.java`

```java
public interface ConfigItemProvider {
    GUIItem getGuiItem(YamlConfiguration config, String loc);
}
```

This interface defines the contract for loading GUI items from configuration files.

### 2. Spigot_1_8_8 Module - Legacy Implementation

**File:** `spigot_1_8_8/src/main/java/dev/nandi0813/practice_1_8_8/interfaces/LegacyConfigItemProvider.java`

**Behavior:**

- Preserves original 1.8.8 logic
- **Uses DAMAGE value** from config for item data (colors, variants, subtypes)
- Example: `DAMAGE: 5` on wool = Lime Wool
- No special handling needed for durability bars (they don't exist in 1.8 for non-damageable items)

### 3. Spigot_Modern Module - Modern Implementation

**File:** `spigot_modern/src/main/java/dev/nandi0813/practice_modern/interfaces/ModernConfigItemProvider.java`

**Behavior:**

- **Ignores DAMAGE value** from config (1.13+ removed data values via "The Flattening")
- **Sets items as unbreakable** to prevent durability bars from showing
- **Adds ItemFlags:**
    - `HIDE_UNBREAKABLE` - Hides the unbreakable tag from tooltip
    - `HIDE_ATTRIBUTES` - Hides attribute modifiers
- Result: Clean, pristine-looking lobby items without visual damage

### 4. Core Module - GUIItem Enhancements

**File:** `core/src/main/java/dev/nandi0813/practice/manager/gui/GUIItem.java`

**Changes:**

- Added `unbreakable` boolean field with getter/setter
- Updated `get()` method to apply unbreakable status via `LadderUtil.setUnbreakable()`
- Updated `cloneItem()` method to preserve unbreakable state

### 5. Integration with ClassImport System

**Files Updated:**

- `core/src/main/java/dev/nandi0813/practice/module/util/Classes.java`
    - Added `getConfigItemProvider()` method to interface

- `spigot_1_8_8/src/main/java/dev/nandi0813/practice_1_8_8/Classes.java`
    - Instantiates `LegacyConfigItemProvider`

- `spigot_modern/src/main/java/dev/nandi0813/practice_modern/Classes.java`
    - Instantiates `ModernConfigItemProvider`

### 6. BackendUtil Simplification

**File:** `core/src/main/java/dev/nandi0813/practice/manager/backend/BackendUtil.java`

**Before:** 93 lines of item loading logic
**After:** 26 lines - delegates to version-specific provider

```java
public static GUIItem getGuiItem(YamlConfiguration config, String loc) {
    return ClassImport.getClasses().getConfigItemProvider().getGuiItem(config, loc);
}
```

## Benefits

### 1. **Version-Specific Behavior**

- 1.8.8: Items work exactly as before with data values
- Modern: Items appear clean without durability bars

### 2. **Separation of Concerns**

- Version-specific logic is isolated in appropriate modules
- Core module remains version-agnostic

### 3. **Maintainability**

- Easy to add new version-specific behaviors
- Clear where to make changes for each version
- Follows existing ClassImport pattern used throughout the plugin

### 4. **No Breaking Changes**

- External API remains identical (`BackendUtil.getGuiItem()`)
- Existing configuration files work without modification
- Backward compatible with all existing code

## Configuration File Format

No changes required to existing config files. They continue to work as before:

```yaml
EXAMPLE_ITEM:
  NAME: "&aExample Item"
  MATERIAL: WOOL
  DAMAGE: 5        # Used in 1.8.8 for lime wool; ignored in modern
  AMOUNT: 1
  LORE:
    - "&7Line 1"
    - "&7Line 2"
  FLAGS:
    - HIDE_ENCHANTS
  ENCHANTMENTS:
    - DURABILITY:1
```

## Testing Recommendations

1. **Test on 1.8.8 Server:**
    - Verify colored items (wool, glass, etc.) show correct colors
    - Verify DAMAGE values are applied correctly

2. **Test on Modern Server (1.20+):**
    - Verify lobby items don't show durability bars
    - Verify items look clean and pristine
    - Verify unbreakable tag is hidden from tooltips

3. **Test GUI Items:**
    - Leaderboard GUIs
    - Kit editor items
    - Lobby selector items
    - Any items loaded via `BackendUtil.getGuiItem()`

## Migration Notes

- **No manual migration required** - the refactoring is transparent to existing code
- Old configuration files continue to work
- The system automatically selects the correct implementation based on server version

## Future Enhancements

This architecture makes it easy to:

- Add custom item handling for specific Minecraft versions
- Implement version-specific material mappings
- Handle future Minecraft API changes cleanly
- Add new item properties without modifying core logic

---

**Date:** January 20, 2026  
**Status:** ✅ Complete  
**Modules Affected:** Core, Spigot_1_8_8, Spigot_Modern
