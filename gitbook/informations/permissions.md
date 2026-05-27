# Permissions

This page lists key permission groups and newer nodes added since older docs.

## Default baseline

Grant the default group node to regular players:

- `zpp.group.default`

## Admin and setup

- `zpp.admin` (default op)
- `zpp.setup`
- `zpp.practice.*`
- `zpp.event.*`
- `zpp.settings.*`
- `zpp.update.notify`

## Staff

- `zpp.staff`
- `zpp.staffmode`
- `zpp.staffmode.fly`
- `zpp.staffmode.chat`
- `zpp.staffmode.stop`
- `zpp.staffmode.forceend`
- `zpp.staffmode.see`

## Core player permissions

- `zpp.duel`
- `zpp.spectate`
- `zpp.statistics`
- `zpp.statistics.other`
- `zpp.divisions.view`
- `zpp.ignorequeue`
- `zpp.queue.multi`

## Party and duel modifiers

- `zpp.duel.selectarena`
- `zpp.duel.selectrounds`
- `zpp.duel.infiniteinvite`
- `zpp.party.selectarena`
- `zpp.party.selectrounds`
- `zpp.party.infiniteinvite`
- `zpp.party.info.others`
- `zpp.party.changelimit`
- `zpp.party.public`
- `zpp.party.broadcast`
- `zpp.party.allinvite`
- `zpp.party.partychat`
- `zpp.party.duelrequest`

## Match limits and progression

- `zpp.practice.unranked.default`
- `zpp.practice.unranked.add`
- `zpp.practice.ranked.default`
- `zpp.practice.ranked.add`
- `zpp.practice.ranked.ban`
- `zpp.practice.ranked.unban`
- `zpp.practice.exp.reset`
- `zpp.practice.exp.add`
- `zpp.practice.exp.set`

## Custom kit and queue

- `zpp.customkit.1` to `zpp.customkit.4`
- `zpp.playerkit.share`
- `zpp.playerkit.copy`
- `zpp.playerkit.queue.use`
- `zpp.playerkit.queue.host`

## Cosmetics

- Entry: `zpp.cosmetics.main`
- Armor base tiers: `zpp.cosmetics.armortrim.base.*` and tier nodes
- Apply all armor trims: `zpp.cosmetics.armortrim.apply-global`
- Dynamic trim nodes (registered at runtime):
  - `zpp.cosmetics.armortrim.pattern.<id>`
  - `zpp.cosmetics.armortrim.material.<id>`
- Death effects: `zpp.cosmetics.deatheffect.*` and per-effect nodes
- Lobby movement cosmetics: `zpp.cosmetics.lobby.*`
- Shield cosmetics: `zpp.cosmetics.shield.use`, layout limit nodes

## Name format and match history

- `zpp.nick.use`
- `zpp.nick.others`
- `zpp.nick.different-name`
- `zpp.nick.reset.others`
- `zpp.matchhistory`

## Bypass permissions

- `zpp.bypass.privatemessage`
- `zpp.bypass.ranked.requirements`
- `zpp.bypass.cooldown`
- `zpp.bypass.unranked.limit`
- `zpp.bypass.ranked.limit`
- `zpp.bypass.party.broadcast.limit`
- `zpp.bypass.elo.change`
- `zpp.bypass.forceend`
- `zpp.bypass.stop`
- `zpp.bypass.nametag`
- `zpp.bypass.spectate`
