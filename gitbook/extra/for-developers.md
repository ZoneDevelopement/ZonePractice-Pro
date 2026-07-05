---
description: Developer API setup for addons and integrations.
---

# For Developers

## Download the API

The API is published on JitPack. The API module (`ZonePracticePro-Api`) provides interfaces and events without exposing internal plugin classes.

## Maven

```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>com.github.ZoneDevelopement</groupId>
    <artifactId>ZonePracticePro-Api</artifactId>
    <version>2.2.0</version>
    <scope>provided</scope>
  </dependency>
</dependencies>
```

## Gradle (Groovy)

```groovy
repositories {
  maven { url 'https://jitpack.io' }
}

dependencies {
  compileOnly 'com.github.ZoneDevelopement:ZonePracticePro-Api:2.2.0'
}
```

## plugin.yml dependency

Add ZonePracticePro as a dependency so your plugin loads after it:

```yaml
name: MyFirstZPPAddon
version: 1.0
main: com.me.myfirstzppaddon.AddonPlugin
depend: [ZonePracticePro]
```

## API methods

```java
ZonePracticeApi api = ZonePracticeApi.getInstance();

// Division
String shortDivision = api.getPlayerDivision(player, DivisionName.SHORT);
String fullDivision = api.getPlayerDivision(player, DivisionName.FULL);

// Match limits
int unrankedLeft = api.getPlayerUnRankedLeft(player);
int rankedLeft = api.getPlayerRankedLeft(player);

// Modify limits
api.resetPlayerUnRanked(player);
api.resetPlayerRanked(player);
api.addPlayerUnRanked(player, 5);
api.addPlayerRanked(player, 3);

// End match
api.endMatch(player, "Match ended by addon");

// Statistics
int elo = api.getElo(player, "Archer");
int exp = api.getExperience(player);
int wins = api.getLadderWins(player, "Nodebuff", WeightClass.RANKED);
int losses = api.getLadderLosses(player, "Nodebuff", WeightClass.UNRANKED);
int globalWins = api.getGlobalWins(player);

// Nametag
PlayerNametag nametag = api.getPlayerNametag(player);
// Returns: prefix, nameColor, suffix, sortPriority
```

## API events

API event classes are under the `dev.nandi0813.api.Event` namespace:

| Event | Cancellable | Description |
| --- | --- | --- |
| `MatchStartEvent` | Yes | Fired when a match starts |
| `MatchEndEvent` | No | Fired when a match ends |
| `MatchRoundStartEvent` | No | Fired when a round starts |
| `MatchRoundEndEvent` | No | Fired when a round ends |
| `QueueStartEvent` | Yes | Fired when a player starts queueing |
| `QueueEndEvent` | No | Fired when a player stops queueing |
| `EventStartEvent` | Yes | Fired when a game event starts |
| `EventEndEvent` | No | Fired when a game event ends |
| `PartyCreateEvent` | Yes | Fired when a party is created |
| `NewPlayerJoin` | No | Fired when a new player joins for the first time |
| `FFARemovePlayerEvent` | No | Fired when a player is removed from FFA |

### Spectate events

| Event | Description |
| --- | --- |
| `MatchSpectateStartEvent / MatchSpectateEndEvent` | Spectating a match |
| `EventSpectateStartEvent / EventSpectateEndEvent` | Spectating an event |
| `FFASpectateStartEvent / FFASpectateEndEvent` | Spectating an FFA arena |

## API interfaces

These interfaces are accessible from event objects or via the API:

| Interface | Key methods |
| --- | --- |
| `Match` | `getPlayers()`, `getSpectators()`, `getWinsNeeded()` |
| `Party` | `getLeader()`, `getMembers()` |
| `Queue` | `getPlayer()`, `getDuration()`, `isRanked()` |
| `Event` | `getStarter()`, `getPlayers()`, `getSpectators()`, `getStartPlayerCount()`, `getWinner()` |
| `FFA` | Spectatable arena interface |
| `Spectatable` | `getSpectators()`, `addSpectator()`, `removeSpectator()`, `sendMessage()` |

## Basic usage example

```java
public final class MyFirstZPPAddon extends JavaPlugin implements Listener {

    private ZonePracticeApi api;

    @Override
    public void onEnable() {
        api = ZonePracticeApi.getInstance();
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        player.sendMessage("Your division: " + api.getPlayerDivision(player, DivisionName.FULL));
        player.sendMessage("Your wins: " + api.getLadderWins(player, "Nodebuff", WeightClass.UNRANKED));
    }

    @EventHandler
    public void onMatchEnd(MatchEndEvent event) {
        Match match = event.getMatch();
        Bukkit.broadcastMessage("Match ended! Players: " + match.getPlayers().size());
    }
}
```
