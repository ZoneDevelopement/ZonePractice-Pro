---
description: Here you can find a detailed description about event setup and hosting.
---

# 🎮 Event Setup

## Setup

### Step 1: Editing the events through config.yml

There are some specific settings that can only be set from the config.yml file. After you edited these settings, please reset the server, because they can only be recharged when the server is restarted.

### Step 2: Set the icon

First you have to create an icon for the event just like you did with arenas and ladders. Then you open the event setup GUI and click on the events default icon.

<figure><img src="../.gitbook/assets/ezgif.com-optimize.gif" alt="" width="300"><figcaption><p>How to set an event's icon</p></figcaption></figure>

### **Step 3: Change the event's basic settings**

<figure><img src="../.gitbook/assets/ezgif.com-reverse-8.gif" alt=""><figcaption><p>Event settings GUI</p></figcaption></figure>

### Step 4: Event specific settings

For almost all events, there are also event-specific settings that you must configure before you can enable the event. You can achieve this by using the **/event \<event>** command. For example if you want to set the LMS event's kit you can, by using the /event lms setkit command. These types of commands also set the potion effects.

### Step 5: Set the event map

First of all you have to build or paste the event's map in the arenas world, otherwise you won't be able to select the corner's of it.

{% hint style="warning" %}
You have to select the event's arena big enough so players can't jump out in the sides, because then they will instanly be eliminated.
{% endhint %}

You can get a map marker item by left clicking in the location item in the map setup GUI.

<figure><img src="../.gitbook/assets/ezgif.com-reverse-9.gif" alt=""><figcaption><p>How to get a marker item</p></figcaption></figure>

After you have the marker item, you need to select the two corners of event map like this:

<div align="left"><figure><img src="../.gitbook/assets/68747470733a2f2f776f726c64656469742e656e67696e656875622e6f72672f656e2f6c61746573742f5f696d616765732f6375626f69642e706e67.webp" alt=""><figcaption></figcaption></figure></div>

### Step 6: Set positions & scan the map

You need to set player position markers inside the event map.

{% hint style="warning" %}
The scan will only take 2 player position markers into account in case of Brackets or Sumo event.
{% endhint %}

The markers should look like this (the head's direction will be converted accordingly)

<div align="left"><figure><img src="../.gitbook/assets/Képernyőfotó 2023-03-26 - 17.20.53.png" alt=""><figcaption></figcaption></figure></div>

After you set all the position markers in the map you need to scan it by right clicking on the location item in the event setup GUI. Depends on the size of the map but it can event last for minutes so don't panic.

**If you did all the steps above, the event is ready to be enabled.**

## Additional information

Event arena center should be always a free space where players can be teleported, because it will happen for example when they die and become spectators.

In Splegg event players can only destroy wool blocks.

In Splegg & Sumo events players will die if they fell into water or out of the arena.

## **Hosting**

Enabled events are in the event hosting GUI. Which the players can open by using the **/event host** command. If the player has zpp.event.host.all permission or the specific event's hosting permission zpp.event.host.\<event\_name>, for example zpp.event.host.lms they can start hosting the event.

You can set up automatic events by addig the AUTO-EVENTS list section in the config.yml under the event's section. This works with every event. Like this:

{% code overflow="wrap" lineNumbers="true" %}
```
LMS:
  AUTO-EVENTS:
    - "18:30"
```
{% endcode %}
