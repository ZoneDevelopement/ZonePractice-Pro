# Commands

This page documents current command families and major subcommands.

## Core commands

| Command | Description | Permission |
| --- | --- | --- |
| `/setup` | Open setup manager GUI | `zpp.setup` |
| `/practice` (`/prac`, `/zonepractice`, `/zoneprac`, `/zonep`) | Main utility/admin command family | subcommand-based |
| `/arena` | Arena management commands | subcommand-based |
| `/ladder` | Ladder management commands | subcommand-based |
| `/event` (`/e`) | Event setup/host/join/stop commands | subcommand-based |
| `/staff` (`/staffmode`) | Staff mode commands | `zpp.staff` + subnodes |

## Player queue/match commands

| Command | Description | Permission |
| --- | --- | --- |
| `/unranked` | Open unranked queue GUI | default |
| `/ranked` | Open ranked queue GUI | default |
| `/duel <player>` | Send duel request | `zpp.duel` |
| `/accept` | Accept duel/party requests | default |
| `/party ...` | Party management | mixed |
| `/ffa join` | Open FFA selector | default |
| `/ffa leave` | Leave FFA | default |
| `/ffa kit` | Reapply FFA kit | default |
| `/ffa spectate <arena>` | Spectate FFA | default |
| `/spectate <player>` | Spectate a match | `zpp.spectate` |
| `/preview <ladder>` | Preview ladder kit | default |

## Player progression/utility commands

| Command | Description | Permission |
| --- | --- | --- |
| `/statistics [player]` | View stats | `zpp.statistics`, `zpp.statistics.other` |
| `/divisions` | Open division GUI | `zpp.divisions.view` |
| `/editor` | Open kit editor selector | default |
| `/copykit <code>` | Copy shared custom kit | `zpp.playerkit.copy` |
| `/matchinv <match_id> <player_uuid> <round>` | View match inventory snapshot | default |
| `/matchhistory [player]` | Open recent match history | `zpp.matchhistory` |
| `/ignorequeue <player>` | Toggle queue ignore for player | `zpp.ignorequeue` |
| `/customqueue ...` | Custom kit queue commands (`join`, `host`, `leave`, `open`) | `zpp.playerkit.queue.use` |
| `/cosmetics` | Open cosmetics GUI | `zpp.cosmetics.main` |
| `/nick ...` | Set/reset formatted display name | `zpp.nick.use` + optional subnodes |

## Private chat and leave commands

`/message`, `/reply`, and `/leave` are runtime-registered only when enabled in `config.yml`.

## Important `/practice` subcommands

- `/practice lobby set|load`
- `/practice arenas`
- `/practice teleport <world>`
- `/practice rename <name>`
- `/practice info <player>`
- `/practice elo ...`
- `/practice ranked ...`
- `/practice unranked ...`
- `/practice exp ...`
- `/practice reset <player>`
- `/practice nametag ...`
- `/practice hologram create <name> <global|ladder_static|ladder_dynamic>`
- `/practice reload`

## Important `/arena` subcommands

- `/arena create <name>`
- `/arena setup <arena>`
- `/arena set icon <arena>`
- `/arena set portalprot <arena> <radius>`
- `/arena set sidebuildlimit <arena> <value>`
- `/arena set partyffacenter <arena>`
- `/arena enable|disable|freeze|stop <arena>`
- `/arena teleport <arena>`
- `/arena info <arena>`

## Important `/ladder` subcommands

- `/ladder create <name>`
- `/ladder set icon <ladder>`
- `/ladder set inventory <ladder>`
- `/ladder set effect <ladder>`
- `/ladder freeze|stop <ladder>`
- `/ladder info|delete <ladder>`

## Important `/event` subcommands

- `/event host`
- `/event join`
- `/event stop <event_name>`
- `/event lms|oitc|tnttag|brackets|sumo|splegg|juggernaut ...`
