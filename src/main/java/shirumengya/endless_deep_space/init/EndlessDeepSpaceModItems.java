
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package shirumengya.endless_deep_space.init;

import shirumengya.endless_deep_space.item.TotemSwordItem;
import shirumengya.endless_deep_space.item.LogoItem;
import shirumengya.endless_deep_space.EndlessDeepSpaceMod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.item.Item;

public class EndlessDeepSpaceModItems {
	public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, EndlessDeepSpaceMod.MODID);
	public static final RegistryObject<Item> TOTEM_SWORD = REGISTRY.register("totem_sword", () -> new TotemSwordItem());
	public static final RegistryObject<Item> LOGO = REGISTRY.register("logo", () -> new LogoItem());
	// Start of user code block custom items
	// End of user code block custom items
}
