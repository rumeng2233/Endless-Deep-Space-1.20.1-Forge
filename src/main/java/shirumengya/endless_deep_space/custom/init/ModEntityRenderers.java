
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package shirumengya.endless_deep_space.custom.init;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import shirumengya.endless_deep_space.custom.client.renderer.blockentity.GuidingStoneRenderer;
import shirumengya.endless_deep_space.custom.client.renderer.entity.*;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEntityRenderers {
	@SubscribeEvent
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(ModEntities.ENDER_DRAGON.get(), EnderDragonRenderer::new);
		event.registerEntityRenderer(ModEntities.ENDER_LORD.get(), EnderLordRenderer::new);
		event.registerEntityRenderer(ModEntities.CLUSTER_FIREBALL.get(), ClusterFireballRenderer::new);
		event.registerEntityRenderer(ModEntities.COLORFUL_LIGHTNING_BOLT.get(), ColorfulLightningBoltRenderer::new);
		event.registerEntityRenderer(ModEntities.SCREEN_SHAKE.get(), NullRenderer::new);
		event.registerEntityRenderer(ModEntities.MUTATION_RAVAGER.get(), MutationRavagerRenderer::new);
		event.registerEntityRenderer(ModEntities.BLAZNANA_SHULKER_TRICK.get(), BlaznanaShulkerTrickRenderer::new);
		event.registerEntityRenderer(ModEntities.BLAZNANA_SHULKER_TRICK_BULLET.get(), BlaznanaShulkerTrickBulletRenderer::new);
		event.registerEntityRenderer(ModEntities.ARROW.get(), ArrowRenderer::new);
		event.registerEntityRenderer(ModEntities.OCEAN_DEFENDER.get(), OceanDefenderRenderer::new);
		event.registerEntityRenderer(ModEntities.ABYSSAL_TORPEDO.get(), AbyssalTorpedoRenderer::new);
		
		event.registerBlockEntityRenderer(ModBlockEntities.GUIDING_STONE.get(), GuidingStoneRenderer::new);
	}
}
