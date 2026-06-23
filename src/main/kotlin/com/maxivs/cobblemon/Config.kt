package com.maxivs.cobblemon

import com.google.gson.GsonBuilder
import net.fabricmc.loader.api.FabricLoader
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

/**
 * Configuration for Cobblemon Max IVs.
 *
 * Stored at config/cobblemonmaxivs.json. Created with defaults on first run,
 * and re-read on every world load so server admins can tweak it without
 * restarting (call [ConfigManager.load] from anywhere if you want a manual
 * reload command later).
 */
data class MaxIvsConfig(
    /** Whether this mod is active at all. Master on/off switch. */
    var enabled: Boolean = true,

    /**
     * The IV value applied to every stat. Valid range is 0-31. Defaults to
     * 31 (perfect IVs). You could, for instance, set this to 0 for a
     * "nightmare mode" challenge config, though that's not really what this
     * mod is for.
     */
    var maxIvValue: Int = 31,

    /**
     * If true, wild Pokémon (and fishing/spawn-event Pokémon) get the max IVs.
     */
    var applyToWildPokemon: Boolean = true,

    /**
     * If true, Pokémon owned by NPC/non-player trainers (including trainers
     * spawned by addons such as Radical Cobblemon Trainers, since those are
     * still just regular Cobblemon `Pokemon` instances under the hood) get
     * the max IVs.
     */
    var applyToTrainerPokemon: Boolean = true,

    /**
     * If true, player-owned Pokémon (caught, hatched, gifted, traded-in,
     * etc.) also get the max IVs. Turn this off if you only want to
     * guarantee perfect IVs for things that battle you, not things you
     * yourself catch or breed.
     */
    var applyToPlayerPokemon: Boolean = true,

    /**
     * If true, the mod will only ever raise IVs up towards [maxIvValue],
     * never lower them. This matters only if [maxIvValue] is set below 31
     * and some other mod or mechanic already rolled higher IVs.
     */
    var onlyRaiseNeverLower: Boolean = false
) {
    fun clampedIvValue(): Int = maxIvValue.coerceIn(0, 31)
}

object ConfigManager {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val configPath: Path by lazy {
        FabricLoader.getInstance().configDir.resolve("cobblemonmaxivs.json")
    }

    @Volatile
    var config: MaxIvsConfig = MaxIvsConfig()
        private set

    fun load() {
        try {
            if (Files.exists(configPath)) {
                val json = Files.readString(configPath)
                val loaded = gson.fromJson(json, MaxIvsConfig::class.java)
                config = loaded ?: MaxIvsConfig()
            } else {
                config = MaxIvsConfig()
            }
        } catch (e: Exception) {
            CobblemonMaxIVs.logger.error("Failed to load cobblemonmaxivs.json, falling back to defaults", e)
            config = MaxIvsConfig()
        }
        // Always (re)write the file so new fields/comments-via-defaults show up
        // for users upgrading from an older version of the mod.
        save()
    }

    fun save() {
        try {
            Files.createDirectories(configPath.parent)
            Files.writeString(configPath, gson.toJson(config))
        } catch (e: IOException) {
            CobblemonMaxIVs.logger.error("Failed to save cobblemonmaxivs.json", e)
        }
    }
}
