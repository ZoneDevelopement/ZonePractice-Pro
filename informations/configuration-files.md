---
description: Here you can find a detailed description about the configuration files.
---

# 📜 Configuration Files

{% hint style="danger" %}
The configuration files are different in version 1.20.4. On first startup, the plugin automatically generates the files according to the version.
{% endhint %}

### Config file

The config file contains the settings to configure the basic functionality of the plugin.

### Language file

The language file uses a special formatting library called [Adventure MiniMessage](https://docs.advntr.dev/minimessage/format.html). This allows you to add actions to any message. You can find a detailed description in their page.

### Item setup

The gui file contains every GUI settings including titles and items. You can configure every item for your needs. There are 2 <mark style="color:red;">requirements</mark> for every item:

* You have to set a valid material.
* You have to add the path for every item in upper case. like NAME: and LORE: etc.

This is also true for the inventories file.

For example you can add the lore or set the damage for every item. Here below is an example for a fully set icon:

{% code lineNumbers="true" %}
```yaml
EXAMPLE-ITEM:
  NAME: "&a&lExample name"
  MATERIAL: STAINED_GLASS_PANE
  DAMAGE: 3
  LORE:
    - "&8&m------------------------"
    - "&eExample lore"
    - "&8&m------------------------"
  DURABILITY: 20
  AMOUNT: 4
  ENCHANTMENTS:
    - "UNBREAKING:2"
  ITEMFLAGS:
    - "HIDE_ENCHANTS"
```
{% endcode %}

But this is a valid icon as well:

{% code lineNumbers="true" %}
```yaml
EXAMPLE-ITEM2:
  NAME: "&a&lExample name 2"
  MATERIAL: COMPASS
```
{% endcode %}

{% hint style="info" %}
config.yml, guis.yml and inventories.yml contains items that needs to be set up like this.
{% endhint %}

{% hint style="danger" %}
If an item doesn't have a **MATERIAL** section, it cannot be configured like the one above. Usually this happens with ladder or arena icons, where the material is the icon.
{% endhint %}

### Inventories file

The inventories file contains every inventory including the lobby, staff and party inventories. You can modify these icons for your needs. If you don't want to use an icon, please don't delete it just set the slot to the -1 value.

### Divisions file

In this file you can configure the divisions. You can only create 28 division in the file.

### Sidebar file

In this file you can configure every sidebar there is for the plugin.

{% tabs %}
{% tab title="Match placeholders" %}
Available placeholders in <mark style="color:orange;">every</mark> match:

* %player%, %duration%, %ping%, %arena%, %ladder%

Available placeholders in <mark style="color:orange;">duel</mark> matches:

* %rounds%,  %roundsNumber%, %playerTeamName%, %playerTeamColor%
* %enemyName%, %enemyPing%, %enemyRounds%, %enemyRoundsNumber%, %enemyTeamName%
* %enemyTeamColor%

Available placeholders in <mark style="color:orange;">party ffa</mark> matches:

* %players%, %alivePlayers%, %rounds%
* %player1%, %player1rounds%, %player1roundsNumber%
* %player2%, %player3rounds%, %player2roundsNumber%
* %player3%, %player2rounds%, %player3roundsNumber%

Available placeholders in <mark style="color:orange;">party split</mark> matches:

* %team1name%, %team1color%, %team1players%, %team1alivePlayers%, %team1rounds%, %team1roundsNumber%
* %team2name%, %team2color%, %team2players%, %team2alivePlayers%, %team2rounds%, %team2roundsNumber%

Available placeholders in <mark style="color:orange;">party vs party</mark> matches:

* %partyTeamName%, %partyTeamColor%, %partyTeamPlayers%,
* %partyTeamAlivePlayers%, %partyTeamRounds%, %partyTeamRoundsNumber%
* %enemyTeamName%, %enemyTeamColor%, %enemyTeamPlayers%,
* %enemyTeamAlivePlayers%, %enemyTeamRounds%, %enemyTeamRoundsNumber%
{% endtab %}

{% tab title="Spectator placeholders" %}
Available placeholders in <mark style="color:orange;">every</mark> match:

* %player%, %duration%, %ping%, %arena%, %ladder%

Available placeholders in <mark style="color:orange;">every match type</mark> except party ffa:

* %team1color%, %team1name%, %team2color%, %team2name%

Available placeholders in <mark style="color:orange;">duel</mark> matches:

* %player1%, %player1ping%, %player1rounds%, %player1roundsNumber%
* %player2%, %player2ping%, %player2rounds%, %player2roundsNumber%

Available placeholders in <mark style="color:orange;">party ffa</mark> matches:

* %players%, %alivePlayers%
* %player1%, %player1rounds%, %player1roundsNumber%
* %player2%, %player2rounds%, %player2roundsNumber%
* %player3%, %player3rounds%, %player3roundsNumber%

Available placeholders in <mark style="color:orange;">party split</mark> matches:

* %team1players%, %team1alivePlayers%, %team1rounds%, %team1roundsNumber%
* %team2players%, %team2alivePlayers%, %team2rounds%, %team2roundsNumber%

Available placeholders in <mark style="color:orange;">party vs party</mark> matches:

* %team1players%, %team1alivePlayers%, %team1rounds%, %team1roundsNumber%
* %team2players%, %team2alivePlayers%, %team2rounds%, %team2roundsNumber%
{% endtab %}
{% endtabs %}
