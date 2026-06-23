package com.maxivs.cobblemon

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import com.maxivs.cobblemon.command.MaxIvsCommand
import com.maxivs.cobblemon.event.PokemonEventListener
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Entrypoint for Cobblemon Max IVs.
 *
 * What this mod does, in one sentence: every Pokémon that exists on the
 * server — wild, trainer-owned (including addons like Radical Cobblemon
 * Trainers), player-owned, hatched, gifted, whatever — gets every IV stat
 * set to a configurable value (31/perfect by default).
 *
 * How: a Mixin into the [com.cobblemon.mod.common.pokemon.Pokemon]
 * constructor catches every Pokémon unconditionally the instant it's
 * created, and an event listener on [com.cobblemon.mod.common.api.events.CobblemonEvents.POKEMON_ENTITY_SPAWN]
 * re-applies the value with full category awareness (wild/trainer/player)
 * once Cobblemon has assigned the Pokémon's original trainer type. See
 * [IvApplier] for the actual stat-writing logic and [MaxIvsConfig] for the
 * available settings (config/cobblemonmaxivs.json).
 */
object CobblemonMaxIVs : ModInitializer {
    const val MOD_ID = "cobblemonmaxivs"
    val logger: Logger = LoggerFactory.getLogger(MOD_ID)

    override fun onInitialize() {
        ConfigManager.load()
        PokemonEventListener.register()

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            MaxIvsCommand.register(dispatcher)
        }

        logger.info(
            "Cobblemon Max IVs loaded (enabled={}, value={}, wild={}, trainer={}, player={})",
            ConfigManager.config.enabled,
            ConfigManager.config.clampedIvValue(),
            ConfigManager.config.applyToWildPokemon,
            ConfigManager.config.applyToTrainerPokemon,
            ConfigManager.config.applyToPlayerPokemon
        )
    }
}
