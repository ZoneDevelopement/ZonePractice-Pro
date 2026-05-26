# ⚙️ Commands

### Staff & Admin commands

| Command                                  | Description                                   | Permission                                                 |
| ---------------------------------------- | --------------------------------------------- | ---------------------------------------------------------- |
| /setup                                   | Open the server manager GUI.                  | zpp.setup                                                  |
| /arena create \<name>                    | Create an arena.                              | zpp.setup                                                  |
| /arena delete \<arena>                   | Delete an arena.                              | zpp.setup                                                  |
| /arena freeze \<arena>                   | Freeze an arena.                              | zpp.arena.freeze                                           |
| /arena info \<arena>                     | Get detailed informations of an arena.        | zpp.setup                                                  |
| /arena set bed \<arena> <1/2>            | Set the beds for bed arenas.                  | zpp.setup                                                  |
| /arena set portal \<arena> <1/2>         | Set the portals for portal arenas.            | zpp.setup                                                  |
| /arena set corner <1/2>                  | Set the corner of an arena.                   | zpp.setup                                                  |
| /arena set icon \<arena>                 | Set the icon of an arena.                     | zpp.setup                                                  |
| /arena set pos \<arena> <1/2>            | Set the positions for an arena.               | zpp.setup                                                  |
| /arena set buildmax \<arena>             | Set the maximum building height.              | zpp.setup                                                  |
| /arena set deadzone \<arena>             | Set the deadzone for the arena.               | zpp.setup                                                  |
| /arena set portalprot \<arena>           | Set the portal protection for the arena.      | zpp.setup                                                  |
| /arena set ffapos \<arena>               | Set the FFA positions.                        | zpp.setup                                                  |
| /arena set bed \<arena>                  | Set the beds for bed related arenas.          | zpp.setup                                                  |
| /arena set portal \<arena>               | Set the portals for portal related arenas.    | zpp.setup                                                  |
| /arena stop \<arena>                     | Stop all current matches in an arena.         | zpp.arena.stop                                             |
| /arena teleport \<arena> <1/2>           | Teleport to an arena.                         | zpp.setup                                                  |
| /event stop \<event\_name>               | Stop a live event.                            | <p>zpp.event.stop.collecting</p><p>zpp.event.stop.live</p> |
| /event \<event\_name>                    | Events setup commands.                        | zpp.setup                                                  |
| /hologram create \<name>                 | Create a new hologram.                        | zpp.setup                                                  |
| /ladder info \<ladder>                   | Get detailed informations of a ladder.        | zpp.setup                                                  |
| /ladder create \<name>                   | Create a ladder.                              | zpp.setup                                                  |
| /ladder delete \<ladder>                 | Delete a ladder.                              | zpp.setup                                                  |
| /ladder se ticon \<ladder>               | Set the icon of a ladder.                     | zpp.setup                                                  |
| /ladder set inv \<ladder>                | Set the inventory of a ladder.                | zpp.setup                                                  |
| /ladder set effect \<ladder>             | Set the effects of a ladder.                  | zpp.setup                                                  |
| /ladder freeze \<ladder>                 | Freeze a ladder.                              | zpp.ladder.freeze                                          |
| /ladder stop \<ladder>                   | Stop all current matches in a ladder.         | zpp.ladder.stop                                            |
| /practice arenas                         | Teleport to the arenas world.                 | zpp.practice.arenas                                        |
| /practice elo reset                      | Reset player's elo to default.                | zpp.practice.elo.default                                   |
| /practice elo set                        | Set player's elo to a specific number.        | zpp.practice.elo.specific                                  |
| /practice info \<player>                 | Open player's info GUI.                       | zpp.practice.info                                          |
| /practice lobby                          | Teleport to lobby.                            | zpp.practice.lobby                                         |
| /practice lobby load                     | Load the lobby inventory.                     | zpp.practice.lobby                                         |
| /practice lobby set                      | Set the lobby location.                       | zpp.practice.lobby                                         |
| /practice nametag prefix                 | Set a player's nametag prefix.                | zpp.practice.nametag.set                                   |
| /practice nametag suffix                 | Set a player's nametag suffix.                | zpp.practice.nametag.set                                   |
| /practice nametag reset                  | Reset a player's nametag.                     | zpp.practice.nametag.reset                                 |
| /practice ranked reset                   | Set player's ranked to default.               | zpp.practice.ranked.default                                |
| /practice ranked set                     | Set player's ranked to a specific number.     | zpp.practice.ranked.add                                    |
| /practice ranked ban \<player> \<reason> | Ban the player from playing ranked matches.   | zpp.practice.ranked.ban                                    |
| /practice ranked unban \<player>         | Unban the player from playing ranked matches. | zpp.practice.ranked.unban                                  |
| /practice unranked reset                 | Set player's unranked to default.             | zpp.practice.unranked.default                              |
| /practice unranked set                   | Set player's unranked to a specific number.   | zpp.practice.unranked.add                                  |
| /prac exp reset \<player>                | Reset player's exp.                           | zpp.practice.exp.reset                                     |
| /prac exp add \<player> \<exp>           | Add exp to a player.                          | zpp.practice.exp.add                                       |
| /prac exp set \<player> \<exp>           | Set a player's exp.                           | zpp.practice.exp.set                                       |
| /practice rename \<name>                 | Rename an item in your hand.                  | zpp.practice.rename                                        |
| /practice reset \<player>                | Reset a player's all information.             | zpp.practice.reset                                         |
| /practice teleport \<world>              | Teleport to a world.                          | zpp.setup                                                  |
| /staff enable                            | Enable staff mode.                            | zpp.staff                                                  |
| /staff chat                              | Toggle staff chat.                            | zpp.staffmode.chat                                         |
| /staff forceend \<player>                | Force end a player's match or event.          | zpp.staffmode.forceend                                     |
| /staff stop \<player>                    | Get a player out of a match or event.         | zpp.staffmode.stop                                         |
| /staff vanish                            | Toggle vanish                                 | zpp.staffmode                                              |

### Player commands

| Command                                         | Description                                       | Permission                           |
| ----------------------------------------------- | ------------------------------------------------- | ------------------------------------ |
| /unranked                                       | Opens the unranked GUI.                           | <mark style="color:red;">null</mark> |
| /ranked                                         | Opens the ranked GUI.                             | <mark style="color:red;">null</mark> |
| /editor                                         | Opens the kit editor GUI.                         | <mark style="color:red;">null</mark> |
| /accept                                         | Accept duel & party fight invites.                | <mark style="color:red;">null</mark> |
| /duel \<player>                                 | Invite player's to duel.                          | zpp.duel                             |
| /event host                                     | Host events.                                      | zpp.event.host                       |
| /event join                                     | Join events.                                      | zpp.event.join                       |
| /matchinv \<match\_id> \<player\_uuid> \<round> | View match statistics.                            | <mark style="color:red;">null</mark> |
| /party accept \<player>                         | Accept a player's party invite.                   | <mark style="color:red;">null</mark> |
| /party disband                                  | Disband your party.                               | <mark style="color:red;">null</mark> |
| /party info                                     | View your party information.                      | <mark style="color:red;">null</mark> |
| /party info \<player>                           | View other player's party info.                   | zpp.party.info.others                |
| /party invite \<player>                         | Invite players to your party.                     | <mark style="color:red;">null</mark> |
| /party kick \<player>                           | Kick player's from your party.                    | <mark style="color:red;">null</mark> |
| /party leader \<player>                         | Give the party leader position to another member. | <mark style="color:red;">null</mark> |
| /party leave                                    | Leave the party.                                  | <mark style="color:red;">null</mark> |
| /preview \<ladder>                              | Preview a ladder.                                 | <mark style="color:red;">null</mark> |
| /message \<player>                              | Send a private message to a player.               | <mark style="color:red;">null</mark> |
| /reply                                          | Reply to the latest private message.              | <mark style="color:red;">null</mark> |
| /divisions                                      | Open the ranks GUI.                               | zpp.divisions.view (default)         |
| /settings                                       | Open the settings GUI.                            | zpp.settings.open                    |
| /spectate \<player>                             | Spectate a player.                                | zpp.spectate                         |
| /statistics                                     | View own statistics.                              | zpp.statistics                       |
| /statistics \<player>                           | View player's statistics.                         | zpp.statistics.other                 |
| /ffa join \<arena>                              | Join an FFA.                                      | <mark style="color:red;">null</mark> |
| /ffa list                                       | List all the available FFAs.                      | <mark style="color:red;">null</mark> |
| /ffa leave                                      | Leave the FFA.                                    | <mark style="color:red;">null</mark> |
| /ffa spectate                                   | Spectate an FFA.                                  | <mark style="color:red;">null</mark> |
| /copykit \<code>                                | Copy a player's custom kit.                       | zpp.playerkit.copy                   |

