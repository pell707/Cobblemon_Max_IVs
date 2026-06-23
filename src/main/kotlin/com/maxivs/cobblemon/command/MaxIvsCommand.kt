package com.maxivs.cobblemon.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.maxivs.cobblemon.ConfigManager
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component

/**
 * Registers `/maxivs reload`, letting server admins tweak
 * config/cobblemonmaxivs.json and apply the changes without restarting the
 * whole server. Permission level 2 (the standard "/op" gate) is required.
 */
object MaxIvsCommand {

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("maxivs")
                .requires { it.hasPermission(2) }
                .then(
                    Commands.literal("reload").executes(::reload)
                )
        )
    }

    private fun reload(context: CommandContext<CommandSourceStack>): Int {
        ConfigManager.load()
        val config = ConfigManager.config
        context.source.sendSuccess(
            {
                Component.literal(
                    "Cobblemon Max IVs config reloaded. enabled=${config.enabled}, value=${config.clampedIvValue()}, " +
                        "wild=${config.applyToWildPokemon}, trainer=${config.applyToTrainerPokemon}, player=${config.applyToPlayerPokemon}"
                )
            },
            true
        )
        return 1
    }
}
