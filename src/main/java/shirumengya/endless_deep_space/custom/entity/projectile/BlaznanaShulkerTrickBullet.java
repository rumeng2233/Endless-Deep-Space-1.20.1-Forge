package shirumengya.endless_deep_space.custom.entity.projectile;

import com.google.common.base.MoreObjects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import shirumengya.endless_deep_space.custom.init.ModEntities;
import shirumengya.endless_deep_space.custom.util.entity.TrackingUtil;
import shirumengya.endless_deep_space.custom.world.explosion.CustomExplosion;

public class BlaznanaShulkerTrickBullet extends ThrowableProjectile {

   public BlaznanaShulkerTrickBullet(EntityType<? extends BlaznanaShulkerTrickBullet> p_36892_, Level p_36893_) {
      super(p_36892_, p_36893_);
      this.noCulling = true;
   }

   public BlaznanaShulkerTrickBullet(Level p_36903_, Entity p_36904_) {
      this(ModEntities.BLAZNANA_SHULKER_TRICK_BULLET.get(), p_36903_);
      this.setOwner(p_36904_);
   }

   @Override
   protected float getGravity() {
      return 0.06F;
   }

   @Override
   protected void defineSynchedData() {
   }

   private void destroy() {
      this.discard();
      this.level().gameEvent(GameEvent.ENTITY_DAMAGE, this.position(), GameEvent.Context.of(this));
      if (!this.level().isClientSide) {
         CustomExplosion.nukeExplode(this.level(), this.getOwner(), this.getX(), this.getY(), this.getZ(), 4.0F, this.level().getLevelData().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING), this.level().getLevelData().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) ? CustomExplosion.BlockInteraction.DESTROY_WITH_DECAY : CustomExplosion.BlockInteraction.KEEP, 1300.0D, 1);
      }
   }

   @Override
   public void tick() {
      super.tick();

      Vec3 vec3 = this.getDeltaMovement();
      float v0 = (float) vec3.length();
      float maxTurningAngleCos = Mth.cos(7.1619724F * v0 * Mth.DEG_TO_RAD);
      float maxTurningAngleSin = Mth.sin(7.1619724F * v0 * Mth.DEG_TO_RAD);
      if (!this.level().isClientSide) {
         TrackingUtil.TrackingEntity(this, maxTurningAngleCos, maxTurningAngleSin, false);

         if (this.level().getDifficulty() == Difficulty.PEACEFUL) {
            this.destroy();
         }
      }

      Vec3 vec31 = this.getDeltaMovement();
      if (this.level().isClientSide) {
         this.level().addParticle(ParticleTypes.END_ROD, this.getX() - vec31.x, this.getY() - vec31.y + 0.15D, this.getZ() - vec31.z, 0.0D, 0.0D, 0.0D);
      }
   }

   protected void onHitEntity(EntityHitResult p_37345_) {
      super.onHitEntity(p_37345_);
      Entity entity = p_37345_.getEntity();
      Entity entity1 = this.getOwner();
      LivingEntity livingentity = entity1 instanceof LivingEntity ? (LivingEntity)entity1 : null;
      entity.invulnerableTime = 0;
      boolean flag = false;
      for (int i = 0; i < 4; i++) {
         flag = entity.hurt(this.damageSources().mobProjectile(this, livingentity), (entity instanceof LivingEntity livingEntity ? livingEntity.getMaxHealth() / 10.0F : 8.0F) + i);
         entity.invulnerableTime = 0;
      }
      if (flag && livingentity != null) {
         this.doEnchantDamageEffects(livingentity, entity);
         if (entity instanceof LivingEntity) {
            LivingEntity livingentity1 = (LivingEntity)entity;
            livingentity1.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 1), MoreObjects.firstNonNull(entity1, this));
         }
      }

      this.destroy();
   }

   @Override
   protected void onHitBlock(BlockHitResult p_37258_) {
      super.onHitBlock(p_37258_);

      if (!this.level().isClientSide) {
         if (this.getDeltaMovement().lengthSqr() < 0.25 || this.getOwner() == null || (this.getOwner() instanceof Mob mob && mob.getTarget() == null)) {
            this.destroy();
         }
      }
   }

   public boolean isPickable() {
      return true;
   }

   public boolean hurt(DamageSource p_37338_, float p_37339_) {
      if (!this.level().isClientSide) {
         this.playSound(SoundEvents.SHULKER_BULLET_HURT, 1.0F, 1.0F);
         if (this.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(), 15, 0.2D, 0.2D, 0.2D, 0.0D);
         }
         this.destroy();
      }
      return true;
   }

   public boolean isPushable() {
      return false;
   }

   public boolean isPushedByFluid() {
      return false;
   }

   public boolean ignoreExplosion() {
      return true;
   }

   public boolean displayFireAnimation() {
      return false;
   }
}