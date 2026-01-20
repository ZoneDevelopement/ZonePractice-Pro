# Version-Specific Item Loading - Quick Reference

## How It Works

The refactored system automatically selects the correct item loading implementation based on the server version:

```
BackendUtil.getGuiItem(config, "ITEMS.EXAMPLE")
         ↓
ClassImport.getClasses().getConfigItemProvider()
         ↓
    [Version Check]
         ↓
    ┌────┴────┐
    ↓         ↓
1.8.8     Modern (1.13+)
    ↓         ↓
LegacyConfigItemProvider  ModernConfigItemProvider
    ↓         ↓
Uses DAMAGE   Ignores DAMAGE
for colors    Sets Unbreakable
              Hides flags
```

## Example Configuration

```yaml
LOBBY_SELECTOR:
  NAME: "&6&lPlay Game"
  MATERIAL: DIAMOND_SWORD
  DAMAGE: 0
  AMOUNT: 1
  LORE:
    - "&7Click to join a match!"
  FLAGS:
    - HIDE_ENCHANTS
  ENCHANTMENTS:
    - DURABILITY:1

COLORED_WOOL:
  NAME: "&aLime Wool"
  MATERIAL: WOOL
  DAMAGE: 5        # 1.8.8: Lime color | Modern: Ignored
  AMOUNT: 1
```

## Usage Examples

### 1. Basic Usage (Most Common)

```java
// Through GUIFile (recommended)
GUIItem item = GUIFile.getGuiItem("GUIS.MAIN.ICONS.PLAY");
ItemStack stack = item.get();

// Through ConfigFile
GUIItem item = configFile.getGuiItem("ITEMS.SELECTOR");
ItemStack stack = item.get();

// Direct usage
GUIItem item = BackendUtil.getGuiItem(config, "ITEMS.EXAMPLE");
ItemStack stack = item.get();
```

### 2. Setting Items in Inventory

```java

@Override
public void build() {
    Inventory inventory = gui.get(1);

    // Single item
    inventory.setItem(0, GUIFile.getGuiItem("GUIS.MAIN.ICONS.PLAY").get());

    // With modifications
    GUIItem item = GUIFile.getGuiItem("GUIS.MAIN.ICONS.SETTINGS");
    item.replace("%player%", player.getName());
    inventory.setItem(8, item.get());
}
```

## Version-Specific Behavior

### On 1.8.8 Server (Legacy)

```java
// Config: MATERIAL: WOOL, DAMAGE: 5
GUIItem item = BackendUtil.getGuiItem(config, "ITEMS.WOOL");
ItemStack stack = item.get();

// Result:
// - Material: WOOL
// - Data Value: 5 (Lime color)
// - Unbreakable: false (not needed in 1.8)
// - No special flags added
```

### On 1.20+ Server (Modern)

```java
// Config: MATERIAL: DIAMOND_SWORD, DAMAGE: 5
GUIItem item = BackendUtil.getGuiItem(config, "ITEMS.SWORD");
ItemStack stack = item.get();

// Result:
// - Material: DIAMOND_SWORD
// - Data Value: IGNORED (would cause durability bar)
// - Unbreakable: true (prevents durability bar)
// - Flags: HIDE_UNBREAKABLE, HIDE_ATTRIBUTES
// - Clean visual appearance, no damage bar
```

## Common Use Cases

### Lobby Items

```yaml
LOBBY_PLAY:
  NAME: "&a&lPlay"
  MATERIAL: DIAMOND_SWORD
  LORE:
    - "&7Click to play!"
```

**Result:**

- 1.8.8: Normal sword
- Modern: Unbreakable sword, no durability bar

### Colored Items (1.8 Style)

```yaml
RED_WOOL:
  NAME: "&cRed Team"
  MATERIAL: WOOL
  DAMAGE: 14  # Red in 1.8
```

**Result:**

- 1.8.8: Red wool
- Modern: White wool (DAMAGE ignored, use MATERIAL: RED_WOOL instead)

### GUI Decorations

```yaml
FILLER:
  NAME: " "
  MATERIAL: STAINED_GLASS_PANE
  DAMAGE: 7  # Light gray in 1.8
```

**Result:**

- 1.8.8: Light gray stained glass pane
- Modern: White glass pane (use MATERIAL: GRAY_STAINED_GLASS_PANE)

## Migration Tips for Modern Servers

If you're updating configs for modern servers, replace old DAMAGE-based items:

### Old Style (1.8)

```yaml
ITEM:
  MATERIAL: WOOL
  DAMAGE: 5  # Lime
```

### New Style (1.13+)

```yaml
ITEM:
  MATERIAL: LIME_WOOL  # No DAMAGE needed
```

**Note:** The refactoring handles both automatically! No immediate changes required.

## Troubleshooting

### Issue: Items show durability bars on modern servers

**Solution:** This refactoring fixes it automatically. Items are now unbreakable by default.

### Issue: Wrong color on modern servers

**Check:**

1. Material name changed in 1.13 (WOOL → LIME_WOOL, etc.)
2. DAMAGE value is ignored on modern - update MATERIAL instead

### Issue: Items look different between versions

**This is expected:**

- 1.8: Uses DAMAGE for variants
- Modern: Uses specific material names
- Consider maintaining separate config files for different versions if exact parity is needed

## Testing Checklist

- [ ] Test on 1.8.8: Colored items show correct colors
- [ ] Test on Modern: No durability bars on lobby items
- [ ] Test on Modern: Items look clean and pristine
- [ ] Test GUI items: Selectors, decorations, buttons
- [ ] Test custom items: Kits, arenas, events

---

**Documentation Date:** January 20, 2026  
**Related Files:**

- `ITEM_LOADING_REFACTORING.md` - Full technical details
- Core: `BackendUtil.java`, `ConfigItemProvider.java`, `GUIItem.java`
- Legacy: `LegacyConfigItemProvider.java`
- Modern: `ModernConfigItemProvider.java`
