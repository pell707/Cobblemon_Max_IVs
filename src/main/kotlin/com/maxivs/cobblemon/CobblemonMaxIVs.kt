package com.maxivs.cobblemon

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import com.maxivs.cobblemon.command.MaxIvsCommand
import com.maxivs.cobblemon.event.PokemonEventListener
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object CobblemonMaxIVs {
    const val MOD_ID = "cobblemonmaxivs"
    val logger: Logger = LoggerFactory.getLogger(MOD_ID)

    fun onInitialize() {
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

class CobblemonMaxIvsEntrypoint : ModInitializer {
    override fun onInitialize() {
        CobblemonMaxIVs.onInitialize()
    }
}
