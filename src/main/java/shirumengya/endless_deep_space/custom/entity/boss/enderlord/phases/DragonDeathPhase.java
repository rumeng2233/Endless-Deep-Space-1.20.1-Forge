package shirumengya.endless_deep_space.custom.entity.boss.enderlord.phases;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import shirumengya.endless_deep_space.custom.entity.boss.enderlord.EnderLord;

import javax.annotation.Nullable;

public class DragonDeathPhase extends AbstractDragonPhaseInstance {
   @Nullable
   private Vec3 targetLocation;
   private int time;

   public DragonDeathPhase(EnderLord p_31217_) {
      super(p_31217_);
   }

   public void doClientTick() {
      if (this.time++ % 10 == 0) {
         float f = (this.dragon.getRandom().nextFloat() - 0.5F) * 8.0F;
         float f1 = (this.dragon.getRandom().nextFloat() - 0.5F) * 4.0F;
         float f2 = (this.dragon.getRandom().nextFloat() - 0.5F) * 8.0F;
         this.dragon.level().addParticle(ParticleTypes.EXPLOSION_EMITTER, this.dragon.getX() + (double)f, this.dragon.getY() + 2.0D + (double)f1, this.dragon.getZ() + (double)f2, 0.0D, 0.0D, 0.0D);
      }

   }

   public void doServerTick() {
      ++this.time;
      this.dragon.setShield(0.0F);
      this.dragon.setAttackTimes(0);
      this.dragon.setChargingTime(0);
      if (this.targetLocation == null) {
         BlockPos blockpos = this.dragon.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, this.dragon.getFightOrigin());
         this.targetLocation = Vec3.atBottomCenterOf(blockpos);
      }

      double d0 = this.targetLocation.distanceToSqr(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
      if (!(d0 < 100.0D)) {
         this.dragon.reallySetDragonHealth(1.0F);
      } else {
         this.dragon.reallySetDragonHealth(0.0F);
         this.dragon.setCanBeDead(true);
      }

   }

   public void begin() {
      this.targetLocation = null;
      this.time = 0;
   }

   public float getFlySpeed() {
      return 3.0F;
   }

   @Nullable
   public Vec3 getFlyTargetLocation() {
      return this.targetLocation;
   }

   public EnderDragonPhase<DragonDeathPhase> getPhase() {
      return EnderDragonPhase.DYING;
   }
}