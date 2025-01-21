
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package shirumengya.endless_deep_space.custom.init;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import shirumengya.endless_deep_space.EndlessDeepSpaceMod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModAttributes {
	public static final DeferredRegister<Attribute> REGISTRY = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, EndlessDeepSpaceMod.MODID);
	public static final RegistryObject<Attribute> DAMAGE_REDUCTION = REGISTRY.register("damage_reduction", () -> new RangedAttribute("attribute.endless_deep_space.damage_reduction", 0, Long.MIN_VALUE, Long.MAX_VALUE).setSyncable(true));

	@SubscribeEvent
	public static void addAttributes(EntityAttributeModificationEvent event) {
		event.getTypes().forEach(entity -> event.add(entity, DAMAGE_REDUCTION.get()));
	}
}
