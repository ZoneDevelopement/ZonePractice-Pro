---
description: Detailed event setup and hosting workflow.
---

# Event Setup

Supported event setup commands:

- `/event lms ...`
- `/event oitc ...`
- `/event tnttag ...`
- `/event brackets ...`
- `/event sumo ...`
- `/event splegg ...`
- `/event juggernaut ...` (alias: `jn`)

## Step 1: Configure event settings in `config.yml`

Review the `EVENT` section and per-event blocks. For major edits, restart server after saving.

## Step 2: Set event icon in setup GUI

Open event setup and replace default icon.

<figure><img src="../.gitbook/assets/ezgif.com-optimize.gif" alt="Setting event icon"><figcaption><p>Set event icon</p></figcaption></figure>

## Step 3: Configure basic event settings

Use event setup GUI to set core options.

<figure><img src="../.gitbook/assets/ezgif.com-reverse-8.gif" alt="Event settings GUI"><figcaption><p>Event settings GUI</p></figcaption></figure>

## Step 4: Configure event-specific options

Use event commands (example):

- `/event lms ...`

Each event has specific required options (kits, effects, behavior).

## Step 5: Set event map area

1. Build/paste map in `arenas` world.
2. Get marker item from event map setup.

<figure><img src="../.gitbook/assets/ezgif.com-reverse-9.gif" alt="Getting event marker item"><figcaption><p>Get map marker item</p></figcaption></figure>

3. Select 2 corners so full map is enclosed.

<div align="left"><figure><img src="../.gitbook/assets/68747470733a2f2f776f726c64656469742e656e67696e656875622e6f72672f656e2f6c61746573742f5f696d616765732f6375626f69642e706e67.webp" alt="Event map corner selection"><figcaption><p>Corner selection</p></figcaption></figure></div>

## Step 6: Set spawn markers and scan

Place player position markers, then scan map.

<div align="left"><figure><img src="../.gitbook/assets/Képernyőfotó 2023-03-26 - 17.20.53.png" alt="Event spawn marker example"><figcaption><p>Spawn marker example</p></figcaption></figure></div>

Notes:

- For Brackets/Sumo, scan uses only the needed marker count.
- Large maps can take longer to scan.

## Step 7: Enable and host

Host flow:

- `/event host` to open host GUI
- `/event join` for players
- `/event stop <event_name>` to stop (permission required)

## Automatic events

You can define per-event schedule times in each event block under `AUTO-EVENTS`, for example:

```yaml
LMS:
  AUTO-EVENTS:
    - "18:30"
```

## Beginner troubleshooting

If players cannot host:

- Check `zpp.event.host` or `zpp.event.host.all`
- Check specific permission like `zpp.event.host.lms`

If event cannot start:

- Re-check icon/settings/map/spawn setup
- Re-run map scan after region edits
