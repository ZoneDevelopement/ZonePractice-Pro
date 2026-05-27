---
description: Developer API setup for addons and integrations.
---

# For Developers

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

Add ZonePracticePro as a hard dependency so your plugin loads after it:

```yaml
name: MyFirstZPPAddon
version: 1.0
main: com.me.myfirstzppaddon.AddonPlugin
depend: [ZonePracticePro]
```

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
        player.sendMessage("Your wins: " + api.getLadderWins(player, "FireballFight", WeightClass.UNRANKED));
    }
}
```
