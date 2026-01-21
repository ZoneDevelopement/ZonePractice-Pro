# Spawn Marker Visual Guide

## What You'll See In-Game

### Standard Arena (Duel/1v1)

When you enter setup mode for a standard arena:

```
        Spawn 1 (Right-click to remove)
                    ↓
            [Player Head]
            [  Body   ]
            [  Sword  ]
            [Red Boots]
            
            
        Spawn 2 (Right-click to remove)
                    ↓
            [Player Head]
            [  Body   ]
            [  Sword  ]
            [Red Boots]
```

### FFA Arena

When you enter setup mode for an FFA arena with 5 spawns:

```
   FFA Spawn #1 (Right-click to remove)
                ↓
        [Player Head]
        [  Body   ]
        [  Sword  ]
        [Red Boots]


   FFA Spawn #2 (Right-click to remove)
                ↓
        [Player Head]
        [  Body   ]
        [  Sword  ]
        [Red Boots]


   FFA Spawn #3 (Right-click to remove)
                ↓
        [Player Head]
        [  Body   ]
        [  Sword  ]
        [Red Boots]

... and so on up to #18
```

## What Each Part Means

### The Label (Above the Armor Stand)
- **Color**: Red & Bold for spawn number, Gray for instruction
- **Text**: Shows spawn identifier and removal instructions
- **Position**: Floats above the armor stand (visible from far away)

Examples:
- Standard: `Spawn 1 (Right-click to remove)`
- FFA: `FFA Spawn #7 (Right-click to remove)`

### The Visual Elements

1. **Player Head** (Steve head)
   - Shows this represents a player spawn
   - Makes it obvious at a glance

2. **Body** (Visible armor stand body)
   - Shows the exact size/space the player will occupy
   - Helps you position spawns accurately

3. **Sword** (Diamond sword in hand)
   - Held in natural position
   - Shows the direction the player will be facing

4. **Red Boots** (Bright red leather boots)
   - High visibility from any distance
   - Easy to spot when flying above

## Label Colors Explained

The labels use Minecraft color codes:
- `&c&l` = **Red Bold** for the spawn identifier (e.g., "Spawn 1", "FFA Spawn #3")
- `&7` = **Gray** for the instruction text (e.g., "(Right-click to remove)")

In-game this appears as:
**Spawn 1** (Right-click to remove)
  ↑ Red Bold    ↑ Gray

## Why This Design?

✅ **Self-documenting** - The label tells you exactly how to remove it
✅ **Highly visible** - Red boots + floating label easy to see from far away
✅ **Realistic** - Shows exactly how a player will spawn (position, size, facing)
✅ **Clear instructions** - No need to remember commands or hotkeys

## Flying Setup View

When you fly above your arena:

```
                     (You're flying here)
                            👤
                            |
                            | (Looking down)
                            ↓
    
    FFA Spawn #1 (Right-click to remove)
            [🟥]    ← Red boots visible
            
    FFA Spawn #2 (Right-click to remove)
            [🟥]
            
    FFA Spawn #3 (Right-click to remove)  ← This one looks wrong
            [🟥]
            
    FFA Spawn #4 (Right-click to remove)
            [🟥]
```

Just look at the one you want to remove and right-click!
