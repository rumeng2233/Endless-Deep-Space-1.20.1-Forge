package shirumengya.endless_deep_space.custom.entity.boss.oceandefenders;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.SwimNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shirumengya.endless_deep_space.custom.block.entity.GuidingStoneBlockEntity;
import shirumengya.endless_deep_space.custom.client.event.CustomServerBossEvent;
import shirumengya.endless_deep_space.custom.client.gui.screens.components.wheel.Color;
import shirumengya.endless_deep_space.custom.client.gui.screens.components.wheel.WheelConstants;
import shirumengya.endless_deep_space.custom.config.ModCommonConfig;
import shirumengya.endless_deep_space.custom.entity.ColorfulLightningBolt;
import shirumengya.endless_deep_space.custom.entity.ScreenShakeEntity;
import shirumengya.endless_deep_space.custom.entity.boss.ColoredEntityPart;
import shirumengya.endless_deep_space.custom.entity.boss.PartBoss;
import shirumengya.endless_deep_space.custom.entity.boss.enderlord.EnderLord;
import shirumengya.endless_deep_space.custom.entity.projectile.AbyssalTorpedo;
import shirumengya.endless_deep_space.custom.event.SwordBlockEvent;
import shirumengya.endless_deep_space.custom.init.ModAttributes;
import shirumengya.endless_deep_space.custom.init.ModEntities;
import shirumengya.endless_deep_space.custom.init.ModMobEffects;
import shirumengya.endless_deep_space.custom.networking.ModMessages;
import shirumengya.endless_deep_space.custom.networking.packet.DeleteEntityS2CPacket;
import shirumengya.endless_deep_space.custom.networking.packet.JumpStringS2CPacket;
import shirumengya.endless_deep_space.custom.networking.packet.UpdateAttritionS2CPacket;
import shirumengya.endless_deep_space.custom.networking.packet.UpdateIsDyingS2CPacket;
import shirumengya.endless_deep_space.custom.util.entity.TrackingUtil;
import shirumengya.endless_deep_space.custom.util.java.color.RGBtoTen;
import shirumengya.endless_deep_space.custom.world.data.ModWorldData;
import shirumengya.endless_deep_space.init.EndlessDeepSpaceModGameRules;
import shirumengya.endless_deep_space.mixins.EntityAccessor;
import shirumengya.endless_deep_space.mixins.LivingEntityInvoker;

import javax.annotation.Nullable;
import java.util.*;

public class OceanDefender extends PartBoss implements Enemy, RangedAttackMob {
   private static final TargetingConditions NEW_TARGET_TARGETING = TargetingConditions.forCombat().selector((livingEntity) -> {
      return !(livingEntity instanceof OceanDefender) && !(livingEntity instanceof Guardian) && !(livingEntity instanceof Drowned);
   }).ignoreLineOfSight().range(400);
   public static final EntityDimensions OceanDefenderSize = EntityDimensions.scalable(0.85F, 0.85F);
   public static final float ELDER_SIZE_SCALE = 1.9975F / 0.85F;
   private static final int EFFECT_INTERVAL = 1200;
   private static final int EFFECT_RADIUS = 50;
   private static final int EFFECT_DURATION = 6000;
   private static final int EFFECT_AMPLIFIER = 2;
   private static final int EFFECT_DISPLAY_LIMIT = 1200;
   private static final EntityDataAccessor<Boolean> DATA_ID_MOVING = SynchedEntityData.defineId(OceanDefender.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Integer> DATA_ID_ATTACK_TARGET = SynchedEntityData.defineId(OceanDefender.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> DATA_PHASE = SynchedEntityData.defineId(OceanDefender.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> DATA_ID_OTHER_OCEAN_DEFENDER = SynchedEntityData.defineId(OceanDefender.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Boolean> DATA_TYPE_ONE = SynchedEntityData.defineId(OceanDefender.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> DATA_DYING = SynchedEntityData.defineId(OceanDefender.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> DATA_PROGRESS_TWO = SynchedEntityData.defineId(OceanDefender.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Integer> DATA_CHARGING_TIMER = SynchedEntityData.defineId(OceanDefender.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> DATA_ATTACK_TARGET = SynchedEntityData.defineId(OceanDefender.class, EntityDataSerializers.INT);
   private static final Logger log = LoggerFactory.getLogger(OceanDefender.class);
   public int oceanDefenderChargingTime;
   public int oceanDefenderDeathTime;
   private float clientSideTailAnimation;
   private float clientSideTailAnimationO;
   private float clientSideTailAnimationSpeed;
   private float clientSideSpikesAnimation;
   private float clientSideSpikesAnimationO;
   @Nullable
   private LivingEntity clientSideCachedAttackTarget;
   private int clientSideAttackTime;
   private boolean clientSideTouchedGround;
   @Nullable
   protected RandomStrollGoal randomStrollGoal;
   @Nullable
   public UUID otherOceanDefenderUUID;
   @Nullable
   public OceanDefender otherOceanDefender;
   @Nullable
   public OceanDefender clientSideOtherOceanDefender;
   @Nullable
   public Vec3 otherOceanDefenderPosition;
   @Nullable
   public ResourceKey<Level> otherOceanDefenderDimension;
   @Nullable
   public LivingEntity attackTarget;
   @Nullable
   public LivingEntity clientSideAttackTarget;
   public static final int PHASE_SHOOTING_ABYSSAL_TORPEDO = 3;
   public static final int PHASE_ATTRITION_SPRECHSTIMME = 2;
   public static final int PHASE_LASER_ATTACK = 1;
   public static final int PHASE_NORMAL = 0;
   public static final int PHASE_CHARGING = -1;
   public EntityDimensions size;
   public final OceanDefenderPart[] subEntities;
   public final OceanDefenderPart body;
   public final CustomServerBossEvent bossEvent = new CustomServerBossEvent(this, this.getDisplayName(), BossEvent.BossBarColor.BLUE, false, 13);
   public final CustomServerBossEvent oceanDefendersEvent = new CustomServerBossEvent(Component.translatable("entity.endless_deep_space.ocean_defenders.description"), Component.translatable("entity.endless_deep_space.ocean_defenders"), BossEvent.BossBarColor.BLUE, false, 14);
   public final CustomServerBossEvent oceanDefendersOtherEvent = new CustomServerBossEvent(Component.empty(), Component.empty(), BossEvent.BossBarColor.BLUE, false, 15);
   public final CustomServerBossEvent oceanDefenderChargingEvent = new CustomServerBossEvent(Component.empty(), Component.empty(), BossEvent.BossBarColor.BLUE, false, 16);
   
   
   public static final Map<LivingEntity, Float> livingEntityAttrition = new HashMap<>();
   public static final Map<LivingEntity, Integer> livingEntityAttritionTick = new HashMap<>();
   public static final Map<LivingEntity, Integer> livingEntityAttritionMaxTick = new HashMap<>();
   public static final Map<LivingEntity, Boolean> livingEntityIsDying = new HashMap<>();
   
   public static final Map<LivingEntity, Float> clientSideLivingEntityAttrition = new HashMap<>();
   public static final Map<LivingEntity, Integer> clientSideLivingEntityAttritionTick = new HashMap<>();
   public static final Map<LivingEntity, Integer> clientSideLivingEntityAttritionMaxTick = new HashMap<>();
   public static final Map<LivingEntity, Boolean> clientSideLivingEntityIsDying = new HashMap<>();
   
   public OceanDefender(EntityType<? extends OceanDefender> p_32460_, Level p_32461_) {
      super(p_32460_, p_32461_);
      this.noCulling = true;
      this.size = EntityDimensions.scalable(1.9975F * (this.isTypeOne() ? 4.0F : 2.0F), 1.9975F * (this.isTypeOne() ? 4.0F : 2.0F));
      this.refreshDimensions();
      this.body = new OceanDefenderPart(this, "body", this.size.width, this.size.height);
      this.subEntities = new OceanDefenderPart[]{this.body};
      this.xpReward = 0;
      this.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
      this.moveControl = new OceanDefender.OceanDefenderMoveControl(this);
      this.clientSideTailAnimation = this.random.nextFloat();
      this.clientSideTailAnimationO = this.clientSideTailAnimation;
      this.setId(ENTITY_COUNTER.getAndAdd(this.getSubEntities().length + 1) + 1); // Forge: Fix MC-158205: Make sure part ids are successors of parent mob id
   }
   
   @Override
   public EntityDimensions getDimensions(Pose p_21047_) {
      return OceanDefenderSize;
   }
   
   @Override
   protected void pushEntities() {
      if (this.level().isClientSide()) {
         this.level().getEntities(EntityTypeTest.forClass(Player.class), this.size.makeBoundingBox(this.position()), EntitySelector.pushableBy(this)).forEach(this::doPush);
      } else {
         List<Entity> list = this.level().getEntities(this, this.size.makeBoundingBox(this.position()), EntitySelector.pushableBy(this));
         if (!list.isEmpty()) {
            int i = this.level().getGameRules().getInt(GameRules.RULE_MAX_ENTITY_CRAMMING);
            if (i > 0 && list.size() > i - 1 && this.random.nextInt(4) == 0) {
               int j = 0;
               
               for(int k = 0; k < list.size(); ++k) {
                  if (!list.get(k).isPassenger()) {
                     ++j;
                  }
               }
               
               if (j > i - 1) {
                  this.hurt(this.damageSources().cramming(), 6.0F);
               }
            }
            
            for(int l = 0; l < list.size(); ++l) {
               Entity entity = list.get(l);
               this.doPush(entity);
            }
         }
         
      }
   }
   
   @Override
   protected void spawnSprintParticle() {
      BlockPos blockpos = this.getOnPosLegacy();
      BlockState blockstate = this.level().getBlockState(blockpos);
      if (!blockstate.addRunningEffects(this.level(), blockpos, this)) {
         if (blockstate.getRenderShape() != RenderShape.INVISIBLE) {
            Vec3 vec3 = this.getDeltaMovement();
            BlockPos blockpos1 = this.blockPosition();
            double d0 = this.getX() + (this.random.nextDouble() - 0.5D) * (double) this.size.width;
            double d1 = this.getZ() + (this.random.nextDouble() - 0.5D) * (double) this.size.width;
            if (blockpos1.getX() != blockpos.getX()) {
               d0 = Mth.clamp(d0, (double) blockpos.getX(), (double) blockpos.getX() + 1.0D);
            }
            
            if (blockpos1.getZ() != blockpos.getZ()) {
               d1 = Mth.clamp(d1, (double) blockpos.getZ(), (double) blockpos.getZ() + 1.0D);
            }
            
            this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockstate).setPos(blockpos), d0, this.getY() + 0.1D, d1, vec3.x * -4.0D, 1.5D, vec3.z * -4.0D);
         }
      }
   }
   
   @Override
   public double getPassengersRidingOffset() {
      return this.size.height + 1.0D;
   }
   
   @Override
   public boolean isInWall() {
      if (this.noPhysics) {
         return false;
      } else {
         float f = this.size.width * 0.8F;
         AABB aabb = AABB.ofSize(this.getEyePosition(), (double)f, 1.0E-6D, (double)f);
         return BlockPos.betweenClosedStream(aabb).anyMatch((p_201942_) -> {
            BlockState blockstate = this.level().getBlockState(p_201942_);
            return !blockstate.isAir() && blockstate.isSuffocating(this.level(), p_201942_) && Shapes.joinIsNotEmpty(blockstate.getCollisionShape(this.level(), p_201942_).move((double)p_201942_.getX(), (double)p_201942_.getY(), (double)p_201942_.getZ()), Shapes.create(aabb), BooleanOp.AND);
         });
      }
   }
   
   @Override
   protected void doWaterSplashEffect() {
      Entity entity = (Entity)(this.isVehicle() && this.getControllingPassenger() != null ? this.getControllingPassenger() : this);
      float f = entity == this ? 0.2F : 0.9F;
      Vec3 vec3 = entity.getDeltaMovement();
      float f1 = Math.min(1.0F, (float)Math.sqrt(vec3.x * vec3.x * (double)0.2F + vec3.y * vec3.y + vec3.z * vec3.z * (double)0.2F) * f);
      if (f1 < 0.25F) {
         this.playSound(this.getSwimSplashSound(), f1, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
      } else {
         this.playSound(this.getSwimHighSpeedSplashSound(), f1, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
      }
      
      float f2 = (float)Mth.floor(this.getY());
      
      for(int i = 0; (float)i < 1.0F + this.size.width * 20.0F; ++i) {
         double d0 = (this.random.nextDouble() * 2.0D - 1.0D) * (double)this.size.width;
         double d1 = (this.random.nextDouble() * 2.0D - 1.0D) * (double)this.size.width;
         this.level().addParticle(ParticleTypes.BUBBLE, this.getX() + d0, (double)(f2 + 1.0F), this.getZ() + d1, vec3.x, vec3.y - this.random.nextDouble() * (double)0.2F, vec3.z);
      }
      
      for(int j = 0; (float)j < 1.0F + this.size.width * 20.0F; ++j) {
         double d2 = (this.random.nextDouble() * 2.0D - 1.0D) * (double)this.size.width;
         double d3 = (this.random.nextDouble() * 2.0D - 1.0D) * (double)this.size.width;
         this.level().addParticle(ParticleTypes.SPLASH, this.getX() + d2, (double)(f2 + 1.0F), this.getZ() + d3, vec3.x, vec3.y, vec3.z);
      }
      
      this.gameEvent(GameEvent.SPLASH);
   }
   
   @Override
   public Vec3 getDeltaMovement() {
      if (this.getChargingTimer() >= 100 || this.isDeadOrDying()) {
         return Vec3.ZERO;
      }
      
      return super.getDeltaMovement();
   }
   
   protected void registerGoals() {
      MoveTowardsRestrictionGoal movetowardsrestrictiongoal = new MoveTowardsRestrictionGoal(this, 1.0D);
      this.randomStrollGoal = new RandomStrollGoal(this, 1.0D, 80);
      this.goalSelector.addGoal(2, new OceanDefender.OceanDefenderMeleeAttackGoal(this));
      this.goalSelector.addGoal(2, new OceanDefender.OceanDefenderTridentAttackGoal(this, 1.0D, 40, 20.0F));
      this.goalSelector.addGoal(2, new OceanDefender.OceanDefenderAttackGoal(this));
      this.goalSelector.addGoal(2, new OceanDefender.OceanDefenderAttritionSprechstimmeGoal(this));
      this.goalSelector.addGoal(2, new OceanDefender.OceanDefenderShootingAbyssalTorpedoGoal(this));
      this.goalSelector.addGoal(5, movetowardsrestrictiongoal);
      this.goalSelector.addGoal(4, new OceanDefender.OceanDefenderMoveToTargetGoal(this));
      this.goalSelector.addGoal(3, new OceanDefender.OceanDefenderMoveToOtherOceanDefenderGoal(this));
      this.goalSelector.addGoal(7, this.randomStrollGoal);
      this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
      this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, OceanDefender.class, 12.0F, 0.01F));
      this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
      this.randomStrollGoal.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
      movetowardsrestrictiongoal.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
      //this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 0, false, false, new OceanDefender.OceanDefenderAttackSelector(this)));
      this.targetSelector.addGoal(1, new OceanDefenderHurtByTargetGoal(this).setAlertOthers());
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Monster.createMonsterAttributes().add(Attributes.FOLLOW_RANGE, 256.0D).add(Attributes.MOVEMENT_SPEED, (double)0.6F).add(Attributes.ATTACK_DAMAGE, 4.0D).add(Attributes.MAX_HEALTH, 660.0D);
   }
   
   protected PathNavigation createNavigation(Level p_32846_) {
      return new OceanDefenderWaterBoundPathNavigation(this, p_32846_);
   }
   
   @Override
   protected void checkFallDamage(double p_20990_, boolean p_20991_, BlockState p_20992_, BlockPos p_20993_) {
   }
   
   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_ID_MOVING, false);
      this.entityData.define(DATA_ID_ATTACK_TARGET, 0);
      this.entityData.define(DATA_PHASE, 0);
      this.entityData.define(DATA_ID_OTHER_OCEAN_DEFENDER, 0);
      this.entityData.define(DATA_TYPE_ONE, true);
      this.entityData.define(DATA_DYING, false);
      this.entityData.define(DATA_PROGRESS_TWO, false);
      this.entityData.define(DATA_CHARGING_TIMER, 0);
      this.entityData.define(DATA_ATTACK_TARGET, 0);
   }
   
   @Override
   public void addAdditionalSaveData(CompoundTag p_21484_) {
      super.addAdditionalSaveData(p_21484_);
      p_21484_.putInt("Phase", this.getPhase());
      
      if (this.getOtherOceanDefender() != null && this.getOtherOceanDefenderUUID() != null && this.getOtherOceanDefenderPosition() != null && this.getOtherOceanDefenderDimension() != null) {
         ListTag otherOceanDefenderList = new ListTag();
         CompoundTag otherOceanDefenderTag = new CompoundTag();
         otherOceanDefenderTag.putUUID("UUID", this.getOtherOceanDefenderUUID());
         ListTag otherOceanDefenderPositionList = new ListTag();
         CompoundTag otherOceanDefenderPositionTag = new CompoundTag();
         otherOceanDefenderPositionTag.putDouble("X", this.getOtherOceanDefenderPosition().x);
         otherOceanDefenderPositionTag.putDouble("Y", this.getOtherOceanDefenderPosition().y);
         otherOceanDefenderPositionTag.putDouble("Z", this.getOtherOceanDefenderPosition().z);
         otherOceanDefenderPositionList.add(otherOceanDefenderPositionTag);
         otherOceanDefenderTag.put("Position", otherOceanDefenderPositionList);
         otherOceanDefenderTag.putString("Dimension", this.getOtherOceanDefenderDimension().location().toString());
         otherOceanDefenderList.add(otherOceanDefenderTag);
         p_21484_.put("OtherOceanDefender", otherOceanDefenderList);
      }
      
      p_21484_.putBoolean("Type", this.isTypeOne());
      p_21484_.putBoolean("Dying", this.isDying());
      p_21484_.putBoolean("ProgressTwo", this.isProgressTwo());
      p_21484_.putInt("OceanDefenderDeathTime", this.oceanDefenderDeathTime);
      p_21484_.putInt("ChargingTimer", this.getChargingTimer());
      p_21484_.putInt("ChargingTime", this.oceanDefenderChargingTime);
   }
   
   @Override
   public void readAdditionalSaveData(CompoundTag p_21450_) {
      super.readAdditionalSaveData(p_21450_);
      this.setPhase(p_21450_.getInt("Phase"));
      
      if (p_21450_.contains("OtherOceanDefender")) {
         ListTag otherOceanDefenderList = p_21450_.getList("OtherOceanDefender", 10);
         for (int i = 0; i < otherOceanDefenderList.size(); i++) {
            CompoundTag tag = otherOceanDefenderList.getCompound(i);
            this.setOtherOceanDefenderUUID(tag.getUUID("UUID"));
            ListTag otherOceanDefenderPositionList = tag.getList("Position", 10);
            for (int times = 0; times < otherOceanDefenderPositionList.size(); times++) {
               CompoundTag otherOceanDefenderPositionTag = otherOceanDefenderPositionList.getCompound(times);
               this.setOtherOceanDefenderPosition(new Vec3(otherOceanDefenderPositionTag.getDouble("X"), otherOceanDefenderPositionTag.getDouble("Y"), otherOceanDefenderPositionTag.getDouble("Z")));
            }
            this.setOtherOceanDefenderDimension(ResourceKey.create(Registries.DIMENSION, new ResourceLocation(tag.getString("Dimension"))));
         }
      }
      
      this.setTypeOne(p_21450_.getBoolean("Type"));
      this.setDying(p_21450_.getBoolean("Dying"));
      this.setProgressTwo(p_21450_.getBoolean("ProgressTwo"));
      if (p_21450_.contains("OceanDefenderDeathTime")) {
         this.oceanDefenderDeathTime = p_21450_.getInt("OceanDefenderDeathTime");
      }
      this.setChargingTimer(p_21450_.getInt("ChargingTimer"));
      if (p_21450_.contains("ChargingTime")) {
         this.oceanDefenderChargingTime = p_21450_.getInt("ChargingTime");
      }
   }
   
   @Nullable
   public ResourceKey<Level> getOtherOceanDefenderDimension() {
      return this.otherOceanDefenderDimension;
   }
   
   public void setOtherOceanDefenderDimension(@Nullable ResourceKey<Level> dimension) {
      this.otherOceanDefenderDimension = dimension;
   }
   
   @Nullable
   public Vec3 getOtherOceanDefenderPosition() {
      return this.otherOceanDefenderPosition;
   }
   
   public void setOtherOceanDefenderPosition(@Nullable Vec3 position) {
      this.otherOceanDefenderPosition = position;
   }
   
   public void setChargingTimer(int chargingTimer) {
      this.entityData.set(DATA_CHARGING_TIMER, Mth.clamp(chargingTimer, 0, 100));
   }
   
   public int getChargingTimer() {
      return this.entityData.get(DATA_CHARGING_TIMER);
   }
   
   public void setProgressTwo(boolean progressTwo) {
      this.entityData.set(DATA_PROGRESS_TWO, progressTwo);
   }
   
   public boolean isProgressTwo() {
      return this.entityData.get(DATA_PROGRESS_TWO);
   }
   
   public void setDying(boolean dying) {
      this.entityData.set(DATA_DYING, dying);
   }
   
   public boolean isDying() {
      return this.entityData.get(DATA_DYING);
   }
   
   public void setTypeOne(boolean type) {
      this.entityData.set(DATA_TYPE_ONE, type);
   }
   
   public boolean isTypeOne() {
      return this.entityData.get(DATA_TYPE_ONE);
   }
   
   @Override
   public void startSeenByPlayer(ServerPlayer p_20119_) {
      super.startSeenByPlayer(p_20119_);
      this.bossEvent.addPlayer(p_20119_);
      this.oceanDefendersEvent.addPlayer(p_20119_);
      this.oceanDefendersOtherEvent.addPlayer(p_20119_);
      this.oceanDefenderChargingEvent.addPlayer(p_20119_);
   }
   
   @Override
   public void stopSeenByPlayer(ServerPlayer p_20174_) {
      super.stopSeenByPlayer(p_20174_);
      this.bossEvent.removePlayer(p_20174_);
      this.oceanDefendersEvent.removePlayer(p_20174_);
      this.oceanDefendersOtherEvent.removePlayer(p_20174_);
      this.oceanDefenderChargingEvent.removePlayer(p_20174_);
   }
   
   @Override
   public int getTeamColor() {
      Color dangerous = WheelConstants.DEPLETED_1.blend(WheelConstants.DEPLETED_2, WheelConstants.cycle(System.currentTimeMillis(), WheelConstants.DEPLETED_BLINK));
      return this.getPhase() == PHASE_ATTRITION_SPRECHSTIMME ? RGBtoTen.OutputResult((int) (dangerous.red * 255), (int) (dangerous.green * 255), (int) (dangerous.blue * 255)) : super.getTeamColor();
   }
   
   @Nullable
   public OceanDefender getOtherOceanDefender() {
      if (!this.hasOtherOceanDefender()) {
         return null;
      } else if (this.level().isClientSide) {
         if (this.clientSideOtherOceanDefender != null) {
            return this.clientSideOtherOceanDefender;
         } else {
            Entity entity = this.level().getEntity(this.entityData.get(DATA_ID_OTHER_OCEAN_DEFENDER));
            if (entity instanceof OceanDefender defender && defender.isTypeOne() != this.isTypeOne()) {
               this.clientSideOtherOceanDefender = defender;
               return this.clientSideOtherOceanDefender;
            } else {
               return null;
            }
         }
      } else {
         return this.otherOceanDefender;
      }
   }
   
   public boolean hasOtherOceanDefender() {
      return this.entityData.get(DATA_ID_OTHER_OCEAN_DEFENDER) != 0;
   }
   
   public void setOtherOceanDefender(@Nullable OceanDefender defender) {
      if (defender == null) {
         this.otherOceanDefender = null;
         this.setOtherOceanDefenderUUID(null);
         this.entityData.set(DATA_ID_OTHER_OCEAN_DEFENDER, 0);
         this.setOtherOceanDefenderPosition(null);
         this.setOtherOceanDefenderDimension(null);
      } else if (defender.isTypeOne() != this.isTypeOne()) {
         this.otherOceanDefender = defender;
         this.setOtherOceanDefenderUUID(defender.getUUID());
         this.entityData.set(DATA_ID_OTHER_OCEAN_DEFENDER, defender.getId());
         this.setOtherOceanDefenderPosition(defender.position());
         this.setOtherOceanDefenderDimension(defender.level().dimension());
      }
   }
   
   @Nullable
   public UUID getOtherOceanDefenderUUID() {
      return this.otherOceanDefenderUUID;
   }
   
   public void setOtherOceanDefenderUUID(UUID uuid) {
      this.otherOceanDefenderUUID = uuid;
   }
   
   public void setPhase(int phase) {
      this.entityData.set(DATA_PHASE, phase);
   }
   
   public int getPhase() {
      return this.entityData.get(DATA_PHASE);
   }
   
   public boolean canBreatheUnderwater() {
      return true;
   }
   
   public MobType getMobType() {
      return MobType.WATER;
   }
   
   public boolean isMoving() {
      return this.entityData.get(DATA_ID_MOVING);
   }
   
   public void setMoving(boolean p_32862_) {
      this.entityData.set(DATA_ID_MOVING, p_32862_);
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
      
      if (DATA_ID_OTHER_OCEAN_DEFENDER.equals(p_32834_)) {
         this.clientSideOtherOceanDefender = null;
      }
      
      if (DATA_ATTACK_TARGET.equals(p_32834_)) {
         this.clientSideAttackTarget = null;
      }
      
      if (DATA_TYPE_ONE.equals(p_32834_)) {
         this.size = EntityDimensions.scalable(1.9975F * (this.isTypeOne() ? 4.0F : 2.0F), 1.9975F * (this.isTypeOne() ? 4.0F : 2.0F));
         this.refreshDimensions();
         this.body.setSize(this.size.width, this.size.height);
      }
   }
   
   public int getAmbientSoundInterval() {
      return 160;
   }

   public int getAttackDuration() {
      return 120;
   }

   protected SoundEvent getAmbientSound() {
      return this.isInWaterOrBubble() ? SoundEvents.ELDER_GUARDIAN_AMBIENT : SoundEvents.ELDER_GUARDIAN_AMBIENT_LAND;
   }

   protected SoundEvent getHurtSound(DamageSource p_32468_) {
      return this.isInWaterOrBubble() ? SoundEvents.ELDER_GUARDIAN_HURT : SoundEvents.ELDER_GUARDIAN_HURT_LAND;
   }
   
   @Override
   protected float getSoundVolume() {
      return 5.0F;
   }
   
   protected SoundEvent getFlopSound() {
      return SoundEvents.ELDER_GUARDIAN_FLOP;
   }
   
   protected Entity.MovementEmission getMovementEmission() {
      return MovementEmission.EVENTS;
   }
   
   protected float getStandingEyeHeight(Pose p_32843_, EntityDimensions p_32844_) {
      return this.size != null ? this.size.height * 0.5F : p_32844_.height * 0.5F;
   }
   
   public float getWalkTargetValue(BlockPos p_32831_, LevelReader p_32832_) {
      return p_32832_.getFluidState(p_32831_).is(FluidTags.WATER) ? 10.0F + p_32832_.getPathfindingCostFromLightLevels(p_32831_) : super.getWalkTargetValue(p_32831_, p_32832_);
   }
   
   public void aiStep() {
      if (this.isAlive()) {
         if (!this.level().isClientSide) {
            if (this.getTarget() != null && (!this.getTarget().isAlive() || this.getTarget().isDeadOrDying() || this.getTarget().isRemoved())) {
               this.setTarget(null);
            }
            
            if (this.level().getDifficulty() == Difficulty.PEACEFUL) {
               this.setTarget(null);
               if (this.getOtherOceanDefender() != null) {
                  this.getOtherOceanDefender().setTarget(null);
               }
            } else {
               if (this.getTarget() == null || !this.getTarget().isAlive()) {
                  LivingEntity entity = this.level().getNearestEntity(LivingEntity.class, NEW_TARGET_TARGETING, this, this.getX(), this.getY(), this.getZ(), new AABB(this.position(), this.position()).inflate(400 / 2d));
                  if (entity != null) {
                     this.setTarget(entity);
                     if (this.getOtherOceanDefender() != null) {
                        if (this.getOtherOceanDefender().getTarget() == null || !this.getTarget().isAlive()) {
                           this.getOtherOceanDefender().setTarget(entity);
                        }
                     }
                  }
               }
            }
            
            if (this.tickCount % 20 == 0) {
               if (this.getTarget() != null) {
                  if (this.getPhase() == PHASE_NORMAL) {
                     if (this.getRandom().nextFloat() < 0.3F) {
                        if (this.getTarget() instanceof Player) {
                           if (this.distanceTo(this.getTarget()) < 40) {
                              if (this.getOtherOceanDefender() != null) {
                                 if (this.getOtherOceanDefender().getPhase() != PHASE_LASER_ATTACK) {
                                    this.setPhase(PHASE_LASER_ATTACK);
                                 }
                              } else {
                                 this.setPhase(PHASE_LASER_ATTACK);
                              }
                           }
                        } else {
                           if (this.getOtherOceanDefender() != null) {
                              if (this.getOtherOceanDefender().getPhase() != PHASE_LASER_ATTACK) {
                                 this.setPhase(PHASE_LASER_ATTACK);
                              }
                           } else {
                              this.setPhase(PHASE_LASER_ATTACK);
                           }
                        }
                     } else if (this.getRandom().nextFloat() < 0.3F) {
                        if (this.distanceTo(this.getTarget()) < 20 || !(this.getTarget() instanceof Player)) {
                           this.setPhase(PHASE_ATTRITION_SPRECHSTIMME);
                        }
                     } else if (this.getRandom().nextFloat() < 0.3F) {
                        if (this.distanceTo(this.getTarget()) < 40 || !(this.getTarget() instanceof Player)) {
                           if (this.getOtherOceanDefender() != null) {
                              if (this.getOtherOceanDefender().getPhase() == PHASE_NORMAL) {
                                 this.setPhase(PHASE_SHOOTING_ABYSSAL_TORPEDO);
                                 this.getOtherOceanDefender().setPhase(PHASE_SHOOTING_ABYSSAL_TORPEDO);
                              }
                           } else {
                              this.setPhase(PHASE_SHOOTING_ABYSSAL_TORPEDO);
                           }
                        }
                     }
                  }
               } else {
                  this.setPhase(PHASE_NORMAL);
               }
            }
            
            if (this.getTarget() != null) {
               if (this.tickCount % 200 == 0) {
                  if (!this.isProgressTwo() && this.isInWaterOrBubble()) {
                     List<Drowned> drownedList = this.level().getEntitiesOfClass(Drowned.class, new AABB(this.position(), this.position()).inflate(400 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(this.position()))).toList();
                     if (drownedList.size() < 10 * Math.max(1, this.level().getDifficulty().getId()) && this.level() instanceof ServerLevel serverLevel && serverLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
                        this.playSound(SoundEvents.EVOKER_PREPARE_SUMMON, this.getSoundVolume(), this.getVoicePitch());
                        for (int i = 0; i < 3; ++i) {
                           BlockPos blockpos = this.blockPosition().offset(-2 + this.random.nextInt(5), 1, -2 + this.random.nextInt(5));
                           Drowned drowned = EntityType.DROWNED.create(serverLevel);
                           if (drowned != null) {
                              Objects.requireNonNull(drowned.getAttribute(ModAttributes.DAMAGE_REDUCTION.get())).setBaseValue(-6.0D);
                              drowned.moveTo(blockpos, 0.0F, 0.0F);
                              drowned.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(blockpos), MobSpawnType.MOB_SUMMONED, null, null);
                              serverLevel.addFreshEntity(drowned);
                              drowned.setTarget(this.getTarget());
                           }
                        }
                     }
                     
                     if (!drownedList.isEmpty()) {
                        for (Drowned drowned : drownedList) {
                           drowned.setTarget(this.getTarget());
                        }
                     }
                  } else {
                     List<Guardian> guardianList = this.level().getEntitiesOfClass(Guardian.class, new AABB(this.position(), this.position()).inflate(400 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(this.position()))).toList();
                     if (guardianList.size() < 10 * Math.max(1, this.level().getDifficulty().getId()) && this.level() instanceof ServerLevel serverLevel && serverLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
                        this.playSound(SoundEvents.EVOKER_PREPARE_SUMMON, this.getSoundVolume(), this.getVoicePitch());
                        for (int i = 0; i < 3; ++i) {
                           BlockPos blockpos = this.blockPosition().offset(-2 + this.random.nextInt(5), 1, -2 + this.random.nextInt(5));
                           Guardian guardian = EntityType.GUARDIAN.create(serverLevel);
                           if (this.getRandom().nextFloat() < 0.1F) {
                              guardian = EntityType.ELDER_GUARDIAN.create(serverLevel);
                              if (guardian != null) {
                                 Objects.requireNonNull(guardian.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(40.0D);
                              }
                           }
                           if (guardian != null) {
                              Objects.requireNonNull(guardian.getAttribute(ModAttributes.DAMAGE_REDUCTION.get())).setBaseValue(-6.0D);
                              guardian.moveTo(blockpos, 0.0F, 0.0F);
                              guardian.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(blockpos), MobSpawnType.MOB_SUMMONED, null, null);
                              serverLevel.addFreshEntity(guardian);
                              guardian.setTarget(this.getTarget());
                           }
                        }
                     }
                     
                     if (!guardianList.isEmpty()) {
                        for (Guardian guardian : guardianList) {
                           guardian.setTarget(this.getTarget());
                        }
                     }
                  }
               }
            }
         }
         
         if (this.isInWaterOrBubble()) {
            this.setAirSupply(300);
         } else if (this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().add((double)((this.random.nextFloat() * 2.0F - 1.0F) * 0.4F), 0.5D, (double)((this.random.nextFloat() * 2.0F - 1.0F) * 0.4F)));
            this.setYRot(this.random.nextFloat() * 360.0F);
            this.setOnGround(false);
            this.hasImpulse = true;
         }
         
         if (this.hasAttackTarget()) {
            this.setYRot(this.yHeadRot);
         }
      }
      
      super.aiStep();
   }
   
   @Override
   protected void tickDeath() {
      ++this.oceanDefenderDeathTime;
      
      if (this.oceanDefenderDeathTime == 1) {
         this.playSound(this.isInWaterOrBubble() ? SoundEvents.ELDER_GUARDIAN_DEATH : SoundEvents.ELDER_GUARDIAN_DEATH_LAND, this.getSoundVolume(), this.getVoicePitch());
      }
      
      if (this.level() instanceof ServerLevel) {
         this.setPhase(PHASE_NORMAL);
         this.setChargingTimer(0);
         TrackingUtil.forceEffect(this, LivingEntity.class, true, 200, (double) this.oceanDefenderDeathTime / 9, 200.0D);
         List<Entity> _entfound = this.level().getEntitiesOfClass(Entity.class, new AABB(this.position(), this.position()).inflate(400 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(this.position()))).toList();
         for (Entity entityiterator : _entfound) {
            Entity entity = (Entity) entityiterator;
            if (!(entity instanceof ColorfulLightningBolt)) {
               if (this.tickCount % 10 == 0) {
                  if (_entfound.size() <= 40) {
                     for (int l = 0; l < 4; l++){
                        if (ModCommonConfig.ENDER_LORD_STRIKE_LIGHTNING_UPON_DEATH.get()) {
                           ColorfulLightningBolt lightningBolt = new ColorfulLightningBolt(this.level(), entityiterator.getX(), entityiterator.getY(), entityiterator.getZ(), 0.0F, true);
                           this.level().addFreshEntity(lightningBolt);
                        }
                     }
                  }
                  
                  if (ModCommonConfig.ENDER_LORD_STRIKE_LIGHTNING_UPON_DEATH.get()) {
                     ColorfulLightningBolt lightningBolt = new ColorfulLightningBolt(this.level(), entityiterator.getX(), entityiterator.getY(), entityiterator.getZ(), 0.0F, true);
                     this.level().addFreshEntity(lightningBolt);
                  }
                  
                  if (entity instanceof LivingEntity && !(entity instanceof Player) && !(entity instanceof OceanDefender)) {
                     LivingEntity livingentity = (LivingEntity) entity;
                     for (int j = 0; j < 20; j++) {
                        livingentity.invulnerableTime = 0;
                        livingentity.hurt(EnderLord.enderLordAttack(this), Float.MAX_VALUE);
                        livingentity.invulnerableTime = 0;
                        OceanDefender.actuallyHurt(livingentity, EnderLord.enderLordAttack(this), Float.MAX_VALUE, true);
                        livingentity.removeAllEffects();
                        livingentity.setHealth(0);
                        OceanDefender.setIsDying(livingentity, true);
                        livingentity.die(EnderLord.enderLordAttack(this));
                     }
                  } else if (entity instanceof LivingEntity livingEntity && !(livingEntity instanceof OceanDefender)) {
                     OceanDefender.addAttrition(livingEntity, 0.01F);
                     if (this.oceanDefenderDeathTime >= 1000) {
                        if (livingEntity instanceof Player player) {
                           if (!player.isCreative()) {
                              SwordBlockEvent.setVertigoTime(player, Math.max(80, SwordBlockEvent.getVertigoTime(player)));
                           }
                        } else {
                           SwordBlockEvent.setVertigoTime(livingEntity, Math.max(80, SwordBlockEvent.getVertigoTime(livingEntity)));
                        }
                     }
                  }
               }
            }
         }
      }
      
      boolean flag = this.level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT);
      int i = 1000000;
      if (this.oceanDefenderDeathTime == 1800 && this.level() instanceof ServerLevel) {
         if (flag) {
            int award = net.minecraftforge.event.ForgeEventFactory.getExperienceDrop(this, this.lastHurtByPlayer, i);
            ExperienceOrb.award((ServerLevel) this.level(), this.position(), award);
            this.dropAllDeathLoot(this.getLastDamageSource() == null ? EnderLord.enderLordAttack(this) : this.getLastDamageSource());
         }
         
         List<LivingEntity> _entfound = this.level().getEntitiesOfClass(LivingEntity.class, new AABB(this.position(), this.position()).inflate(400 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(this.position()))).toList();
         for (LivingEntity entityiterator : _entfound) {
            if (!(entityiterator instanceof Player) && !(entityiterator instanceof PartBoss)) {
               ModMessages.sendToAllPlayers(new DeleteEntityS2CPacket(entityiterator.getId()));
               entityiterator.hurt(EnderLord.enderLordAttack(this), Float.MAX_VALUE);
               OceanDefender.actuallyHurt(entityiterator, EnderLord.enderLordAttack(this), Float.MAX_VALUE, true);
               OceanDefender.setIsDying(entityiterator, true);
               entityiterator.die(EnderLord.enderLordAttack(this));
               entityiterator.setHealth(Float.MIN_VALUE);
               entityiterator.setRemoved(RemovalReason.DISCARDED);
               entityiterator.canUpdate(false);
               entityiterator.gameEvent(GameEvent.ENTITY_DIE);
               //entityiterator.setPos(new Vec3(Double.NaN, Double.NaN, Double.NEGATIVE_INFINITY));
               entityiterator.setRemoved(RemovalReason.UNLOADED_TO_CHUNK);
               entityiterator.setLevelCallback(EntityInLevelCallback.NULL);
               entityiterator.tickCount = Integer.MIN_VALUE;
               entityiterator.setDeltaMovement(new Vec3(Double.NaN, Double.NaN, Double.NEGATIVE_INFINITY));
               entityiterator.setBoundingBox(new AABB(Vec3.ZERO, Vec3.ZERO));
               entityiterator.deathTime = Integer.MAX_VALUE;
               ((EntityAccessor)entityiterator).setBlockPosition(new BlockPos(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
               ((EntityAccessor)entityiterator).setChunkPosition(new ChunkPos(Integer.MAX_VALUE, Integer.MIN_VALUE));
               ((EntityAccessor)entityiterator).setDeltaMovement(new Vec3(Double.NaN, Double.NaN, Double.NEGATIVE_INFINITY));
               ((EntityAccessor)entityiterator).setPosition(new Vec3(Double.NaN, Double.NaN, Double.NEGATIVE_INFINITY));
               ((EntityAccessor)entityiterator).setRemovalReason(RemovalReason.UNLOADED_TO_CHUNK);
               entityiterator.invalidateCaps();
            }
         }
         
         this.remove(Entity.RemovalReason.KILLED);
         this.gameEvent(GameEvent.ENTITY_DIE);
      }
   }
   
   public boolean hasAttackTarget() {
      return this.entityData.get(DATA_ATTACK_TARGET) != 0;
   }
   
   @Nullable
   public LivingEntity getAttackTarget() {
      if (!this.hasAttackTarget()) {
         return null;
      } else if (this.level().isClientSide) {
         if (this.clientSideAttackTarget != null) {
            return this.clientSideAttackTarget;
         } else {
            Entity entity = this.level().getEntity(this.entityData.get(DATA_ATTACK_TARGET));
            if (entity instanceof LivingEntity) {
               this.clientSideAttackTarget = (LivingEntity)entity;
               return this.clientSideAttackTarget;
            } else {
               return null;
            }
         }
      } else {
         return this.getTarget();
      }
   }
   
   public void setAttackTarget(int p_32818_) {
      this.entityData.set(DATA_ATTACK_TARGET, p_32818_);
   }
   
   @Override
   public @org.jetbrains.annotations.Nullable LivingEntity getTarget() {
      return this.attackTarget;
   }
   
   @Override
   public void setTarget(@org.jetbrains.annotations.Nullable LivingEntity p_21544_) {
      if (!(p_21544_ instanceof OceanDefender) && !(p_21544_ instanceof Guardian) && !(p_21544_ instanceof Drowned)) {
         this.attackTarget = p_21544_;
         if (this.attackTarget != null) {
            this.setAttackTarget(this.attackTarget.getId());
         } else {
            this.setAttackTarget(0);
         }
      }
   }
   
   @Override
   public double getX(double p_20166_) {
      return this.position().x + (double)this.size.width * p_20166_;
   }
   
   @Override
   public double getY(double p_20166_) {
      return this.position().y + (double)this.size.height * p_20166_;
   }
   
   @Override
   public double getZ(double p_20247_) {
      return this.position().z + (double)this.size.width * p_20247_;
   }
   
   @Override
   public void setPos(double p_20210_, double p_20211_, double p_20212_) {
      super.setPos(p_20210_, p_20211_, p_20212_);
      if (this.level() instanceof ServerLevel level) {
         ChunkPos chunkPos = level.getChunkAt(this.blockPosition()).getPos();
         level.getChunkSource().addRegionTicket(PartBoss.BOSS_LOAD, chunkPos, GuidingStoneBlockEntity.TICKET_TYPE_MAX_DIFFUSIBLE_VALUE - level.getGameRules().getInt(EndlessDeepSpaceModGameRules.BOSS_LOAD_TICKET_LEVEL), this.blockPosition());
         if (!level.isLoaded(this.blockPosition())) {
            level.getChunkAt(this.blockPosition()).setLoaded(true);
         }
         level.resetEmptyTime();
         
         ModWorldData.WorldVariables worldData = ModWorldData.WorldVariables.get(level);
         if (worldData.getBossPos(this.getUUID()) == null) {
            worldData.addBossPos(this.getUUID(), this.blockPosition());
            worldData.syncData(level);
         } else if (worldData.getBossPos(this.getUUID()) != this.blockPosition()) {
            worldData.addBossPos(this.getUUID(), this.blockPosition());
            worldData.syncData(level);
         }
      }
   }
   
   @Override
   public boolean canUpdate() {
      return true;
   }
   
   @Override
   public void tick() {
      super.tick();
      if (this.level() instanceof ServerLevel level) {
         if (this.getOtherOceanDefenderPosition() != null && this.getOtherOceanDefenderDimension() != null) {
            ServerLevel serverLevel = level.getServer().getLevel(this.getOtherOceanDefenderDimension());
            if (serverLevel != null) {
               ChunkPos chunkPos = serverLevel.getChunkAt(BlockPos.containing(this.getOtherOceanDefenderPosition())).getPos();
               serverLevel.getChunkSource().addRegionTicket(PartBoss.BOSS_LOAD, chunkPos, GuidingStoneBlockEntity.TICKET_TYPE_MAX_DIFFUSIBLE_VALUE - serverLevel.getGameRules().getInt(EndlessDeepSpaceModGameRules.BOSS_LOAD_TICKET_LEVEL), BlockPos.containing(this.getOtherOceanDefenderPosition()));
               if (!serverLevel.isLoaded(BlockPos.containing(this.getOtherOceanDefenderPosition()))) {
                  serverLevel.getChunkAt(BlockPos.containing(this.getOtherOceanDefenderPosition())).setLoaded(true);
               }
               serverLevel.resetEmptyTime();
            }
         }
         ChunkPos chunkPos = level.getChunkAt(this.blockPosition()).getPos();
         level.getChunkSource().addRegionTicket(PartBoss.BOSS_LOAD, chunkPos, GuidingStoneBlockEntity.TICKET_TYPE_MAX_DIFFUSIBLE_VALUE - level.getGameRules().getInt(EndlessDeepSpaceModGameRules.BOSS_LOAD_TICKET_LEVEL), this.blockPosition());
         if (!level.isLoaded(this.blockPosition())) {
            level.getChunkAt(this.blockPosition()).setLoaded(true);
         }
         level.resetEmptyTime();
         
         ModWorldData.WorldVariables worldData = ModWorldData.WorldVariables.get(level);
         if (worldData.getBossPos(this.getUUID()) == null) {
            worldData.addBossPos(this.getUUID(), this.blockPosition());
            worldData.syncData(level);
         } else if (worldData.getBossPos(this.getUUID()) != this.blockPosition()) {
            worldData.addBossPos(this.getUUID(), this.blockPosition());
            worldData.syncData(level);
         }
         
         for (ServerLevel serverLevel : level.getServer().getAllLevels()) {
            if (this.getOtherOceanDefenderUUID() != null && serverLevel.getEntity(this.getOtherOceanDefenderUUID()) instanceof OceanDefender defender) {
               this.setOtherOceanDefender(defender);
            }
         }
         if (this.getOtherOceanDefender() != null && (this.getOtherOceanDefender().isRemoved() || this.getOtherOceanDefender().isTypeOne() == this.isTypeOne() || this.getOtherOceanDefender().getOtherOceanDefenderUUID() == null || !this.getOtherOceanDefender().getOtherOceanDefenderUUID().equals(this.getUUID()))) {
            this.setOtherOceanDefender(null);
         }
      }
      
      if (!this.level().isClientSide) {
         this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
         this.oceanDefendersEvent.setProgress(this.getHealth() / this.getMaxHealth());
         this.oceanDefenderChargingEvent.setProgress(this.getChargingTimer() / 100.0F);
         
         if (this.getTarget() != null) {
            if (this.tickCount % ((this.isProgressTwo() ? 20 : 40) / (this.level().getDifficulty() == Difficulty.HARD ? 2 : 1)) == 0) {
               this.setChargingTimer(this.getChargingTimer() + 1);
            }
         }
         
         if (this.getOtherOceanDefender() != null) {
            this.oceanDefendersOtherEvent.setProgress(this.getOtherOceanDefender().getHealth() / this.getOtherOceanDefender().getMaxHealth());
            if (this.isTypeOne()) {
               this.bossEvent.setVisible(false);
               this.oceanDefendersEvent.setVisible(true);
               this.oceanDefendersOtherEvent.setVisible(true);
               this.oceanDefenderChargingEvent.setVisible(true);
            } else {
               this.setChargingTimer(this.getOtherOceanDefender().getChargingTimer());
               this.bossEvent.setVisible(false);
               this.oceanDefendersEvent.setVisible(false);
               this.oceanDefendersOtherEvent.setVisible(false);
               this.oceanDefenderChargingEvent.setVisible(false);
            }
            
            if (this.isProgressTwo() && !this.getOtherOceanDefender().isProgressTwo()) {
               this.reallySetHealth(Math.max(this.getMaxHealth() / 2, this.getHealth()));
            }
            
            if (this.getOtherOceanDefenderPosition() != this.getOtherOceanDefender().position()) {
               this.setOtherOceanDefenderPosition(this.getOtherOceanDefender().position());
            }
            if (this.getOtherOceanDefenderDimension() != this.getOtherOceanDefender().level().dimension()) {
               this.setOtherOceanDefenderDimension(this.getOtherOceanDefender().level().dimension());
            }
         } else {
            this.bossEvent.setVisible(true);
            this.oceanDefendersEvent.setVisible(false);
            this.oceanDefendersOtherEvent.setVisible(false);
            this.oceanDefenderChargingEvent.setVisible(true);
         }
         
         if (this.getHealth() < 0.0F || this.isDying() || Float.isNaN(this.getHealth())) {
            this.setHealth(0.0F);
         }
      } else {
         this.clientSideTailAnimationO = this.clientSideTailAnimation;
         if (this.isDeadOrDying()) {
            this.clientSideTailAnimationSpeed = 0.0F;
         } else if (!this.isInWater()) {
            this.clientSideTailAnimationSpeed = 2.0F;
            Vec3 vec3 = this.getDeltaMovement();
            if (vec3.y > 0.0D && this.clientSideTouchedGround && !this.isSilent()) {
               this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), this.getFlopSound(), this.getSoundSource(), this.getSoundVolume(), 1.0F, false);
            }
            
            this.clientSideTouchedGround = vec3.y < 0.0D && this.level().loadedAndEntityCanStandOn(this.blockPosition().below(), this);
         } else if (this.isMoving()) {
            if (this.clientSideTailAnimationSpeed < 0.5F) {
               this.clientSideTailAnimationSpeed = 4.0F;
            } else {
               this.clientSideTailAnimationSpeed += (0.5F - this.clientSideTailAnimationSpeed) * 0.1F;
            }
         } else {
            this.clientSideTailAnimationSpeed += (0.125F - this.clientSideTailAnimationSpeed) * 0.2F;
         }
         
         this.clientSideTailAnimation += this.clientSideTailAnimationSpeed;
         this.clientSideSpikesAnimationO = this.clientSideSpikesAnimation;
         if (!this.isInWaterOrBubble() || this.isDeadOrDying()) {
            this.clientSideSpikesAnimation = this.random.nextFloat();
         } else {
            this.clientSideSpikesAnimation += (1.0F - this.clientSideSpikesAnimation) * 0.06F;
         }
         
         if (this.isMoving() && this.isInWater()) {
            Vec3 vec31 = this.getViewVector(0.0F);
            
            for(int i = 0; i < 2; ++i) {
               this.level().addParticle(ParticleTypes.BUBBLE, this.getRandomX(0.5D) - vec31.x * 1.5D, this.getRandomY() - vec31.y * 1.5D, this.getRandomZ(0.5D) - vec31.z * 1.5D, 0.0D, 0.0D, 0.0D);
            }
         }
         
         if (this.hasActiveAttackTarget()) {
            if (this.clientSideAttackTime < this.getAttackDuration()) {
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
      
      if (this.getChargingTimer() >= 100) {
         this.setDeltaMovement(Vec3.ZERO);
         this.hasImpulse = true;
         
         if (!this.level().isClientSide) {
            this.setPhase(PHASE_CHARGING);
            TrackingUtil.forceEffect(this, Entity.class, true, 200, 200, 200);
            if (this.oceanDefenderChargingTime == 0 && (this.isTypeOne() || this.getOtherOceanDefender() == null)) {
               List<ServerPlayer> serverPlayerList = this.level().getEntitiesOfClass(ServerPlayer.class, new AABB(this.position(), this.position()).inflate(400 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(this.position()))).toList();
               for (ServerPlayer player : serverPlayerList) {
                  ModMessages.sendToPlayer(new JumpStringS2CPacket(Component.translatable(this.isInWaterRainOrBubble() ? "message.endless_deep_space.entity.endless_deep_space.ocean_defender.charging.water" : "message.endless_deep_space.entity.endless_deep_space.ocean_defender.charging.air"), 400, false, true), player);
               }
            }
         }
         ++this.oceanDefenderChargingTime;
         if (this.oceanDefenderChargingTime >= 200) {
            this.oceanDefenderChargingTime = 0;
            if (!this.level().isClientSide) {
               if (this.getOtherOceanDefender() != null) {
                  this.setPhase(PHASE_NORMAL);
                  if (this.isTypeOne()) {
                     ScreenShakeEntity.ScreenShake(this.level(), this.position(), 100.0F, 100.0F / 600.0F, 140, 40);
                     this.setChargingTimer(0);
                  } else {
                     this.getOtherOceanDefender().setChargingTimer(0);
                  }
                  List<Entity> _entfound = this.level().getEntitiesOfClass(Entity.class, new AABB(this.position(), this.position()).inflate(400 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(this.position()))).toList();
                  for (Entity entityiterator : _entfound) {
                     if (!(entityiterator instanceof ColorfulLightningBolt) && !(entityiterator instanceof OceanDefender) && !(entityiterator instanceof OceanDefenderPart)) {
                        if (entityiterator instanceof LivingEntity livingentity) {
                           if (livingentity instanceof Player player) {
                              if (this.isTypeOne() && !player.isCreative()) {
                                 if (player.isInWaterRainOrBubble() == this.isInWaterRainOrBubble()) {
                                    OceanDefender.actuallyHurt(player, EnderLord.enderLordAttack(this), Float.MAX_VALUE, true);
                                    player.invulnerableTime = 0;
                                    player.removeAllEffects();
                                    player.setHealth(0);
                                    OceanDefender.addAttrition(player, 1000.0F);
                                 } else {
                                    player.hurt(this.damageSources().indirectMagic(this, this), 2.0F);
                                    OceanDefender.addAttrition(player, 0.75F + (this.level().getDifficulty() == Difficulty.HARD ? 0.15F : 0.0F));
                                 }
                              }
                           } else {
                              for (int i = 0; i < 20; i++) {
                                 OceanDefender.actuallyHurt(livingentity, EnderLord.enderLordAttack(this), Float.MAX_VALUE, true);
                                 livingentity.invulnerableTime = 0;
                                 livingentity.removeAllEffects();
                                 livingentity.setHealth(0);
                                 OceanDefender.setIsDying(livingentity, true);
                                 livingentity.die(EnderLord.enderLordAttack(this));
                                 OceanDefender.addAttrition(livingentity, 1000.0F);
                              }
                           }
                        } else {
                           OceanDefender.actuallyHurt(entityiterator, EnderLord.enderLordAttack(this), Float.MAX_VALUE, true);
                        }
                        this.doEnchantDamageEffects(this, entityiterator);
                        
                        ColorfulLightningBolt lightningBolt = new ColorfulLightningBolt(this.level(), entityiterator.getX(), entityiterator.getY(), entityiterator.getZ(), 1.0F, true);
                        this.level().addFreshEntity(lightningBolt);
                     }
                  }
                  if (this.getTarget() != null && !(this.getTarget() instanceof Player)) {
                     for (int i = 0; i < 20; i++) {
                        OceanDefender.actuallyHurt(this.getTarget(), EnderLord.enderLordAttack(this), Float.MAX_VALUE, true);
                        this.getTarget().invulnerableTime = 0;
                        this.getTarget().removeAllEffects();
                        this.getTarget().setHealth(0);
                        OceanDefender.setIsDying(this.getTarget(), true);
                        this.getTarget().die(EnderLord.enderLordAttack(this));
                        OceanDefender.addAttrition(this.getTarget(), 1000.0F);
                        this.doEnchantDamageEffects(this, this.getTarget());
                     }
                     ColorfulLightningBolt lightningBolt = new ColorfulLightningBolt(this.level(), this.getTarget().getX(), this.getTarget().getY(), this.getTarget().getZ(), 1.0F, true);
                     this.level().addFreshEntity(lightningBolt);
                  }
               } else {
                  this.setChargingTimer(0);
                  this.setPhase(PHASE_NORMAL);
                  ScreenShakeEntity.ScreenShake(this.level(), this.position(), 100.0F, 100.0F / 600.0F, 140, 40);
                  List<Entity> _entfound = this.level().getEntitiesOfClass(Entity.class, new AABB(this.position(), this.position()).inflate(400 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(this.position()))).toList();
                  for (Entity entityiterator : _entfound) {
                     if (!(entityiterator instanceof ColorfulLightningBolt) && !(entityiterator instanceof OceanDefender) && !(entityiterator instanceof OceanDefenderPart)) {
                        if (entityiterator instanceof LivingEntity livingentity) {
                           if (livingentity instanceof Player player) {
                              if (!player.isCreative()) {
                                 if (player.isInWaterRainOrBubble() == this.isInWaterRainOrBubble()) {
                                    OceanDefender.actuallyHurt(player, EnderLord.enderLordAttack(this), Float.MAX_VALUE, true);
                                    player.invulnerableTime = 0;
                                    player.removeAllEffects();
                                    player.setHealth(0);
                                    OceanDefender.addAttrition(player, 1000.0F);
                                 } else {
                                    player.hurt(this.damageSources().indirectMagic(this, this), 2.0F);
                                    OceanDefender.addAttrition(player, 0.75F + (this.level().getDifficulty() == Difficulty.HARD ? 0.15F : 0.0F));
                                 }
                              }
                           } else {
                              for (int i = 0; i < 20; i++) {
                                 OceanDefender.actuallyHurt(livingentity, EnderLord.enderLordAttack(this), Float.MAX_VALUE, true);
                                 livingentity.invulnerableTime = 0;
                                 livingentity.removeAllEffects();
                                 livingentity.setHealth(0);
                                 OceanDefender.setIsDying(livingentity, true);
                                 livingentity.die(EnderLord.enderLordAttack(this));
                                 OceanDefender.addAttrition(livingentity, 1000.0F);
                              }
                           }
                        } else {
                           OceanDefender.actuallyHurt(entityiterator, EnderLord.enderLordAttack(this), Float.MAX_VALUE, true);
                        }
                        this.doEnchantDamageEffects(this, entityiterator);
                        
                        ColorfulLightningBolt lightningBolt = new ColorfulLightningBolt(this.level(), entityiterator.getX(), entityiterator.getY(), entityiterator.getZ(), 1.0F, true);
                        this.level().addFreshEntity(lightningBolt);
                     }
                  }
                  if (this.getTarget() != null && !(this.getTarget() instanceof Player)) {
                     for (int i = 0; i < 20; i++) {
                        OceanDefender.actuallyHurt(this.getTarget(), EnderLord.enderLordAttack(this), Float.MAX_VALUE, true);
                        this.getTarget().invulnerableTime = 0;
                        this.getTarget().removeAllEffects();
                        this.getTarget().setHealth(0);
                        OceanDefender.setIsDying(this.getTarget(), true);
                        this.getTarget().die(EnderLord.enderLordAttack(this));
                        OceanDefender.addAttrition(this.getTarget(), 1000.0F);
                        this.doEnchantDamageEffects(this, this.getTarget());
                     }
                     ColorfulLightningBolt lightningBolt = new ColorfulLightningBolt(this.level(), this.getTarget().getX(), this.getTarget().getY(), this.getTarget().getZ(), 1.0F, true);
                     this.level().addFreshEntity(lightningBolt);
                  }
               }
            }
         }
      } else {
         this.oceanDefenderChargingTime = 0;
      }
      
      Vec3[] avec3 = new Vec3[this.subEntities.length];
      for(int j = 0; j < this.subEntities.length; ++j) {
         avec3[j] = new Vec3(this.subEntities[j].getX(), this.subEntities[j].getY(), this.subEntities[j].getZ());
      }
      this.tickPart(this.body, 0.0D, 0.0D, 0.0D);
      for(int l = 0; l < this.subEntities.length; ++l) {
         this.subEntities[l].xo = avec3[l].x;
         this.subEntities[l].yo = avec3[l].y;
         this.subEntities[l].zo = avec3[l].z;
         this.subEntities[l].xOld = avec3[l].x;
         this.subEntities[l].yOld = avec3[l].y;
         this.subEntities[l].zOld = avec3[l].z;
      }
   }
   
   @Override
   public void setHealth(float p_21154_) {
      if (!this.firstTick) {
         if (this.getChargingTimer() < 100) {
            if (this.getOtherOceanDefender() != null) {
               if (!this.isProgressTwo() && p_21154_ <= this.getMaxHealth() / 2) {
                  this.reallySetHealth(this.getMaxHealth() / 2);
                  this.setProgressTwo(true);
                  if (this.isTypeOne()) {
                     this.setChargingTimer(100);
                  } else {
                     this.getOtherOceanDefender().setChargingTimer(100);
                  }
               } else {
                  this.reallySetHealth(!this.getOtherOceanDefender().isProgressTwo() ? Math.max(this.getMaxHealth() / 2, p_21154_) : p_21154_);
               }
            } else {
               if (!this.isProgressTwo() && p_21154_ <= this.getMaxHealth() / 2) {
                  this.reallySetHealth(this.getMaxHealth() / 2);
                  this.setProgressTwo(true);
                  this.setChargingTimer(100);
               } else {
                  this.reallySetHealth(p_21154_);
               }
            }
         }
      } else {
         this.reallySetHealth(p_21154_);
      }
   }
   
   public void reallySetHealth(float p_21154_) {
      super.setHealth(p_21154_);
   }
   
   private void tickPart(OceanDefenderPart p_31116_, double p_31117_, double p_31118_, double p_31119_) {
      p_31116_.setPos(this.getX() + p_31117_, this.getY() + p_31118_, this.getZ() + p_31119_);
   }
   
   @Override
   public boolean isAlive() {
      if (this.getOtherOceanDefender() != null) {
         return !this.isRemoved() && (this.getHealth() > 0.0F || !this.isDying()) || !this.getOtherOceanDefender().isRemoved() && (this.getOtherOceanDefender().getHealth() > 0.0F || !this.getOtherOceanDefender().isDying());
      }
      return !this.isRemoved() && (this.getHealth() > 0.0F || !this.isDying());
   }
   
   @Override
   public boolean isDeadOrDying() {
      if (this.getOtherOceanDefender() != null) {
         return this.getHealth() <= 0.0F && this.getOtherOceanDefender().getHealth() <= 0.0F && this.isDying() && this.getOtherOceanDefender().isDying();
      }
      return this.getHealth() <= 0.0F && this.isDying();
   }
   
   @Override
   public void heal(float p_21116_) {
      if (this.getHealth() > 0.0F) {
         super.heal(p_21116_);
      }
   }
   
   public float getTailAnimation(float p_32864_) {
      return Mth.lerp(p_32864_, this.clientSideTailAnimationO, this.clientSideTailAnimation);
   }
   
   public float getSpikesAnimation(float p_32866_) {
      return Mth.lerp(p_32866_, this.clientSideSpikesAnimationO, this.clientSideSpikesAnimation);
   }
   
   public float getAttackAnimationScale(float p_32813_) {
      return ((float)this.clientSideAttackTime + p_32813_) / (float)this.getAttackDuration();
   }
   
   public float getClientSideAttackTime() {
      return (float)this.clientSideAttackTime;
   }
   
   public boolean checkSpawnObstruction(LevelReader p_32829_) {
      return p_32829_.isUnobstructed(this);
   }
   
   @Override
   public void kill() {
      super.kill();
      this.setDying(true);
      this.setHealth(0.0F);
   }
   
   @Override
   public boolean hurt(DamageSource p_21016_, float p_21017_) {
      return this.hurt(this.body, p_21016_, p_21017_);
   }
   
   public boolean hurt(OceanDefenderPart part, DamageSource p_32820_, float p_32821_) {
      if (this.level().isClientSide) {
         return false;
      } else if (!this.isDying() && this.getPhase() != PHASE_LASER_ATTACK && this.getChargingTimer() < 100) {
         if (p_32820_.getEntity() != null && this.distanceTo(p_32820_.getEntity()) < 40 && p_32820_.getEntity() == p_32820_.getDirectEntity()) {
            if (p_32820_.getEntity() instanceof Player player) {
               boolean flag = false;
               if (p_32821_ > 40) {
                  if (this.getOtherOceanDefender() != null) {
                     if (this.isTypeOne()) {
                        this.setChargingTimer((int) (this.getChargingTimer() + Math.floor(p_32821_ / this.getMaxHealth() * 100.0F)));
                     } else {
                        this.getOtherOceanDefender().setChargingTimer((int) (this.getOtherOceanDefender().getChargingTimer() + Math.floor(p_32821_ / this.getOtherOceanDefender().getMaxHealth() * 100.0F)));
                     }
                  } else {
                     this.setChargingTimer((int) (this.getChargingTimer() + Math.floor(p_32821_ / this.getMaxHealth() * 100.0F)));
                  }
                  OceanDefender.addAttrition(player, (float) Math.floor(p_32821_ / this.getMaxHealth() * 100.0F));
                  flag = true;
                  p_32821_ = 40;
               }
               
               if (!p_32820_.is(DamageTypes.THORNS)) {
                  Entity entity = p_32820_.getDirectEntity();
                  if (entity instanceof LivingEntity) {
                     LivingEntity livingentity = (LivingEntity) entity;
                     livingentity.hurt(this.damageSources().thorns(this), 4.0F);
                  }
               }
               
               if (this.randomStrollGoal != null) {
                  this.randomStrollGoal.trigger();
               }
               
               if (this.getOtherOceanDefender() != null) {
                  if (this.getHealth() - p_32821_ <= 0.0F && this.isProgressTwo() && this.getOtherOceanDefender().isProgressTwo()) {
                     super.hurt(p_32820_, p_32821_);
                     this.setHealth(0.0F);
                     this.setDying(true);
                     return true;
                  }
               } else {
                  if (this.getHealth() - p_32821_ <= 0.0F && this.isProgressTwo()) {
                     super.hurt(p_32820_, p_32821_);
                     this.setHealth(0.0F);
                     this.setDying(true);
                     return true;
                  }
               }
               
               if (this.getPhase() == PHASE_ATTRITION_SPRECHSTIMME && p_32821_ >= this.getMaxHealth() / 100.0F) {
                  this.setPhase(PHASE_NORMAL);
                  p_32821_ = p_32821_ * 1.5F;
               }
               
               boolean damaged = super.hurt(p_32820_, p_32821_);
               if (flag && !player.isCreative()) {
                  SwordBlockEvent.addVertigoTime(player, 40);
               }
               return damaged;
            } else if (!(p_32820_.getEntity() instanceof OceanDefender) && !(p_32820_.getEntity() instanceof Guardian) && !(p_32820_.getEntity() instanceof Drowned)) {
               boolean flag = false;
               if (p_32821_ > 40) {
                  if (this.getOtherOceanDefender() != null) {
                     if (this.isTypeOne()) {
                        this.setChargingTimer((int) (this.getChargingTimer() + Math.floor(p_32821_ / this.getMaxHealth() * 100.0F)));
                     } else {
                        this.getOtherOceanDefender().setChargingTimer((int) (this.getOtherOceanDefender().getChargingTimer() + Math.floor(p_32821_ / this.getOtherOceanDefender().getMaxHealth() * 100.0F)));
                     }
                  } else {
                     this.setChargingTimer((int) (this.getChargingTimer() + Math.floor(p_32821_ / this.getMaxHealth() * 100.0F)));
                  }
                  if (p_32820_.getEntity() instanceof LivingEntity livingEntity) {
                     OceanDefender.addAttrition(livingEntity, (float) Math.floor(p_32821_ / this.getMaxHealth() * 100.0F));
                     flag = true;
                  }
                  p_32821_ = 40;
               }
               p_32821_ = Math.min(20, p_32821_ / 4.0F);
               
               if (this.getOtherOceanDefender() != null) {
                  if (this.isTypeOne()) {
                     this.setChargingTimer(this.getChargingTimer() + 1);
                  } else {
                     this.getOtherOceanDefender().setChargingTimer(this.getOtherOceanDefender().getChargingTimer() + 1);
                  }
               } else {
                  this.setChargingTimer(this.getChargingTimer() + 1);
               }
               
               if (!p_32820_.is(DamageTypes.THORNS)) {
                  Entity entity = p_32820_.getDirectEntity();
                  if (entity instanceof LivingEntity) {
                     LivingEntity livingentity = (LivingEntity) entity;
                     livingentity.hurt(this.damageSources().thorns(this), 4.0F);
                  }
               }
               
               if (this.randomStrollGoal != null) {
                  this.randomStrollGoal.trigger();
               }
               
               if (this.getPhase() == PHASE_LASER_ATTACK) {
                  p_32821_ = Math.max(2.0F, p_32821_ / 20.0F);
               }
               
               
               boolean damaged = super.hurt(p_32820_, p_32821_);
               if (p_32820_.getEntity() instanceof LivingEntity livingEntity && flag && !(livingEntity instanceof PartBoss)) {
                  SwordBlockEvent.addVertigoTime(livingEntity, 80);
               }
               return damaged;
            }
         }
      }
      
      return false;
   }
   
   public int getMaxHeadXRot() {
      return 180;
   }
   
   public void travel(Vec3 p_32858_) {
      if (this.isControlledByLocalInstance() && this.isInWater()) {
         this.moveRelative(0.1F, p_32858_);
         this.move(MoverType.SELF, this.getDeltaMovement());
         this.setDeltaMovement(this.getDeltaMovement().scale(0.9D));
         if (!this.isMoving() && this.getTarget() == null) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.005D, 0.0D));
         }
      } else {
         super.travel(p_32858_);
      }
      
   }

   protected void customServerAiStep() {
      super.customServerAiStep();
      if (this.tickCount % 40 == 0) {
         MobEffectInstance mobeffectinstance = new MobEffectInstance(MobEffects.CONDUIT_POWER, 6000, 1, true, true);
         MobEffectInstance mobeffectinstance1 = new MobEffectInstance(ModMobEffects.SWIMMING_ACCELERATION.get(), 6000, 4, true, true);
         List<ServerPlayer> list = MobEffectUtil.addEffectToPlayersAround((ServerLevel)this.level(), this, this.position(), 100.0D, mobeffectinstance, 1200);
         MobEffectUtil.addEffectToPlayersAround((ServerLevel)this.level(), this, this.position(), 100.0D, mobeffectinstance1, 1200);
         list.forEach((p_289459_) -> {
            p_289459_.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.GUARDIAN_ELDER_EFFECT, this.isSilent() ? 0.0F : 1.0F));
         });
      }

      if (!this.hasRestriction()) {
         this.restrictTo(this.blockPosition(), 16);
      }
   }
   
   @Override
   public OceanDefenderPart[] getSubEntities() {
      return this.subEntities;
   }
   
   @Override
   public boolean canAttack(LivingEntity p_21171_) {
      return p_21171_.canBeSeenAsEnemy();
   }
   
   @Override
   public boolean isPickable() {
      return false;
   }
   
   @Override
   public boolean isPushable() {
      return false;
   }
   
   @Override
   public boolean doHurtTarget(Entity p_21372_) {
      float f = (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
      float f1 = (float)this.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
      if (p_21372_ instanceof LivingEntity) {
         f += EnchantmentHelper.getDamageBonus(this.getMainHandItem(), ((LivingEntity)p_21372_).getMobType());
         f1 += (float)EnchantmentHelper.getKnockbackBonus(this);
      }
      
      int i = EnchantmentHelper.getFireAspect(this);
      if (i > 0) {
         p_21372_.setSecondsOnFire(i * 4);
      }
      
      boolean flag = p_21372_.hurt(EnderLord.enderLordAttack(this), f);
      if (this.getOtherOceanDefender() != null) {
         if (this.isTypeOne()) {
            this.setChargingTimer(this.getChargingTimer() + 1);
         } else {
            this.getOtherOceanDefender().setChargingTimer(this.getOtherOceanDefender().getChargingTimer() + 1);
         }
      } else {
         this.setChargingTimer(this.getChargingTimer() + 1);
      }
      if (flag) {
         if (f1 > 0.0F && p_21372_ instanceof LivingEntity) {
            ((LivingEntity)p_21372_).knockback((double)(f1 * 0.5F), (double)Mth.sin(this.getYRot() * ((float)Math.PI / 180F)), (double)(-Mth.cos(this.getYRot() * ((float)Math.PI / 180F))));
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
         }
         
         if (p_21372_ instanceof Player) {
            Player player = (Player)p_21372_;
            this.maybeDisableShield(player, this.getMainHandItem(), player.isUsingItem() ? player.getUseItem() : ItemStack.EMPTY);
         }
         
         this.doEnchantDamageEffects(this, p_21372_);
         this.setLastHurtMob(p_21372_);
      }
      
      return flag;
   }
   
   private void maybeDisableShield(Player p_21425_, ItemStack p_21426_, ItemStack p_21427_) {
      if (!p_21426_.isEmpty() && !p_21427_.isEmpty() && p_21426_.getItem() instanceof AxeItem && p_21427_.is(Items.SHIELD)) {
         float f = 0.25F + (float)EnchantmentHelper.getBlockEfficiency(this) * 0.05F;
         if (this.random.nextFloat() < f) {
            p_21425_.getCooldowns().addCooldown(Items.SHIELD, 100);
            this.level().broadcastEntityEvent(p_21425_, (byte)30);
         }
      }
   }
   
   @Override
   public boolean ignoreExplosion() {
      return true;
   }
   
   public static void spawnCoralDefenders(OceanDefender defender) {
      OceanDefender defenderTwo = new OceanDefender(ModEntities.OCEAN_DEFENDER.get(), defender.level());
      defenderTwo.setPos(defender.position());
      defenderTwo.setTypeOne(!defender.isTypeOne());
      defender.setOtherOceanDefender(defenderTwo);
      defenderTwo.setOtherOceanDefender(defender);
      if (defenderTwo.level() instanceof ServerLevel serverLevel) {
         defenderTwo.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(defenderTwo.blockPosition()), MobSpawnType.EVENT, null, null);
      }
      defender.level().addFreshEntity(defenderTwo);
   }
   
   @Override
   public boolean isNoAi() {
      return false;
   }
   
   @Override
   public void performRangedAttack(LivingEntity p_32356_, float p_32357_) {
      ItemStack itemStack = new ItemStack(Items.TRIDENT);
      itemStack.enchant(Enchantments.CHANNELING, 1);
      ThrownTrident throwntrident = new ThrownTrident(this.level(), this, itemStack);
      double d0 = p_32356_.getX() - this.getX();
      double d1 = p_32356_.getY(0.3333333333333333D) - throwntrident.getY();
      double d2 = p_32356_.getZ() - this.getZ();
      double d3 = Math.sqrt(d0 * d0 + d2 * d2);
      throwntrident.shoot(d0, d1 + d3 * (double)0.2F, d2, this.isProgressTwo() ? 4.0F : 2.0F, (float)(14 - this.level().getDifficulty().getId() * 4));
      this.playSound(SoundEvents.DROWNED_SHOOT, this.getSoundVolume(), this.getVoicePitch() / (this.getRandom().nextFloat() * 0.4F + 0.8F));
      this.level().addFreshEntity(throwntrident);
      if (this.getOtherOceanDefender() != null) {
         if (this.isTypeOne()) {
            this.setChargingTimer(this.getChargingTimer() + 1);
         } else {
            this.getOtherOceanDefender().setChargingTimer(this.getOtherOceanDefender().getChargingTimer() + 1);
         }
      } else {
         this.setChargingTimer(this.getChargingTimer() + 1);
      }
   }
   
   static class OceanDefenderAttackGoal extends Goal {
      private final OceanDefender guardian;
      private int attackTime;
      
      public OceanDefenderAttackGoal(OceanDefender p_32871_) {
         this.guardian = p_32871_;
         this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
      }
      
      public boolean canUse() {
         LivingEntity livingentity = this.guardian.getTarget();
         return this.guardian.getPhase() == PHASE_LASER_ATTACK && livingentity != null && livingentity.isAlive();
      }
      
      public boolean canContinueToUse() {
         return this.guardian.getPhase() == PHASE_LASER_ATTACK && super.canContinueToUse() && (this.attackTime <= this.guardian.getAttackDuration() + 80 && this.guardian.getTarget() != null && this.guardian.distanceTo(this.guardian.getTarget()) > 10);
      }
      
      public void start() {
         this.attackTime = -10;
         this.guardian.getNavigation().stop();
         LivingEntity livingentity = this.guardian.getTarget();
         if (livingentity != null) {
            this.guardian.getLookControl().setLookAt(livingentity, 90.0F, 90.0F);
         }
         
         this.guardian.hasImpulse = true;
      }
      
      public void stop() {
         this.guardian.setActiveAttackTarget(0);
         this.guardian.randomStrollGoal.trigger();
         if (this.guardian.getRandom().nextFloat() < 0.2F) {
            if (this.guardian.getOtherOceanDefender() != null) {
               this.guardian.getOtherOceanDefender().setTarget(this.guardian.getTarget());
               this.guardian.getOtherOceanDefender().setPhase(PHASE_LASER_ATTACK);
            }
         }
         this.guardian.setPhase(PHASE_NORMAL);
      }
      
      public boolean requiresUpdateEveryTick() {
         return true;
      }
      
      public void tick() {
         LivingEntity livingentity = this.guardian.getTarget();
         if (livingentity != null) {
            this.guardian.getNavigation().stop();
            this.guardian.getLookControl().setLookAt(livingentity, 90.0F, 90.0F);
            if (this.guardian.distanceTo(livingentity) > 10) {
               ++this.attackTime;
               if (this.attackTime <= this.guardian.getAttackDuration() * 2) {
                  if (this.attackTime == 0) {
                     this.guardian.setActiveAttackTarget(livingentity.getId());
                     if (!this.guardian.isSilent()) {
                        this.guardian.level().broadcastEntityEvent(this.guardian, (byte) -5);
                     }
                     if (livingentity instanceof ServerPlayer player) {
                        ModMessages.sendToPlayer(new JumpStringS2CPacket(Component.translatable("message.endless_deep_space.entity.endless_deep_space.ocean_defender.laser_attack"), 160, false, true), player);
                     }
                  } else if (this.attackTime >= this.guardian.getAttackDuration()) {
                     float f = 2.0F;
                     if (this.guardian.level().getDifficulty() == Difficulty.HARD) {
                        f += 0.5F;
                     }
                     
                     if (!(livingentity instanceof Player)) {
                        f += livingentity.getMaxHealth() / 10.0F + this.guardian.distanceTo(livingentity) - 6;
                     }
                     
                     if (livingentity.tickCount % 4 == 0) {
                        livingentity.invulnerableTime = 0;
                        livingentity.hurt(EnderLord.enderLordAttack(this.guardian), f);
                        if (this.guardian.getOtherOceanDefender() != null) {
                           if (this.guardian.isTypeOne()) {
                              this.guardian.setChargingTimer(this.guardian.getChargingTimer() + 2);
                           } else {
                              this.guardian.getOtherOceanDefender().setChargingTimer(this.guardian.getOtherOceanDefender().getChargingTimer() + 2);
                           }
                        } else {
                           this.guardian.setChargingTimer(this.guardian.getChargingTimer() + 2);
                        }
                        if (!(livingentity instanceof Player)) {
                           OceanDefender.addAttrition(livingentity, 0.5F);
                        }
                     }
                  }
               } else {
                  this.stop();
               }
               super.tick();
            } else {
               this.stop();
            }
         }
      }
   }
   
   static class OceanDefenderMeleeAttackGoal extends MeleeAttackGoal {
      private final OceanDefender defender;
      
      public OceanDefenderMeleeAttackGoal(OceanDefender p_25552_) {
         super(p_25552_, 1.0F, true);
         this.defender = p_25552_;
      }
      
      @Override
      public boolean canUse() {
         return this.defender.getPhase() == PHASE_NORMAL && super.canUse();
      }
      
      @Override
      public void stop() {
         this.mob.setAggressive(false);
         this.mob.getNavigation().stop();
      }
      
      @Override
      public boolean canContinueToUse() {
         return this.defender.getPhase() == PHASE_NORMAL && super.canContinueToUse() && this.defender.getTarget() != null && this.defender.distanceTo(this.defender.getTarget()) <= 10;
      }
      
      @Override
      protected double getAttackReachSqr(LivingEntity p_25556_) {
         return this.defender.size.width * this.defender.size.width + p_25556_.getBbWidth();
      }
   }
   
   static class OceanDefenderTridentAttackGoal extends RangedAttackGoal {
      private final OceanDefender defender;
      
      public OceanDefenderTridentAttackGoal(RangedAttackMob p_32450_, double p_32451_, int p_32452_, float p_32453_) {
         super(p_32450_, p_32451_, p_32452_, p_32453_);
         this.defender = (OceanDefender)p_32450_;
      }
      
      @Override
      public boolean canUse() {
         return this.defender.getPhase() == PHASE_NORMAL && super.canUse();
      }
      
      @Override
      public boolean canContinueToUse() {
         return this.defender.getPhase() == PHASE_NORMAL && super.canContinueToUse() && this.defender.getTarget() != null && this.defender.distanceTo(this.defender.getTarget()) > 10;
      }
      
      @Override
      public void start() {
         super.start();
         this.defender.setAggressive(true);
         this.defender.startUsingItem(InteractionHand.MAIN_HAND);
      }
      
      @Override
      public void stop() {
         super.stop();
         this.defender.stopUsingItem();
         this.defender.setAggressive(false);
      }
   }
   
   static class OceanDefenderAttritionSprechstimmeGoal extends Goal {
      private final OceanDefender guardian;
      private int attackTime;
      
      public OceanDefenderAttritionSprechstimmeGoal(OceanDefender p_32871_) {
         this.guardian = p_32871_;
         this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
      }
      
      public boolean canUse() {
         LivingEntity livingentity = this.guardian.getTarget();
         return this.guardian.getPhase() == PHASE_ATTRITION_SPRECHSTIMME && (this.guardian.getHealth() > 0.0F || !(livingentity instanceof Player))  && livingentity != null && livingentity.isAlive();
      }
      
      public boolean canContinueToUse() {
         return this.guardian.getPhase() == PHASE_ATTRITION_SPRECHSTIMME && (this.guardian.getHealth() > 0.0F || !(this.guardian.getTarget() instanceof Player)) && super.canContinueToUse();
      }
      
      public void start() {
         this.attackTime = 0;
         this.guardian.getNavigation().stop();
         LivingEntity livingentity = this.guardian.getTarget();
         if (livingentity != null) {
            this.guardian.getLookControl().setLookAt(livingentity, 90.0F, 90.0F);
         }
         
         this.guardian.hasImpulse = true;
      }
      
      public void stop() {
         this.guardian.randomStrollGoal.trigger();
         this.guardian.setPhase(PHASE_NORMAL);
      }
      
      public boolean requiresUpdateEveryTick() {
         return true;
      }
      
      public void tick() {
         LivingEntity livingentity = this.guardian.getTarget();
         if (livingentity != null) {
            this.guardian.getNavigation().stop();
            this.guardian.getLookControl().setLookAt(livingentity, 90.0F, 90.0F);
            this.guardian.getLookControl().tick();
            if (this.attackTime <= 100) {
               livingentity.level().broadcastEntityEvent(livingentity, (byte) -6);
               if (this.attackTime == 0) {
                  if (!this.guardian.isSilent()) {
                     this.guardian.playSound(SoundEvents.EVOKER_PREPARE_SUMMON, this.guardian.getSoundVolume(), this.guardian.getVoicePitch());
                  }
                  if (livingentity instanceof ServerPlayer player) {
                     ModMessages.sendToPlayer(new JumpStringS2CPacket(Component.translatable("message.endless_deep_space.entity.endless_deep_space.ocean_defender.attrition_sprechstimme", Component.literal(String.valueOf(this.guardian.getMaxHealth() / 100.0F))).withStyle(ChatFormatting.GOLD), 160, false, true), player);
                  }
               } else if (this.attackTime >= 100) {
                  livingentity.hurt(this.guardian.damageSources().indirectMagic(this.guardian, this.guardian), 4.0F);
                  OceanDefender.addAttrition(livingentity, 0.25F);
                  livingentity.setLastHurtByMob(this.guardian);
                  if (livingentity instanceof ServerPlayer player) {
                     player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.GUARDIAN_ELDER_EFFECT, this.guardian.isSilent() ? 0.0F : 1.0F));
                  }
                  if (this.guardian.getOtherOceanDefender() != null) {
                     if (this.guardian.isTypeOne()) {
                        this.guardian.setChargingTimer(this.guardian.getChargingTimer() + 4);
                     } else {
                        this.guardian.getOtherOceanDefender().setChargingTimer(this.guardian.getOtherOceanDefender().getChargingTimer() + 4);
                     }
                  } else {
                     this.guardian.setChargingTimer(this.guardian.getChargingTimer() + 4);
                  }
               }
            } else {
               this.stop();
            }
            ++this.attackTime;
            super.tick();
         }
      }
   }
   
   static class OceanDefenderShootingAbyssalTorpedoGoal extends Goal {
      private final OceanDefender guardian;
      private int attackTime;
      
      public OceanDefenderShootingAbyssalTorpedoGoal(OceanDefender p_32871_) {
         this.guardian = p_32871_;
      }
      
      public boolean canUse() {
         LivingEntity livingentity = this.guardian.getTarget();
         return this.guardian.getPhase() == PHASE_SHOOTING_ABYSSAL_TORPEDO && livingentity != null && livingentity.isAlive();
      }
      
      public boolean canContinueToUse() {
         return this.guardian.getPhase() == PHASE_SHOOTING_ABYSSAL_TORPEDO && super.canContinueToUse();
      }
      
      public void start() {
         this.attackTime = 0;
      }
      
      public void stop() {
         this.guardian.randomStrollGoal.trigger();
         if (this.guardian.getOtherOceanDefender() != null) {
            if (this.guardian.isTypeOne()) {
               if (this.guardian.getRandom().nextFloat() < 0.5F) {
                  this.guardian.setPhase(PHASE_ATTRITION_SPRECHSTIMME);
                  this.guardian.getOtherOceanDefender().setPhase(PHASE_NORMAL);
               } else {
                  this.guardian.setPhase(PHASE_NORMAL);
                  this.guardian.getOtherOceanDefender().setPhase(PHASE_ATTRITION_SPRECHSTIMME);
               }
            }
         } else {
            this.guardian.setPhase(PHASE_ATTRITION_SPRECHSTIMME);
         }
      }
      
      public boolean requiresUpdateEveryTick() {
         return true;
      }
      
      public void tick() {
         LivingEntity livingentity = this.guardian.getTarget();
         if (livingentity != null) {
            if (this.attackTime <= 80) {
               if (this.attackTime == 0) {
                  if (!this.guardian.isSilent()) {
                     this.guardian.level().levelEvent((Player)null, 1015, this.guardian.blockPosition(), 0);
                  }
               } else if (this.attackTime >= 40) {
                  if (this.guardian.tickCount % 5 == 0) {
                     Vec3 vec32 = this.guardian.getViewVector(1.0F);
                     double d6 = this.guardian.getX() - vec32.x;
                     double d7 = this.guardian.getY(0.5D) + 0.5D;
                     double d8 = this.guardian.getZ() - vec32.z;
                     double d9 = livingentity.getX() - d6;
                     double d10 = livingentity.getY(0.5D) - d7;
                     double d11 = livingentity.getZ() - d8;
                     if (!this.guardian.isSilent()) {
                        this.guardian.level().levelEvent((Player)null, 1016, this.guardian.blockPosition(), 0);
                     }
                     AbyssalTorpedo abyssalTorpedo = new AbyssalTorpedo(ModEntities.ABYSSAL_TORPEDO.get(), this.guardian, this.guardian.level());
                     abyssalTorpedo.shoot(d9, d10, d11, 1, 0);
                     this.guardian.level().addFreshEntity(abyssalTorpedo);
                  }
               }
            } else {
               this.stop();
            }
            ++this.attackTime;
            super.tick();
         }
      }
   }
   
   static class OceanDefenderMoveToTargetGoal extends Goal {
      private final OceanDefender guardian;
      
      public OceanDefenderMoveToTargetGoal(OceanDefender defender) {
         this.guardian = defender;
      }
      
      @Override
      public boolean canUse() {
         return this.guardian.getTarget() != null && this.guardian.distanceTo(this.guardian.getTarget()) > 10;
      }
      
      @Override
      public boolean canContinueToUse() {
         return !this.guardian.getNavigation().isDone() && this.guardian.getTarget() != null && this.guardian.distanceTo(this.guardian.getTarget()) > 10;
      }
      
      @Override
      public void start() {
         if (this.guardian.getTarget() != null) {
            this.guardian.getNavigation().moveTo(this.guardian.getTarget(), 1.0D);
         }
      }
      
      @Override
      public void stop() {
         this.guardian.getNavigation().stop();
         super.stop();
      }
   }
   
   static class OceanDefenderMoveToOtherOceanDefenderGoal extends Goal {
      private final OceanDefender guardian;
      
      public OceanDefenderMoveToOtherOceanDefenderGoal(OceanDefender defender) {
         this.guardian = defender;
      }
      
      @Override
      public boolean canUse() {
         return this.guardian.getOtherOceanDefender() != null && this.guardian.distanceTo(this.guardian.getOtherOceanDefender()) > 20;
      }
      
      @Override
      public boolean canContinueToUse() {
         return !this.guardian.getNavigation().isDone() && this.guardian.getOtherOceanDefender() != null && this.guardian.distanceTo(this.guardian.getOtherOceanDefender()) > (this.guardian.getTarget() != null ? 60 : 40);
      }
      
      @Override
      public void start() {
         if (this.guardian.getOtherOceanDefender() != null) {
            this.guardian.getNavigation().moveTo(this.guardian.getOtherOceanDefender(), this.guardian.distanceTo(this.guardian.getOtherOceanDefender()) > 60 ? 2.0D : 1.0D);
         }
      }
      
      @Override
      public void stop() {
         this.guardian.getNavigation().stop();
         super.stop();
      }
   }
   
   static class OceanDefenderHurtByTargetGoal extends HurtByTargetGoal {
      public OceanDefenderHurtByTargetGoal(PathfinderMob p_26039_, Class<?>... p_26040_) {
         super(p_26039_, p_26040_);
      }
      
      @Override
      public boolean canContinueToUse() {
         return this.mob.level().getDifficulty() != Difficulty.PEACEFUL && super.canContinueToUse();
      }
      
      @Override
      public boolean canUse() {
         return this.mob.level().getDifficulty() != Difficulty.PEACEFUL &&  super.canUse();
      }
   }
   
   static class OceanDefenderMoveControl extends MoveControl {
      private final OceanDefender guardian;
      
      public OceanDefenderMoveControl(OceanDefender p_32886_) {
         super(p_32886_);
         this.guardian = p_32886_;
      }
      
      public void tick() {
         if (this.operation == MoveControl.Operation.MOVE_TO && !this.guardian.getNavigation().isDone()) {
            Vec3 vec3 = new Vec3(this.wantedX - this.guardian.getX(), this.wantedY - this.guardian.getY(), this.wantedZ - this.guardian.getZ());
            double d0 = vec3.length();
            double d1 = vec3.x / d0;
            double d2 = vec3.y / d0;
            double d3 = vec3.z / d0;
            float f = (float)(Mth.atan2(vec3.z, vec3.x) * (double)(180F / (float)Math.PI)) - 90.0F;
            this.guardian.setYRot(this.rotlerp(this.guardian.getYRot(), f, 90.0F));
            this.guardian.yBodyRot = this.guardian.getYRot();
            float f1 = (float)(this.speedModifier * this.guardian.getAttributeValue(Attributes.MOVEMENT_SPEED));
            float f2 = Mth.lerp(0.125F, this.guardian.getSpeed(), f1);
            this.guardian.setSpeed(f2);
            double d4 = Math.sin((double)(this.guardian.tickCount + this.guardian.getId()) * 0.5D) * 0.05D;
            double d5 = Math.cos((double)(this.guardian.getYRot() * ((float)Math.PI / 180F)));
            double d6 = Math.sin((double)(this.guardian.getYRot() * ((float)Math.PI / 180F)));
            double d7 = Math.sin((double)(this.guardian.tickCount + this.guardian.getId()) * 0.75D) * 0.05D;
            this.guardian.setDeltaMovement(this.guardian.getDeltaMovement().add(d4 * d5, d7 * (d6 + d5) * 0.25D + (double)f2 * d2 * 0.1D, d4 * d6));
            LookControl lookcontrol = this.guardian.getLookControl();
            double d8 = this.guardian.getX() + d1 * 2.0D;
            double d9 = this.guardian.getEyeY() + d2 / d0;
            double d10 = this.guardian.getZ() + d3 * 2.0D;
            double d11 = lookcontrol.getWantedX();
            double d12 = lookcontrol.getWantedY();
            double d13 = lookcontrol.getWantedZ();
            if (!lookcontrol.isLookingAtTarget()) {
               d11 = d8;
               d12 = d9;
               d13 = d10;
            }
            
            this.guardian.getLookControl().setLookAt(Mth.lerp(0.125D, d11, d8), Mth.lerp(0.125D, d12, d9), Mth.lerp(0.125D, d13, d10), 10.0F, 40.0F);
            this.guardian.setMoving(true);
         } else {
            this.guardian.setSpeed(0.0F);
            this.guardian.setMoving(false);
         }
      }
   }
   
   static class OceanDefenderWaterBoundPathNavigation extends WaterBoundPathNavigation {
      public OceanDefenderWaterBoundPathNavigation(Mob p_26594_, Level p_26595_) {
         super(p_26594_, p_26595_);
      }
      
      @Override
      protected PathFinder createPathFinder(int p_26598_) {
         this.nodeEvaluator = new SwimNodeEvaluator(true);
         return new PathFinder(this.nodeEvaluator, p_26598_);
      }
      
      @Override
      protected boolean canUpdatePath() {
         return true;
      }
   }
   
   public static DamageSource attrition(Entity entity) {
      return new DamageSource(entity.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("endless_deep_space:attrition"))), entity);
   }
   
   public static DamageSource attrition(LevelAccessor level) {
      return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("endless_deep_space:attrition"))));
   }
   
   public static void actuallyHurt(Entity entity, DamageSource p_21240_, float p_21241_, boolean bypassInvulnerable) {
      if (entity instanceof LivingEntity livingEntity) {
         if (!livingEntity.level().isClientSide()) {
            if (!livingEntity.isInvulnerableTo(p_21240_) || bypassInvulnerable) {
               if (p_21241_ != 0.0F) {
                  net.minecraftforge.common.ForgeHooks.onLivingAttack(livingEntity, p_21240_, p_21241_);
                  net.minecraftforge.common.ForgeHooks.onLivingHurt(livingEntity, p_21240_, p_21241_);
                  net.minecraftforge.common.ForgeHooks.onLivingDamage(livingEntity, p_21240_, p_21241_);
                  if (livingEntity.isSleeping()) {
                     livingEntity.stopSleeping();
                  }
                  livingEntity.walkAnimation.setSpeed(1.5F);
                  livingEntity.getCombatTracker().recordDamage(p_21240_, p_21241_);
                  float health = livingEntity.getHealth() - p_21241_;
                  livingEntity.setHealth(health);
                  if (livingEntity.getHealth() != health && bypassInvulnerable) {
                     livingEntity.getEntityData().set(((LivingEntityInvoker) livingEntity).getDataHealthID(), health);
                     if (livingEntity.getHealth() != health) {
                        ((LivingEntityInvoker) livingEntity).invokerActuallyHurt(p_21240_, p_21241_);
                        if (livingEntity.getHealth() != health) {
                           for (int i = 0; i < 20; i++) {
                              livingEntity.invulnerableTime = 0;
                              ((LivingEntityInvoker) livingEntity).invokerActuallyHurt(p_21240_, p_21241_ / 20.0F);
                           }
                           if (livingEntity.getHealth() != health) {
                              for (int i = 0; i < 20; i++) {
                                 livingEntity.invulnerableTime = 0;
                                 livingEntity.hurt(p_21240_, Math.max(0.0F, p_21241_ / 20.0F));
                              }
                           }
                        }
                     }
                     livingEntity.setHealth(health);
                  }
                  livingEntity.gameEvent(GameEvent.ENTITY_DAMAGE);
                  
                  Entity entity1 = p_21240_.getEntity();
                  if (entity1 != null) {
                     if (entity1 instanceof LivingEntity) {
                        LivingEntity livingentity1 = (LivingEntity) entity1;
                        if (!p_21240_.is(DamageTypeTags.NO_ANGER)) {
                           livingEntity.setLastHurtByMob(livingentity1);
                        }
                     }
                     
                     if (entity1 instanceof Player) {
                        Player player1 = (Player) entity1;
                        ((LivingEntityInvoker) livingEntity).setLastHurtByPlayerTime(100);
                        ((LivingEntityInvoker) livingEntity).setLastHurtByPlayer(player1);
                     } else if (entity1 instanceof net.minecraft.world.entity.TamableAnimal tamableEntity) {
                        if (tamableEntity.isTame()) {
                           ((LivingEntityInvoker) livingEntity).setLastHurtByPlayerTime(100);
                           LivingEntity livingentity2 = tamableEntity.getOwner();
                           if (livingentity2 instanceof Player) {
                              Player player = (Player) livingentity2;
                              ((LivingEntityInvoker) livingEntity).setLastHurtByPlayer(player);
                           } else {
                              ((LivingEntityInvoker) livingEntity).setLastHurtByPlayer(null);
                           }
                        }
                     }
                     
                     if (!p_21240_.is(DamageTypeTags.IS_EXPLOSION)) {
                        double d0 = entity1.getX() - livingEntity.getX();
                        
                        double d1;
                        for (d1 = entity1.getZ() - livingEntity.getZ(); d0 * d0 + d1 * d1 < 1.0E-4D; d1 = (Math.random() - Math.random()) * 0.01D) {
                           d0 = (Math.random() - Math.random()) * 0.01D;
                        }
                        
                        livingEntity.knockback(0.4F, d0, d1);
                        livingEntity.indicateDamage(d0, d1);
                     }
                  }
                  
                  livingEntity.level().broadcastDamageEvent(livingEntity, p_21240_);
                  livingEntity.hurtMarked = true;
                  
                  if (livingEntity.isDeadOrDying()) {
                     if (!((LivingEntityInvoker) livingEntity).invokerCheckTotemDeathProtection(p_21240_)) {
                        livingEntity.die(p_21240_);
                     }
                  } else {
                     ((LivingEntityInvoker) livingEntity).invokerPlayHurtSound(p_21240_);
                  }
                  
                  if (p_21241_ > 0.0F) {
                     ((LivingEntityInvoker) livingEntity).setLastDamageSource(p_21240_);
                     ((LivingEntityInvoker) livingEntity).setLastDamageStamp(livingEntity.level().getGameTime());
                  }
                  
                  if (livingEntity instanceof ServerPlayer) {
                     CriteriaTriggers.ENTITY_HURT_PLAYER.trigger((ServerPlayer) livingEntity, p_21240_, p_21241_, p_21241_, true);
                  }
                  
                  if (entity1 instanceof ServerPlayer) {
                     CriteriaTriggers.PLAYER_HURT_ENTITY.trigger((ServerPlayer) entity1, livingEntity, p_21240_, p_21241_, p_21241_, true);
                  }
               }
            }
         }
      } else {
         entity.hurt(p_21240_, p_21241_);
      }
   }
   
   public static float getAttrition(LivingEntity entity) {
      return !entity.level().isClientSide ? OceanDefender.livingEntityAttrition.getOrDefault(entity, 0.0F) : OceanDefender.clientSideLivingEntityAttrition.getOrDefault(entity, 0.0F);
   }
   
   public static int getAttritionTick(LivingEntity entity) {
      return !entity.level().isClientSide ? OceanDefender.livingEntityAttritionTick.getOrDefault(entity, 0) : OceanDefender.clientSideLivingEntityAttritionTick.getOrDefault(entity, 0);
   }
   
   public static int getAttritionMaxTick(LivingEntity entity) {
      return !entity.level().isClientSide ? OceanDefender.livingEntityAttritionMaxTick.getOrDefault(entity, 0) : OceanDefender.clientSideLivingEntityAttritionMaxTick.getOrDefault(entity, 0);
   }
   
   public static void setAttrition(LivingEntity entity, float value) {
      if (!entity.level().isClientSide) {
         OceanDefender.livingEntityAttrition.put(entity, Math.max(0.0F, value));
      } else {
         OceanDefender.clientSideLivingEntityAttrition.put(entity, Math.max(0.0F, value));
      }
   }
   
   public static void setAttritionTick(LivingEntity entity, int value) {
      if (!entity.level().isClientSide) {
         OceanDefender.livingEntityAttritionTick.put(entity, Math.max(0, value));
      } else {
         OceanDefender.clientSideLivingEntityAttritionTick.put(entity, Math.max(0, value));
      }
   }
   
   public static void setAttritionMaxTick(LivingEntity entity, int value) {
      if (!entity.level().isClientSide) {
         OceanDefender.livingEntityAttritionMaxTick.put(entity, Math.max(0, value));
      } else {
         OceanDefender.clientSideLivingEntityAttritionMaxTick.put(entity, Math.max(0, value));
      }
   }
   
   public static void addAttrition(LivingEntity entity, float level) {
      OceanDefender.addAttrition(entity, level, 40, true, true);
   }
   
   public static void addAttrition(LivingEntity entity, float level, int timeleft, boolean addLevel, boolean updateTick) {
      OceanDefender.setAttrition(entity, addLevel ? OceanDefender.getAttrition(entity) + level : level);
      OceanDefender.setAttritionTick(entity, updateTick ? Math.max(timeleft, OceanDefender.getAttritionTick(entity)) : timeleft);
      OceanDefender.setAttritionMaxTick(entity, updateTick ? Math.max(timeleft, OceanDefender.getAttritionTick(entity)) : timeleft);
      
      if (!entity.level().isClientSide) {
         ModMessages.sendToAllPlayers(new UpdateAttritionS2CPacket(entity, false, OceanDefender.getAttrition(entity), OceanDefender.getAttritionTick(entity)));
      }
   }
   
   public static void removeAttrition(LivingEntity entity) {
      if (!entity.level().isClientSide) {
         OceanDefender.livingEntityAttrition.remove(entity);
         OceanDefender.livingEntityAttritionTick.remove(entity);
         OceanDefender.livingEntityAttritionMaxTick.remove(entity);
         
         ModMessages.sendToAllPlayers(new UpdateAttritionS2CPacket(entity, true, OceanDefender.getAttrition(entity), OceanDefender.getAttritionTick(entity)));
      } else {
         OceanDefender.clientSideLivingEntityAttrition.remove(entity);
         OceanDefender.clientSideLivingEntityAttritionTick.remove(entity);
         OceanDefender.clientSideLivingEntityAttritionMaxTick.remove(entity);
      }
   }
   
   public static void setIsDying(LivingEntity entity, boolean dying) {
      if (!entity.level().isClientSide) {
         OceanDefender.livingEntityIsDying.put(entity, dying);
         
         ModMessages.sendToAllPlayers(new UpdateIsDyingS2CPacket(entity, false, dying));
      } else {
         OceanDefender.clientSideLivingEntityIsDying.put(entity, dying);
      }
   }
   
   public static boolean isDying(LivingEntity entity) {
      if (entity instanceof PartBoss) {
         return false;
      }
      
      if (entity instanceof Player player) {
         if (player.isCreative()) {
            return false;
         }
      }
      
      return !entity.level().isClientSide ? OceanDefender.livingEntityIsDying.getOrDefault(entity, false) : OceanDefender.clientSideLivingEntityIsDying.getOrDefault(entity, false);
   }
   
   public static void removeDying(LivingEntity entity) {
      if (!entity.level().isClientSide) {
         OceanDefender.livingEntityIsDying.remove(entity);
         
         ModMessages.sendToAllPlayers(new UpdateIsDyingS2CPacket(entity, true, OceanDefender.isDying(entity)));
      } else {
         OceanDefender.clientSideLivingEntityIsDying.remove(entity);
      }
   }
}