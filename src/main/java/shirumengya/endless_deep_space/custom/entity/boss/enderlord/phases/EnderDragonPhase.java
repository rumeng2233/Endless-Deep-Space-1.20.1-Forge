package shirumengya.endless_deep_space.custom.entity.boss.enderlord.phases;

import shirumengya.endless_deep_space.custom.entity.boss.enderlord.EnderLord;

import java.lang.reflect.Constructor;
import java.util.Arrays;

public class EnderDragonPhase<T extends DragonPhaseInstance> {
   private static EnderDragonPhase<?>[] phases = new EnderDragonPhase[0];
   public static final EnderDragonPhase<DragonHoldingPatternPhase> HOLDING_PATTERN = create(DragonHoldingPatternPhase.class, "HoldingPattern");
   public static final EnderDragonPhase<DragonStrafeEntityPhase> STRAFE_ENTITY = create(DragonStrafeEntityPhase.class, "StrafeEntity");
   public static final EnderDragonPhase<DragonLandingApproachPhase> LANDING_APPROACH = create(DragonLandingApproachPhase.class, "LandingApproach");
   public static final EnderDragonPhase<DragonLandingPhase> LANDING = create(DragonLandingPhase.class, "Landing");
   public static final EnderDragonPhase<DragonTakeoffPhase> TAKEOFF = create(DragonTakeoffPhase.class, "Takeoff");
   public static final EnderDragonPhase<DragonSittingFlamingPhase> SITTING_FLAMING = create(DragonSittingFlamingPhase.class, "SittingFlaming");
   public static final EnderDragonPhase<DragonSittingScanningPhase> SITTING_SCANNING = create(DragonSittingScanningPhase.class, "SittingScanning");
   public static final EnderDragonPhase<DragonSittingAttackingPhase> SITTING_ATTACKING = create(DragonSittingAttackingPhase.class, "SittingAttacking");
   public static final EnderDragonPhase<DragonChargeEntityPhase> CHARGING_ENTITY = create(DragonChargeEntityPhase.class, "ChargingEntity");
   public static final EnderDragonPhase<DragonDeathPhase> DYING = create(DragonDeathPhase.class, "Dying");
   public static final EnderDragonPhase<DragonHoverPhase> HOVERING = create(DragonHoverPhase.class, "Hover");
   public static final EnderDragonPhase<DragonTrackingEntityPhase> TRACKING_ENTITY = create(DragonTrackingEntityPhase.class, "TrackingEntity");
   public static final EnderDragonPhase<DragonExplosionPhase> EXPLOSION = create(DragonExplosionPhase.class, "Explosion");
   public static final EnderDragonPhase<DragonNoAIPhase> NO_AI = create(DragonNoAIPhase.class, "NoAI");
   public static final EnderDragonPhase<DragonClusterStrafeEntityPhase> CLUSTER_STRAFE_ENTITY = create(DragonClusterStrafeEntityPhase.class, "ClusterStrafeEntity");
   private final Class<? extends DragonPhaseInstance> instanceClass;
   private final int id;
   private final String name;

   private EnderDragonPhase(int p_31394_, Class<? extends DragonPhaseInstance> p_31395_, String p_31396_) {
      this.id = p_31394_;
      this.instanceClass = p_31395_;
      this.name = p_31396_;
   }

   public DragonPhaseInstance createInstance(EnderLord p_31401_) {
      try {
         Constructor<? extends DragonPhaseInstance> constructor = this.getConstructor();
         return constructor.newInstance(p_31401_);
      } catch (Exception exception) {
         throw new Error(exception);
      }
   }

   protected Constructor<? extends DragonPhaseInstance> getConstructor() throws NoSuchMethodException {
      return this.instanceClass.getConstructor(EnderLord.class);
   }

   public int getId() {
      return this.id;
   }

   public String toString() {
      return this.name + " (#" + this.id + ")";
   }

   public static EnderDragonPhase<?> getById(int p_31399_) {
      return p_31399_ >= 0 && p_31399_ < phases.length ? phases[p_31399_] : HOLDING_PATTERN;
   }

   public static int getCount() {
      return phases.length;
   }

   private static <T extends DragonPhaseInstance> EnderDragonPhase<T> create(Class<T> p_31403_, String p_31404_) {
      EnderDragonPhase<T> enderdragonphase = new EnderDragonPhase<>(phases.length, p_31403_, p_31404_);
      phases = Arrays.copyOf(phases, phases.length + 1);
      phases[enderdragonphase.getId()] = enderdragonphase;
      return enderdragonphase;
   }
}