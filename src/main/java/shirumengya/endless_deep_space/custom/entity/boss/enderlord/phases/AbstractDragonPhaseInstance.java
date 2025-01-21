package shirumengya.endless_deep_space.custom.entity.boss.enderlord.phases;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;
import shirumengya.endless_deep_space.custom.entity.boss.enderlord.EnderLord;

import javax.annotation.Nullable;

public abstract class AbstractDragonPhaseInstance implements DragonPhaseInstance {
   protected final EnderLord dragon;

   public AbstractDragonPhaseInstance(EnderLord p_31178_) {
      this.dragon = p_31178_;
   }

   public boolean isSitting() {
      return false;
   }

   public void doClientTick() {
   }

   public void doServerTick() {
   }

   public void begin() {
   }

   public void end() {
   }

   public float getFlySpeed() {
      return 0.6F;
   }

   public float getMoveSpeed() {
   	  return 2.0F;
   }

   @Nullable
   public Vec3 getFlyTargetLocation() {
      return null;
   }

   public float onHurt(DamageSource p_31181_, float p_31182_) {
      return p_31182_;
   }

   public float getTurnSpeed() {
      float f = (float)this.dragon.getDeltaMovement().horizontalDistance() + 1.0F;
      float f1 = Math.min(f, 40.0F);
      return 0.7F / f1 / f;
   }
}