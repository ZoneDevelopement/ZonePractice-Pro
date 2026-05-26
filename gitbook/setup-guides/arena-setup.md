---
description: Here you can find a detailed description about arena creation and setup.
---

# 🧊 Arena Setup

{% hint style="danger" %}
I recommend starting with ladder setup since you will need to assign ladders to arenas and it's easier if you start with the ladders.
{% endhint %}

### Step 1: Create an empty arena

First of all by using the **/arena create \<name>** command you can create empty arenas.

I am going to use the "Teszt" arena name in this example. Also I chose build arena type so I can show you the arena copy manager as well. It is not available in basic arenas, since they can run infinite matches in the same time.

If you gave the command an acceptable arena name and run it, this GUI below should pop up. Here you can select the type of the arena. It should look like this:

<figure><img src="../.gitbook/assets/Screenshot 2024-10-10 at 22.01.02.png" alt=""><figcaption></figcaption></figure>

After you select the arena type the arena's setup GUI shows up.

<figure><img src="../.gitbook/assets/Screenshot 2024-10-10 at 22.09.25.png" alt=""><figcaption><p>Build Arena</p></figcaption></figure>

<figure><img src="../.gitbook/assets/Screenshot 2024-10-10 at 22.14.17.png" alt=""><figcaption><p>FFA Arena</p></figcaption></figure>

Here you can set up everything there is for the arena.

### Step 2: Set the icon & displayname

You have to create an item that is going to be the arena's icon. There is a command to help you to do this. You can rename items with the **/practice rename \<name>** command if you hold an item in your hand. Then you can set the arena's icon by using the **/arena set icon \<arena>** command.

{% hint style="warning" %}
The icon's name is going to be the arena's display name, that is going to be shown everywhere for players and administrators. This is for the sake of customisability.
{% endhint %}

### Step 3: Edit the locations

First of all you have to build or paste the arena into the "arenas" world. I recommend using [FAWE](https://www.spigotmc.org/resources/fastasyncworldedit.13932/) for building.

{% hint style="info" %}
I recommend changing the world edit compass to another item, because the plugin uses the compass item for multiple features and it can interfere when admin's use it.&#x20;
{% endhint %}

The plugin is using a setup wand to ensure a smooth solution for arena setup.

The Arena Setup Wand is an in-world configuration tool that allows admins to set up arenas without opening GUIs. Here's how it works:\
\
**What It Is?**

* A blaze rod item with dynamic lore that guides players through arena configuration&#x20;
* Enables a "setup session" where players enter an interactive mode to define arena properties

**How to Use It?**

1. **Start Setup Mode:** Go to the arenas **MAIN GUI** where you can see the icon, settings, copies etc, then click on the arena's icon.
2. **Receives the Wand:** A blaze rod with instructions for the current setup mode
3. **Click Blocks:** Left/right click blocks and air to set positions based on the current mode
4. **Switch Modes:** Shift + Left/Right Click to navigate between setup modes
5. **Exit:** Drop the wand (press Q) to stop editing.

**Setup Modes**\
The wand has 8 different modes depending on arena type:

* **Corner Selection:** Define the arena region with two corner blocks
* **Spawn Points (Standard):** Set blue and red team spawn locations
* **Spawn Points (FFA):** Add/remove multiple FFA spawns using armor stands
* **Build Height Limit:** Set max build height
* **Dead Zone:** Define the void damage threshold
* **Bed Locations:** Mark bed positions
* **Portal Setup:** Define portal regions
* **Arena Status:** Enable/disable the arena

You have to select the two corners of the arena like this, everything in the map needs to be inside these two points.

<div align="left"><figure><img src="../.gitbook/assets/68747470733a2f2f776f726c64656469742e656e67696e656875622e6f72672f656e2f6c61746573742f5f696d616765732f6375626f69642e706e67.webp" alt=""><figcaption></figcaption></figure></div>

### Step 4: Assign ladders

You need to assign ladders to the arena that players can play with in the arena. Each arena needs at least 1 ladder to be assigned to it. For basic arenas you can assign non-build ladders and for build arenas you can assign build ladders. You can achive this from the arenas setup GUI.

First you will need to assign ladder types for the arena.

{% hint style="info" %}
If you enable a ladder whose type is already assigned to arenas, it will be automatically assigned to those arenas.
{% endhint %}

<div data-full-width="true"><figure><img src="../.gitbook/assets/Screenshot 2023-09-24 at 11.07.50.png" alt="" width="563"><figcaption></figcaption></figure></div>

All ladders of the type you have assigned to the arena will be automatically assigned. You can then delete the assignment one by one.

<figure><img src="../.gitbook/assets/Screenshot 2023-09-24 at 11.16.11.png" alt="" width="563"><figcaption></figcaption></figure>

### Step 5: Specific arenas

You can create bed arenas where you need to set the two bed for the teams. You have to use the setup wand for this as well.

You can create portal arenas where you have to set the two portals for the teams. You have to look at the portals center (you don't have to set the portal blocks, the plugin will do that) and use the setup wand's designated setting for this.

### Step 6: Optional

You can set the maximum height that players can build in the arena. You should just go to the desired location within the arena than use the **/arena set buildmax \<arena>** command. Otherwise, the maximum building height will be calculated from the level of the first player's position, by adding the number specified in the config.

You can set the deadzone where players die if they fall below that. This can be useful if the arena cube's lowest Y level it too low for you. You should just go to the desired location within the arena than use the **/arena set deadzone \<arena>** command.

You can set the portal protection for arenas that have portals. This setting is only used with BattleRush or Bridges ladder types. It basically prevents the portal to get damaged in a circle. You can set it with the **/arena set portalprot \<arena> \<radius>** command.

### Step 7: Making copies (Only for build arenas)

ZonePractice makes it extremely easy to make copies of arenas, your only job is to open the arena copy manager from the arena main setup GUI and click "Create a new copy". \
The plugin automatically finds a free space for it in the arenas\_copy world and copies it asynchronously. You can only create 1 copy for an arena at the time. This is done to avoid lagging. \
After the new copy is generated, the players can start playing in it straight away.

<figure><img src="../.gitbook/assets/ezgif.com-reverse.gif" alt=""><figcaption><p>Arena copying asynchronously</p></figcaption></figure>

### Comment:

Players can only play in enabled arenas and enabling an arena has a few requirements:

* Set an icon.
* Set the two corners.
* Set the player positions.
* At least 1 assigned ladder.
* If the arena is bed related (Bedwars, Fireball Fight), set the two beds.
* If the arena is portal related (BattleRush or Bridges), set the two portals.

{% hint style="danger" %}
You can only customize disabled arenas.
{% endhint %}

You cannot change anything in the arenas\_copy world!

If you want to edit an enabled arena but there are ongoing matches in it you can force stop all of them by using the **/arena stop \<arena>** command or you can <mark style="color:blue;">**freeze**</mark> the arena, which means new matches cannot start in it, and wait for the ongoing matches to end.
