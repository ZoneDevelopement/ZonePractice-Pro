# 📚 Permissions

{% hint style="info" %}
You can find the command permission [here](commands.md). On this page the other permissions are listed.
{% endhint %}

{% hint style="danger" %}
First add **zpp.group.default** permission to the default rank of the server.
{% endhint %}

### Admin Permissions

*   zpp.admin

    General permission for admins. Default for OP players. Has the following permissions assigned to it:

    * ```
      zpp.setup
      zpp.staff
      zpp.event.*
      zpp.settings.*
      zpp.admin.scoreboard
      ```
* zpp.setup\
  Gives permission for setting up everything for the plugin.
* zpp.autosave.alert\
  Alerts when auto save is starting and when auto save ends.
* zpp.practice.\*\
  Permission for the /practice command and all of it's sub commands. Has the following permission assigned to it:
  * ```
    zpp.practice.lobby
    zpp.practice.arenas
    zpp.practice.info
    zpp.practice.info.teleport
    zpp.practice.info.cancel
    zpp.practice.info.resetstats
    zpp.practice.elo.default
    zpp.practice.elo.specific
    zpp.practice.ranked
    zpp.practice.rename
    zpp.practice.stop
    zpp.practice.unranked
    zpp.practice.exp
    ```

### Staff Permissions

* zpp.staff\
  General permission for staffs. Has the following permissions assigned to it:
  * ```
    zpp.staffmode
    zpp.staffmode.fly
    zpp.staffmode.stop
    zpp.staffmode.see
    zpp.staffmode.chat
    zpp.bypass.privatemessage
    zpp.bypass.spectate
    ```
* zpp.practice.info\
  Gives permission for the /practice info \<player> command, which opens the player's information GUI.
  * zpp.practice.info.teleport\
    In the information GUI, you can teleport to the player's current activites.
  * zpp.practice.info.cancel\
    In the information GUI, you can cancel the player's current activites.
  * zpp.practice.info.resetstats\
    In the information GUI, you can reset the player's statistics.
* zpp.practice.exp\
  Gives permission for the /practice exp \<player> command.
  * zpp.practice.exp.reset\
    Reset the player's experience.&#x20;
  * zpp.practice.exp.add\
    Add experience to a player.
  * zpp.practice.exp.set\
    Set the experience of a player.
* zpp.staffmode\
  Permission for using the staff mode. Contains the zonepractice.bypass.spectate permission.
  * zpp.staffmode.fly\
    Flying in the lobby.
  * zpp.staffmode.see\
    See other hiding staffs.

### Player Permissions

Player Settings permissions

* zpp.settings.\*\
  The player can edit all the settings in the settings GUI. Has the following permissions assigned to it:
  * ```
    zpp.settings.open
    zpp.bypass.settings-cooldown
    zpp.settings.duelrequest
    zpp.settings.scoreboard
    zpp.settings.privatemessage
    zpp.settings.yyinvite
    zpp.settings.playerhide
    zpp.settings.allowspectate
    zpp.settings.worldtime
    ```
  * zpp.settings.duelrequest\
    Toggle the duel request player setting.
  * zpp.settings.scoreboard\
    Toggle the scoreboard.
  * zpp.settings.privatemessage\
    You can toggle whether or not to accept private messages.
  * zpp.settings.partyinvite\
    You can toggle whether or not to accept party invites.
  * zpp.settings.playerhide\
    You can toggle whether or not you want to see players in the lobby.
  * zpp.settings.allowspectate\
    You can toggle whether you want spectators in your matches or not.\
    <mark style="color:red;">**Note:**</mark> Even if 1 player has this on, only players with zonepractice.bypass.spectate permission can spectate these matches.
  * zpp.settings.worldtime\
    You can toggle the world time for yourself.

Spectate permissions

* zpp.spectate.vanish\
  You can toggle whether or not you want to see other spectators.
* zpp.spectate.menu\
  You can use the spectator menu.
* zpp.spectate.random\
  You can use the random match spectate item.

Duel permissions

* zpp.duel.selectarena\
  You can choose the arena when you invite someone to a duel.
* zpp.duel.selectrounds\
  You can choose the number of rounds when you invite someone to a duel.
* zpp.duel.infiniteinvite\
  You can invite a player any time. You don't have to wait for the other invite to expire.

Party permissions

* zpp.party.selectarena\
  You can choose the arena when you invite another party for a match.
* zpp.party.selectrounds\
  You can choose the number of rounds when you invite another party for a match.
* zpp.party.infiniteinvite\
  You can invite another party for a match any time. You don't have to wait for the other invite to expire.
* zpp.party.changelimit\
  Change the maximum party player limit in the party settings GUI.
* zpp.party.public\
  Make the party public in the party settings GUI.
* zpp.party.broadcast\
  Broadcast the party for the available players.
* zpp.party.allinvite\
  Any player in your party can invite other players.
* zpp.party.partychat\
  Change the status of the party chat.
* zpp.party.duelrequest\
  Toggle whether your party accepts match requests or not.

Event permissions

* zpp.event.host\
  Open the event selector GUI.
* zpp.event.host.\<event\_name> (like brackets/sumo/juggernaut etc.)\
  Permission to host an event.
* zpp.event.\*\
  Give permission for event related stuff. Has the following permissions assigned to it:
  * ```
    zpp.event.host.all
    zpp.event.join
    zpp.event.stop.collecting
    zpp.event.stop.live
    ```
  * zpp.event.host.all\
    Permission for hosting any event.
  * zpp.event.join\
    Joining events.
  * zpp.event.stop.collecting\
    Stop events while its collecting.
  * zpp.event.stop.live\
    Stop events while its live.

Custom Player Kit permissions:

* zpp.playerkit.share\
  Permission for sharing your custom kits.
* zpp.playerkit.copy\
  Permission for copying other's custom kit (if shared).

### Bypass Permissions

* zpp.bypass.privatemessage\
  Players with this permission can send private messages to players who don't accept them.
* zpp.bypass.ranked.requirements\
  Bypass ranked requirements.
* zpp.bypass.cooldown\
  Bypass for some cooldown like vanish toggle, settings toggle etc.
* zpp.bypass.unranked.limit\
  Bypass unranked limit.
* zpp.bypass.ranked.limit\
  Bypass ranked limit.
* zpp.bypass.elo.change\
  Player's elo cannot be changed if they have this permission.
* zpp.bypass.forceend\
  Player's match cannot be force ended.
* zpp.bypass.stop\
  Player cannot be removed from any match or event.
* zpp.bypass.nametag\
  Player's nametag cannot be changed.
* zpp.bypass.spectate\
  Player with this permission can spectate not-viewable matches.
