package shirumengya.endless_deep_space.procedures;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import shirumengya.endless_deep_space.custom.entity.ColorfulLightningBolt;
import shirumengya.endless_deep_space.custom.entity.boss.oceandefenders.OceanDefender;
import shirumengya.endless_deep_space.custom.networking.ModMessages;
import shirumengya.endless_deep_space.custom.networking.packet.DeleteEntityS2CPacket;
import shirumengya.endless_deep_space.init.EndlessDeepSpaceModItems;
import shirumengya.endless_deep_space.mixins.EntityAccessor;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

@Mod.EventBusSubscriber
public class TotemSwordYouJiKongQiShiShiTiDeWeiZhiProcedure {
	@SubscribeEvent
	public static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
		if (event.getHand() != event.getEntity().getUsedItemHand())
			return;
		execute(event, event.getLevel(), event.getTarget(), event.getEntity());
	}

	public static void execute(LevelAccessor world, Entity entity, Entity sourceentity) {
		execute(null, world, entity, sourceentity);
	}

	public static void execute(@Nullable Event event, LevelAccessor world, Entity entity, Entity sourceentity) {
		if (entity == null || sourceentity == null)
			return;

		if ((sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == EndlessDeepSpaceModItems.TOTEM_SWORD.get()) {
			DamageSource damage = ColorfulLightningBolt.colorfulLightningBoltDamage(sourceentity);

			ColorfulLightningBolt lightningBolt = new ColorfulLightningBolt(entity.level(), entity.getX(), entity.getY(), entity.getZ(), 0, true);
			entity.level().addFreshEntity(lightningBolt);

			if (!Screen.hasAltDown()) {
				entity.hurt(damage, Float.MAX_VALUE);
				if (entity instanceof LivingEntity _entity)
					_entity.setHealth(0);
				if (entity instanceof LivingEntity _entity)
					OceanDefender.setIsDying(_entity, true);
				if (entity instanceof LivingEntity _entity)
					_entity.die(damage);
			} else {
				if (!entity.level().isClientSide()) {
					if (entity instanceof ServerPlayer player) {
						player.connection.disconnect(Component.translatable("commands.endless_deep_space.delete.success.single", player.getDisplayName()));
					} else {
						ModMessages.sendToAllPlayers(new DeleteEntityS2CPacket(entity.getId()));
						entity.setRemoved(Entity.RemovalReason.DISCARDED);
						entity.canUpdate(false);
						entity.gameEvent(GameEvent.ENTITY_DIE);
						//entity.setPos(new Vec3(Double.NaN, Double.NaN, Double.NEGATIVE_INFINITY));
						entity.setRemoved(Entity.RemovalReason.UNLOADED_TO_CHUNK);
						entity.setLevelCallback(EntityInLevelCallback.NULL);
						entity.tickCount = Integer.MIN_VALUE;
						entity.setDeltaMovement(new Vec3(Double.NaN, Double.NaN, Double.NEGATIVE_INFINITY));
						entity.setBoundingBox(new AABB(Vec3.ZERO, Vec3.ZERO));
						((EntityAccessor) entity).setBlockPosition(new BlockPos(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
						((EntityAccessor) entity).setChunkPosition(new ChunkPos(Integer.MAX_VALUE, Integer.MIN_VALUE));
						((EntityAccessor) entity).setDeltaMovement(new Vec3(Double.NaN, Double.NaN, Double.NEGATIVE_INFINITY));
						((EntityAccessor) entity).setPosition(new Vec3(Double.NaN, Double.NaN, Double.NEGATIVE_INFINITY));
						((EntityAccessor) entity).setRemovalReason(Entity.RemovalReason.UNLOADED_TO_CHUNK);
						entity.invalidateCaps();
					}
				}
			}
			
			if (sourceentity instanceof LivingEntity _sourceentity)
			_sourceentity.swing(InteractionHand.MAIN_HAND, true);
		}
	}

	public static void RightClick(LevelAccessor world, Entity entity) {
		if (Screen.hasControlDown()) {
			{
				final Vec3 _center = new Vec3(entity.getX(), entity.getY(), entity.getZ());
				List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(100 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center))).toList();
				for (Entity entityiterator : _entfound) {
					if (entityiterator != entity) {
						DamageSource damage = ColorfulLightningBolt.colorfulLightningBoltDamage(entityiterator);

						if (!(entityiterator instanceof ColorfulLightningBolt)) {
							ColorfulLightningBolt lightningBolt = new ColorfulLightningBolt(entityiterator.level(), entityiterator.getX(), entityiterator.getY(), entityiterator.getZ(), 0, true);
							entityiterator.level().addFreshEntity(lightningBolt);
						}

						if (!Screen.hasAltDown()) {
							entityiterator.hurt(damage, Float.MAX_VALUE);
							if (entityiterator instanceof LivingEntity _entity)
								_entity.setHealth(0);
							if (entityiterator instanceof LivingEntity _entity)
								OceanDefender.setIsDying(_entity, true);
							if (entityiterator instanceof LivingEntity _entity)
								_entity.die(damage);
						} else {
							if (entityiterator instanceof ServerPlayer player) {
								player.connection.disconnect(Component.translatable("commands.endless_deep_space.delete.success.single", player.getDisplayName()));
							} else {
								ModMessages.sendToAllPlayers(new DeleteEntityS2CPacket(entityiterator.getId()));
								entityiterator.setRemoved(Entity.RemovalReason.DISCARDED);
								entityiterator.canUpdate(false);
								entityiterator.gameEvent(GameEvent.ENTITY_DIE);
								//entityiterator.setPos(new Vec3(Double.NaN, Double.NaN, Double.NEGATIVE_INFINITY));
								entityiterator.setRemoved(Entity.RemovalReason.UNLOADED_TO_CHUNK);
								entityiterator.setLevelCallback(EntityInLevelCallback.NULL);
								entityiterator.tickCount = Integer.MIN_VALUE;
								entityiterator.setDeltaMovement(new Vec3(Double.NaN, Double.NaN, Double.NEGATIVE_INFINITY));
								entityiterator.setBoundingBox(new AABB(Vec3.ZERO, Vec3.ZERO));
								((EntityAccessor) entityiterator).setBlockPosition(new BlockPos(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
								((EntityAccessor) entityiterator).setChunkPosition(new ChunkPos(Integer.MAX_VALUE, Integer.MIN_VALUE));
								((EntityAccessor) entityiterator).setDeltaMovement(new Vec3(Double.NaN, Double.NaN, Double.NEGATIVE_INFINITY));
								((EntityAccessor) entityiterator).setPosition(new Vec3(Double.NaN, Double.NaN, Double.NEGATIVE_INFINITY));
								((EntityAccessor) entityiterator).setRemovalReason(Entity.RemovalReason.UNLOADED_TO_CHUNK);
								entityiterator.invalidateCaps();
							}
						}
					}
				}
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.translatable("item.endless_deep_space.totem_sword.attack_entities", _entfound.size() - 1), false);
			}

			if (entity instanceof LivingEntity _entity)
			_entity.swing(InteractionHand.MAIN_HAND, true);
		}
	}
}
