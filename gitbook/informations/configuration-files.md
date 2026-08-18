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
- `match-history/` (per-player match history files)

## File details

- `config.yml`: Core behavior, queue/event/match settings, integrations, database, knockback, sounds, leaderboard format, cooldowns, fireball/tntsumo/boxing physics — versioned auto-updates, preserves unknown keys
- `language.yml`: All messages in MiniMessage format. GUI titles, item names, lores, chat and action bar messages are all configurable.
- `guis.yml`: GUI definitions with slot layout, icon materials, names, and lores for all plugin GUIs.
- `inventories.yml`: Lobby, FFA, queue, spectator, and staff inventory items with slot positions.
- `sidebar.yml`: Sidebar templates per state — lobby, queue, match, spectate, FFA, staff.
- `groups.yml`: Group metadata (prefix, suffix, name color, sort priority) and per-group match limits.
- `divisions.yml`: Division progression — thresholds, names, colors, and ranked queue requirements.
- `playerkit.yml`: Custom player kit slot limits, shared kit settings.
- `backend.yml`: Runtime backend state — do not edit manually.
- `ladders/*.yml`: Individual ladder definitions — type, icon, inventory, effects, settings, match types. Defaults: `archer`, `axe`, `battlerush`, `bedwars`, `boxing`, `bridges`, `builduhc`, `cart`, `crystal`, `diamondpotion`, `fireball`, `gapple`, `mace`, `mlgrush`, `netheritepotion`, `pearlfight`, `sg`, `skywars`, `soup`, `spearelytra`, `spearmace`, `spleef`, `sumo`, `sword`, `tntsumo`.
- `match-history/<uuid>.yml`: Per-player recent match records (also stored in MySQL if enabled).

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

ZonePractice uses versioned config updates (config version key). Unknown custom keys in `config.yml` are preserved during auto-updates. Always back up `plugins/ZonePracticePro/` before major version upgrades.

## Sidebar configuration

### `SIDEBAR-NAME-FORMAT`

Controls how player names are rendered in all sidebar placeholders. Set this in `sidebar.yml`:

```yaml
# Options:
#   NAME_ONLY            - Just the player name (no prefix, no suffix)
#   PREFIX_NAME          - Prefix + Name (no suffix)
#   NAME_SUFFIX          - Name + Suffix (no prefix)
#   PREFIX_NAME_SUFFIX   - Full format: Prefix + Name + Suffix (default)
SIDEBAR-NAME-FORMAT: PREFIX_NAME_SUFFIX
```

This setting applies globally to all sidebar name placeholders. It does **not** affect chat, tab, or nametag formatting, which are controlled by the player's `/settings` prefix visibility option.

### Placeholders

These placeholders are available in `sidebar.yml`:

| Placeholder | Description | Respects `SIDEBAR-NAME-FORMAT` |
| --- | --- | :-: |
| `%player%` | Your name | ✅ |
| `%playerNameOnly%` | Your name (always prefix/suffix-free) | ❌ |
| `%enemyName%` | Enemy name | ✅ |
| `%enemyNameOnly%` | Enemy name (always prefix/suffix-free) | ❌ |
| `%partyLeader%` | Party leader name | ✅ |
| `%partyLeaderNameOnly%` | Party leader name (always prefix/suffix-free) | ❌ |
| `%player1%` | Player 1 name | ✅ |
| `%player1NameOnly%` | Player 1 name (always prefix/suffix-free) | ❌ |
| `%player2%` | Player 2 name | ✅ |
| `%player2NameOnly%` | Player 2 name (always prefix/suffix-free) | ❌ |
| `%player1boxing%` / `%player2boxing%` / `%player3boxing%` | Boxing top player names | ✅ |
| `%topPlayer%` | Event highest-point player (OITC) | ✅ |
| `%enemy%` | Event opponent (Brackets/Sumo) | ✅ |
| `%playerTeamName%` / `%playerTeamColor%` | Your team display name / color | ❌ |
| `%enemyTeamName%` / `%enemyTeamColor%` | Enemy team display name / color | ❌ |
| `%team%` / `%teamColor%` | Your team name / color | ❌ |
| `%rounds%` / `%roundsNumber%` | Your round progress / won count | ❌ |
| `%enemyRounds%` / `%enemyRoundsNumber%` | Enemy round progress / won count | ❌ |
| `%enemyPing%` | Enemy ping | ❌ |
| `%onlinePlayers%` | Total online players | ❌ |
| `%inFightPlayers%` | Players in matches | ❌ |
| `%inQueuePlayer%` | Players in queue | ❌ |
| `%division%` / `%division_short%` | Your division (full / short) | ❌ |
| `%maxMember%` / `%members%` | Party max members / current members | ❌ |
| `%arena%` | Arena display name | ❌ |
| `%ladder%` | Ladder display name | ❌ |
| `%duration%` / `%roundDuration%` / `%matchDuration%` | Match/round duration | ❌ |
| `%ping%` | Your ping | ❌ |
| `%kills%` / `%deaths%` | Your kills/deaths (FFA) | ❌ |
| `%players%` / `%alivePlayers%` / `%spectators%` | Player counts | ❌ |
| `%hits%` / `%enemyHits%` / `%overAllHits%` | Boxing hit counts | ❌ |
