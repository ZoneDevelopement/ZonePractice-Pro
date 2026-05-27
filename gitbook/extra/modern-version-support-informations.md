# Compatibility Notes

## Server/runtime

Current runtime version detection accepts:

- `1.21.x` (including `1.21.11`)
- `26.x`

ZonePractice disables itself automatically on unsupported versions.

## Required runtime dependency

- `PacketEvents` must be installed as a separate plugin.

## Configuration migration behavior

- Core config files are auto-updated by version key.
- Unknown custom keys in `config.yml` are preserved where possible.
- Always back up your `plugins/ZonePracticePro/` folder before upgrading major plugin versions.

## Legacy data warning

Do not assume old setups from significantly different protocol/material eras are directly portable without validation. Always test migration on a staging server first.
