package shirumengya.endless_deep_space.custom.entity.boss.enderlord.phases;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.projectile.AbstractArrow;
import shirumengya.endless_deep_space.custom.entity.boss.enderlord.EnderLord;
import shirumengya.endless_deep_space.custom.entity.projectile.Arrow;

public abstract class AbstractDragonSittingPhase extends AbstractDragonPhaseInstance {
   public AbstractDragonSittingPhase(EnderLord p_31196_) {
      super(p_31196_);
   }

   public boolean isSitting() {
      return true;
   }

   public float onHurt(DamageSource p_31199_, float p_31200_) {
      if (p_31199_.getDirectEntity() instanceof AbstractArrow || p_31199_.getDirectEntity() instanceof Arrow) {
         p_31199_.getDirectEntity().setSecondsOnFire(1);
         return 0.0F;
      } else {
         return super.onHurt(p_31199_, p_31200_);
      }
   }
}