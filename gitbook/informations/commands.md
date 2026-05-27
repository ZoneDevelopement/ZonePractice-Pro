# Commands

This page documents all current commands, aliases, and major subcommands.

## Core commands

| Command | Aliases | Description | Permission |
| --- | --- | --- | --- |
| `/setup` | | Open setup manager GUI | `zpp.setup` |
| `/practice` | `prac`, `zonepractice`, `zoneprac`, `zonep` | Main utility/admin command family | subcommand-based |
| `/arena` | | Arena management | subcommand-based |
| `/ladder` | | Ladder management | subcommand-based |
| `/event` | `e` | Event setup/host/join/stop | subcommand-based |
| `/staff` | `staffmode` | Staff mode (vanish, chat, force end, follow) | `zpp.staff` |
| `/settings` | | Player settings GUI | default |

## Player queue and match commands

| Command | Aliases | Description | Permission |
| --- | --- | --- | --- |
| `/unranked` | | Open unranked queue GUI | default |
| `/ranked` | | Open ranked queue GUI | default |
| `/duel <player>` | | Send duel request | `zpp.duel` |
| `/accept` | | Accept duel/party requests | default |
| `/party` | `p` | Party management (create, invite, kick, disband, etc.) | mixed |
| `/ffa ...` | | FFA join/leave/kit/spectate | default |
| `/spectate <player>` | `spec` | Spectate a match | `zpp.spectate` |
| `/preview <ladder>` | | Preview ladder kit | default |
| `/customqueue` | `cqueue`, `customkitqueue` | Custom kit queue (join, host, leave, open) | `zpp.playerkit.queue.use` |

## Player progression and utility commands

| Command | Aliases | Description | Permission |
| --- | --- | --- | --- |
| `/statistics [player]` | `stat`, `stats` | View player statistics | `zpp.statistics` |
| `/divisions` | | Open division progression GUI | `zpp.divisions.view` |
| `/editor` | | Open kit editor selector | default |
| `/copykit <code>` | | Copy a shared custom kit | `zpp.playerkit.copy` |
| `/matchhistory [player]` | `mh`, `matchhist` | View recent match history | `zpp.matchhistory` |
| `/matchinv <id> <uuid> <round>` | | View match inventory snapshot | default |
| `/ignorequeue <player>` | | Toggle queue ignore | `zpp.ignorequeue` |
| `/cosmetics` | `cosmetc`, `csmetic` | Open cosmetics GUI | `zpp.cosmetics.main` |
| `/nick` | `nickname` | Set/reset formatted display name | `zpp.nick.use` |

## Runtime-registered commands

These are registered only when enabled in `config.yml`:

- `/message` (`/msg`, `/m`) — private messaging (requires `CHAT.PRIVATE-CHAT-ENABLED: true`)
- `/reply` (`/re`, `/r`) — reply to last private message
- `/leave` — leave current match (requires `MATCH-SETTINGS.LEAVE-COMMAND.ENABLED: true`)

## Important `/practice` subcommands

| Subcommand | Description |
| --- | --- |
| `lobby set` | Set the lobby spawn location |
| `lobby load` | Teleport to the lobby |
| `arenas` | Teleport to arenas world |
| `teleport <world>` | Teleport to a world |
| `rename <name>` | Rename held item |
| `info <player>` | View player info GUI |
| `elo ...` | Manage player elo |
| `ranked ...` | Manage daily ranked limits |
| `unranked ...` | Manage daily unranked limits |
| `exp ...` | Manage player experience |
| `reset <player>` | Fully reset a player |
| `nametag ...` | Manage player nametag formatting |
| `hologram create <name> <global\|ladder_static\|ladder_dynamic>` | Create a hologram |
| `hologram teleport <name>` | Move hologram to your position |
| `hologram list` | List all holograms |
| `hologram delete <name>` | Delete a hologram |
| `reload` | Reload configuration files |
| `stop` | Stop all matches |

## Important `/arena` subcommands

| Subcommand | Description |
| --- | --- |
| `create <name>` | Create a new arena |
| `setup <arena>` | Enter setup wand mode |
| `set icon <arena>` | Set arena icon from held item |
| `set portalprot <arena> <radius>` | Set portal protection radius |
| `set sidebuildlimit <arena> <value>` | Set side build limit |
| `set partyffacenter <arena>` | Set party FFA center point |
| `enable <arena>` | Enable an arena (make it available for matches) |
| `disable <arena>` | Disable an arena |
| `freeze <arena>` | Freeze an arena (no new matches, ongoing ones finish) |
| `stop <arena>` | Stop all matches in an arena |
| `teleport <arena>` | Teleport to an arena |
| `info <arena>` | View arena info |
| `delete <arena>` | Delete an arena |

## Important `/ladder` subcommands

| Subcommand | Description |
| --- | --- |
| `create <name>` | Create a new ladder |
| `set icon <ladder>` | Set ladder icon from held item |
| `set inventory <ladder>` | Record held inventory as ladder kit |
| `set effect <ladder>` | Record held potion effects as ladder effects |
| `freeze <ladder>` | Freeze a ladder (no new matches) |
| `stop <ladder>` | Stop all matches using this ladder |
| `info <ladder>` | View ladder info |
| `delete <ladder>` | Delete a ladder |

## Important `/event` subcommands

| Subcommand | Description |
| --- | --- |
| `host` | Open event host GUI |
| `join` | Join an event |
| `stop <event_name>` | Stop an event |
| `lms [...]` | LMS event options |
| `oitc [...]` | OITC event options |
| `tnttag [...]` | TNT Tag event options |
| `brackets [...]` | Brackets event options |
| `sumo [...]` | Sumo event options |
| `splegg [...]` | Splegg event options |
| `juggernaut [...]` | Juggernaut event options (alias: `jn`) |

## Important `/ffa` subcommands

| Subcommand | Description |
| --- | --- |
| `join` | Open FFA arena selector |
| `leave` | Leave current FFA arena |
| `kit` | Reapply FFA kit |
| `spectate <arena>` | Spectate an FFA arena |

## Important `/party` subcommands

| Subcommand | Description |
| --- | --- |
| `create` | Create a party |
| `invite <player>` | Invite a player |
| `accept <player>` | Accept party invite |
| `kick <player>` | Kick a member |
| `leave` | Leave current party |
| `disband` | Disband the party |
| `info [player]` | View party info |
| `help` | View party command help |

## Important `/staff` subcommands

| Subcommand | Description |
| --- | --- |
| `enable` | Enable staff mode |
| `disable` | Disable staff mode |
| `chat` | Toggle staff chat |
| `follow <player>` | Follow a player |
| `vanish` | Toggle vanish |
| `help` | View staff command help |
| `stop <player>` | Stop a player's match |
| `forceend <player>` | Force end a match |

## Important `/customqueue` subcommands

| Subcommand | Description |
| --- | --- |
| `open` | Open custom kit queue GUI |
| `join <code>` | Join a custom kit queue |
| `host <code>` | Host a custom kit queue |
| `leave` | Leave current custom kit queue |

## Important `/nick` subcommands

| Subcommand | Description |
| --- | --- |
| `<template>` | Set own display name with MiniMessage format |
| `<player> <template>` | Set another player's display name |
| `reset [player]` | Reset display name to original |
