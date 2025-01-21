
/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package shirumengya.endless_deep_space.init;

import shirumengya.endless_deep_space.potion.TotemOfUndyingMobEffect;
import shirumengya.endless_deep_space.potion.InvincibleMobEffect;
import shirumengya.endless_deep_space.EndlessDeepSpaceMod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.effect.MobEffect;

public class EndlessDeepSpaceModMobEffects {
	public static final DeferredRegister<MobEffect> REGISTRY = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, EndlessDeepSpaceMod.MODID);
	public static final RegistryObject<MobEffect> INVINCIBLE = REGISTRY.register("invincible", () -> new InvincibleMobEffect());
	public static final RegistryObject<MobEffect> TOTEM_OF_UNDYING = REGISTRY.register("totem_of_undying", () -> new TotemOfUndyingMobEffect());
}
