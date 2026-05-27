---
description: Overview of generated files and what each one controls.
---

# Configuration Files

## Auto-generated files

On startup, ZonePractice maintains these files in `plugins/ZonePracticePro/`:

- `config.yml`
- `language.yml`
- `guis.yml`
- `inventories.yml`
- `sidebar.yml`
- `groups.yml`
- `divisions.yml`
- `playerkit.yml`
- `backend.yml`
- `ladders/*.yml`

## File purposes

- `config.yml`: Core behavior, queue/event/match settings, optional integrations, database options
- `language.yml`: All messages (MiniMessage format)
- `guis.yml`: GUI definitions and configurable GUI items
- `inventories.yml`: Lobby/staff/party/loadout inventory items
- `sidebar.yml`: Sidebar templates and lines
- `groups.yml`: Group metadata and per-group limits
- `divisions.yml`: Division progression model and thresholds
- `playerkit.yml`: Custom player kit limits/settings
- `backend.yml`: Runtime backend state and internal persisted values
- `ladders/*.yml`: Individual ladder definitions

## Item configuration format

Most configurable GUI/inventory items follow sections like:

- `MATERIAL`
- `NAME`
- `LORE`
- `AMOUNT`
- `ENCHANTMENTS`
- `ITEMFLAGS`

If you want an item hidden from a GUI, keep it in the file and disable/move it using its slot logic instead of deleting unknown keys.

## Update behavior

ZonePractice uses versioned config updates and preserves unknown custom keys in `config.yml` where possible.

## Sidebar placeholders

Match/spectator placeholders are documented in this file’s sidebar section and in the Placeholder API page when used through PlaceholderAPI.
