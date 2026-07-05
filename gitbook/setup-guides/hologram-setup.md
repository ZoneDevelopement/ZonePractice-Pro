---
description: Hologram setup for global and ladder leaderboards.
---

# Hologram Setup

Holograms are created and managed with `/practice hologram`.

## Step 1: Review hologram settings in `config.yml`

Check hologram update timing and visual settings.

## Step 2: Create hologram

Command:

- `/practice hologram create <name> <type>`

Hologram type determines what data it shows:

- `global` — shows one global leaderboard (e.g. top ELO across all ladders)
- `ladder_static` — shows a single ladder's leaderboard (you pick which ladder)
- `ladder_dynamic` — cycles through ladders automatically, switching every few seconds

## Step 3: Position and configure

Move an existing hologram to your location:

- `/practice hologram teleport <name>`

View hologram list:

- `/practice hologram list`

Delete a hologram:

- `/practice hologram delete <name>`

Then finish setup in the hologram GUI.

<figure><img src="../.gitbook/assets/ezgif.com-reverse-10.gif" alt="Hologram setup walkthrough"><figcaption><p>Hologram setup walkthrough</p></figcaption></figure>

## Choosing what to display in the hologram GUI

Right-click the hologram to open its settings GUI. There you pick:

1. **Leaderboard stat** — what data to rank players by
2. **Ladder** (for `ladder_static`/`ladder_dynamic` types) — which ladder's stats to show
3. **Lines** — how many top positions to show (1–10)

Available leaderboard stats:

| Stat | Ranks players by |
| --- | --- |
| ELO | Ranked elo rating |
| WIN | Total wins |
| KILLS | Total kills |
| DEATHS | Total deaths |
| WIN_STREAK | Current win streak |
| BEST_WIN_STREAK | Best ever win streak |
| LOSE_STREAK | Current lose streak |
| BEST_LOSE_STREAK | Best ever lose streak |

## Customizing the look in `config.yml`

All hologram visuals are configured in `config.yml` under `LEADERBOARD.HOLOGRAM.FORMAT`. You can change:

| Setting | What it does |
| --- | --- |
| `TITLE-LINE-SPACING` | Vertical space between title and first player line |
| `LINE-SPACING` | Vertical space between player lines |
| `FORMAT` | How each line looks, using placeholders |
| `NULL-LINE` | How empty slots appear (no player data yet) |
| `NOTHING-TO-DISPLAY` | Shown when no leaderboard data exists at all |

Available line placeholders:
`%placement%` — position number, `%player%` — player name, `%score%` — stat value, `%division%` — full division name, `%division_short%` — short division, `%group%` — group prefix/suffix, `%ladder_name%`, `%ladder_displayName%`

Each stat type (ELO, WIN, etc.) has its own `TITLE` and `LINES` templates — you can set different titles per stat.

Update intervals (in `LEADERBOARD.HOLOGRAM`):
- `DYNAMIC-UPDATE`: how often dynamic holograms switch ladders (default 10 sec)
- `STATIC-UPDATE`: how often static/global holograms refresh data (default 30 sec)

## Showcase

<figure><img src="../.gitbook/assets/ezgif.com-gif-maker.gif" alt="Dynamic hologram example"><figcaption><p>Dynamic hologram updates</p></figcaption></figure>

<div><figure><img src="../.gitbook/assets/Képernyőfotó 2023-03-27 - 15.08.49.png" alt="Elo hologram example"><figcaption><p>Elo leaderboard</p></figcaption></figure> <figure><img src="../.gitbook/assets/Képernyőfotó 2023-03-27 - 15.25.11.png" alt="Wins hologram example"><figcaption><p>Win leaderboard</p></figcaption></figure></div>

## Notes

- On fresh servers, holograms may show placeholder/empty data until stats exist.
- Data updates automatically as matches are played and saved.

## Troubleshooting

If hologram shows no values:

- Make sure ladders/matches are active
- Wait for leaderboard update cycle
- Restart once if data was just imported
