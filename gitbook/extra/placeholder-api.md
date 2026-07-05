---
description: PlaceholderAPI placeholders provided by ZonePractice.
---

# Placeholder API

ZonePractice registers both identifiers:

- `zpp`
- `zppro`

So `%zpp_x%` and `%zppro_x%` both work.

## General placeholders

| Placeholder | Description |
| --- | --- |
| `%zpp_in_queue%` | Total players currently in queue |
| `%zpp_in_queue_<ladder>%` | Queue size for a specific ladder |
| `%zpp_in_fight%` | Total players currently in matches |
| `%zpp_in_fight_<ladder>%` | Players currently in matches for a specific ladder |

## Group placeholders

| Placeholder | Description |
| --- | --- |
| `%zpp_group_name%` | Player's group name |
| `%zpp_group_prefix%` | Player's group prefix |
| `%zpp_group_suffix%` | Player's group suffix |
| `%zpp_group_limit_u%` | Player's daily unranked limit |
| `%zpp_group_limit_r%` | Player's daily ranked limit |

## Division placeholders

| Placeholder | Description |
| --- | --- |
| `%zpp_division_short%` | Short division name (e.g. B1, S2) |
| `%zpp_division_full%` | Full division name (e.g. Bronze I, Silver II) |

## Win/loss placeholders

| Placeholder | Description |
| --- | --- |
| `%zpp_wins_global%` | Total global wins |
| `%zpp_wins_global_u%` | Global unranked wins |
| `%zpp_wins_global_r%` | Global ranked wins |
| `%zpp_wins_ladder_<ladder>_u%` | Unranked wins on a specific ladder |
| `%zpp_wins_ladder_<ladder>_r%` | Ranked wins on a specific ladder |
| `%zpp_losses_global%` | Total global losses |
| `%zpp_losses_global_u%` | Global unranked losses |
| `%zpp_losses_global_r%` | Global ranked losses |
| `%zpp_losses_ladder_<ladder>_u%` | Unranked losses on a specific ladder |
| `%zpp_losses_ladder_<ladder>_r%` | Ranked losses on a specific ladder |
| `%zpp_kills_ladder_<ladder>%` | Kills on a specific ladder |
| `%zpp_deaths_ladder_<ladder>%` | Deaths on a specific ladder |

## Elo placeholders

| Placeholder | Description |
| --- | --- |
| `%zpp_elo_global%` | Global elo rating (sum of all ladders) |
| `%zpp_elo_ladder_<ladder>%` | Elo rating on a specific ladder |

## Experience placeholder

| Placeholder | Description |
| --- | --- |
| `%zpp_exp%` | Player experience |

## Leaderboard placeholders

Global leaderboards:

- `%zpp_lb_global_wins_<1-10>_k%` — player name at position
- `%zpp_lb_global_wins_<1-10>_v%` — win count at position
- `%zpp_lb_global_elo_<1-10>_k%` — player name at position
- `%zpp_lb_global_elo_<1-10>_v%` — elo at position

Per ladder:

- `%zpp_lb_ladder_<ladder>_wins_<1-10>_k%` — player name at position
- `%zpp_lb_ladder_<ladder>_wins_<1-10>_v%` — win count at position
- `%zpp_lb_ladder_<ladder>_elo_<1-10>_k%` — player name at position
- `%zpp_lb_ladder_<ladder>_elo_<1-10>_v%` — elo at position

## FFA placeholders

| Placeholder | Description |
| --- | --- |
| `%zpp_ffa_<arena>_players%` | Player count in an FFA arena |
| `%zpp_ffa_<arena>_spectators%` | Spectator count in an FFA arena |

## Nametag color placeholder

| Placeholder | Description |
| --- | --- |
| `%zpp_nametag_color%` | MiniMessage color tag (e.g. `<red>`) based on match team or lobby nametag |
