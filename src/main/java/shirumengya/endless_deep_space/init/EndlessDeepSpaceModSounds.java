
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package shirumengya.endless_deep_space.init;

import shirumengya.endless_deep_space.EndlessDeepSpaceMod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;

public class EndlessDeepSpaceModSounds {
	public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, EndlessDeepSpaceMod.MODID);
	public static final RegistryObject<SoundEvent> ITEM_SWORD_BLOCK = REGISTRY.register("item.sword.block", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("endless_deep_space", "item.sword.block")));
}
