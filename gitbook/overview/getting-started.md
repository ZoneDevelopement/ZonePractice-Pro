# Getting Started

This page is a beginner-safe checklist. Follow it in order, and do not skip steps.

## Step 0: Before you start

You need:

- A Paper server (`1.21.11` or `26.1.2`)
- Java 25
- `PacketEvents` 2.x plugin installed in `plugins/`

Optional but recommended:

- LuckPerms (for permissions)
- PlaceholderAPI (for placeholders)
- FastAsyncWorldEdit (for fast arena copying)

## Step 1: Install ZonePractice Pro

1. Stop your server.
2. Put ZonePractice Pro `.jar` into your `plugins/` folder.
3. Make sure `PacketEvents` is also in `plugins/`.
4. Start the server.

If startup succeeds, ZonePractice creates:

- Worlds: `arenas`, `arenas_copy`
- Files:
  - `config.yml`
  - `language.yml`
  - `guis.yml`
  - `inventories.yml`
  - `sidebar.yml`
  - `groups.yml`
  - `divisions.yml`
  - `playerkit.yml`
  - `backend.yml`
  - `ladders/*.yml` (default ladder files)
  - `match-history/` (player match history)

## Step 2: Set the lobby first

Run:

- `/practice lobby set`

This is required before most player features work correctly.

## Step 3: Give default player permission

Give your default group:

- `zpp.group.default`

If you use LuckPerms, add this permission to your base rank.

## Step 4: Open setup manager

Run:

- `/setup`

This opens the main setup GUI where you manage ladders, arenas, events, holograms, and more.

<div align="center"><figure><img src="../.gitbook/assets/ezgif.com-reverse-7.gif" alt="Server Manager walkthrough"><figcaption><p>Server Manager setup GUI</p></figcaption></figure></div>

## Step 5: Configure in the correct order

Use this order to avoid confusion:

1. Ladders
2. Arenas
3. Events
4. Holograms

Then test with real players.

## Useful teleport commands while setting up

- `/practice arenas`
- `/practice lobby`
- `/practice teleport <world>`

## Beginner troubleshooting

If `/setup` does not open:

- Make sure you are OP or have `zpp.setup` permission
- Check the plugin loaded successfully in the server console

If players cannot use normal features:

- Make sure they have `zpp.group.default` permission
- Make sure the lobby was set with `/practice lobby set`

If plugin disables on startup:

- Check Java version (requires Java 25)
- Check your server version (`1.21.11` or `26.1.2` — see [Compatibility Notes](../extra/modern-version-support-informations.md))
- Check that PacketEvents 2.x is in your plugins folder and loaded
