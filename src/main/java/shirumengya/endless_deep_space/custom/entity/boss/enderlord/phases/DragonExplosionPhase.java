package shirumengya.endless_deep_space.custom.entity.boss.enderlord.phases;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import shirumengya.endless_deep_space.custom.entity.ColorfulLightningBolt;
import shirumengya.endless_deep_space.custom.entity.ScreenShakeEntity;
import shirumengya.endless_deep_space.custom.entity.boss.enderlord.EnderLord;
import shirumengya.endless_deep_space.custom.entity.boss.enderlord.EnderLordPart;
import shirumengya.endless_deep_space.custom.entity.boss.oceandefenders.OceanDefender;
import shirumengya.endless_deep_space.custom.util.entity.TrackingUtil;
import shirumengya.endless_deep_space.custom.world.explosion.CustomExplosion;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

public class DragonExplosionPhase extends AbstractDragonPhaseInstance {
   @Nullable
   private Vec3 targetLocation;

   private int timeout;

   public DragonExplosionPhase(EnderLord p_31305_) {
      super(p_31305_);
   }

   public void doClientTick() {
      this.dragon.chargingTime = this.dragon.getChargingTime();
      if (this.dragon.chargingTime <= 0) {
         Vec3 vec3 = this.dragon.getHeadLookVector(1.0F).normalize();
         vec3.yRot((-(float)Math.PI / 4F));
         double d0 = this.dragon.head.getX();
         double d1 = this.dragon.head.getY(0.5D);
         double d2 = this.dragon.head.getZ();

         for(int i = 0; i < 8; ++i) {
            RandomSource randomsource = this.dragon.getRandom();
            double d3 = d0 + randomsource.nextGaussian() / 2.0D;
            double d4 = d1 + randomsource.nextGaussian() / 2.0D;
            double d5 = d2 + randomsource.nextGaussian() / 2.0D;
            Vec3 vec31 = this.dragon.getDeltaMovement();
            this.dragon.level().addParticle(ParticleTypes.END_ROD, d3, d4, d5, -vec3.x * (double)0.08F + vec31.x, -vec3.y * (double)0.3F + vec31.y, -vec3.z * (double)0.08F + vec31.z);
            vec3.yRot(0.19634955F);
         }
      }
   }

   public void doServerTick() {
      if (this.dragon.getChargingTime() <= 0) {
         this.timeout++;
      
         if (this.targetLocation == null) {
            this.targetLocation = Vec3.atBottomCenterOf(this.dragon.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, this.dragon.getFightOrigin())).add(0.0D, 10.0D, 0.0D);
         }

         if (this.targetLocation.distanceToSqr(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ()) < 1.0D || timeout >= 1200) {
            this.dragon.setChargingTime(1);
         }
      } else {
      	  this.dragon.setChargingTime(this.dragon.getChargingTime() + 1);
      	  this.targetLocation = null;
          if (this.dragon.getProgressTwo()) {
              TrackingUtil.forceEffect(this.dragon, Entity.class, false, 200, this.dragon.getChargingTime(), 200.0D);
              if (this.dragon.getChargingTime() >= 200) {
                  CustomExplosion.nukeExplode(this.dragon.level(), this.dragon, this.dragon.getX(), this.dragon.getY(), this.dragon.getZ(), 100.0F, this.dragon.level().getLevelData().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING), this.dragon.level().getLevelData().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) ? CustomExplosion.BlockInteraction.DESTROY_WITH_DECAY : CustomExplosion.BlockInteraction.KEEP, 3600000.0D, 1);
                  ScreenShakeEntity.ScreenShake(this.dragon.level(), new Vec3(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ()), 100.0F, 100.0F / 600.0F, 140, 40);
                  List<Entity> _entfound = this.dragon.level().getEntitiesOfClass(Entity.class, new AABB(new Vec3(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ()), new Vec3(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ())).inflate(400 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(new Vec3(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ())))).toList();
                  for (Entity entityiterator : _entfound) {
                      Entity entity = (Entity) entityiterator;
                      if (entity != this.dragon && !(entity instanceof ColorfulLightningBolt) && !(entity instanceof EnderLordPart)) {
                          if (entity instanceof LivingEntity) {
                              LivingEntity livingentity = (LivingEntity) entity;
                              if (!(livingentity instanceof Player)) {
                                  for (int i = 0; i < 20; i++) {
                                      livingentity.hurt(EnderLord.enderLordAttack(this.dragon), Float.MAX_VALUE);
                                      livingentity.invulnerableTime = 0;
                                      livingentity.removeAllEffects();
                                      livingentity.setHealth(0);
                                     OceanDefender.setIsDying(livingentity, true);
                                      livingentity.die(EnderLord.enderLordAttack(this.dragon));
                                  }
                              }
                          } else {
                              entity.hurt(EnderLord.enderLordAttack(this.dragon), Float.MAX_VALUE);
                          }
                          this.dragon.doEnchantDamageEffects(this.dragon, entity);

                          ColorfulLightningBolt lightningBolt = new ColorfulLightningBolt(this.dragon.level(), entityiterator.getX(), entityiterator.getY(), entityiterator.getZ(), 1.0F, false);
                          this.dragon.level().addFreshEntity(lightningBolt);
                      }
                  }
                  if (this.dragon.attackTarget != null && !(this.dragon.attackTarget instanceof Player)) {
                      for (int i = 0; i < 20; i++) {
                          this.dragon.attackTarget.hurt(EnderLord.enderLordAttack(this.dragon), Float.MAX_VALUE);
                          this.dragon.attackTarget.invulnerableTime = 0;
                          this.dragon.attackTarget.removeAllEffects();
                          this.dragon.attackTarget.setHealth(0);
                         OceanDefender.setIsDying(this.dragon.attackTarget, true);
                          this.dragon.attackTarget.die(EnderLord.enderLordAttack(this.dragon));
                          this.dragon.attackTarget.gameEvent(GameEvent.ENTITY_DIE);
                      }
                  }
                  this.dragon.setChargingTime(0);
                  this.dragon.setAttackTimes(0);
                  this.dragon.setDeltaMovement(Vec3.ZERO);
                  this.dragon.hasImpulse = true;
                  this.dragon.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
                  return;
              }
          } else {
              TrackingUtil.forceEffect(this.dragon, Entity.class, false, 100, this.dragon.getChargingTime(), 100.0D);
              List<Entity> _entfound = this.dragon.level().getEntitiesOfClass(Entity.class, new AABB(new Vec3(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ()), new Vec3(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ())).inflate(200 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(new Vec3(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ())))).toList();
              for (Entity entityiterator : _entfound) {
                  Entity entity = (Entity) entityiterator;
                  if (this.dragon.tickCount % 10 == 0) {
                      if (entity != this.dragon) {
                          entity.hurt(EnderLord.enderLordAttack(this.dragon), entity instanceof Player ? this.dragon.getChargingTime() / 10.0F : entity instanceof LivingEntity ? Math.max(((LivingEntity) entity).getMaxHealth() / 4.0F, this.dragon.getMaxHealth()) : this.dragon.getMaxHealth());
                      }
                      this.dragon.doEnchantDamageEffects(this.dragon, entity);
                  }
              }
              if (this.dragon.attackTarget != null && !(this.dragon.attackTarget instanceof Player)) {
                  if (this.dragon.tickCount % 10 == 0) {
                      this.dragon.attackTarget.hurt(EnderLord.enderLordAttack(this.dragon), Math.max(this.dragon.attackTarget.getMaxHealth() / 4.0F, this.dragon.getMaxHealth()));
                      this.dragon.doEnchantDamageEffects(this.dragon, this.dragon.attackTarget);
                  }
              }
              if (this.dragon.getChargingTime() >= 200) {
                  CustomExplosion.nukeExplode(this.dragon.level(), this.dragon, this.dragon.getX(), this.dragon.getY(), this.dragon.getZ(), 40.0F, this.dragon.level().getLevelData().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING), this.dragon.level().getLevelData().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) ? CustomExplosion.BlockInteraction.DESTROY_WITH_DECAY : CustomExplosion.BlockInteraction.KEEP, 1300.0D, 1);
                  ScreenShakeEntity.ScreenShake(this.dragon.level(), new Vec3(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ()), 40.0F, 40.0F / 600.0F, 100, 40);
                  for (Entity entityiterator : _entfound) {
                      Entity entity = (Entity) entityiterator;
                      if (entity != this.dragon) {
                          if (entity instanceof LivingEntity) {
                              LivingEntity livingentity = (LivingEntity) entity;
                              livingentity.hurt(EnderLord.enderLordAttack(this.dragon), livingentity.getMaxHealth() - livingentity.getMaxHealth() / 10.0F);
                              if (!(livingentity instanceof Player)) {
                                  for (int i = 0; i < 20; i++) {
                                      livingentity.hurt(EnderLord.enderLordAttack(this.dragon), Float.MAX_VALUE);
                                      livingentity.invulnerableTime = 0;
                                      livingentity.removeAllEffects();
                                      livingentity.setHealth(0);
                                     OceanDefender.setIsDying(livingentity, true);
                                      livingentity.die(EnderLord.enderLordAttack(this.dragon));
                                  }
                              }
                          } else {
                              entity.hurt(EnderLord.enderLordAttack(this.dragon), Float.MAX_VALUE);
                          }
                          this.dragon.doEnchantDamageEffects(this.dragon, entity);
                      }
                  }
                  if (this.dragon.attackTarget != null && !(this.dragon.attackTarget instanceof Player)) {
                      for (int i = 0; i < 20; i++) {
                          this.dragon.attackTarget.hurt(EnderLord.enderLordAttack(this.dragon), Float.MAX_VALUE);
                          this.dragon.attackTarget.invulnerableTime = 0;
                          this.dragon.attackTarget.removeAllEffects();
                          this.dragon.attackTarget.setHealth(0);
                         OceanDefender.setIsDying(this.dragon.attackTarget, true);
                          this.dragon.attackTarget.die(EnderLord.enderLordAttack(this.dragon));
                          this.dragon.attackTarget.gameEvent(GameEvent.ENTITY_DIE);
                      }
                  }
                  this.dragon.setChargingTime(0);
                  this.dragon.setAttackTimes(0);
                  if (this.dragon.getHealth() <= this.dragon.getMaxHealth() / 2 && !this.dragon.getProgressTwo()) {
                      this.dragon.setProgressTwo(true);
                      this.dragon.reallySetDragonHealth(this.dragon.getMaxHealth() / 2);
                      this.dragon.setShield(this.dragon.getMaxShield());
                  }
                  this.dragon.setDeltaMovement(Vec3.ZERO);
                  this.dragon.hasImpulse = true;
                  this.dragon.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
                  return;
              }
          }
      }
   }

   public float getFlySpeed() {
      return 3.0F;
   }

   public float getTurnSpeed() {
      float f = (float)this.dragon.getDeltaMovement().horizontalDistance() + 1.0F;
      float f1 = Math.min(f, 40.0F);
      return f1 / f;
   }

   public float getMoveSpeed() {
   	  return 1.4F;
   }

   public float onHurt(DamageSource p_31181_, float p_31182_) {
      return p_31182_ / (this.dragon.getChargingTime() > 0 ? 10.0F : 4.0F);
   }

   public void begin() {
      this.targetLocation = null;
      this.timeout = 0;
      this.dragon.setChargingTime(0);
   }

   @Nullable
   public Vec3 getFlyTargetLocation() {
      return this.targetLocation;
   }

   public EnderDragonPhase<DragonExplosionPhase> getPhase() {
      return EnderDragonPhase.EXPLOSION;
   }
}