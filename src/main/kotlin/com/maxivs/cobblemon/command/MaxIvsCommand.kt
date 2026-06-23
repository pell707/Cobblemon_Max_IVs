package com.maxivs.cobblemon.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.maxivs.cobblemon.ConfigManager
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

/**
 * Registers `/maxivs reload`, letting server admins tweak
 * config/cobblemonmaxivs.json and apply the changes without restarting the
 * whole server. Permission level 2 (the standard "/op" gate) is required.
 */
object MaxIvsCommand {

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(
            CommandManager.literal("maxivs")
                .requires { it.hasPermissionLevel(2) }
                .then(
                    CommandManager.literal("reload").executes(::reload)
                )
        )
    }

    private fun reload(context: CommandContext<ServerCommandSource>): Int {
        ConfigManager.load()
        val config = ConfigManager.config
        context.source.sendFeedback(
            {
                Text.literal(
                    "Cobblemon Max IVs config reloaded. enabled=${config.enabled}, value=${config.clampedIvValue()}, " +
                        "wild=${config.applyToWildPokemon}, trainer=${config.applyToTrainerPokemon}, player=${config.applyToPlayerPokemon}"
                )
            },
            true
        )
        return 1
    }
}
