package shirumengya.endless_deep_space.custom.entity.projectile;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import shirumengya.endless_deep_space.custom.entity.boss.oceandefenders.OceanDefender;
import shirumengya.endless_deep_space.custom.util.entity.TrackingUtil;
import shirumengya.endless_deep_space.custom.world.explosion.CustomExplosion;

import java.util.Comparator;
import java.util.List;

public class AbyssalTorpedo extends ModThrowableProjectile {
   private int trackingTime = 0;
   
   public AbyssalTorpedo(EntityType<? extends ModThrowableProjectile> p_37466_, Level p_37467_) {
      super(p_37466_, p_37467_);
   }
   
   public AbyssalTorpedo(EntityType<? extends ModThrowableProjectile> p_37456_, double p_37457_, double p_37458_, double p_37459_, Level p_37460_) {
      super(p_37456_, p_37457_, p_37458_, p_37459_, p_37460_);
   }
   
   public AbyssalTorpedo(EntityType<? extends ModThrowableProjectile> p_37462_, LivingEntity p_37463_, Level p_37464_) {
      super(p_37462_, p_37463_, p_37464_);
      this.trackingTime = 80;
   }
   
   @Override
   protected void defineSynchedData() {
   }
   
   @Override
   protected float getWaterResistance() {
      return 1.0F;
   }
   
   @Override
   protected float getAirResistance() {
      return 1.0F;
   }
   
   @Override
   protected float getGravity() {
      return 0.3F;
   }
   
   @Override
   public void tick() {
      super.tick();
      Vec3 vec3 = this.getDeltaMovement();
      float v0 = (float) vec3.length();
      float maxTurningAngleCos = Mth.cos(7.1619724F * v0 * Mth.DEG_TO_RAD);
      float maxTurningAngleSin = Mth.sin(7.1619724F * v0 * Mth.DEG_TO_RAD);
      if (!this.level().isClientSide) {
         if (this.trackingTime > 0) {
            TrackingUtil.TrackingEntity(this, maxTurningAngleCos, maxTurningAngleSin, true);
            this.trackingTime--;
         } else {
            this.setNoGravity(false);
         }
      }
   }
   
   @Override
   protected void onHit(HitResult p_37260_) {
      super.onHit(p_37260_);
      if (!this.level().isClientSide) {
         List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, new AABB(this.position(), this.position()).inflate(12 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(this.position()))).toList();
         for (LivingEntity livingEntity : entities) {
            if (livingEntity != this.getOwner() && !(livingEntity instanceof OceanDefender)) {
               livingEntity.hurt(this.damageSources().indirectMagic(this, this.getOwner()), 2.0F);
               OceanDefender.addAttrition(livingEntity, 0.1F);
            }
         }
         CustomExplosion.nukeExplode(this.level(), this.getOwner(), this.getX(), this.getY(), this.getZ(), 4.0F, 0.0F, false, CustomExplosion.BlockInteraction.KEEP, 0.0D, 1.0F);
         this.discard();
      }
   }
}
