# Commit Message Conventions

This project uses commit message conventions to automatically generate categorized changelogs for releases.

## Commit Message Format

Start your commit messages with one of these prefixes to automatically categorize them in the release changelog:

### Bug Fixes 🐛
Use these prefixes for bug fixes:
- `fix:`
- `bugfix:`
- `fixed:`
- `bug:`

**Example:**
```
fix: Players names now get colorized in nametag when using TAB plugin
```

### New Features ✨
Use these prefixes for new features:
- `feat:`
- `feature:`
- `add:`
- `added:`

**Example:**
```
feat: Add support for custom arena configurations
```

### Enhancements 🚀
Use these prefixes for improvements to existing features:
- `enhance:`
- `enhancement:`
- `improve:`
- `improved:`
- `update:`
- `updated:`

**Example:**
```
enhance: Improve group management for de-opped players
```

### Breaking Changes ⚠️
Use these prefixes for breaking changes:
- `break:`
- `breaking:`
- `BREAKING:`

**Example:**
```
breaking: Change API method signatures for match handling
```

### Documentation 📝
Use these prefixes for documentation updates:
- `doc:`
- `docs:`
- `documentation:`

**Example:**
```
docs: Update installation guide
```

### Other Changes 🔧
Commits that don't match any of the above categories will be grouped under "Other Changes".

## Examples

Good commit messages:
- ✅ `fix: De-opped players now revert to default group`
- ✅ `feat: Add TAB integration for tablist formatting`
- ✅ `enhance: Optimize database queries for player profiles`
- ✅ `docs: Add commit conventions guide`

These will be automatically categorized in the release notes!

## Release Process

1. Make your changes and commit them using the conventions above
2. Update the version in `pom.xml`
3. Push to `master` or `main` branch
4. GitHub Actions will automatically:
   - Detect the version change
   - Build the project
   - Generate a categorized changelog
   - Create a GitHub release with the changelog

The release description will look like:

```markdown
# Changelog v6.3

## 🐛 Bug Fixes
- fix: Players names now get colorized in nametag when using TAB plugin
- fix: De-opped players now revert to default group

## 🚀 Enhancements
- enhance: Improve group permission validation
- update: Optimize TAB integration for modern versions

## ✨ New Features
- feat: Add automatic changelog generation

## 🔧 Other Changes
- Minor code cleanup
- Update dependencies
```
