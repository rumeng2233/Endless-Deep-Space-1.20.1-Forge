
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package shirumengya.endless_deep_space.custom.init;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import shirumengya.endless_deep_space.EndlessDeepSpaceMod;
import shirumengya.endless_deep_space.custom.client.model.BlaznanaShulkerTrickModel;
import shirumengya.endless_deep_space.custom.client.model.MutationRavagerModel;
import shirumengya.endless_deep_space.custom.client.model.OceanDefenderModel;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = {Dist.CLIENT})
public class ModModels {
	public static final ModelLayerLocation MUTATION_RAVAGER = createLocation("mutation_ravager", "main");
	public static final ModelLayerLocation BLAZNANA_SHULKER_TRICK = createLocation("blaznana_shulker_trick", "main");
	public static final ModelLayerLocation OCEAN_DEFENDER = createLocation("ocean_defender", "main");
	public static final ModelLayerLocation OCEAN_DEFENDER_ARMOR = createLocation("ocean_defender", "armor");
	public static final ModelLayerLocation OCEAN_DEFENDER_PROGRESS_TWO = createLocation("ocean_defender", "progress_two");

	@SubscribeEvent
	public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(MUTATION_RAVAGER, MutationRavagerModel::createBodyLayer);
		event.registerLayerDefinition(BLAZNANA_SHULKER_TRICK, BlaznanaShulkerTrickModel::createBodyLayer);
		event.registerLayerDefinition(OCEAN_DEFENDER, OceanDefenderModel::createBodyLayer);
		event.registerLayerDefinition(OCEAN_DEFENDER_ARMOR, OceanDefenderModel::createBodyLayer);
		event.registerLayerDefinition(OCEAN_DEFENDER_PROGRESS_TWO, OceanDefenderModel::createBodyLayer);
	}

	private static ModelLayerLocation createLocation(String model, String layer) {
		return new ModelLayerLocation(new ResourceLocation(EndlessDeepSpaceMod.MODID, model), layer);
	}
}
