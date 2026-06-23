package com.maxivs.cobblemon

import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.pokemon.Pokemon

/**
 * Central place where IVs actually get written onto a [Pokemon]. Both the
 * constructor mixin (the primary, universal hook) and the event-based safety
 * net call into here, so there's exactly one code path to reason about.
 */
object IvApplier {

    /** All six standard stats whose IVs we touch. */
    private val ALL_STATS = listOf(
        Stats.HP,
        Stats.ATTACK,
        Stats.DEFENCE,
        Stats.SPECIAL_ATTACK,
        Stats.SPECIAL_DEFENCE,
        Stats.SPEED
    )

    /**
     * Unconditionally writes the configured max IV value to every stat on
     * [pokemon], ignoring the wild/trainer/player category toggles.
     *
     * This is called from the [Pokemon] constructor mixin, which is the one
     * hook guaranteed to see *every* Pokémon ever created — wild spawns,
     * RCTrainers/other addon trainer parties, eggs, gifts, command-given
     * Pokémon, you name it. The catch is that at construction time,
     * [Pokemon.originalTrainerType] hasn't been assigned by Cobblemon yet
     * (it's set shortly *after* construction by whichever system is creating
     * the Pokémon), so we can't yet tell wild apart from trainer-owned here.
     *
     * Because of that, this entry point only makes sense to call when the
     * category toggles are all effectively irrelevant — i.e. when the user
     * wants max IVs everywhere. [applyMaxIvsIfEnabled] is the one that
     * actually respects the per-category toggles, applied later via
     * [com.maxivs.cobblemon.event.PokemonEventListener] once Cobblemon has
     * had a chance to assign an original trainer.
     */
    fun applyUnconditionally(pokemon: Pokemon) {
        val config = ConfigManager.config
        if (!config.enabled) return
        // If the user has restricted this to a subset of categories, skip the
        // construction-time blanket pass entirely and let the later,
        // category-aware pass (see applyMaxIvsIfEnabled) handle it instead.
        // Otherwise (the common case - everything enabled) just do it now,
        // which is cheaper and catches Pokémon that for whatever reason never
        // fire any of our later hooks (e.g. some third-party code paths).
        if (config.applyToWildPokemon && config.applyToTrainerPokemon && config.applyToPlayerPokemon) {
            writeIvs(pokemon, config)
        }
    }

    /**
     * Category-aware application: checks [Pokemon.originalTrainerType] (which
     * is reliable by the time this is called - after spawn, after a trainer
     * party has been built, etc.) and only writes IVs if the matching config
     * toggle is enabled.
     *
     * This is intentionally idempotent and cheap to call repeatedly - it's
     * fine to invoke this multiple times on the same Pokémon (e.g. once after
     * spawning and again after a battle starts); repeat calls are just a
     * harmless no-op write of the same values.
     */
    fun applyMaxIvsIfEnabled(pokemon: Pokemon) {
        val config = ConfigManager.config
        if (!config.enabled) return
        if (!shouldApply(pokemon, config)) return
        writeIvs(pokemon, config)
    }

    private fun writeIvs(pokemon: Pokemon, config: MaxIvsConfig) {
        val target = config.clampedIvValue()
        for (stat in ALL_STATS) {
            if (config.onlyRaiseNeverLower) {
                val current = pokemon.ivs[stat] ?: 0
                if (current >= target) continue
            }
            // Pokemon#setIV both writes the IV and, for HP specifically,
            // recalculates current health so the Pokémon doesn't end up with
            // a max-health value mismatched against its (now higher) HP IV.
            pokemon.setIV(stat, target)
        }
    }

    private fun shouldApply(pokemon: Pokemon, config: MaxIvsConfig): Boolean {
        // OriginalTrainerType.NPC  -> owned by a non-player trainer (gym
        //                             leaders, rivals, RCTrainers-style NPC
        //                             trainers, etc.)
        // OriginalTrainerType.NONE -> wild Pokémon (spawned in the world,
        //                             fished, etc.) that hasn't been caught
        // OriginalTrainerType.PLAYER, or otherwise -> player-owned Pokémon
        //                             (caught, bred, gifted, traded in, etc.)
        //
        // Wild Pokémon that get caught will transition from NONE to PLAYER
        // at the moment of capture, and we re-apply via the catch event
        // listener at that point too, so the wild-vs-player distinction
        // stays correct across that transition.
        return when (pokemon.originalTrainerType.name) {
            "NPC" -> config.applyToTrainerPokemon
            "NONE" -> config.applyToWildPokemon
            else -> config.applyToPlayerPokemon
        }
    }
}
