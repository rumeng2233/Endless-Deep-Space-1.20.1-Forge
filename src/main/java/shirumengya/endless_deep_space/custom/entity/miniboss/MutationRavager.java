package shirumengya.endless_deep_space.custom.entity.miniboss;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import shirumengya.endless_deep_space.custom.client.event.CustomServerBossEvent;
import shirumengya.endless_deep_space.custom.entity.ShieldEntity;
import shirumengya.endless_deep_space.custom.entity.boss.enderlord.EnderLord;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.function.Predicate;

public class MutationRavager extends Raider implements ShieldEntity {
   private static final Predicate<Entity> NO_RAVAGER_AND_ALIVE = (p_33346_) -> {
      return p_33346_.isAlive() && !(p_33346_ instanceof MutationRavager);
   };
   private static final double BASE_MOVEMENT_SPEED = 0.3D;
   private static final double ATTACK_MOVEMENT_SPEED = 0.35D;
   private static final int STUNNED_COLOR = 8356754;
   private static final double STUNNED_COLOR_BLUE = 0.5725490196078431D;
   private static final double STUNNED_COLOR_GREEN = 0.5137254901960784D;
   private static final double STUNNED_COLOR_RED = 0.4980392156862745D;
   private static final int ATTACK_DURATION = 10;
   public static final int STUN_DURATION = 40;
   private int attackTick;
   private int stunnedTick;
   private int roarTick;
   @Nullable
   private LivingEntity clientSideCachedAttackTarget;
   private int clientSideAttackTime;
   public static final EntityDataAccessor<Float> DATA_SHIELD = SynchedEntityData.defineId(MutationRavager.class, EntityDataSerializers.FLOAT);
   public static final EntityDataAccessor<Boolean> PROGRESS_TWO = SynchedEntityData.defineId(MutationRavager.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Integer> DATA_ID_ATTACK_TARGET = SynchedEntityData.defineId(MutationRavager.class, EntityDataSerializers.INT);
   private final CustomServerBossEvent bossEvent = new CustomServerBossEvent(this, this.getDisplayName(), BossEvent.BossBarColor.PURPLE, false, 6);
   private final CustomServerBossEvent bossShieldEvent = new CustomServerBossEvent(Component.empty(), Component.empty(), BossEvent.BossBarColor.WHITE, false, 7);

   private final CustomServerBossEvent hasPassengerBossEvent = new CustomServerBossEvent(this, this.getDisplayName(), BossEvent.BossBarColor.PURPLE, false, 8);
   private final CustomServerBossEvent hasPassengerBossShieldEvent = new CustomServerBossEvent(Component.empty(), Component.empty(), BossEvent.BossBarColor.WHITE, false, 9);
   private final CustomServerBossEvent hasPassengerBossEventPassenger = new CustomServerBossEvent(Component.empty(), Component.empty(), BossEvent.BossBarColor.PURPLE, false, 10);

   public MutationRavager(EntityType<? extends MutationRavager> p_33325_, Level p_33326_) {
      super(p_33325_, p_33326_);
      this.noCulling = true;
      this.setMaxUpStep(1.0F);
      this.xpReward = 200;
      this.setPathfindingMalus(BlockPathTypes.LEAVES, 0.0F);
   }

   protected void registerGoals() {
      super.registerGoals();
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(4, new MutationRavager.RavagerMeleeAttackGoal());
      this.goalSelector.addGoal(1, new MutationRavager.MutationRavagerRemoteAttackGoal(this));
      this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.4D));
      this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
      this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
      this.targetSelector.addGoal(2, (new HurtByTargetGoal(this, Raider.class)).setAlertOthers());
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, true));
      this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, true, (p_199899_) -> {
         return !p_199899_.isBaby();
      }));
      this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
   }

   protected void updateControlFlags() {
      boolean flag = !(this.getControllingPassenger() instanceof Mob) || this.getControllingPassenger().getType().is(EntityTypeTags.RAIDERS);
      boolean flag1 = !(this.getVehicle() instanceof Boat);
      this.goalSelector.setControlFlag(Goal.Flag.MOVE, flag);
      this.goalSelector.setControlFlag(Goal.Flag.JUMP, flag && flag1);
      this.goalSelector.setControlFlag(Goal.Flag.LOOK, flag);
      this.goalSelector.setControlFlag(Goal.Flag.TARGET, flag);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 100.0D).add(Attributes.MOVEMENT_SPEED, 0.4D).add(Attributes.KNOCKBACK_RESISTANCE, 1.0D).add(Attributes.ATTACK_DAMAGE, 14.0D).add(Attributes.ATTACK_KNOCKBACK, 1.8D).add(Attributes.FOLLOW_RANGE, 64.0D);
   }

   public void tick() {
      if (!this.level().isClientSide) {
         this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
         this.bossShieldEvent.setProgress(this.getShield() / this.getMaxShield());
         if (this.isAlive() && this.deathTime <= 0) {
            if (this.hasShield()) {
               this.setHealth(this.getProgressTwo() ? this.getMaxHealth() / 2.0F : this.getMaxHealth());
               if (this.tickCount % 20 == 0) {
                  this.setShield(this.getShield() + 1.0F);
               }
            } else {
               if (this.tickCount % 20 == 0 && this.isAlive()) {
                  this.setHealth(this.getHealth() + 1.0F);
               }
            }
         }

         if (this.getProgressTwo() && this.getHealth() > this.getMaxHealth() / 2.0F) {
            this.setHealth(this.getMaxHealth() / 2.0F);
         }

         if (this.getFirstPassenger() != null && this.getFirstPassenger() instanceof Raider raider) {
            this.hasPassengerBossEvent.setVisible(true);
            this.hasPassengerBossShieldEvent.setVisible(true);
            this.hasPassengerBossEventPassenger.setVisible(true);
            this.bossEvent.setVisible(false);
            this.bossShieldEvent.setVisible(false);

            this.hasPassengerBossEvent.setProgress(this.getHealth() / this.getMaxHealth());
            this.hasPassengerBossEvent.setDescription(Component.translatable("entity.endless_deep_space.mutation_ravager.description.passenger", raider.getDisplayName()));
            this.hasPassengerBossShieldEvent.setProgress(this.getShield() / this.getMaxShield());
            this.hasPassengerBossEventPassenger.setProgress(raider.getHealth() / raider.getMaxHealth());
         } else {
            this.hasPassengerBossEvent.setVisible(false);
            this.hasPassengerBossShieldEvent.setVisible(false);
            this.hasPassengerBossEventPassenger.setVisible(false);
            this.bossEvent.setVisible(true);
            this.bossShieldEvent.setVisible(true);
         }
      }

      super.tick();
   }

   @Override
   public void checkDespawn() {
   }

   public boolean canChangeDimensions() {
      return false;
   }

   public void startSeenByPlayer(ServerPlayer player) {
      super.startSeenByPlayer(player);
      this.bossEvent.addPlayer(player);
      this.bossShieldEvent.addPlayer(player);
      this.hasPassengerBossEvent.addPlayer(player);
      this.hasPassengerBossShieldEvent.addPlayer(player);
      this.hasPassengerBossEventPassenger.addPlayer(player);
   }

   public void stopSeenByPlayer(ServerPlayer player) {
      super.stopSeenByPlayer(player);
      this.bossEvent.removePlayer(player);
      this.bossShieldEvent.removePlayer(player);
      this.hasPassengerBossEvent.removePlayer(player);
      this.hasPassengerBossShieldEvent.removePlayer(player);
      this.hasPassengerBossEventPassenger.removePlayer(player);
   }

   public void addAdditionalSaveData(CompoundTag p_33353_) {
      super.addAdditionalSaveData(p_33353_);
      p_33353_.putInt("AttackTick", this.attackTick);
      p_33353_.putInt("StunTick", this.stunnedTick);
      p_33353_.putInt("RoarTick", this.roarTick);
      p_33353_.putBoolean("ProgressTwo", this.getProgressTwo());
      p_33353_.putFloat("Shield", this.getShield());
   }

   public void readAdditionalSaveData(CompoundTag p_33344_) {
      super.readAdditionalSaveData(p_33344_);
      this.attackTick = p_33344_.getInt("AttackTick");
      this.stunnedTick = p_33344_.getInt("StunTick");
      this.roarTick = p_33344_.getInt("RoarTick");
      this.setProgressTwo(p_33344_.getBoolean("ProgressTwo"));
      this.setShield(p_33344_.getFloat("Shield"));
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.getEntityData().define(PROGRESS_TWO, false);
      this.getEntityData().define(DATA_SHIELD, this.getMaxShield());
      this.getEntityData().define(DATA_ID_ATTACK_TARGET, 0);
   }

   public int getRemoteAttackDuration() {
      return 60;
   }

   public void setActiveAttackTarget(int p_32818_) {
      this.entityData.set(DATA_ID_ATTACK_TARGET, p_32818_);
   }

   public boolean hasActiveAttackTarget() {
      return this.entityData.get(DATA_ID_ATTACK_TARGET) != 0;
   }

   @Nullable
   public LivingEntity getActiveAttackTarget() {
      if (!this.hasActiveAttackTarget()) {
         return null;
      } else if (this.level().isClientSide) {
         if (this.clientSideCachedAttackTarget != null) {
            return this.clientSideCachedAttackTarget;
         } else {
            Entity entity = this.level().getEntity(this.entityData.get(DATA_ID_ATTACK_TARGET));
            if (entity instanceof LivingEntity) {
               this.clientSideCachedAttackTarget = (LivingEntity)entity;
               return this.clientSideCachedAttackTarget;
            } else {
               return null;
            }
         }
      } else {
         return this.getTarget();
      }
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> p_32834_) {
      super.onSyncedDataUpdated(p_32834_);
      if (DATA_ID_ATTACK_TARGET.equals(p_32834_)) {
         this.clientSideAttackTime = 0;
         this.clientSideCachedAttackTarget = null;
      }

   }

   @Override
   public boolean hurt(DamageSource p_37849_, float p_37850_) {
      if (p_37849_.getEntity() != null && p_37849_.getEntity() instanceof Raider) {
         return false;
      }
      if (p_37849_.getEntity() != null && p_37849_.getDirectEntity() != null && p_37849_.getEntity() != p_37849_.getDirectEntity()) {
         return false;
      }

      return super.hurt(p_37849_, p_37850_);
   }

   @Override
   protected void actuallyHurt(DamageSource p_21240_, float p_21241_) {
      if (!this.isInvulnerableTo(p_21240_)) {
         p_21241_ = net.minecraftforge.common.ForgeHooks.onLivingHurt(this, p_21240_, p_21241_);
         if (p_21241_ <= 0) return;
         p_21241_ = this.getDamageAfterArmorAbsorb(p_21240_, p_21241_);
         p_21241_ = this.getDamageAfterMagicAbsorb(p_21240_, p_21241_);
         float f1 = Math.max(p_21241_ - this.getAbsorptionAmount(), 0.0F);
         this.setAbsorptionAmount(this.getAbsorptionAmount() - (p_21241_ - f1));
         float f = p_21241_ - f1;
         if (f > 0.0F && f < 3.4028235E37F) {
            Entity entity = p_21240_.getEntity();
            if (entity instanceof ServerPlayer) {
               ServerPlayer serverplayer = (ServerPlayer)entity;
               serverplayer.awardStat(Stats.DAMAGE_DEALT_ABSORBED, Math.round(f * 10.0F));
            }
         }

         f1 = net.minecraftforge.common.ForgeHooks.onLivingDamage(this, p_21240_, f1);
         if (f1 != 0.0F) {
            this.getCombatTracker().recordDamage(p_21240_, f1);
            if (this.hasShield()) {
               this.setShield(this.getShield() - f1);
            } else {
               this.setHealth(this.getHealth() - f1);
            }

            if (this.getHealth() <= this.getMaxHealth() / 2 && !this.getProgressTwo()) {
               this.setProgressTwo(true);
               super.setHealth(this.getMaxHealth() / 2.0F);
               this.setShield(this.getMaxShield());
               this.playSound(SoundEvents.RAVAGER_ROAR, 1.0F, 1.0F);
               this.roarTick = 20;
               this.level().broadcastEntityEvent(this, (byte) -3);
            }

            this.setAbsorptionAmount(this.getAbsorptionAmount() - f1);
            this.gameEvent(GameEvent.ENTITY_DAMAGE);
         }
      }
   }

   @Override
   public void kill() {
      super.kill();
      this.setShield(0.0F);
      this.setHealth(0.0F);
   }

   @Override
   public boolean isAlive() {
      return !this.isRemoved() && this.getHealth() > 0 || !this.isRemoved() && this.hasShield();
   }

   @Override
   public boolean isDeadOrDying() {
      return this.getHealth() <= 0.0F && !this.hasShield();
   }

   public SoundEvent getCelebrateSound() {
      return SoundEvents.RAVAGER_CELEBRATE;
   }

   public int getMaxHeadYRot() {
      return 45;
   }

   public double getPassengersRidingOffset() {
      return 2.1D;
   }

   @Nullable
   public LivingEntity getControllingPassenger() {
      if (!this.isNoAi()) {
         Entity entity = this.getFirstPassenger();
         if (entity instanceof LivingEntity) {
            return (LivingEntity)entity;
         }
      }

      return null;
   }

   public void aiStep() {
      super.aiStep();
      if (this.isAlive()) {
         if (this.isImmobile()) {
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.0D);
         } else {
            double d0 = this.getTarget() != null ? 0.35D : 0.3D;
            double d1 = this.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue();
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(Mth.lerp(0.1D, d1, d0));
         }

         if (this.horizontalCollision && net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.level(), this)) {
            boolean flag = false;
            AABB aabb = this.getBoundingBox().inflate(0.2D);

            for(BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
               BlockState blockstate = this.level().getBlockState(blockpos);
               Block block = blockstate.getBlock();
               if (block instanceof LeavesBlock) {
                  flag = this.level().destroyBlock(blockpos, true, this) || flag;
               }
            }

            if (!flag && this.onGround()) {
               this.jumpFromGround();
            }
         }

         if (this.roarTick > 0) {
            --this.roarTick;
            if (this.roarTick == 10) {
               this.roar(this.getProgressTwo() ? 12.0F : 6.0F);
            }
         }

         if (this.attackTick > 0) {
            --this.attackTick;
         }

         if (this.stunnedTick > 0) {
            --this.stunnedTick;
            this.stunEffect();
            if (this.stunnedTick == 0) {
               this.playSound(SoundEvents.RAVAGER_ROAR, 1.0F, 1.0F);
               this.roarTick = 20;
            }
         }

         if (this.hasActiveAttackTarget()) {
            if (this.clientSideAttackTime < this.getRemoteAttackDuration()) {
               ++this.clientSideAttackTime;
            }

            LivingEntity livingentity = this.getActiveAttackTarget();
            if (livingentity != null) {
               this.getLookControl().setLookAt(livingentity, 90.0F, 90.0F);
               this.getLookControl().tick();
               double d5 = (double)this.getAttackAnimationScale(0.0F);
               double d0 = livingentity.getX() - this.getX();
               double d1 = livingentity.getY(0.5D) - this.getEyeY();
               double d2 = livingentity.getZ() - this.getZ();
               double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
               d0 /= d3;
               d1 /= d3;
               d2 /= d3;
               double d4 = this.random.nextDouble();

               while(d4 < d3) {
                  d4 += 1.8D - d5 + this.random.nextDouble() * (1.7D - d5);
                  this.level().addParticle(ParticleTypes.BUBBLE, this.getX() + d0 * d4, this.getEyeY() + d1 * d4, this.getZ() + d2 * d4, 0.0D, 0.0D, 0.0D);
               }
            }
         }

      }
   }

   public float getAttackAnimationScale(float p_32813_) {
      return ((float)this.clientSideAttackTime + p_32813_) / (float)this.getRemoteAttackDuration();
   }

   public float getClientSideAttackTime() {
      return (float)this.clientSideAttackTime;
   }

   private void stunEffect() {
      if (this.random.nextInt(6) == 0) {
         double d0 = this.getX() - (double)this.getBbWidth() * Math.sin((double)(this.yBodyRot * ((float)Math.PI / 180F))) + (this.random.nextDouble() * 0.6D - 0.3D);
         double d1 = this.getY() + (double)this.getBbHeight() - 0.3D;
         double d2 = this.getZ() + (double)this.getBbWidth() * Math.cos((double)(this.yBodyRot * ((float)Math.PI / 180F))) + (this.random.nextDouble() * 0.6D - 0.3D);
         this.level().addParticle(ParticleTypes.ENTITY_EFFECT, d0, d1, d2, 0.4980392156862745D, 0.5137254901960784D, 0.5725490196078431D);
      }

   }

   protected boolean isImmobile() {
      return super.isImmobile() || this.attackTick > 0 || this.stunnedTick > 0 || this.roarTick > 0;
   }

   public boolean hasLineOfSight(Entity p_149755_) {
      return this.stunnedTick <= 0 && this.roarTick <= 0 ? super.hasLineOfSight(p_149755_) : false;
   }

   protected void blockedByShield(LivingEntity p_33361_) {
      if (this.roarTick == 0) {
         if (this.random.nextDouble() < 0.5D) {
            if (this.hasShield()) {
               this.playSound(SoundEvents.RAVAGER_ROAR, 1.0F, 1.0F);
               this.roarTick = 20;
               this.level().broadcastEntityEvent(this, (byte) -3);
            } else {
               this.stunnedTick = 40;
               this.playSound(SoundEvents.RAVAGER_STUNNED, 1.0F, 1.0F);
               this.level().broadcastEntityEvent(this, (byte) 39);
               p_33361_.push(this);
            }
         } else {
            this.strongKnockback(p_33361_);
         }

         p_33361_.hurtMarked = true;
      }

   }

   private void roar(float damage) {
      if (this.isAlive()) {
         for(LivingEntity livingentity : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(4.0D), NO_RAVAGER_AND_ALIVE)) {
            if (!(livingentity instanceof AbstractIllager)) {
               livingentity.hurt(this.damageSources().mobAttack(this), damage);
            }

            this.strongKnockback(livingentity);
         }

         Vec3 vec3 = this.getBoundingBox().getCenter();

         for(int i = 0; i < 40; ++i) {
            double d0 = this.random.nextGaussian() * 0.2D;
            double d1 = this.random.nextGaussian() * 0.2D;
            double d2 = this.random.nextGaussian() * 0.2D;
            this.level().addParticle(ParticleTypes.POOF, vec3.x, vec3.y, vec3.z, d0, d1, d2);
         }

         this.gameEvent(GameEvent.ENTITY_ROAR);
      }

   }

   private void strongKnockback(Entity p_33340_) {
      double d0 = p_33340_.getX() - this.getX();
      double d1 = p_33340_.getZ() - this.getZ();
      double d2 = Math.max(d0 * d0 + d1 * d1, 0.001D);
      p_33340_.push(d0 / d2 * 4.0D, 0.2D, d1 / d2 * 4.0D);
   }

   public void handleEntityEvent(byte p_33335_) {
      if (p_33335_ == -3) {
         this.roarTick = 20;
      } else if (p_33335_ == 4) {
         this.attackTick = 10;
         this.playSound(SoundEvents.RAVAGER_ATTACK, 1.0F, 1.0F);
      } else if (p_33335_ == 39) {
         this.stunnedTick = 40;
      }

      super.handleEntityEvent(p_33335_);
   }

   public int getAttackTick() {
      return this.attackTick;
   }

   public int getStunnedTick() {
      return this.stunnedTick;
   }

   public int getRoarTick() {
      return this.roarTick;
   }

   public boolean doHurtTarget(Entity p_33328_) {
      this.attackTick = 10;
      this.level().broadcastEntityEvent(this, (byte)4);
      this.playSound(SoundEvents.RAVAGER_ATTACK, 1.0F, 1.0F);
      return super.doHurtTarget(p_33328_);
   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      return SoundEvents.RAVAGER_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource p_33359_) {
      return SoundEvents.RAVAGER_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.RAVAGER_DEATH;
   }

   protected void playStepSound(BlockPos p_33350_, BlockState p_33351_) {
      this.playSound(SoundEvents.RAVAGER_STEP, 0.15F, 1.0F);
   }

   public boolean checkSpawnObstruction(LevelReader p_33342_) {
      return !p_33342_.containsAnyLiquid(this.getBoundingBox());
   }

   public void applyRaidBuffs(int p_33337_, boolean p_33338_) {
   }

   public boolean canBeLeader() {
      return false;
   }

   @Override
   public void setShield(float value) {
      this.getEntityData().set(DATA_SHIELD, Mth.clamp(value, 0.0F, this.getMaxShield()));
   }

   @Override
   public float getShield() {
      return this.getEntityData().get(DATA_SHIELD);
   }

   @Override
   public float getMaxShield() {
      return this.getProgressTwo() ? 60.0F : 40.0F;
   }

   @Override
   public boolean hasShield() {
      return this.getShield() > 0.0F;
   }

   public void setProgressTwo(boolean value) {
      this.getEntityData().set(PROGRESS_TWO, value);
   }

   public boolean getProgressTwo() {
      return this.getEntityData().get(PROGRESS_TWO);
   }

   class RavagerMeleeAttackGoal extends MeleeAttackGoal {
      public RavagerMeleeAttackGoal() {
         super(MutationRavager.this, 1.0D, true);
      }

      protected double getAttackReachSqr(LivingEntity p_33377_) {
         float f = MutationRavager.this.getBbWidth() - 0.1F;
         return (double)(f * 2.0F * f * 2.0F + p_33377_.getBbWidth());
      }
   }

   static class MutationRavagerRemoteAttackGoal extends Goal {
      private final MutationRavager ravager;
      private int attackTime;
      private int maxAttackTime;

      public MutationRavagerRemoteAttackGoal(MutationRavager ravager) {
         this.ravager = ravager;
         this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
      }

      public boolean canUse() {
         LivingEntity livingentity = this.ravager.getTarget();
         return livingentity != null && livingentity.isAlive() && (this.ravager.distanceToSqr(livingentity) > 256.0D || livingentity.getMaxHealth() > this.ravager.getMaxHealth() * 2.0F);
      }

      public boolean canContinueToUse() {
         return super.canContinueToUse() && (this.ravager.getTarget() != null && (this.ravager.distanceToSqr(this.ravager.getTarget()) > 256.0D || this.ravager.getTarget().getMaxHealth() > this.ravager.getMaxHealth() * 2.0F)) && this.maxAttackTime > 0;
      }

      public void start() {
         this.attackTime = -10;
         this.maxAttackTime = 200;
         this.ravager.getNavigation().stop();
         LivingEntity livingentity = this.ravager.getTarget();
         if (livingentity != null) {
            this.ravager.getLookControl().setLookAt(livingentity, 90.0F, 90.0F);
         }

         this.ravager.hasImpulse = true;
      }

      public void stop() {
         this.ravager.setActiveAttackTarget(0);
      }

      public boolean requiresUpdateEveryTick() {
         return true;
      }

      public void tick() {
         LivingEntity livingentity = this.ravager.getTarget();
         if (livingentity != null) {
            this.ravager.getNavigation().stop();
            this.ravager.getLookControl().setLookAt(livingentity, 90.0F, 90.0F);
            if (this.ravager.hasLineOfSight(livingentity)) {
               ++this.attackTime;
               if (this.attackTime == 0) {
                  this.ravager.setActiveAttackTarget(livingentity.getId());
                  if (!this.ravager.isSilent()) {
                     this.ravager.level().broadcastEntityEvent(this.ravager, (byte)-4);
                  }
               } else if (this.attackTime >= this.ravager.getRemoteAttackDuration() && this.maxAttackTime > 0) {
                  livingentity.hurt(EnderLord.enderLordAttack(this.ravager), livingentity.getMaxHealth() / 100.0F);
                  livingentity.invulnerableTime = 10;
                  this.maxAttackTime--;
               }

               super.tick();
            }
         }
      }
   }
}
