package shirumengya.endless_deep_space.custom.entity.boss.enderlord.phases;

import com.mojang.logging.LogUtils;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import shirumengya.endless_deep_space.custom.entity.boss.enderlord.EnderLord;

import javax.annotation.Nullable;

public class DragonChargeEntityPhase extends AbstractDragonPhaseInstance {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int CHARGE_RECOVERY_TIME = 10;
   @Nullable
   private Vec3 targetLocation;
   private int timeSinceCharge;

   public DragonChargeEntityPhase(EnderLord p_31206_) {
      super(p_31206_);
   }

   public void doServerTick() {
      if (this.targetLocation == null) {
         LOGGER.warn("Aborting charge entity as no target was set.");
         this.dragon.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
      } else if (this.timeSinceCharge > 0 && this.timeSinceCharge++ >= 10) {
         this.dragon.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
      } else {
         double d0 = this.targetLocation.distanceToSqr(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
         if (d0 < 100.0D || d0 > 22500.0D || this.dragon.horizontalCollision || this.dragon.verticalCollision) {
            ++this.timeSinceCharge;
         }

      }
   }

   public void begin() {
      this.targetLocation = null;
      this.timeSinceCharge = 0;
   }

   public void setTarget(Vec3 p_31208_) {
      this.targetLocation = p_31208_;
   }

   public float getFlySpeed() {
      return 3.0F;
   }

   @Nullable
   public Vec3 getFlyTargetLocation() {
      return this.targetLocation;
   }

   public EnderDragonPhase<DragonChargeEntityPhase> getPhase() {
      return EnderDragonPhase.CHARGING_ENTITY;
   }
}