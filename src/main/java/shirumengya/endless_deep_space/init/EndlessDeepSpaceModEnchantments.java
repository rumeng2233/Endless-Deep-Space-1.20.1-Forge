
/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package shirumengya.endless_deep_space.init;

import shirumengya.endless_deep_space.enchantment.SpeedEnchantment;
import shirumengya.endless_deep_space.enchantment.RiptideEnchantment;
import shirumengya.endless_deep_space.enchantment.ReduceResistanceEnchantment;
import shirumengya.endless_deep_space.enchantment.ExplosionEnchantment;
import shirumengya.endless_deep_space.enchantment.DecapitateEnchantment;
import shirumengya.endless_deep_space.enchantment.BlockStrengthenedEnchantment;
import shirumengya.endless_deep_space.EndlessDeepSpaceMod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.item.enchantment.Enchantment;

public class EndlessDeepSpaceModEnchantments {
	public static final DeferredRegister<Enchantment> REGISTRY = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, EndlessDeepSpaceMod.MODID);
	public static final RegistryObject<Enchantment> DECAPITATE = REGISTRY.register("decapitate", () -> new DecapitateEnchantment());
	public static final RegistryObject<Enchantment> BLOCK_STRENGTHENED = REGISTRY.register("block_strengthened", () -> new BlockStrengthenedEnchantment());
	public static final RegistryObject<Enchantment> RIPTIDE = REGISTRY.register("riptide", () -> new RiptideEnchantment());
	public static final RegistryObject<Enchantment> SPEED = REGISTRY.register("speed", () -> new SpeedEnchantment());
	public static final RegistryObject<Enchantment> REDUCE_RESISTANCE = REGISTRY.register("reduce_resistance", () -> new ReduceResistanceEnchantment());
	public static final RegistryObject<Enchantment> EXPLOSION = REGISTRY.register("explosion", () -> new ExplosionEnchantment());
}
