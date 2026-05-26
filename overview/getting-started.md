# 💡 Getting Started

### Download The Plugin

After you purchased and downloaded the plugin, you should put it in the plugins folder and start the server. Please be aware the following **requirements** that must be fulfilled for proper operation:

* Paper based 1.21.11 server and **Java 25.**

And in both cases the latest stable version of the [PacketEvents](https://www.spigotmc.org/resources/packetevents-api.80279/).

### **Starting up**

If these conditions are met you can start the server for the first time. It generates two arenas world, one which you can work with and one that is for copies. It also creates the main .yml files that you need to set up the plugin for your needs.

<details>

<summary>Configuration files</summary>

* **config.yml** contains the settings to configure the basic functionality of the plugin.
* **language.yml** contains every language element that flows through the chat.
* **guis.yml** contains every GUI settings including titles and items.
* **inventories.yml** contains the items for the lobby, party, queue etc. inventories.
* **ranks.yml** contains all the rank settings.
* **sidebar.yml** contains all the scoreboard/sidebar settings.
* **license.yml** contains the license informations.
* **playerkit.yml** contains the config for custom player kit.
* It also generates some <mark style="color:yellow;">default ladders</mark>.

</details>

You can find a **detailed description** about the configuration files [here](../informations/configuration-files.md).

The first thing to do is to **set the lobby location** with the \
<mark style="color:orange;">/practice lobby set</mark> command, then you can start configuring everything.\
You can use the <mark style="color:red;">/practice teleport</mark> command to teleport between worlds.

Add **zpp.group.default** permission to the default rank of the server. (I recommend using Luckperms for permission management)

There is GUI system called <mark style="color:orange;">**Server Manager**</mark> where you can set up nearly everything.

<div align="center"><figure><img src="../.gitbook/assets/ezgif.com-reverse-7.gif" alt=""><figcaption><p>Server Manager</p></figcaption></figure></div>

### Recommendations

Event & Match arena's center should be always a free space where players can be teleported, because it will happen for example when they die and become spectators.

If there are teams in a match (not PartyFFA) and the player's inventory contains leather armor or wool, they will get it according to their team color.

You can teleport between the arenas and lobby world with the /practice arenas and /practice lobby commands.
