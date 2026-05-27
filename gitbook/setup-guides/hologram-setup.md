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

Types:

- `global`
- `ladder_static`
- `ladder_dynamic`

## Step 3: Position and configure

Move an existing hologram to your location:

- `/practice hologram teleport <name>`

Then finish setup in the hologram GUI.

<figure><img src="../.gitbook/assets/ezgif.com-reverse-10.gif" alt="Hologram setup walkthrough"><figcaption><p>Hologram setup walkthrough</p></figcaption></figure>

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
