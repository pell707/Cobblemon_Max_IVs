package com.maxivs.cobblemon.mixin;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.maxivs.cobblemon.IvApplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks into the tail end of every {@link Pokemon} constructor call.
 *
 * This is deliberately the most universal hook available: literally every
 * Pokemon in Cobblemon — wild spawns, trainer parties built by Cobblemon
 * itself or by addons like Radical Cobblemon Trainers, eggs, gift Pokémon,
 * command-given Pokémon, anything — is, at some point, a freshly-constructed
 * {@code Pokemon} object. By injecting at the very end of the constructor we
 * guarantee our IV overwrite runs after Cobblemon's own
 * {@code IVs.createRandomIVs()} call has already populated (random) IVs,
 * so we're safely overwriting rather than racing against it.
 *
 * Because {@code Pokemon.originalTrainerType} is not yet meaningfully set at
 * construction time (Cobblemon assigns it shortly afterwards depending on how
 * the Pokémon is being created), this mixin only performs the *unconditional*
 * blanket IV write — see {@link IvApplier#applyUnconditionally} — which is a
 * no-op unless every category toggle in the config is enabled. The
 * category-aware logic (only wild, only trainers, etc.) is handled separately
 * by event listeners once Cobblemon has had a chance to assign an original
 * trainer; see {@code com.maxivs.cobblemon.event.PokemonEventListener}.
 */
@Mixin(Pokemon.class)
public abstract class PokemonMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void cobblemonMaxIvs$onConstructed(CallbackInfo ci) {
        IvApplier.applyUnconditionally((Pokemon) (Object) this);
    }
}
