---
description: PlaceholderAPI placeholders provided by ZonePractice.
---

# Placeholder API

ZonePractice registers both identifiers:

- `zpp`
- `zppro`

So `%zpp_x%` and `%zppro_x%` both work.

## General placeholders

- `%zpp_in_queue%`
- `%zpp_in_queue_<ladder>%`
- `%zpp_in_fight%`
- `%zpp_in_fight_<ladder>%`

## Group placeholders

- `%zpp_group_name%`
- `%zpp_group_prefix%`
- `%zpp_group_suffix%`
- `%zpp_group_limit_u%`
- `%zpp_group_limit_r%`

## Division placeholders

- `%zpp_division_short%`
- `%zpp_division_full%`

## Win/loss placeholders

- `%zpp_wins_global%`
- `%zpp_wins_global_u%`
- `%zpp_wins_global_r%`
- `%zpp_wins_ladder_<ladder>_u%`
- `%zpp_wins_ladder_<ladder>_r%`
- `%zpp_losses_global%`
- `%zpp_losses_global_u%`
- `%zpp_losses_global_r%`
- `%zpp_losses_ladder_<ladder>_u%`
- `%zpp_losses_ladder_<ladder>_r%`

## Elo placeholders

- `%zpp_elo_global%`
- `%zpp_elo_ladder_<ladder>%`

## Leaderboard placeholders

Global:

- `%zpp_lb_global_wins_<1-10>_k%`
- `%zpp_lb_global_wins_<1-10>_v%`
- `%zpp_lb_global_elo_<1-10>_k%`
- `%zpp_lb_global_elo_<1-10>_v%`

Per ladder:

- `%zpp_lb_ladder_<ladder>_wins_<1-10>_k%`
- `%zpp_lb_ladder_<ladder>_wins_<1-10>_v%`
- `%zpp_lb_ladder_<ladder>_elo_<1-10>_k%`
- `%zpp_lb_ladder_<ladder>_elo_<1-10>_v%`

## FFA placeholders

- `%zpp_ffa_<arena>_players%`
- `%zpp_ffa_<arena>_spectators%`

## Nametag color placeholder

- `%zpp_nametag_color%`

This returns MiniMessage color tag output (for example `<red>`) based on match team color or lobby nametag color.
