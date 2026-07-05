# Permissions

Complete permission reference as registered in `plugin.yml`.

## Default baseline

Grant `zpp.group.default` to regular players for access to basic features.

## Admin and setup

| Permission | Default | Description |
| --- | --- | --- |
| `zpp.admin` | op | Grants all setup, staff, event, settings, scoreboard, and update notify |
| `zpp.admin.scoreboard` | | Access to admin scoreboard features |
| `zpp.setup` | op | Setup everything + ladder freeze + arena freeze |
| `zpp.update.notify` | op | Receive in-game notifications on new releases |

## Practice command family

`zpp.practice.*` grants all children below:

| Permission | Default | Description |
| --- | --- | --- |
| `zpp.practice.lobby` | | Teleport to and set the lobby |
| `zpp.practice.arenas` | | Teleport to arenas world |
| `zpp.practice.info` | | View `/practice info <player>` |
| `zpp.practice.info.teleport` | | Teleport to player's activities |
| `zpp.practice.info.cancel` | | Cancel player's activities |
| `zpp.practice.info.resetstats` | | Reset player's statistics |
| `zpp.practice.elo.default` | | Reset player elos to default |
| `zpp.practice.elo.specific` | | Set player elos to a specific number |
| `zpp.practice.ranked` | | Manage daily ranked limits |
| `zpp.practice.unranked` | | Manage daily unranked limits |
| `zpp.practice.exp` | | Manage player experience |
| `zpp.practice.rename` | | Use `/practice rename` |
| `zpp.practice.reload` | | Reload config files |
| `zpp.practice.stop` | | Legacy permission node (currently no `/practice stop` subcommand) |
| `zpp.practice.reset` | | Fully reset a player |
| `zpp.practice.nametag.set` | | Set player's nametag |
| `zpp.practice.nametag.reset` | | Reset player's nametag |
| `zpp.practice.nametag.name.others` | op | Change other players' name format |

## Staff

| Permission | Description |
| --- | --- |
| `zpp.staff` | Grants staffmode + fly + stop + see + chat + bypasses |
| `zpp.staffmode` | Turn on staff mode |
| `zpp.staffmode.fly` | Fly in lobby |
| `zpp.staffmode.forceend` | Force end matches and freeze players |
| `zpp.staffmode.chat` | Use staff chat |
| `zpp.staffmode.see` | See hiding staff members |
| `zpp.autosave.alert` | Receive auto save alerts |

## Core player permissions

| Permission | Default | Description |
| --- | --- | --- |
| `zpp.duel` | | Use `/duel` |
| `zpp.spectate` | true | Spectate matches |
| `zpp.spectate.vanish` | | Toggle vanish when spectating |
| `zpp.spectate.menu` | | Use spectator menu |
| `zpp.spectate.random` | | Spectate random match |
| `zpp.statistics` | true | View own statistics |
| `zpp.statistics.other` | | View other players' statistics |
| `zpp.divisions.view` | true | Open divisions GUI |
| `zpp.ignorequeue` | true | Ignore a player in unranked queue |
| `zpp.queue.multi` | | Queue for multiple ladders simultaneously |

## Duel modifiers

| Permission | Description |
| --- | --- |
| `zpp.duel.selectarena` | Choose arena for duel |
| `zpp.duel.selectrounds` | Choose rounds for duel |
| `zpp.duel.infiniteinvite` | No limit on duel invitations |

## Party permissions

| Permission | Description |
| --- | --- |
| `zpp.party.create` | Create a party |
| `zpp.party.selectarena` | Choose arena for party |
| `zpp.party.selectrounds` | Choose rounds for party |
| `zpp.party.infiniteinvite` | No limit on party invitations |
| `zpp.party.info.others` | View other parties |
| `zpp.party.changelimit` | Change party player limit |
| `zpp.party.public` | Set party to public |
| `zpp.party.broadcast` | Broadcast public party |
| `zpp.party.allinvite` | Invite all online players |
| `zpp.party.partychat` | Toggle party chat |
| `zpp.party.duelrequest` | Toggle party duel requests |

## Settings permissions

| Permission | Default | Description |
| --- | --- | --- |
| `zpp.settings.*` | | Opens + all settings toggles (does not grant `zpp.bypass.cooldown`) |
| `zpp.settings.open` | true | Open settings GUI |
| `zpp.settings.duelrequest` | | Toggle duel requests |
| `zpp.settings.scoreboard` | | Toggle scoreboard |
| `zpp.settings.privatemessage` | | Toggle private messages |
| `zpp.settings.partyinvite` | | Toggle party invites |
| `zpp.settings.playerhide` | | Toggle player hider |
| `zpp.settings.allowspectate` | | Toggle spectators |
| `zpp.settings.worldtime` | | Change world time |
| `zpp.settings.prefixvisibility` | | Change prefix/suffix visibility |
| `zpp.settings.fly` | | Toggle fly in lobby |

## Event permissions

| Permission | Description |
| --- | --- |
| `zpp.event.*` | Host all + join + stop collecting + stop live |
| `zpp.event.host` | Open event host GUI |
| `zpp.event.host.all` | Host all event types |
| `zpp.event.join` | Join events (default: true) |
| `zpp.event.stop.collecting` | Stop events while collecting |
| `zpp.event.stop.live` | Stop ongoing events |

## Match limits and progression

| Permission | Description |
| --- | --- |
| `zpp.practice.unranked.default` | Reset daily unranked limit to default |
| `zpp.practice.unranked.add` | Add extra unranked matches |
| `zpp.practice.ranked.default` | Reset daily ranked limit to default |
| `zpp.practice.ranked.add` | Add extra ranked matches |
| `zpp.practice.ranked.ban` | Ban player from ranked |
| `zpp.practice.ranked.unban` | Unban player from ranked |
| `zpp.practice.exp.reset` | Reset player experience |
| `zpp.practice.exp.add` | Add experience |
| `zpp.practice.exp.set` | Set experience |

## Ladder/arena freeze and stop

| Permission | Description |
| --- | --- |
| `zpp.ladder.freeze` | Freeze ladders |
| `zpp.ladder.stop` | Stop all ladder matches |
| `zpp.arena.freeze` | Freeze arenas |
| `zpp.arena.stop` | Stop all arena matches |

## Custom kit permissions

| Permission | Description |
| --- | --- |
| `zpp.customkit.1` | Create 1 custom kit (2, 3, 4 inherit) |
| `zpp.playerkit.share` | Share custom kit with others |
| `zpp.playerkit.copy` | Copy custom kit from others |
| `zpp.playerkit.queue.host` | Host a custom kit queue |
| `zpp.playerkit.queue.use` | true | Use custom kit queue commands |

## Cosmetics permissions

| Permission | Description |
| --- | --- |
| `zpp.cosmetics.main` | Open cosmetics GUI |
| `zpp.cosmetics.armortrim.base.*` | All armor base tiers |
| `zpp.cosmetics.armortrim.base.leather` | Leather tier |
| `zpp.cosmetics.armortrim.base.gold` | Gold tier |
| `zpp.cosmetics.armortrim.base.iron` | Iron tier |
| `zpp.cosmetics.armortrim.base.diamond` | Diamond tier |
| `zpp.cosmetics.armortrim.base.netherite` | Netherite tier |
| `zpp.cosmetics.armortrim.apply-global` | Copy trim to all armor types |
| `zpp.cosmetics.lobby.*` | All lobby movement cosmetics |
| `zpp.cosmetics.lobby.none` | No cosmetic |
| `zpp.cosmetics.lobby.wind_charge` | Wind charge cosmetic |
| `zpp.cosmetics.lobby.trident` | Trident cosmetic |
| `zpp.cosmetics.lobby.spear` | Spear cosmetic |

> Note: `zpp.cosmetics.armortrim.pattern.<id>` and `zpp.cosmetics.armortrim.material.<id>` are registered dynamically at runtime based on available trim patterns/materials. Death effect and shield cosmetic permissions are also registered dynamically from config-defined effects and layouts.

## Name format and match history

| Permission | Default | Description |
| --- | --- | --- |
| `zpp.nick.use` | true | Use `/nick` command |
| `zpp.nick.others` | op | Set another player's display name |
| `zpp.nick.different-name` | op | Use a different visible username |
| `zpp.nick.reset.others` | op | Reset another player's display name |
| `zpp.matchhistory` | op | Access `/matchhistory` |

## Bypass permissions

| Permission | Description |
| --- | --- |
| `zpp.bypass.privatemessage` | Send PMs to players who disabled them |
| `zpp.bypass.ranked.requirements` | Bypass ranked requirements |
| `zpp.bypass.cooldown` | Bypass spectate vanish, lobby vanish cooldowns |
| `zpp.bypass.unranked.limit` | Bypass daily unranked limit |
| `zpp.bypass.ranked.limit` | Bypass daily ranked limit |
| `zpp.bypass.party.broadcast.limit` | Bypass daily party broadcast limit |
| `zpp.bypass.elo.change` | Player elo cannot be changed by commands |
| `zpp.bypass.forceend` | Cannot be force ended by staff |
| `zpp.bypass.stop` | Cannot be removed from match/event by staff |
| `zpp.bypass.nametag` | Bypass nametag changes |
| `zpp.bypass.spectate` | Spectate even when match is not viewable |
