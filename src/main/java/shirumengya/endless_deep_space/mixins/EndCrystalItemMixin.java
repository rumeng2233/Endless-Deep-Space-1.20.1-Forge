package shirumengya.endless_deep_space.mixins;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.item.EndCrystalItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import shirumengya.endless_deep_space.custom.entity.ScreenShakeEntity;
import shirumengya.endless_deep_space.custom.entity.boss.enderlord.EnderLord;
import shirumengya.endless_deep_space.custom.init.ModEntities;
import shirumengya.endless_deep_space.custom.world.explosion.CustomExplosion;

import java.util.List;

@Mixin({EndCrystalItem.class})
public abstract class EndCrystalItemMixin {
	public EndCrystalItemMixin() {
	}
	
	@Inject(method = {"useOn"}, at = {@At("HEAD")}, cancellable = true)
	public void useOn(UseOnContext p_41176_, CallbackInfoReturnable<InteractionResult> ci) {
		Level level = p_41176_.getLevel();
		BlockPos blockpos = p_41176_.getClickedPos();
		BlockState blockstate = level.getBlockState(blockpos);
		if (!blockstate.is(Blocks.OBSIDIAN) && !blockstate.is(Blocks.BEDROCK)) {
			ci.setReturnValue(InteractionResult.FAIL);
		} else {
			BlockPos blockpos1 = blockpos.above();
			if (!level.isEmptyBlock(blockpos1)) {
				ci.setReturnValue(InteractionResult.FAIL);
			} else {
				double d0 = blockpos1.getX();
				double d1 = blockpos1.getY();
				double d2 = blockpos1.getZ();
				List<Entity> list = level.getEntities(null, new AABB(d0, d1, d2, d0 + 1.0D, d1 + 2.0D, d2 + 1.0D));
				if (!list.isEmpty()) {
					ci.setReturnValue(InteractionResult.FAIL);
				} else {
					if (level instanceof ServerLevel) {
						EndCrystal endcrystal = new EndCrystal(level, d0 + 0.5D, d1, d2 + 0.5D);
						endcrystal.setShowBottom(false);
						level.addFreshEntity(endcrystal);
						level.gameEvent(p_41176_.getPlayer(), GameEvent.ENTITY_PLACE, blockpos1);
						EndDragonFight enddragonfight = ((ServerLevel)level).getDragonFight();
						if (enddragonfight != null) {
							enddragonfight.tryRespawn();
							if (((EndDragonFightInvoker)enddragonfight).getPortalLocation() != null) {
								for (Direction direction : Direction.Plane.HORIZONTAL) {
									List<EndCrystal> endCrystals = level.getEntitiesOfClass(EndCrystal.class, new AABB(((EndDragonFightInvoker) enddragonfight).getPortalLocation().above(1).relative(direction, 2)));
									if (!endCrystals.isEmpty()) {
										if (enddragonfight.getCrystalsAlive() >= 10 && enddragonfight.getDragonUUID() != null) {
											ScreenShakeEntity.ScreenShake(level, new Vec3(d0, d1, d2), 100.0F, 0.2F, 160, 40);
											((EnderDragon) ((ServerLevel) level).getEntity(enddragonfight.getDragonUUID())).getPhaseManager().setPhase(EnderDragonPhase.DYING);
											endcrystal.discard();
											CustomExplosion.nukeExplode(level, null, d0, d1, d2, 40.0F, 0.0F, false, CustomExplosion.BlockInteraction.KEEP, 0.0D, 1.0F);
											EnderLord enderLord = new EnderLord(ModEntities.ENDER_LORD.get(), level);
											enderLord.setPos(0, 100, 0);
											if (enderLord.getRandom().nextFloat() > 0.4F) {
												enderLord.getPhaseManager().setPhase(shirumengya.endless_deep_space.custom.entity.boss.enderlord.phases.EnderDragonPhase.CLUSTER_STRAFE_ENTITY);
											} else if (enderLord.getRandom().nextFloat() > 0.5F) {
												enderLord.getPhaseManager().setPhase(shirumengya.endless_deep_space.custom.entity.boss.enderlord.phases.EnderDragonPhase.TRACKING_ENTITY);
											} else {
												enderLord.setAttackTimes(30);
											}
											enderLord.attackTarget = p_41176_.getPlayer();
											enderLord.setAttackTarget(p_41176_.getPlayer().getId());
											level.addFreshEntity(enderLord);
											level.levelEvent(3001, new BlockPos(0, 100, 0), 0);
											for (ServerPlayer serverplayer : level.getEntitiesOfClass(ServerPlayer.class, enderLord.getBoundingBox().inflate(400.0D))) {
												CriteriaTriggers.SUMMONED_ENTITY.trigger(serverplayer, enderLord);
											}
										}
									}
								}
							}
						}
					}
					
					p_41176_.getItemInHand().shrink(1);
					ci.setReturnValue(InteractionResult.sidedSuccess(level.isClientSide));
				}
			}
		}
	}
}
