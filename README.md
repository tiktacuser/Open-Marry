# OpenMarry

A marriage system for Minecraft servers: players can marry, share a heart icon in the tab list, and get small bonuses as a couple.

Versions **1.21.1** and **26.3** are supported on Paper and Fabric. Paper builds are available both with and without Velocity support.

## Features

- Marriage proposals with accept/deny and an expiry time
- A heart placeholder for your tab list or chat, empty for unmarried players
- One heart color per marriage, changeable by either spouse and updated for both at once
- Heart particles above newlyweds
- Spouses can't hurt each other (melee and projectiles)
- Optional regeneration when spouses are near each other (off by default, radius and strength are configurable)
- Everything is configurable: colors, symbol, messages, effects, timings

## Commands

| Command | Description |
|---|---|
| `/marry <player>` | Propose to a player (the player must be online) |
| `/marry accept` | Accept a proposal |
| `/marry deny` | Decline a proposal |
| `/divorce` | End your marriage |
| `/heartcolor <color>` | Change the heart color of your marriage (both spouses see it) |

## Placeholders

| Platform | Placeholder |
|---|---|
| Paper (PlaceholderAPI) | `%hug_heart%` |
| Fabric (Text Placeholder API) | `%hug:heart%` |

Returns the heart in the marriage color, or nothing if the player is not married.

Example for the TAB plugin (`groups.yml`):

```yaml
_DEFAULT_:
  tabprefix: "%hug_heart% "
```

## Builds

| Module | Platform | Proxy support | Output |
|---|---|---|---|
| `paper-standalone-1.21.1` | Paper 1.21.1 | no | `OpenMarry-Paper-1.21.1-1.0.0.jar` |
| `paper-standalone-26` | Paper 26.3 | no | `OpenMarry-Paper-26-1.0.0.jar` |
| `paper-proxy-1.21.1` | Paper 1.21.1 | yes | `OpenMarry-Paper-Proxy-1.21.1-1.0.0.jar` |
| `paper-proxy-26` | Paper 26.3 | yes | `OpenMarry-Paper-Proxy-26-1.0.0.jar` |
| `fabric-1.21.1` | Fabric 1.21.1 | no | `OpenMarry-Fabric-1.21.1-1.0.0.jar` |
| `fabric-26` | Fabric 26.3 | no | `OpenMarry-Fabric-26-1.0.0.jar` |
| `velocity-proxy` | Velocity | - | `OpenMarry-Velocity-1.0.0.jar` |

Every jar is written to `<module>/build/libs/`.

## Installation

| Setup | What to install |
|---|---|
| Single Paper server | `OpenMarry-Paper-<version>` |
| Paper servers behind Velocity | `OpenMarry-Paper-Proxy-<version>` on every Paper server and `OpenMarry-Velocity` on the proxy |
| Fabric server | `OpenMarry-Fabric-<version>` |

With the proxy setup, marriages are stored on the proxy, so `/marry`, `/divorce` and `/heartcolor` work between all servers of the network.

Requirements:

- **Paper:** PlaceholderAPI (needed for the placeholder)
- **Fabric:** Fabric API and Text Placeholder API
- **Velocity build:** Java 25

## Configuration

| Setup | Config | Marriage data |
|---|---|---|
| Paper, standalone | `plugins/OpenMarry/config.yml` | `plugins/OpenMarry/data.yml` |
| Paper, behind a proxy | `plugins/OpenMarry/config.yml` (effects, protection, regeneration) | stored on the proxy |
| Velocity | `plugins/openmarry/config.properties` (colors, proposal time, messages) | `plugins/openmarry/data.properties` |
| Fabric | `config/openmarry.json` | `config/openmarry-data.json` |

Regeneration is disabled by default. Enable it in the `regeneration` section and set the radius, level, interval and duration.

## Building from source

Java 25 is required. The `gradlew` script downloads Gradle itself (it needs `curl` and `unzip`).

```bash
chmod +x gradlew
./gradlew build --continue
```

Build a single module:

```bash
./gradlew :paper-standalone-26:build
```

Dependency versions (Fabric Loader, Fabric API, Loom, Text Placeholder API) are defined in `gradle.properties`. If a dependency cannot be resolved, replace it with a current version from https://fabricmc.net/develop/.

If `repo.extendedclip.com` is unreachable, download the PlaceholderAPI jar manually, put it into a `libs` folder and replace the dependency in the Paper `build.gradle.kts` files with `compileOnly(files("../libs/placeholderapi-2.11.6.jar"))`.

## Project layout

```
shared/paper-core        code shared by all Paper builds
shared/paper-standalone  Paper build without a proxy
shared/paper-proxy       Paper build that receives marriage data from Velocity
shared/fabric-core       code shared by both Fabric builds
paper-*, fabric-*        Gradle modules, one per build
velocity-proxy           Velocity plugin that stores marriages and handles commands
```
