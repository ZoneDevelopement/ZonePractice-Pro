# Compatibility Notes

## Server/runtime

Current runtime version detection accepts:

- `1.21.11`
- `26.1.2`

ZonePractice disables itself automatically on unsupported versions.

## Java

- **Java 25** is required.

## Required runtime dependency

- `PacketEvents` 2.x must be installed as a separate plugin.

## Soft dependencies (optional)

- **PlaceholderAPI** — enables placeholder expansion (`zpp`, `zppro` identifiers)
- **Multiverse-Core** — world management integration
- **FastAsyncWorldEdit** — fast arena copy support
- **My_Worlds** — world management integration
- **TAB** — nametag and tab list integration

## Load order

ZonePractice Pro loads **before** CMI and CMILib to avoid item/name conflicts.

## Configuration migration behavior

- Core config files are auto-updated by version key (current version: 60).
- Unknown custom keys in `config.yml` are preserved where possible.
- Always back up your `plugins/ZonePracticePro/` folder before upgrading major plugin versions.

## Legacy data warning

Do not assume old setups from significantly different protocol/material eras are directly portable without validation. Always test migration on a staging server first.
