package shirumengya.endless_deep_space.custom.entity.boss.enderlord.phases;

import net.minecraft.world.phys.Vec3;
import shirumengya.endless_deep_space.custom.entity.boss.enderlord.EnderLord;

import javax.annotation.Nullable;

public class DragonNoAIPhase extends AbstractDragonPhaseInstance {
   @Nullable
   private Vec3 targetLocation;

   public DragonNoAIPhase(EnderLord p_31246_) {
      super(p_31246_);
   }

   public void doServerTick() {
      this.targetLocation = null;
   }

   public void begin() {
      this.targetLocation = null;
   }

   public float getFlySpeed() {
      return 0.0F;
   }

   public float getMoveSpeed() {
   	  return 0.0F;
   }

   @Nullable
   public Vec3 getFlyTargetLocation() {
      return this.targetLocation;
   }

   public EnderDragonPhase<DragonNoAIPhase> getPhase() {
      return EnderDragonPhase.NO_AI;
   }
}