package shirumengya.endless_deep_space.custom.entity.boss.enderdragon.phases;

import net.minecraft.world.phys.Vec3;
import shirumengya.endless_deep_space.custom.entity.boss.enderdragon.EnderDragon;

import javax.annotation.Nullable;

public class DragonHoverPhase extends AbstractDragonPhaseInstance {
   @Nullable
   private Vec3 targetLocation;

   public DragonHoverPhase(EnderDragon p_31246_) {
      super(p_31246_);
   }

   public void doServerTick() {
      if (this.targetLocation == null) {
         this.targetLocation = this.dragon.position();
      }

   }

   public boolean isSitting() {
      return true;
   }

   public void begin() {
      this.targetLocation = null;
   }

   public float getFlySpeed() {
      return 1.0F;
   }

   @Nullable
   public Vec3 getFlyTargetLocation() {
      return this.targetLocation;
   }

   public EnderDragonPhase<DragonHoverPhase> getPhase() {
      return EnderDragonPhase.HOVERING;
   }
}