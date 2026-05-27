# Feature Overview

## Core gameplay systems

- Ladders with ranked/unranked support and per-ladder settings
- Arenas with build/non-build logic and async arena copy handling
- Events: LMS, OITC, TNTTag, Brackets, Sumo, Splegg, Juggernaut
- Party modes: FFA, split, and party-vs-party
- FFA arenas with join/leave/kit/spectate flows

## Player progression and data

- Division/rank progression (`divisions.yml`)
- Match history (`/matchhistory`)
- Optional MySQL support
- Leaderboards and holograms

## Player customization

- Custom kits and custom queue flow (`/customqueue`)
- Cosmetics GUI (`/cosmetics`): armor trims, death effects, shield layouts, lobby movement items
- Name formatting command (`/nick`) with permission-based controls

## Admin and moderation

- Setup manager GUI (`/setup`)
- Staff mode (`/staff`)
- Queue ignore toggle (`/ignorequeue <player>`)
- Extended `/practice` admin subcommands (`elo`, `ranked`, `unranked`, `exp`, `reset`, `nametag`, `reload`)
