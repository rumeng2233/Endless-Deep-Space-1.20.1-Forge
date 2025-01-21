package shirumengya.endless_deep_space.custom.entity.boss.enderlord.phases;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import shirumengya.endless_deep_space.custom.entity.boss.enderlord.EnderLord;
import shirumengya.endless_deep_space.custom.entity.projectile.ClusterFireball;
import shirumengya.endless_deep_space.custom.world.explosion.CustomExplosion;

import javax.annotation.Nullable;

public class DragonClusterStrafeEntityPhase extends AbstractDragonPhaseInstance {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int FIREBALL_CHARGE_AMOUNT = 5;
   private int fireballCharge;
   @Nullable
   private Path currentPath;
   @Nullable
   private Vec3 targetLocation;
   private boolean holdingPatternClockwise;
   private static final TargetingConditions NEW_TARGET_TARGETING = TargetingConditions.forCombat().ignoreLineOfSight().range(200);
   private int chargeTimes;

   public DragonClusterStrafeEntityPhase(EnderLord p_31357_) {
      super(p_31357_);
   }

   public void doServerTick() {
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
      } else {
         if (this.currentPath != null && this.currentPath.isDone()) {
            double d0 = this.dragon.attackTarget.getX();
            double d1 = this.dragon.attackTarget.getZ();
            double d2 = d0 - this.dragon.getX();
            double d3 = d1 - this.dragon.getZ();
            double d4 = Math.sqrt(d2 * d2 + d3 * d3);
            double d5 = Math.min((double)0.4F + d4 / 80.0D - 1.0D, 10.0D);
            this.targetLocation = new Vec3(d0, this.dragon.attackTarget.getY() + d5, d1);
         }

         double d12 = this.targetLocation == null ? 0.0D : this.targetLocation.distanceToSqr(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
         if (d12 < 100.0D || d12 > 22500.0D) {
            this.findNewTarget();
         }

         double d13 = 64.0D;
         if (this.dragon.attackTarget.distanceToSqr(this.dragon) < 4096.0D) {
            if (this.dragon.hasLineOfSight(this.dragon.attackTarget)) {
               ++this.fireballCharge;
               Vec3 vec31 = (new Vec3(this.dragon.attackTarget.getX() - this.dragon.getX(), 0.0D, this.dragon.attackTarget.getZ() - this.dragon.getZ())).normalize();
               Vec3 vec3 = (new Vec3((double)Mth.sin(this.dragon.getYRot() * ((float)Math.PI / 180F)), 0.0D, (double)(-Mth.cos(this.dragon.getYRot() * ((float)Math.PI / 180F))))).normalize();
               float f1 = (float)vec3.dot(vec31);
               float f = (float)(Math.acos((double)f1) * (double)(180F / (float)Math.PI));
               f += 0.5F;
               if (this.fireballCharge >= 5 && f >= 0.0F && f < 10.0F) {
                  double d14 = 1.0D;
                  Vec3 vec32 = this.dragon.getViewVector(1.0F);
                  double d6 = this.dragon.head.getX() - vec32.x * 1.0D;
                  double d7 = this.dragon.head.getY(0.5D) + 0.5D;
                  double d8 = this.dragon.head.getZ() - vec32.z * 1.0D;
                  double d9 = this.dragon.attackTarget.getX() - d6;
                  double d10 = this.dragon.attackTarget.getY(0.5D) - d7;
                  double d11 = this.dragon.attackTarget.getZ() - d8;
                  if (!this.dragon.isSilent()) {
                     this.dragon.level().levelEvent((Player)null, 1017, this.dragon.blockPosition(), 0);
                  }

                  ClusterFireball dragonfireball = new ClusterFireball(this.dragon.level(), this.dragon, d9, d10, d11);
                  dragonfireball.moveTo(d6, d7, d8, 0.0F, 0.0F);
                  this.dragon.level().addFreshEntity(dragonfireball);

                  this.fireballCharge = 0;
                  if (this.currentPath != null) {
                     while(!this.currentPath.isDone()) {
                        this.currentPath.advance();
                     }
                  }

                  this.chargeTimes++;
                  if (this.chargeTimes >= 40) {
                     this.dragon.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
                  }
               }
            } else if (this.fireballCharge > 0) {
               --this.fireballCharge;
            }
         } else if (this.fireballCharge > 0) {
            --this.fireballCharge;
         }

      }
   }

   private void findNewTarget() {
      if (this.currentPath == null || this.currentPath.isDone()) {
         int i = this.dragon.findClosestNode();
         int j = i;
         if (this.dragon.getRandom().nextInt(8) == 0) {
            this.holdingPatternClockwise = !this.holdingPatternClockwise;
            j = i + 6;
         }

         if (this.holdingPatternClockwise) {
            ++j;
         } else {
            --j;
         }

         if (this.dragon.getProgressTwo()) {
            j %= 12;
            if (j < 0) {
               j += 12;
            }
         } else {
            j -= 12;
            j &= 7;
            j += 12;
         }

         this.currentPath = this.dragon.findPath(i, j, (Node)null);
         if (this.currentPath != null) {
            this.currentPath.advance();
         }
      }

      this.navigateToNextPathNode();
   }

   private void navigateToNextPathNode() {
      if (this.currentPath != null && !this.currentPath.isDone()) {
         Vec3i vec3i = this.currentPath.getNextNodePos();
         this.currentPath.advance();
         double d0 = (double)vec3i.getX();
         double d2 = (double)vec3i.getZ();

         double d1;
         do {
            d1 = (double)((float)vec3i.getY() + this.dragon.getRandom().nextFloat() * 20.0F);
         } while(d1 < (double)vec3i.getY());

         this.targetLocation = new Vec3(d0, d1, d2);
      }

   }

   public void begin() {
      this.fireballCharge = 0;
      this.targetLocation = null;
      this.currentPath = null;
      this.chargeTimes = 0;
   }

   @Nullable
   public Vec3 getFlyTargetLocation() {
      return this.targetLocation;
   }

   public EnderDragonPhase<DragonClusterStrafeEntityPhase> getPhase() {
      return EnderDragonPhase.CLUSTER_STRAFE_ENTITY;
   }
}