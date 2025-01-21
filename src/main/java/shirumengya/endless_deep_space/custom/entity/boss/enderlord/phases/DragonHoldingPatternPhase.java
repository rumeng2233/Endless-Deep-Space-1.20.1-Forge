package shirumengya.endless_deep_space.custom.entity.boss.enderlord.phases;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import shirumengya.endless_deep_space.custom.entity.boss.enderlord.EnderLord;

import javax.annotation.Nullable;

public class DragonHoldingPatternPhase extends AbstractDragonPhaseInstance {
   @Nullable
   private Path currentPath;
   @Nullable
   private Vec3 targetLocation;
   private boolean clockwise;

   public DragonHoldingPatternPhase(EnderLord p_31230_) {
      super(p_31230_);
   }

   public EnderDragonPhase<DragonHoldingPatternPhase> getPhase() {
      return EnderDragonPhase.HOLDING_PATTERN;
   }

   public void doServerTick() {
      this.dragon.setHangTime(this.dragon.getHangTime() + 1);
      if (this.dragon.getFlyAttackCooldown() > 0) {
      	 this.dragon.setFlyAttackCooldown(this.dragon.getFlyAttackCooldown() - 1);
      }
      double d0 = this.targetLocation == null ? 0.0D : this.targetLocation.distanceToSqr(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
      if (d0 < 100.0D || d0 > 22500.0D || this.dragon.horizontalCollision || this.dragon.verticalCollision) {
         this.findNewTarget();
      }

   }

   public void begin() {
      this.currentPath = null;
      this.targetLocation = null;
   }

   @Nullable
   public Vec3 getFlyTargetLocation() {
      return this.targetLocation;
   }

   private void findNewTarget() {
      if (this.currentPath != null && this.currentPath.isDone()) {
         BlockPos blockpos = this.dragon.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, this.dragon.getFightOrigin());
         int i = this.dragon.getProgressTwo() ? 0 : 10;
         if (this.dragon.getRandom().nextInt(i + 3) == 0 || this.dragon.getHangTime() >= 1200) {
            this.dragon.setHangTime(0);
            this.dragon.getPhaseManager().setPhase(EnderDragonPhase.LANDING_APPROACH);
            return;
         }

         double d0;
         if (this.dragon.attackTarget != null) {
            d0 = blockpos.distToCenterSqr(this.dragon.attackTarget.position()) / 512.0D;
         } else {
            d0 = 64.0D;
         }

         if (this.dragon.getRandom().nextInt(i + 4) == 0 && this.dragon.attackTarget != null) {
            this.dragon.getPhaseManager().setPhase(EnderDragonPhase.CLUSTER_STRAFE_ENTITY);
            return;
         }

         if (this.dragon.attackTarget != null && (this.dragon.getFlyAttackCooldown() <= 0 || (this.dragon.getRandom().nextInt((int)(d0 + 2.0D)) == 0 || this.dragon.getRandom().nextInt(i + 2) == 0))) {
            this.strafeEntity(this.dragon.attackTarget);
            return;
         }
      }

      if (this.currentPath == null || this.currentPath.isDone()) {
         int j = this.dragon.findClosestNode();
         int k = j;
         if (this.dragon.getRandom().nextInt(8) == 0) {
            this.clockwise = !this.clockwise;
            k = j + 6;
         }

         if (this.clockwise) {
            ++k;
         } else {
            --k;
         }

         if (this.dragon.getProgressTwo()) {
            k %= 12;
            if (k < 0) {
               k += 12;
            }
         } else {
            k -= 12;
            k &= 7;
            k += 12;
         }

         this.currentPath = this.dragon.findPath(j, k, (Node)null);
         if (this.currentPath != null) {
            this.currentPath.advance();
         }
      }

      this.navigateToNextPathNode();
   }

   private void strafeEntity(LivingEntity p_31237_) {
      this.dragon.setFlyAttackCooldown(Mth.nextInt(this.dragon.getRandom(), 100, 700));
      if (this.dragon.getRandom().nextFloat() < 0.4F) {
      	 this.dragon.getPhaseManager().setPhase(EnderDragonPhase.TRACKING_ENTITY);
      } else {
      	 this.dragon.getPhaseManager().setPhase(EnderDragonPhase.STRAFE_ENTITY);
      	 this.dragon.getPhaseManager().getPhase(EnderDragonPhase.STRAFE_ENTITY).setTarget(p_31237_);
      }
   }

   private void navigateToNextPathNode() {
      if (this.currentPath != null && !this.currentPath.isDone()) {
         Vec3i vec3i = this.currentPath.getNextNodePos();
         this.currentPath.advance();
         double d0 = (double)vec3i.getX();
         double d1 = (double)vec3i.getZ();

         double d2;
         do {
            d2 = (double)((float)vec3i.getY() + this.dragon.getRandom().nextFloat() * 20.0F);
         } while(d2 < (double)vec3i.getY());

         this.targetLocation = new Vec3(d0, d2, d1);
      }

   }
}