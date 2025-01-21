
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package shirumengya.endless_deep_space.custom.init;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import shirumengya.endless_deep_space.EndlessDeepSpaceMod;
import shirumengya.endless_deep_space.custom.entity.ColorfulLightningBolt;
import shirumengya.endless_deep_space.custom.entity.ScreenShakeEntity;
import shirumengya.endless_deep_space.custom.entity.boss.enderdragon.EnderDragon;
import shirumengya.endless_deep_space.custom.entity.boss.enderlord.EnderLord;
import shirumengya.endless_deep_space.custom.entity.boss.oceandefenders.OceanDefender;
import shirumengya.endless_deep_space.custom.entity.miniboss.BlaznanaShulkerTrick;
import shirumengya.endless_deep_space.custom.entity.miniboss.MutationRavager;
import shirumengya.endless_deep_space.custom.entity.projectile.AbyssalTorpedo;
import shirumengya.endless_deep_space.custom.entity.projectile.Arrow;
import shirumengya.endless_deep_space.custom.entity.projectile.BlaznanaShulkerTrickBullet;
import shirumengya.endless_deep_space.custom.entity.projectile.ClusterFireball;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntities {
	public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, EndlessDeepSpaceMod.MODID);
	public static final RegistryObject<EntityType<EnderDragon>> ENDER_DRAGON = register("ender_dragon", EntityType.Builder.of(EnderDragon::new, MobCategory.MONSTER).fireImmune().sized(16.0F, 8.0F).clientTrackingRange(10));
	public static final RegistryObject<EntityType<EnderLord>> ENDER_LORD = register("ender_lord", EntityType.Builder.of(EnderLord::new, MobCategory.MONSTER).fireImmune().sized(16.0F, 8.0F).clientTrackingRange(10));
	public static final RegistryObject<EntityType<ClusterFireball>> CLUSTER_FIREBALL = register("cluster_fireball", EntityType.Builder.<ClusterFireball>of(ClusterFireball::new, MobCategory.MISC).fireImmune().sized(1.0F, 1.0F).clientTrackingRange(4).updateInterval(10));
	public static final RegistryObject<EntityType<ColorfulLightningBolt>> COLORFUL_LIGHTNING_BOLT = register("colorful_lightning_bolt", EntityType.Builder.<ColorfulLightningBolt>of(ColorfulLightningBolt::new, MobCategory.MISC).noSave().sized(0.0F, 0.0F).clientTrackingRange(16).updateInterval(Integer.MAX_VALUE));
	public static final RegistryObject<EntityType<ScreenShakeEntity>> SCREEN_SHAKE = register("screen_shake", EntityType.Builder.<ScreenShakeEntity>of(ScreenShakeEntity::new, MobCategory.MISC).fireImmune().sized(0.0f, 0.0f).setUpdateInterval(Integer.MAX_VALUE));
	public static final RegistryObject<EntityType<MutationRavager>> MUTATION_RAVAGER = register("mutation_ravager", EntityType.Builder.of(MutationRavager::new, MobCategory.MONSTER).sized(1.95F, 2.2F).clientTrackingRange(10));
	public static final RegistryObject<EntityType<BlaznanaShulkerTrick>> BLAZNANA_SHULKER_TRICK = register("blaznana_shulker_trick", EntityType.Builder.of(BlaznanaShulkerTrick::new, MobCategory.MONSTER).fireImmune().canSpawnFarFromPlayer().sized(1.0F, 1.0F).clientTrackingRange(10));
	public static final RegistryObject<EntityType<BlaznanaShulkerTrickBullet>> BLAZNANA_SHULKER_TRICK_BULLET = register("blaznana_shulker_trick_bullet", EntityType.Builder.<BlaznanaShulkerTrickBullet>of(BlaznanaShulkerTrickBullet::new, MobCategory.MISC).sized(0.3125F, 0.3125F).clientTrackingRange(8));
	public static final RegistryObject<EntityType<Arrow>> ARROW = register("arrow", EntityType.Builder.<Arrow>of(Arrow::new, MobCategory.MISC).sized(0.5F, 0.5F).clientTrackingRange(4).updateInterval(20));
	public static final RegistryObject<EntityType<OceanDefender>> OCEAN_DEFENDER = register("ocean_defender", EntityType.Builder.of(OceanDefender::new, MobCategory.MONSTER).fireImmune().sized(1.9975F * 2.0F, 1.9975F * 2.0F).clientTrackingRange(10));
	public static final RegistryObject<EntityType<AbyssalTorpedo>> ABYSSAL_TORPEDO = register("abyssal_torpedo", EntityType.Builder.<AbyssalTorpedo>of(AbyssalTorpedo::new, MobCategory.MISC).sized(1.0F, 1.0F).clientTrackingRange(4).updateInterval(20));

	private static <T extends Entity> RegistryObject<EntityType<T>> register(String registryname, EntityType.Builder<T> entityTypeBuilder) {
		return REGISTRY.register(registryname, () -> (EntityType<T>) entityTypeBuilder.build(registryname));
	}

	@SubscribeEvent
	public static void registerAttributes(EntityAttributeCreationEvent event) {
		event.put(ENDER_DRAGON.get(), EnderDragon.createAttributes().build());
		event.put(ENDER_LORD.get(), EnderLord.createAttributes().build());
		event.put(MUTATION_RAVAGER.get(), MutationRavager.createAttributes().build());
		event.put(BLAZNANA_SHULKER_TRICK.get(), BlaznanaShulkerTrick.createAttributes().build());
		event.put(OCEAN_DEFENDER.get(), OceanDefender.createAttributes().build());
	}
}
