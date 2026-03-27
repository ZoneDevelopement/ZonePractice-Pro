# ZonePractice

ZonePractice is a modular, production-ready Minecraft PvP/practice plugin written in Java and built with Maven. It is
organized into multiple modules (core logic, platform-specific builds, and distribution packaging) and supports all
major server forks commonly used by competitive PvP networks.

## Documentation

For detailed guides on setup, configuration, and feature usage, please visit our official GitBook:
👉 **[ZonePractice Pro Documentation](https://zone-developement.gitbook.io/zonepractice-pro/)**

## Features

- Ladder, Arena, FFA and Event systems
- Profile management, leaderboards (holograms), sidebars and tablists
- Match engine, kit handling, inventories and utility helpers
- Optional PlaceholderAPI support
- Compatible with Paper/Spigot, FoxSpigot, Carbon and similar forks

## Supported Versions

- Primary targets: **1.20.6 / 1.21.X**
- Actual supported versions are detected at runtime via the `VersionChecker`
- The plugin automatically disables itself on unsupported versions

## Dependencies

### Optional – PlaceholderAPI

Provides additional placeholders when installed. Add the PlaceholderAPI jar to your server’s *plugins/* folder to enable
integration.

### Required (runtime) – PacketEvents

ZonePractice uses PacketEvents for packet-level features. PacketEvents must be installed as an external plugin, not
shaded into ZonePractice.  
**How to install PacketEvents:**

1. Download a compatible build from: https://github.com/retrooper/packetevents/releases
2. Stop your server
3. Place **PacketEvents** and **ZonePractice** into the *plugins/* directory
4. Start the server and ensure PacketEvents loads before ZonePractice  
   Do **not** bundle PacketEvents inside the ZonePractice jar. Keeping it external ensures correct load order and
   compatibility.

## Repository Structure

- **core/** – main logic (`practice-core-*.jar`)
- **distribution/** – release packaging (`ZonePractice Pro v*.jar`)

---

## Cloning & Git LFS

This repository uses **Git LFS (Large File Storage)** to manage heavy binary assets, such as the server builds and
dependencies located in the `libs/` folder.

To ensure that you download the actual files instead of small text pointers, you **must** have Git LFS installed on your
system before cloning or pulling updates:

1. **Install Git LFS:** Run `git lfs install` (only needs to be done once per machine).
2. **Clone the Repo:** Use your standard `git clone` command.
3. **Troubleshooting:** If the files in `libs/` appear as 1KB text files, run:
   ```bash
   git lfs pull
   ```
   This will manually sync the large binary assets to your local workspace.

---

## Building

1. **Prerequisites:** Install JDK 25 and Maven.
3. **Build the Project:**
   ```bash
   mvn clean package
   ```

## Installation (Server)

1. Place the appropriate build (distribution jar or a specific platform module) into *plugins/*.
2. Start the server and watch the console or `logs/latest.log`.
3. On first startup, the plugin will generate configuration files under `plugins/ZonePracticePro/`.

## Configuration

- Default configuration files are generated automatically. Templates live under `core/src/main/resources/<version>/` (
  e.g., `config.yml`, `divisions.yml`, `guis.yml`, `inventories.yml`).
- `config.yml` includes a `VERSION` field (e.g., 13). Review updated templates when
  upgrading.
- Optional MySQL storage is available via the `MYSQL-DATABASE` section; back up configs before enabling.
- Read console output for version validation, warnings and load messages.
- PlaceholderAPI functionality is automatically enabled when detected.

## Commands & Permissions

All commands and permission nodes are defined in `core/src/main/resources/plugin.yml`

Common commands include `/practice` (aliases: `/prac`, `/zonepractice`, `/zoneprac`, `/zonep`), `/arena`, `/ladder`, `/duel`, `/party`, `/spectate`, and many more

Permissions follow the `zpp.*` namespace, such as `zpp.admin` (default: op), `zpp.practice.*`, `zpp.staffmode`, and many granular nodes

### Cosmetics permissions

Some cosmetics permissions are registered dynamically at startup by `CosmeticsPermissionManager`, so they are not fully listed in `plugin.yml`

#### Entry permission

1. `zpp.cosmetics.main`
   Required to run `/cosmetics` (`CosmeticsCommand`)

#### Armor trim permissions

1. Tier access:
   1. `zpp.cosmetics.armortrim.base.leather`
   2. `zpp.cosmetics.armortrim.base.gold`
   3. `zpp.cosmetics.armortrim.base.iron`
   4. `zpp.cosmetics.armortrim.base.diamond`
   5. `zpp.cosmetics.armortrim.base.netherite`
   6. wildcard: `zpp.cosmetics.armortrim.base.*`
2. Pattern access:
   1. `zpp.cosmetics.armortrim.pattern.<id>`
   2. wildcard: `zpp.cosmetics.armortrim.pattern.*`
3. Material access:
   1. `zpp.cosmetics.armortrim.material.<id>`
   2. wildcard: `zpp.cosmetics.armortrim.material.*`
4. Apply trim to all armor (copy from base):
   1. `zpp.cosmetics.armortrim.apply-global`

`<id>` values come from Mojang/Paper trim registries and are sanitized to lowercase alphanumeric/underscore (for example: sentry, vex, amethyst, netherite)

#### Death effect permissions

1. Per effect:
   1. `zpp.cosmetics.deatheffect.none`
   2. `zpp.cosmetics.deatheffect.flame`
   3. `zpp.cosmetics.deatheffect.lightning`
   4. `zpp.cosmetics.deatheffect.firework`
   5. `zpp.cosmetics.deatheffect.explosion`
   6. `zpp.cosmetics.deatheffect.blood`
   7. `zpp.cosmetics.deatheffect.enchant`
   8. `zpp.cosmetics.deatheffect.ender`
   9. `zpp.cosmetics.deatheffect.hearts`
   10. `zpp.cosmetics.deatheffect.ice`
2. Wildcard:
   1. `zpp.cosmetics.deatheffect.*`

#### Shield layout permissions

1. Open/use shield cosmetics:
   1. `zpp.cosmetics.shield.use`
   2. wildcard: `zpp.cosmetics.shield.*`
2. Layout count limits:
   1. `zpp.cosmetics.shield.layouts.1` to `zpp.cosmetics.shield.layouts.21`
   2. wildcard: `zpp.cosmetics.shield.layouts.*`
   3. unlimited alias: `zpp.cosmetics.shield.layouts.unlimited`

If none of the layout count permissions are set, the code falls back to 1 max layout

#### Lobby movement permissions

1. Per item:
   1. `zpp.cosmetics.lobby.none`
   2. `zpp.cosmetics.lobby.wind_charge`
   3. `zpp.cosmetics.lobby.trident`
   4. `zpp.cosmetics.lobby.spear`
2. Wildcard:
   1. `zpp.cosmetics.lobby.*`

### Groups and cosmetics permissions

Player groups are configured in `core/src/main/resources/groups.yml` and selected by group permission:

- `zpp.group.default`
- `zpp.group.premium`
- `zpp.group.supreme`
- `zpp.group.staff`
- `zpp.group.admin`

The active group controls limits like custom kits and party capacity. You can also use your permission plugin (LuckPerms, etc.) to attach cosmetics permissions per group.

Example bundle strategy:

- `DEFAULT`: `zpp.cosmetics.main`, `zpp.cosmetics.armortrim.base.leather`, `zpp.cosmetics.deatheffect.none`, `zpp.cosmetics.shield.use`, `zpp.cosmetics.shield.layouts.1`
- `PREMIUM`: add `zpp.cosmetics.armortrim.base.gold`, selected trim/material nodes, `zpp.cosmetics.deatheffect.flame`, `zpp.cosmetics.shield.layouts.3`
- `SUPREME+`: grant broader wildcards (`zpp.cosmetics.armortrim.pattern.*`, `zpp.cosmetics.armortrim.material.*`, `zpp.cosmetics.deatheffect.*`, `zpp.cosmetics.shield.layouts.unlimited`)

## Developer API

ZonePractice Pro provides a comprehensive API for developers to interact with the core systems, retrieve player statistics, and listen to custom events.

### Setting Up the API

#### 1. Add JitPack Repository and Dependency

Add the following to your `pom.xml`:

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

#### 2. Update Your plugin.yml

Ensure your plugin loads after ZonePractice Pro by adding it as a dependency:

```yaml
name: YourPluginName
version: '1.0'
main: your.package.path.MainClass
api-version: '1.20'
depends: [ZonePracticePro]
```

### Using the API

Access API methods via the singleton instance `ZonePracticeApi.getInstance()`. You can also listen to custom events provided in the `dev.nandi0813.api.Event` package.

#### Example: Fetching Player Statistics

```java
@EventHandler
public void onPlayerJoin(PlayerJoinEvent e) {
    Player player = e.getPlayer();
    ZonePracticeApi api = ZonePracticeApi.getInstance();

    // Get player division and ladder stats
    String division = api.getPlayerDivision(player, DivisionName.FULL);
    int fireballWins = api.getLadderWins(player, "FireballFight", WeightClass.UNRANKED);

    player.sendMessage("Your division: " + division);
    player.sendMessage("Your FireballFight wins: " + fireballWins);
}
```

#### Example: Listening to Custom Events

```java
@EventHandler
public void onMatchStart(MatchStartEvent e) {
    Match match = e.getMatch();

    // Send a custom MiniMessage format string to all match participants
    match.sendMessage("<red>Welcome to the match! Good luck!", true);
    
    // Iterate through players
    match.getPlayers().forEach(player -> player.sendMessage("The battle has begun!"));
}
```

### Complete Example Plugin

```java
public final class ZPPApiExample extends JavaPlugin implements Listener {

    private ZonePracticeApi api;

    @Override
    public void onEnable() {
        // Retrieve the API instance
        api = ZonePracticeApi.getInstance();
        
        // Register your listeners
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("ZonePractice API loaded successfully!");
    }

    // Example 1: Fetching player statistics
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        // Get division and ladder stats using the API
        String division = api.getPlayerDivision(player, DivisionName.FULL);
        int fireballWins = api.getLadderWins(player, "FireballFight", WeightClass.UNRANKED);

        player.sendMessage("Your division: " + division);
        player.sendMessage("Your FireballFight wins: " + fireballWins);
    }

    // Example 2: Listening to custom ZPP Events
    @EventHandler
    public void onMatchStart(MatchStartEvent e) {
        Match match = e.getMatch();

        // Send a custom MiniMessage format string to all match participants
        match.sendMessage("<red>Welcome to the match! Good luck!", true);
        
        // Iterate through players
        match.getPlayers().forEach(player -> player.sendMessage("The battle has begun!"));
    }
}
```

### API Features

- **Player Statistics**: Access divisions, ELO, wins/losses, and experience
- **Ladder Management**: Query ladder-specific stats with weight classes
- **Custom Events**: Listen to match events, player profile updates, and more
- **Match Control**: Interact with active matches and send formatted messages
- **Player Data**: Retrieve nametags, groups, and other player information

## Troubleshooting

### PacketEvents Not Found

- Ensure PacketEvents is in *plugins/* and loads **before** ZonePractice
- Restart the server instead of hot-loading plugins

### MySQL Errors

- Verify MySQL settings in `config.yml`
- Ensure the database accepts external connections
- JDBC is handled via `DriverManager`; ensure a suitable MySQL driver is available

## Plugin Metadata

The canonical `plugin.yml` is located at `core/src/main/resources/plugin.yml` and defines:  
`name: ZonePracticePro`, `api-version: 1.13`, commands, permissions, soft dependencies, and load rules.

## Contributing

- Pull requests are welcome
- Keep changes focused and include tests when possible
- Follow the coding style in the `core` module
- Open an issue for bugs or feature requests

## License

Licensed under the **MIT License (2025)**.  
Copyright © **ZonePractice contributors**

## Contact

For issues, feature requests or contributions, use the project’s GitHub issue tracker.
