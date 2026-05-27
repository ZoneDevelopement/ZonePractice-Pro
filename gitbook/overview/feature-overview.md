# Feature Overview

## Core gameplay systems

- **13 ladder types**: BASIC, BUILD, SUMO, TNT_SUMO, BOXING, PEARL_FIGHT, SPLEEF, SKYWARS, BEDWARS, FIREBALL_FIGHT, MLG_RUSH, BRIDGES, BATTLE_RUSH
- Ladders with ranked/unranked support and per-ladder settings (hearts, cooldowns, effects, match types)
- Arenas with build/non-build logic and async arena copy handling (FAWE supported)
- **7 event modes**: LMS, OITC, TNTTag, Brackets, Sumo, Splegg, Juggernaut
- **3 party modes**: Party FFA, Party Split, Party vs Party
- FFA arenas with join/leave/kit/spectate flows
- Auto-scheduled events via `AUTO-EVENTS` config

## Queue systems

- Combined ranked/unranked queue GUI with switcher
- Multi-queue: queue for multiple ladders simultaneously (`zpp.queue.multi`)
- Division-based queue search narrowing
- Custom kit queue (`/customqueue`) — host or join custom-kit matchmaking
- Category-based ladder selection (PVP / MINIGAMES)
- Dynamic ELO range expansion over time for ranked matching

## Player progression and data

- Division/rank progression (`divisions.yml`) with ranked requirements
- Match history (`/matchhistory`) — last 5 matches per player
- Per-ladder and global statistics (wins, losses, elo, streaks, kills)
- Optional MySQL/MariaDB support with HikariCP connection pooling
- Leaderboard holograms (global, ladder static, ladder dynamic)
- 8 leaderboard types: ELO, Wins, Kills, Deaths, Win Streak, Best Win Streak, Lose Streak, Best Lose Streak

## Player customization

- Cosmetics GUI (`/cosmetics`): armor trims (5 tiers + all patterns/materials), death effects (11+ effects), shield banner layouts (up to 6 layers), lobby movement cosmetics (wind charge, trident, spear)
- Custom player kits via `/editor` — full inventory editor with armor, weapons, tools, bows, potions, shulker boxes
- Kit sharing via codes (`/copykit <code>`)
- Name formatting (`/nick`) with MiniMessage display name support
- Player settings GUI (`/settings`) — toggle duel requests, scoreboard, private messages, party invites, player hider, spectate, world time, prefix visibility, fly

## Admin and moderation

- Setup manager GUI (`/setup`)
- Staff mode (`/staff`) with vanish, fly, staff chat, force end, follow
- Extended `/practice` admin subcommands: `lobby`, `arenas`, `teleport`, `info`, `rename`, `elo`, `ranked`, `unranked`, `exp`, `reset`, `nametag`, `reload`, `hologram`, `stop`
- Queue ignore toggle (`/ignorequeue <player>`)
- OP bypass controls per-feature in config
- Auto-save with configurable interval
- bStats and FastStats analytics
