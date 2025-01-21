package shirumengya.endless_deep_space.procedures;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import shirumengya.endless_deep_space.custom.entity.ColorfulLightningBolt;
import shirumengya.endless_deep_space.custom.entity.boss.oceandefenders.OceanDefender;
import shirumengya.endless_deep_space.custom.networking.ModMessages;
import shirumengya.endless_deep_space.custom.networking.packet.DeleteEntityS2CPacket;
import shirumengya.endless_deep_space.mixins.EntityAccessor;

public class TotemSwordShengWuBeiGongJuJiZhongShiProcedure {
	public static void execute(LevelAccessor world, Entity entity, Entity sourceentity) {
		if (entity == null || sourceentity == null)
			return;
			
		if (!Screen.hasAltDown()) {
			DamageSource damage = ColorfulLightningBolt.colorfulLightningBoltDamage(sourceentity);
			entity.hurt(damage, Float.MAX_VALUE);
			ColorfulLightningBolt lightningBolt = new ColorfulLightningBolt(entity.level(), entity.getX(), entity.getY(), entity.getZ(), 0, true);
			entity.level().addFreshEntity(lightningBolt);
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
	}
}
