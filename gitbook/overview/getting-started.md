# Getting Started

This page is a beginner-safe checklist. Follow it in order, and do not skip steps.

## Step 0: Before you start

You need:

- A Paper-based server
- Java 25
- `PacketEvents` plugin installed in `plugins/`

Optional but recommended:

- LuckPerms (for permissions)
- PlaceholderAPI

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
  - default ladder files under `ladders/`

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

This opens the main setup GUI where you manage ladders, arenas, events, and more.

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

- Check you are OP or have `zpp.setup`
- Check plugin loaded in console

If players cannot use normal features:

- Confirm they have `zpp.group.default`
- Confirm lobby was set using `/practice lobby set`

If plugin disables on startup:

- Check Java version (`25`)
- Check server version compatibility
- Check PacketEvents is installed and loaded
