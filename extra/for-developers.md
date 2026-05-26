---
description: >-
  Welcome! You've embarked on the exciting journey of working with the
  ZonePractice API. To get started, just import the API as you usually do:
---

# 🖥️ For Developers

### Current API version

<div align="left"><figure><img src="https://jitpack.io/v/ZoneDevelopement/ZonePracticePro-Api.svg" alt=""><figcaption></figcaption></figure></div>

### Importing the API

Change API\_VERSION to the version above.

{% code title="Maven" %}
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
		<version>API_VERSION</version>
		<scope>provided</scope>
	</dependency>
</dependencies>
```
{% endcode %}

{% code title="Gradle" %}
```kts
repositories {
    maven {
        url = 'https://jitpack.io'
    }
}

dependencies {
    compileOnly 'com.github.ZoneDevelopement:Zone_LicenseManager:API_VERSION'
}
```
{% endcode %}

### Adding ZonePracticePro as a dependency

It is important to make sure that you add `ZonePracticePro` as a dependancy in you Addons.\
This will insure that your plugin will get loaded after ZonePracticePro, as otherwise you'll face into errors when accessing the API.\
Simply add the `ZonePracticePro` depend in your `plugin.yml`. Thats it!

{% code lineNumbers="true" %}
```yaml
name: MyFirstZPPAddon
version: 1.0
main: com.me.myfirstzppaddon.AddonPlugin
depend: [ZonePracticePro]
```
{% endcode %}

### Usage

{% code lineNumbers="true" %}
```java
public final class MyFirstZPPAddon extends JavaPlugin implements Listener {

    private ZonePracticeApi api;

    @Override
    public void onEnable() {
        api = ZonePracticeApi.getInstance();

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        
        player.sendMessage("Your division: " + api.getPlayerDivision(player, DivisionName.FULL));
        player.sendMessage("Your wins: " + api.getLadderWins(player, "FireballFight", WeightClass.UNRANKED));
    }

}
```
{% endcode %}
