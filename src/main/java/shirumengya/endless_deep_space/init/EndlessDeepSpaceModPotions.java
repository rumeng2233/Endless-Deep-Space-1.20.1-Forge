
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package shirumengya.endless_deep_space.init;

import shirumengya.endless_deep_space.EndlessDeepSpaceMod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.effect.MobEffectInstance;

public class EndlessDeepSpaceModPotions {
	public static final DeferredRegister<Potion> REGISTRY = DeferredRegister.create(ForgeRegistries.POTIONS, EndlessDeepSpaceMod.MODID);
	public static final RegistryObject<Potion> TOTEM_OF_UNDYING_POTION = REGISTRY.register("totem_of_undying_potion", () -> new Potion(new MobEffectInstance(EndlessDeepSpaceModMobEffects.TOTEM_OF_UNDYING.get(), 24000, 4, false, true)));
}
