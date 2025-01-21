package shirumengya.endless_deep_space.custom.entity.boss.enderlord.phases;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import shirumengya.endless_deep_space.custom.entity.boss.enderlord.EnderLord;
import shirumengya.endless_deep_space.custom.world.explosion.CustomExplosion;

import javax.annotation.Nullable;

public class DragonTrackingEntityPhase extends AbstractDragonPhaseInstance {
   @Nullable
   private Vec3 targetLocation;
   private int time;
   private static final TargetingConditions NEW_TARGET_TARGETING = TargetingConditions.forCombat().ignoreLineOfSight().range(200);

   public DragonTrackingEntityPhase(EnderLord p_31217_) {
      super(p_31217_);
   }

   public void doClientTick() {
      if (this.time++ % 10 == 0) {
         for (int i = 0; i < 10; i++) {
         	 float f = (this.dragon.getRandom().nextFloat() - 0.5F) * 8.0F;
            float f1 = (this.dragon.getRandom().nextFloat() - 0.5F) * 4.0F;
            float f2 = (this.dragon.getRandom().nextFloat() - 0.5F) * 8.0F;
            this.dragon.level().addParticle(ParticleTypes.SONIC_BOOM, this.dragon.getX() + (double)f, this.dragon.getY() + 2.0D + (double)f1, this.dragon.getZ() + (double)f2, 0.0D, 0.0D, 0.0D);
         }
      }
   }

   public void doServerTick() {
      ++this.time;

      if (this.dragon.attackTarget == null || !this.dragon.attackTarget.isAlive()) {
         BlockPos blockpos = this.dragon.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, this.dragon.getFightOrigin());
         LivingEntity entity = this.dragon.level().getNearestEntity(LivingEntity.class, NEW_TARGET_TARGETING, this.dragon, (double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), new AABB(new Vec3(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ()), new Vec3(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ())).inflate(200 / 2d));
         if (entity == null) {
            CustomExplosion.nukeExplode(this.dragon.level(), this.dragon, this.dragon.getX(), this.dragon.getY(), this.dragon.getZ(), 10.0F, this.dragon.level().getLevelData().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING), this.dragon.level().getLevelData().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) ? CustomExplosion.BlockInteraction.DESTROY_WITH_DECAY : CustomExplosion.BlockInteraction.KEEP, 1300.0D, 1.0F);
            this.dragon.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
            return;
         }
         this.dragon.attackTarget = entity;
         this.dragon.setAttackTarget(entity.getId());
      }

      if (this.dragon.attackTarget != null) {
         this.targetLocation = new Vec3(this.dragon.attackTarget.getX(), this.dragon.attackTarget.getY(), this.dragon.attackTarget.getZ());
      }

      if (this.time >= (this.dragon.attackTarget instanceof Player ? 300 : 600)) {
         CustomExplosion.nukeExplode(this.dragon.level(), this.dragon, this.dragon.getX(), this.dragon.getY(), this.dragon.getZ(), 10.0F, this.dragon.level().getLevelData().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING), this.dragon.level().getLevelData().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) ? CustomExplosion.BlockInteraction.DESTROY_WITH_DECAY : CustomExplosion.BlockInteraction.KEEP, 1300.0D, 1.0F);
         this.dragon.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
      }
   }

   public void begin() {
      this.targetLocation = null;
      this.time = 0;
   }

   public float getFlySpeed() {
      return 12.0F;
   }

   public float getMoveSpeed() {
   	  return 4.0F;
   }

   @Nullable
   public Vec3 getFlyTargetLocation() {
      return this.targetLocation;
   }

   public EnderDragonPhase<DragonTrackingEntityPhase> getPhase() {
      return EnderDragonPhase.TRACKING_ENTITY;
   }
}