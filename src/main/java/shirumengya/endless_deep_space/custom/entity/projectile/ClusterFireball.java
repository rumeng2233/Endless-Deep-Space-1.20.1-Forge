package shirumengya.endless_deep_space.custom.entity.projectile;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import shirumengya.endless_deep_space.custom.entity.ColorfulLightningBolt;
import shirumengya.endless_deep_space.custom.entity.ScreenShakeEntity;
import shirumengya.endless_deep_space.custom.entity.boss.enderlord.EnderLord;
import shirumengya.endless_deep_space.custom.entity.boss.enderlord.EnderLordPart;
import shirumengya.endless_deep_space.custom.init.ModEntities;
import shirumengya.endless_deep_space.custom.world.explosion.CustomExplosion;

import java.util.List;

public class ClusterFireball extends AbstractHurtingProjectile {

   public ClusterFireball(EntityType<? extends ClusterFireball> p_36892_, Level p_36893_) {
      super(p_36892_, p_36893_);
      this.noCulling = true;
   }

   public ClusterFireball(Level p_36903_, LivingEntity p_36904_, double p_36905_, double p_36906_, double p_36907_) {
      super(ModEntities.CLUSTER_FIREBALL.get(), p_36904_, p_36905_, p_36906_, p_36907_, p_36903_);
   }

   protected void onHit(HitResult p_36913_) {
      super.onHit(p_36913_);
      if (!this.level().isClientSide) {
         List<LivingEntity> list = this.level().getEntitiesOfClass(LivingEntity.class, new AABB(new Vec3(this.getX(), this.getY(), this.getZ()), new Vec3(this.getX(), this.getY(), this.getZ())).inflate(12 / 2d));
         AreaEffectCloud areaeffectcloud = new AreaEffectCloud(this.level(), this.getX(), this.getY(), this.getZ());
         Entity entity = this.getOwner();
         if (entity instanceof LivingEntity) {
            areaeffectcloud.setOwner((LivingEntity)entity);
         }

         //areaeffectcloud.setParticle(ParticleTypes.DRAGON_BREATH);
         areaeffectcloud.setRadius(6.0F);
         areaeffectcloud.setDuration(600);
         areaeffectcloud.setRadiusPerTick((7.0F - areaeffectcloud.getRadius()) / (float)areaeffectcloud.getDuration());
         areaeffectcloud.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 1));
         if (!list.isEmpty()) {
            for(LivingEntity livingentity : list) {
               if (livingentity != this.getOwner()) {
                  livingentity.hurt(EnderLord.enderLordAttack(this.getOwner() == null ? this : this.getOwner()), livingentity instanceof Player ? 4.0F : Math.max(livingentity.getMaxHealth() / 16.0F, 12.0F));
                  ColorfulLightningBolt lightningBolt = new ColorfulLightningBolt(this.level(), livingentity.getX(), livingentity.getY(), livingentity.getZ(), 1.0F, false);
                  this.level().addFreshEntity(lightningBolt);
                  double d0 = this.distanceToSqr(livingentity);
                  if (d0 < 16.0D) {
                     areaeffectcloud.setPos(livingentity.getX(), livingentity.getY(), livingentity.getZ());
                  }
               }
            }
         } else {
            for (int i = 0; i < 4; i++) {
               ColorfulLightningBolt lightningBolt = new ColorfulLightningBolt(this.level(), this.getX(), this.getY(), this.getZ(), 1.0F, false);
               this.level().addFreshEntity(lightningBolt);
            }
         }

         //this.level().levelEvent(2006, this.blockPosition(), this.isSilent() ? -1 : 1);
         //this.level().explode(this.getOwner(), this.getX(), this.getY(), this.getZ(), 4, this.level().getLevelData().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING), Level.ExplosionInteraction.MOB);
         CustomExplosion.nukeExplode(this.level(), this.getOwner(), this.getX(), this.getY(), this.getZ(), 6.0F, this.level().getLevelData().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING), this.level().getLevelData().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) ? CustomExplosion.BlockInteraction.DESTROY_WITH_DECAY : CustomExplosion.BlockInteraction.KEEP, 1300.0D, 1);
         ScreenShakeEntity.ScreenShake(this.level(), new Vec3(this.getX(), this.getY(), this.getZ()), 6.0F, 0.05F, 40, 20);
         this.level().addFreshEntity(areaeffectcloud);
         if (p_36913_.getType() != HitResult.Type.ENTITY || (!this.ownedBy(((EntityHitResult)p_36913_).getEntity()) && ((EntityHitResult)p_36913_).getEntity() instanceof EnderLordPart && ((EntityHitResult)p_36913_).getEntity() instanceof EnderLord && ((EntityHitResult)p_36913_).getEntity() instanceof ClusterFireball)) {
            this.discard();
         }
      }
   }

   public boolean isPickable() {
      return false;
   }

   public boolean hurt(DamageSource p_36910_, float p_36911_) {
      return false;
   }

   protected ParticleOptions getTrailParticle() {
      return ParticleTypes.DRAGON_BREATH;
   }

   protected boolean shouldBurn() {
      return false;
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