---
description: Here you can find a detailed description about ladder creation and setup.
---

# ⚔️ Ladder Setup

<mark style="color:red;">**Note:**</mark> The plugin automatically generates the following ladders: archer, axe, bedwars, boxing, builduhc, combo, debuff, gapple, nodebuff, pearlfight, fireball, sg, skywars, soup, spleef, sumo, vanilla, battlerush and bridges.

### Step 1: Create a ladder.

First of all you have to create a new ladder by using the **/ladder create \<name>**.

If you give the command an acceptable ladder name the type selector GUI shows up.

<figure><img src="../.gitbook/assets/ezgif.com-reverse-2.gif" alt=""><figcaption><p>Ladder types</p></figcaption></figure>

The following ladder types are currently available:

* **Basic:** This type doesn't really have any special property. It has normal PVP rules and building is disabled, so in the same arena multiple matches can happen at the same time.
* **Build:** Same as basic but building is enabled.
* **Sumo:** Damage is disabled, and players die if they touch water or fall out of the arena. Building is disabled.
* **Boxing:** Damage is disabled, players compete to see who can get the damage points they need to win the game first. Building is disabled.
* **Temporary Build:** This type allows you to create very unique ladders. The blocks that players place will be returned to the player after a few seconds.
* **Spleef:** Players have to destroy the blocks bellow other players with their shovel. They can only destory snow block INSIDE the match arena.
* **SkyWars:** Players can loot chest from which they receive a random number of loots which you have to set up beforehand. Building is enabled.
* **BedWars:** Players can only die permanently if their bed is destroyed. Building is enabled.
* **Fireball Fight:** Players can jump between islands using their fireballs and break blocks with their TNTs. The main objective is to protect their beds.
* **Bridges:** In order to win the rounds players have to jump in the other team's portal which is in the other side of the bridge. Building is enabled.
* **BattleRush:** In this gamemode there is no damage. The goal is to jump in the other player's portal. Players can build and there is a respawn cooldown.

{% hint style="info" %}
Every type has a special property that cannot be changed, but there are other settings which allows the creation of customized ladders.&#x20;

A few ladder types like boxing, skywars and bedwars have a unique setting that can be set from the settings GUI of the ladder.
{% endhint %}

After you select the type, the ladder's setup GUI shows up.

<figure><img src="../.gitbook/assets/ezgif.com-reverse-5.gif" alt=""><figcaption><p>Ladder setup GUI</p></figcaption></figure>

Here you can set up everything there is for the ladder.

### Step 2: Set the icon & displayname

You have to create an item that is going to be the ladder's icon. There is a command to help you do this. You can rename items with the **/practice rename \<name>** command if you hold an item in your hand. \
Then you can set the ladder's icon by using the **/ladder set icon \<arena>** command.

{% hint style="warning" %}
The icon's name is going to be the ladder's display name, that is going to be shown everywhere for players and administrators. This is for the sake of customizability.
{% endhint %}

### Step 3: Adjust the settings

Open the settings GUI and customise the settings that are right for you.

<figure><img src="../.gitbook/assets/ezgif.com-reverse-6.gif" alt=""><figcaption><p>Settings GUI</p></figcaption></figure>

### Step 4: Set the inventory

You can use the **/ladder set inventory \<ladder>** command to set the ladder's inventory. This set's the effect as well but you can change this seperately with the **/ladder set effects \<ladder>** command. You can change the inventory content from the GUI as well.\
If the ladder is editable you can add extra items that players can use for their custom kits, separately for unranked and ranked.

### Step 5: Assign match types

Before enabling you need to assign at least one match type for the ladder. This means that for example if you assign duel to the ladder, players will be able to play duels (unranked, ranked, etc.) with the ladder. This setting allows you to create different game modes or set unique game settings for team games.

### Comment:

{% hint style="danger" %}
You can only customize **disabled** ladders!
{% endhint %}

There are several requirements for the enabling of a ladder:

* Set an icon.
* Set the inventory.
* Assign at least one match type.
* If the arena is skywars, set the skywars loot.

Only ladders with duel enabled as match type will contain match statistics like unranked and ranked wins, elo etc.

If a ladder is deactivated, it's statistics are deleted from the mysql database. If it is reactivated, the data will be reloaded into the database the next time it is automatically saved. If a player's data is deleted due to inactivity, the mysql data is also deleted.

If you want to edit an enabled ladder but there are ongoing matches with it you can force stop all of them by using the **/ladder stop \<ladder>** command or you can <mark style="color:blue;">**freeze**</mark> the ladder, which means new matches cannot start with it, and wait for the ongoing matches to end.
