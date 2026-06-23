package com.maxivs.cobblemon.event

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.maxivs.cobblemon.IvApplier

/**
 * Registers a listener on Cobblemon's own event bus to apply max IVs at a
 * point in a Pokemon's life where
 * [com.cobblemon.mod.common.pokemon.Pokemon.originalTrainerType] is already
 * reliably set, so the wild/trainer/player category toggles in the config
 * can actually be respected.
 *
 * This complements (rather than replaces) the constructor mixin
 * ([com.maxivs.cobblemon.mixin.PokemonMixin]), which already handles the
 * "everything enabled" case unconditionally and universally the moment a
 * Pokemon object is created. This listener is what makes the per-category
 * toggles meaningful when the user has disabled one or more of them.
 */
object PokemonEventListener {

    fun register() {
        // POKEMON_ENTITY_SPAWN fires for every PokemonEntity that appears in
        // the world: wild Pokemon spawning naturally or via fishing/bait,
        // AND trainer Pokemon being sent out into battle (gym leaders,
        // rivals, and NPC-trainer addons like Radical Cobblemon Trainers all
        // route through the same Cobblemon sendOut()/entity-spawn pipeline,
        // since their Pokemon are ordinary Cobblemon `Pokemon` instances).
        //
        // By the time this fires, Pokemon#originalTrainerType has already
        // been correctly assigned by Cobblemon (NONE for not-yet-caught wild
        // Pokemon, NPC for trainer-owned, PLAYER/else for player-owned), so
        // this is the right place to apply the category-aware toggles.
        //
        // This event is also the most efficient single hook for "max out
        // every Pokemon a player will actually encounter or battle" since it
        // only fires when a Pokemon actually becomes visible/active in the
        // world, rather than for every transient Pokemon object some code
        // path might construct and discard.
        CobblemonEvents.POKEMON_ENTITY_SPAWN.subscribe { event ->
            IvApplier.applyMaxIvsIfEnabled(event.entity.pokemon)
        }
    }
}
