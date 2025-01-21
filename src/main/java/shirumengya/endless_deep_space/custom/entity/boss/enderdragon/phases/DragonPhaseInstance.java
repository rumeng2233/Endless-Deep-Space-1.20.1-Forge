package shirumengya.endless_deep_space.custom.entity.boss.enderdragon.phases;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public interface DragonPhaseInstance {
   boolean isSitting();

   void doClientTick();

   void doServerTick();

   void begin();

   void end();

   float getFlySpeed();

   float getTurnSpeed();

   EnderDragonPhase<? extends DragonPhaseInstance> getPhase();

   @Nullable
   Vec3 getFlyTargetLocation();

   float onHurt(DamageSource p_31313_, float p_31314_);
}