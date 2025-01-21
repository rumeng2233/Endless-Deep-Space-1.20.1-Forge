package shirumengya.endless_deep_space.mixins;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import shirumengya.endless_deep_space.custom.config.ModCommonConfig;
import shirumengya.endless_deep_space.custom.entity.boss.enderlord.EnderLord;
import shirumengya.endless_deep_space.custom.entity.boss.oceandefenders.OceanDefender;
import shirumengya.endless_deep_space.custom.entity.miniboss.BlaznanaShulkerTrick;
import shirumengya.endless_deep_space.custom.event.SwordBlockEvent;
import shirumengya.endless_deep_space.custom.init.ModMobEffects;
import shirumengya.endless_deep_space.custom.networking.ModMessages;
import shirumengya.endless_deep_space.custom.networking.packet.UpdateIsDyingS2CPacket;
import shirumengya.endless_deep_space.custom.networking.packet.UpdateVertigoTimeS2CPacket;
import shirumengya.endless_deep_space.init.EndlessDeepSpaceModMobEffects;
import shirumengya.endless_deep_space.network.EndlessDeepSpaceModVariables;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

@Mixin({LivingEntity.class})
public abstract class LivingEntityMixin extends Entity {
   @Shadow
   private float speed;
   
   @Shadow @Nullable protected abstract SoundEvent getDeathSound();
   
   @Shadow protected abstract float getSoundVolume();
   
   @Shadow protected abstract void breakItem(ItemStack p_21279_);
   
   @Shadow protected abstract void swapHandItems();
   
   @Shadow protected abstract void makePoofParticles();
   
   @Shadow public float oAttackAnim;
   
   @Shadow public float attackAnim;
   
   @Shadow public abstract Optional<BlockPos> getSleepingPos();
   
   @Shadow protected abstract void setPosToBed(BlockPos p_21081_);
   
   @Shadow public abstract boolean canSpawnSoulSpeedParticle();
   
   @Shadow protected abstract void spawnSoulSpeedParticle();
   
   @Shadow protected abstract int decreaseAirSupply(int p_21303_);
   
   @Shadow protected abstract int increaseAirSupply(int p_21307_);
   
   @Shadow @Deprecated public abstract boolean canBreatheUnderwater();
   
   @Shadow private BlockPos lastPos;
   
   @Shadow protected abstract void onChangedBlock(BlockPos p_21175_);
   
   @Shadow public int hurtTime;
   
   @Shadow public abstract boolean isDeadOrDying();
   
   @Shadow protected abstract void tickDeath();
   
   @Shadow protected int lastHurtByPlayerTime;
   
   @Shadow @Nullable protected Player lastHurtByPlayer;
   
   @Shadow private LivingEntity lastHurtMob;
   
   @Shadow @Nullable private LivingEntity lastHurtByMob;
   
   @Shadow public abstract void setLastHurtByMob(@org.jetbrains.annotations.Nullable LivingEntity p_21039_);
   
   @Shadow private int lastHurtByMobTimestamp;
   
   @Shadow protected abstract void tickEffects();
   
   @Shadow protected float animStepO;
   
   @Shadow protected float animStep;
   
   @Shadow public float yHeadRotO;
   
   @Shadow public float yBodyRot;
   
   @Shadow public float yBodyRotO;
   
   @Shadow public float yHeadRot;
   
   @Shadow protected abstract void updatingUsingItem();
   
   @Shadow protected abstract void updateSwimAmount();
   
   @Shadow public abstract int getArrowCount();
   
   @Shadow public int removeArrowTime;
   
   @Shadow public abstract void setArrowCount(int p_21318_);
   
   @Shadow public abstract int getStingerCount();
   
   @Shadow public int removeStingerTime;
   
   @Shadow public abstract void setStingerCount(int p_21322_);
   
   @Shadow protected abstract void detectEquipmentUpdates();
   
   @Shadow public abstract CombatTracker getCombatTracker();
   
   @Shadow public abstract boolean isSleeping();
   
   @Shadow protected abstract boolean checkBedExists();
   
   @Shadow public abstract void stopSleeping();
   
   @Shadow public abstract void aiStep();
   
   @Shadow protected float oRun;
   
   @Shadow protected float run;
   
   @Shadow protected abstract float tickHeadTurn(float p_21260_, float p_21261_);
   
   @Shadow public abstract boolean isFallFlying();
   
   @Shadow protected int fallFlyTicks;
   
   @Shadow @Final private static EntityDataAccessor<Float> DATA_HEALTH_ID;
   
   public LivingEntityMixin(EntityType<? extends LivingEntity> p_20966_, Level p_20967_) {
      super(p_20966_, p_20967_);
   }
   
   @Inject(method = {"<clinit>"}, at = {@At("TAIL")})
   private static void init(CallbackInfo ci) {
   }
   
   @Inject(method = {"defineSynchedData"}, at = {@At("HEAD")})
   public void defineSynchedData(CallbackInfo ci) {
      LivingEntity entity = ((LivingEntity)(Object)this);
   }
   
   @Inject(method = {"addAdditionalSaveData"}, at = {@At("HEAD")})
   public void addAdditionalSaveData(CompoundTag p_21145_, CallbackInfo ci) {
      LivingEntity entity = ((LivingEntity)(Object)this);
      p_21145_.putInt("VertigoTime", SwordBlockEvent.getVertigoTime(entity));
      
      ListTag attritionList = new ListTag();
      CompoundTag attritionTag = new CompoundTag();
      attritionTag.putFloat("Level", OceanDefender.getAttrition(entity));
      attritionTag.putInt("Tick", OceanDefender.getAttritionTick(entity));
      attritionList.add(attritionTag);
      p_21145_.put("Attrition", attritionList);
      
      p_21145_.putBoolean("EndlessDeepSpaceIsDying", OceanDefender.isDying(entity));
   }
   
   @Inject(method = {"readAdditionalSaveData"}, at = {@At("HEAD")})
   public void readAdditionalSaveData(CompoundTag p_21145_, CallbackInfo ci) {
      LivingEntity entity = ((LivingEntity)(Object)this);
      SwordBlockEvent.setVertigoTime(entity, p_21145_.getInt("VertigoTime"));
      
      ListTag attritionList = p_21145_.getList("Attrition", 10);
      for(int i = 0; i < attritionList.size(); i++) {
         CompoundTag tag = attritionList.getCompound(i);
         OceanDefender.addAttrition(entity, tag.getFloat("Level"), tag.getInt("Tick"), false, false);
      }
      
      OceanDefender.setIsDying(entity, p_21145_.getBoolean("EndlessDeepSpaceIsDying"));
   }
   
   @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;aiStep()V"))
   public void aiStep(LivingEntity instance) {
      if (!SwordBlockEvent.hasVertigoTime(instance)) {
         instance.aiStep();
      }
   }
   
   @Redirect(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isDeadOrDying()Z"))
   public boolean isDeadOrDying(LivingEntity instance) {
      if (OceanDefender.isDying(instance)) {
         return true;
      }
      return instance.isDeadOrDying();
   }
   
   @Inject(method = {"tick"}, at = {@At("HEAD")})
   public void tick(CallbackInfo ci) {
      LivingEntity entity = ((LivingEntity)(Object)this);
      if (SwordBlockEvent.getVertigoTime(entity) > 0) {
         SwordBlockEvent.setVertigoTime(entity, SwordBlockEvent.getVertigoTime(entity) - 1);
      }
      if (!entity.level().isClientSide) {
         ModMessages.sendToAllPlayers(new UpdateVertigoTimeS2CPacket(entity, false, SwordBlockEvent.getVertigoTime(entity)));
      }
      
      if (OceanDefender.getAttrition(entity) > 0) {
         OceanDefender.setAttritionTick(entity, Math.max(0, OceanDefender.getAttritionTick(entity) - 1));
         if (OceanDefender.getAttritionTick(entity) <= 0) {
            if (!entity.level().isClientSide) {
               OceanDefender.actuallyHurt(entity, OceanDefender.attrition(entity.level()), OceanDefender.getAttrition(entity) * entity.getMaxHealth(), true);
            }
            OceanDefender.setAttrition(entity, 0.0F);
            OceanDefender.setAttritionTick(entity, 0);
            OceanDefender.setAttritionMaxTick(entity, 0);
         }
      }
      
      if (Float.isNaN(entity.getHealth()) || entity.getHealth() < 0.0F) {
         entity.getEntityData().set(DATA_HEALTH_ID, 0.0F);
         entity.setHealth(0.0F);
      }
   }
   
   @Inject(method = {"isDeadOrDying"}, at = {@At("HEAD")}, cancellable = true)
   public void isDeadOrDying(CallbackInfoReturnable<Boolean> ci) {
      LivingEntity entity = ((LivingEntity)(Object)this);
      if (OceanDefender.isDying(entity)) {
         ci.setReturnValue(true);
      }
   }
   
   //由于与Do a Barrel Roll模组冲突，此方法已弃用，采用不稳定的@Redirect来实现相同效果
   /*@Inject(method = {"tick"}, at = {@At("HEAD")}, cancellable = true)
   public void tick(CallbackInfo ci) {
      LivingEntity entity = ((LivingEntity)(Object)this);
      if (SwordBlockEvent.getVertigoTime(entity) > 0) {
         SwordBlockEvent.setVertigoTime(entity, SwordBlockEvent.getVertigoTime(entity) - 1);
      }
      if (!entity.level().isClientSide) {
         ModMessages.sendToAllPlayers(new UpdateVertigoTimeS2CPacket(entity, false, SwordBlockEvent.getVertigoTime(entity)));
      }
      
      if (OceanDefender.getAttrition(entity) > 0) {
         OceanDefender.setAttritionTick(entity, Math.max(0, OceanDefender.getAttritionTick(entity) - 1));
         if (OceanDefender.getAttritionTick(entity) <= 0) {
            if (!entity.level().isClientSide) {
               OceanDefender.actuallyHurt(entity, OceanDefender.attrition(entity.level()), OceanDefender.getAttrition(entity) * entity.getMaxHealth(), true);
            }
            OceanDefender.setAttrition(entity, 0.0F);
            OceanDefender.setAttritionTick(entity, 0);
            OceanDefender.setAttritionMaxTick(entity, 0);
         }
      }
      
      if (Float.isNaN(entity.getHealth()) || entity.getHealth() < 0.0F) {
         entity.getEntityData().set(DATA_HEALTH_ID, 0.0F);
         entity.setHealth(0.0F);
      }
      
      if (net.minecraftforge.common.ForgeHooks.onLivingTick(entity)) return;
      super.tick();
      this.updatingUsingItem();
      this.updateSwimAmount();
      if (!this.level().isClientSide) {
         int i = this.getArrowCount();
         if (i > 0) {
            if (this.removeArrowTime <= 0) {
               this.removeArrowTime = 20 * (30 - i);
            }
            
            --this.removeArrowTime;
            if (this.removeArrowTime <= 0) {
               this.setArrowCount(i - 1);
            }
         }
         
         int j = this.getStingerCount();
         if (j > 0) {
            if (this.removeStingerTime <= 0) {
               this.removeStingerTime = 20 * (30 - j);
            }
            
            --this.removeStingerTime;
            if (this.removeStingerTime <= 0) {
               this.setStingerCount(j - 1);
            }
         }
         
         this.detectEquipmentUpdates();
         if (this.tickCount % 20 == 0) {
            this.getCombatTracker().recheckStatus();
         }
         
         if (this.isSleeping() && !this.checkBedExists()) {
            this.stopSleeping();
         }
      }
      
      if (!this.isRemoved() && !SwordBlockEvent.hasVertigoTime(entity)) {
         this.aiStep();
      }
      
      double d1 = this.getX() - this.xo;
      double d0 = this.getZ() - this.zo;
      float f = (float)(d1 * d1 + d0 * d0);
      float f1 = this.yBodyRot;
      float f2 = 0.0F;
      this.oRun = this.run;
      float f3 = 0.0F;
      if (f > 0.0025000002F) {
         f3 = 1.0F;
         f2 = (float)Math.sqrt((double)f) * 3.0F;
         float f4 = (float)Mth.atan2(d0, d1) * (180F / (float)Math.PI) - 90.0F;
         float f5 = Mth.abs(Mth.wrapDegrees(this.getYRot()) - f4);
         if (95.0F < f5 && f5 < 265.0F) {
            f1 = f4 - 180.0F;
         } else {
            f1 = f4;
         }
      }
      
      if (this.attackAnim > 0.0F) {
         f1 = this.getYRot();
      }
      
      if (!this.onGround()) {
         f3 = 0.0F;
      }
      
      this.run += (f3 - this.run) * 0.3F;
      this.level().getProfiler().push("headTurn");
      f2 = this.tickHeadTurn(f1, f2);
      this.level().getProfiler().pop();
      this.level().getProfiler().push("rangeChecks");
      
      while(this.getYRot() - this.yRotO < -180.0F) {
         this.yRotO -= 360.0F;
      }
      
      while(this.getYRot() - this.yRotO >= 180.0F) {
         this.yRotO += 360.0F;
      }
      
      while(this.yBodyRot - this.yBodyRotO < -180.0F) {
         this.yBodyRotO -= 360.0F;
      }
      
      while(this.yBodyRot - this.yBodyRotO >= 180.0F) {
         this.yBodyRotO += 360.0F;
      }
      
      while(this.getXRot() - this.xRotO < -180.0F) {
         this.xRotO -= 360.0F;
      }
      
      while(this.getXRot() - this.xRotO >= 180.0F) {
         this.xRotO += 360.0F;
      }
      
      while(this.yHeadRot - this.yHeadRotO < -180.0F) {
         this.yHeadRotO -= 360.0F;
      }
      
      while(this.yHeadRot - this.yHeadRotO >= 180.0F) {
         this.yHeadRotO += 360.0F;
      }
      
      this.level().getProfiler().pop();
      this.animStep += f2;
      if (this.isFallFlying()) {
         ++this.fallFlyTicks;
      } else {
         this.fallFlyTicks = 0;
      }
      
      if (this.isSleeping()) {
         this.setXRot(0.0F);
      }
      
      ci.cancel();
   }*/
   
   //由于与Do a Barrel Roll模组冲突，此方法已弃用，采用不稳定的@Redirect来实现相同效果
   /*@Inject(method = {"baseTick"}, at = {@At("HEAD")}, cancellable = true)
   public void baseTick(CallbackInfo ci) {
      LivingEntity entity = ((LivingEntity)(Object)this);
      this.oAttackAnim = this.attackAnim;
      if (this.firstTick) {
         this.getSleepingPos().ifPresent(this::setPosToBed);
      }
      
      if (this.canSpawnSoulSpeedParticle()) {
         this.spawnSoulSpeedParticle();
      }
      
      super.baseTick();
      this.level().getProfiler().push("livingEntityBaseTick");
      if (this.fireImmune() || this.level().isClientSide) {
         this.clearFire();
      }
      
      if (this.isAlive()) {
         boolean flag = entity instanceof Player;
         if (!this.level().isClientSide) {
            if (this.isInWall()) {
               this.hurt(this.damageSources().inWall(), 1.0F);
            } else if (flag && !this.level().getWorldBorder().isWithinBounds(this.getBoundingBox())) {
               double d0 = this.level().getWorldBorder().getDistanceToBorder(this) + this.level().getWorldBorder().getDamageSafeZone();
               if (d0 < 0.0D) {
                  double d1 = this.level().getWorldBorder().getDamagePerBlock();
                  if (d1 > 0.0D) {
                     this.hurt(this.damageSources().outOfBorder(), (float)Math.max(1, Mth.floor(-d0 * d1)));
                  }
               }
            }
         }
         
         int airSupply = this.getAirSupply();
         net.minecraftforge.common.ForgeHooks.onLivingBreathe(entity, airSupply - decreaseAirSupply(airSupply), increaseAirSupply(airSupply) - airSupply);
         if (false) // Forge: Handled in ForgeHooks#onLivingBreathe(LivingEntity, int, int)
            if (this.isEyeInFluid(FluidTags.WATER) && !this.level().getBlockState(BlockPos.containing(this.getX(), this.getEyeY(), this.getZ())).is(Blocks.BUBBLE_COLUMN)) {
               boolean flag1 = !this.canBreatheUnderwater() && !MobEffectUtil.hasWaterBreathing(entity) && (!flag || !((Player)entity).getAbilities().invulnerable);
               if (flag1) {
                  this.setAirSupply(this.decreaseAirSupply(this.getAirSupply()));
                  if (this.getAirSupply() == -20) {
                     this.setAirSupply(0);
                     Vec3 vec3 = this.getDeltaMovement();
                     
                     for(int i = 0; i < 8; ++i) {
                        double d2 = this.random.nextDouble() - this.random.nextDouble();
                        double d3 = this.random.nextDouble() - this.random.nextDouble();
                        double d4 = this.random.nextDouble() - this.random.nextDouble();
                        this.level().addParticle(ParticleTypes.BUBBLE, this.getX() + d2, this.getY() + d3, this.getZ() + d4, vec3.x, vec3.y, vec3.z);
                     }
                     
                     this.hurt(this.damageSources().drown(), 2.0F);
                  }
               }
               
               if (!this.level().isClientSide && this.isPassenger() && this.getVehicle() != null && this.getVehicle().dismountsUnderwater()) {
                  this.stopRiding();
               }
            } else if (this.getAirSupply() < this.getMaxAirSupply()) {
               this.setAirSupply(this.increaseAirSupply(this.getAirSupply()));
            }
         
         if (!this.level().isClientSide) {
            BlockPos blockpos = this.blockPosition();
            if (!com.google.common.base.Objects.equal(this.lastPos, blockpos)) {
               this.lastPos = blockpos;
               this.onChangedBlock(blockpos);
            }
         }
      }
      
      if (this.isAlive() && (this.isInWaterRainOrBubble() || this.isInPowderSnow || this.isInFluidType((fluidType, height) -> this.canFluidExtinguish(fluidType)))) {
         this.extinguishFire();
      }
      
      if (this.hurtTime > 0) {
         --this.hurtTime;
      }
      
      if (this.invulnerableTime > 0 && !(entity instanceof ServerPlayer)) {
         --this.invulnerableTime;
      }
      
      if (!entity.level().isClientSide) {
         ModMessages.sendToAllPlayers(new UpdateIsDyingS2CPacket(entity, false, OceanDefender.isDying(entity)));
      }
      
      if (this.isDeadOrDying() && this.level().shouldTickDeath(this) || OceanDefender.isDying(entity)) {
         this.tickDeath();
      }
      
      if (this.lastHurtByPlayerTime > 0) {
         --this.lastHurtByPlayerTime;
      } else {
         this.lastHurtByPlayer = null;
      }
      
      if (this.lastHurtMob != null && !this.lastHurtMob.isAlive()) {
         this.lastHurtMob = null;
      }
      
      if (this.lastHurtByMob != null) {
         if (!this.lastHurtByMob.isAlive()) {
            this.setLastHurtByMob((LivingEntity)null);
         } else if (this.tickCount - this.lastHurtByMobTimestamp > 100) {
            this.setLastHurtByMob((LivingEntity)null);
         }
      }
      
      this.tickEffects();
      this.animStepO = this.animStep;
      this.yBodyRotO = this.yBodyRot;
      this.yHeadRotO = this.yHeadRot;
      this.yRotO = this.getYRot();
      this.xRotO = this.getXRot();
      this.level().getProfiler().pop();
      
      ci.cancel();
   }*/
   
   @Inject(method = {"getMaxHealth"}, at = {@At("HEAD")}, cancellable = true)
   public void getDragonMaxHealth(CallbackInfoReturnable<Float> ci) {
      LivingEntity entity = ((LivingEntity)(Object)this);
      if (entity instanceof EnderLord) {
         ci.setReturnValue((float)ModCommonConfig.ENDER_LORD_MAX_HEALTH.get());
      }
      
      if (entity instanceof BlaznanaShulkerTrick) {
         ci.setReturnValue(5750056.0F * 2.0F);
      }
   }
   
   @Inject(method = {"checkTotemDeathProtection"}, at = {@At("HEAD")}, cancellable = true)
   private void checkTotemDeathProtection(DamageSource p_21263_, CallbackInfoReturnable<Boolean> ci) {
      LivingEntity entity = ((LivingEntity)(Object)this);
      
      if (entity.hasEffect(EndlessDeepSpaceModMobEffects.TOTEM_OF_UNDYING.get())) {
         int amplifier = Objects.requireNonNull(entity.getEffect(EndlessDeepSpaceModMobEffects.TOTEM_OF_UNDYING.get())).getAmplifier();
         int duration = Objects.requireNonNull(entity.getEffect(EndlessDeepSpaceModMobEffects.TOTEM_OF_UNDYING.get())).getDuration();
         if (entity instanceof ServerPlayer serverplayer) {
            CriteriaTriggers.USED_TOTEM.trigger(serverplayer, new ItemStack(Items.TOTEM_OF_UNDYING));
         }
         entity.setHealth(amplifier + 1);
         entity.removeAllEffects();
         if (amplifier - 1 >= 0) {
            entity.addEffect(new MobEffectInstance(EndlessDeepSpaceModMobEffects.TOTEM_OF_UNDYING.get(), duration, amplifier - 1));
         }
         entity.addEffect(new MobEffectInstance(ModMobEffects.DAMAGE_REDUCTION.get(), 200, Math.max(0, amplifier - 1)));
         entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
         entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
         entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
         entity.level().broadcastEntityEvent(entity, (byte) -2);
         
         ci.setReturnValue(true);
      } else {
         if ((entity.getCapability(EndlessDeepSpaceModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new EndlessDeepSpaceModVariables.PlayerVariables())).Invincible || entity.hasEffect(EndlessDeepSpaceModMobEffects.INVINCIBLE.get())) {
            entity.setHealth(entity.getMaxHealth());
            entity.level().broadcastEntityEvent(entity, (byte) -7);
            ci.setReturnValue(true);
         }
         
         if (p_21263_.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            ci.setReturnValue(false);
         } else {
            ItemStack itemstack = null;
            
            for (InteractionHand interactionhand : InteractionHand.values()) {
               ItemStack itemstack1 = entity.getItemInHand(interactionhand);
               if (itemstack1.is(Items.TOTEM_OF_UNDYING) && net.minecraftforge.common.ForgeHooks.onLivingUseTotem(entity, p_21263_, itemstack1, interactionhand)) {
                  itemstack = itemstack1.copy();
                  itemstack1.shrink(1);
                  break;
               }
            }
            
            if (itemstack != null) {
               if (entity instanceof ServerPlayer) {
                  ServerPlayer serverplayer = (ServerPlayer) entity;
                  serverplayer.awardStat(Stats.ITEM_USED.get(Items.TOTEM_OF_UNDYING), 1);
                  CriteriaTriggers.USED_TOTEM.trigger(serverplayer, itemstack);
               }
               
               entity.setHealth(1.0F);
               entity.removeAllEffects();
               entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
               entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
               entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
               entity.level().broadcastEntityEvent(entity, (byte) 35);
            }
            
            ci.setReturnValue(itemstack != null);
         }
      }
   }
}
