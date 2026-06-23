# Cobblemon Max IVs

Sets every Pokémon's IVs to a configurable maximum (31/perfect by default) —
wild spawns, trainer-owned Pokémon (including NPC-trainer addons like
**Radical Cobblemon Trainers**), player-owned Pokémon, eggs, gifts, all of it.

Fabric mod for Minecraft 1.21.1 / Cobblemon 1.7.x.

## How it works

There are two layers, because *when* you can know whether a given Pokémon is
"wild", "trainer-owned", or "player-owned" changes over its lifetime:

1. **A Mixin into `Pokemon`'s constructor.** Every single Pokémon object ever
   created in Cobblemon — no matter which system creates it (the wild
   spawner, an NPC trainer's party builder, an egg hatching, a `/pokegive`
   command, an addon mod like RCTrainers) — passes through this constructor.
   The mixin runs right after Cobblemon's own `IVs.createRandomIVs()` call and
   overwrites every stat with the configured value. This is what guarantees
   *universal* coverage, including addons this mod has never heard of.

2. **An event listener on `CobblemonEvents.POKEMON_ENTITY_SPAWN`.** At
   construction time, Cobblemon hasn't yet decided whether a Pokémon is wild,
   trainer-owned, or player-owned (that gets assigned moments later). So if
   you want different behavior per category (e.g. "max IVs for trainers but
   not for wild Pokémon"), the mixin alone can't make that call correctly.
   This listener re-applies the configured value once the Pokémon actually
   spawns into the world as an entity — by which point Cobblemon has reliably
   assigned its `originalTrainerType`, so the category toggles in the config
   work as expected. This event fires for wild spawns *and* for any Pokémon
   sent out into battle by a trainer (including RCTrainers and other
   NPC-trainer addons), since they're all just ordinary Cobblemon `Pokemon`
   instances under the hood.

Net effect: if you leave all three category toggles enabled (the default),
every Pokémon gets maxed out the instant it's created — simple and total. If
you narrow it down to specific categories, the event listener makes sure that
narrowing is respected correctly.

## Configuration

Found at `config/cobblemonmaxivs.json` after the first launch with the mod
installed. All fields:

```json
{
  "enabled": true,
  "maxIvValue": 31,
  "applyToWildPokemon": true,
  "applyToTrainerPokemon": true,
  "applyToPlayerPokemon": true,
  "onlyRaiseNeverLower": false
}
```

| Field                   | Default | Meaning                                                                                                     |
| ----------------------- | ------- | ------------------------------------------------------------------------------------------------------------|
| `enabled`               | `true`  | Master on/off switch.                                                                                       |
| `maxIvValue`            | `31`    | The IV value written to every stat. Clamped to 0-31.                                                        |
| `applyToWildPokemon`    | `true`  | Wild spawns, fishing, spawn-bait Pokémon, etc.                                                              |
| `applyToTrainerPokemon` | `true`  | NPC-trainer-owned Pokémon — gym leaders, rivals, and addons like RCTrainers.                                |
| `applyToPlayerPokemon`  | `true`  | Player-owned Pokémon: caught, bred, hatched, gifted, traded in.                                             |
| `onlyRaiseNeverLower`   | `false` | If true, never lowers an IV that's already at or above `maxIvValue` — only raises IVs that are below it.   |

After editing the file, run `/maxivs reload` in-game (requires op / permission
level 2) to apply changes without restarting the server.

## Building

This project depends on Cobblemon at compile time. Cobblemon isn't
consistently published to a conventional Maven repository for every version,
so the build is set up to pull it from **Curse Maven**, which lets you pin an
exact CurseForge file.

**Before building**, note that `gradle.properties` already has a confirmed
Curse Maven file ID (`7553235`) pinned for Cobblemon 1.7.3, Fabric, Minecraft
1.21.1 — no lookup needed unless you want a different version. If you do want
a different version:

1. Go to <https://www.curseforge.com/minecraft/mc-mods/cobblemon/files>
2. Filter for Minecraft 1.21.1, Fabric, the version you want
3. Open that file's page — the numeric file ID is in the URL
   (`.../files/1234567` -> file ID is `1234567`)
4. Paste it into `gradle.properties` as `cobblemon_curse_file_id`, and update
   `cobblemon_version` to match for clarity (it's only used in a comment/label,
   not resolved directly, since Curse Maven is what actually fetches the jar)

Then:

```sh
./gradlew build
```

(If you don't have the Gradle wrapper jar yet — e.g. you're starting from
this exported source rather than a git clone — run `gradle wrapper` once with
a local Gradle 8.10+ install, or just open the project folder in IntelliJ
IDEA with the **Minecraft Development** plugin, which will offer to set
everything up for you automatically.)

The built mod jar will be in `build/libs/`.

### Dependencies you need installed alongside the mod

- [Fabric API](https://modrinth.com/mod/fabric-api)
- [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin)
- [Cobblemon](https://modrinth.com/mod/cobblemon) 1.7.x for Minecraft 1.21.1

## Project layout

```
src/main/kotlin/com/maxivs/cobblemon/
  CobblemonMaxIVs.kt             - mod entrypoint, wires everything together
  Config.kt                      - config data class + JSON load/save
  IvApplier.kt                   - the actual "write max IVs onto a Pokemon" logic
  event/PokemonEventListener.kt  - category-aware re-application on entity spawn
  command/MaxIvsCommand.kt       - /maxivs reload

src/main/java/com/maxivs/cobblemon/mixin/
  PokemonMixin.java              - universal constructor hook

src/main/resources/
  fabric.mod.json
  cobblemonmaxivs.mixins.json
  cobblemonmaxivs.accesswidener
```

## Why a Mixin instead of just using Cobblemon's events?

Cobblemon's event bus (`CobblemonEvents`) is great, but every event on it
fires for a *specific* situation — a Pokémon spawning into the world, a
Pokémon being captured, an egg hatching, and so on. There isn't a single
"any Pokémon, anywhere, for any reason was just created" event, and addon
mods are free to build `Pokemon` objects however they like without
necessarily firing any of those events at all (for instance, directly
constructing a `Pokemon` for an NPC trainer's data-driven party before any
spawn-related event would make sense). The constructor is the one place in
the entire codebase that is guaranteed to run for every Pokémon, which is why
the mixin is the foundation and the event listener is the refinement on top.
