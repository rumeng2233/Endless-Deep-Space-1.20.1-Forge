package shirumengya.endless_deep_space.custom.entity.boss.enderlord;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.BinaryHeap;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import shirumengya.endless_deep_space.custom.block.entity.GuidingStoneBlockEntity;
import shirumengya.endless_deep_space.custom.client.event.CustomServerBossEvent;
import shirumengya.endless_deep_space.custom.client.gui.screens.components.wheel.Color;
import shirumengya.endless_deep_space.custom.config.ModCommonConfig;
import shirumengya.endless_deep_space.custom.entity.ColorfulLightningBolt;
import shirumengya.endless_deep_space.custom.entity.ShieldEntity;
import shirumengya.endless_deep_space.custom.entity.boss.PartBoss;
import shirumengya.endless_deep_space.custom.entity.boss.enderlord.phases.DragonPhaseInstance;
import shirumengya.endless_deep_space.custom.entity.boss.enderlord.phases.EnderDragonPhase;
import shirumengya.endless_deep_space.custom.entity.boss.enderlord.phases.EnderDragonPhaseManager;
import shirumengya.endless_deep_space.custom.entity.boss.oceandefenders.OceanDefender;
import shirumengya.endless_deep_space.custom.init.ModEntities;
import shirumengya.endless_deep_space.custom.networking.ModMessages;
import shirumengya.endless_deep_space.custom.networking.packet.DeleteEntityS2CPacket;
import shirumengya.endless_deep_space.custom.util.entity.TrackingUtil;
import shirumengya.endless_deep_space.custom.world.data.ModWorldData;
import shirumengya.endless_deep_space.init.EndlessDeepSpaceModGameRules;
import shirumengya.endless_deep_space.mixins.EntityAccessor;
import shirumengya.endless_deep_space.mixins.ServerPlayerAccessor;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

public class EnderLord extends PartBoss implements Enemy, ShieldEntity {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final EntityDataAccessor<Integer> DATA_PHASE = SynchedEntityData.defineId(EnderLord.class, EntityDataSerializers.INT);
   public final double[][] positions = new double[64][3];
   public int posPointer = -1;
   private final EnderLordPart[] subEntities;
   public final EnderLordPart head;
   private final EnderLordPart neck;
   private final EnderLordPart body;
   private final EnderLordPart tail1;
   private final EnderLordPart tail2;
   private final EnderLordPart tail3;
   private final EnderLordPart wing1;
   private final EnderLordPart wing2;
   public float oFlapTime;
   public float flapTime;
   public boolean inWall;
   public int dragonDeathTime;
   public float yRotA;
   public static final EntityDataAccessor<BlockPos> FIGHT_ORIGIN_POS = SynchedEntityData.defineId(EnderLord.class, EntityDataSerializers.BLOCK_POS);
   private final EnderDragonPhaseManager phaseManager;
   private int growlTime = 100;
   private float sittingDamageReceived;
   public final Node[] nodes = new Node[24];
   private final int[] nodeAdjacency = new int[24];
   private final BinaryHeap openSet = new BinaryHeap();
   private final CustomServerBossEvent dragonEvent = new CustomServerBossEvent(this, this.getDisplayName(), BossEvent.BossBarColor.BLUE, false, 0);
   private final CustomServerBossEvent dragonShieldEvent = new CustomServerBossEvent(Component.empty(), Component.empty(), BossEvent.BossBarColor.WHITE, false, 1);
   private final CustomServerBossEvent dragonAttackTimes = new CustomServerBossEvent(Component.empty(), Component.empty(), BossEvent.BossBarColor.WHITE, false, 3);
   @Nullable
   public LivingEntity attackTarget;
   @Nullable
   public LivingEntity clientSideAttackTarget;
   private static final TargetingConditions NEW_TARGET_TARGETING = TargetingConditions.forCombat().ignoreLineOfSight().range(200);
   public static final EntityDataAccessor<Integer> HANG_TIME = SynchedEntityData.defineId(EnderLord.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Integer> FLY_ATTACK_COOLDOWN = SynchedEntityData.defineId(EnderLord.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Integer> CHARGING_TIME = SynchedEntityData.defineId(EnderLord.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Integer> ATTACK_TIMES = SynchedEntityData.defineId(EnderLord.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Boolean> CAN_BE_DEAD = SynchedEntityData.defineId(EnderLord.class, EntityDataSerializers.BOOLEAN);
   public static final EntityDataAccessor<Boolean> PROGRESS_TWO = SynchedEntityData.defineId(EnderLord.class, EntityDataSerializers.BOOLEAN);
   public static final EntityDataAccessor<Float> DATA_SHIELD = SynchedEntityData.defineId(EnderLord.class, EntityDataSerializers.FLOAT);
   public static final EntityDataAccessor<Float> DATA_HEALTH = SynchedEntityData.defineId(EnderLord.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Integer> DATA_ATTACK_TARGET = SynchedEntityData.defineId(EnderLord.class, EntityDataSerializers.INT);
   public int chargingTime;

   public EnderLord(EntityType<? extends EnderLord> p_31096_, Level p_31097_) {
      super(ModEntities.ENDER_LORD.get(), p_31097_);
      this.head = new EnderLordPart(this, "head", 1.0F, 1.0F, Color.of(255, 0, 0));
      this.neck = new EnderLordPart(this, "neck", 3.0F, 3.0F, Color.of(0, 255, 255));
      this.body = new EnderLordPart(this, "body", 5.0F, 3.0F);
      this.tail1 = new EnderLordPart(this, "tail", 2.0F, 2.0F, Color.of(255, 255, 0));
      this.tail2 = new EnderLordPart(this, "tail", 2.0F, 2.0F, Color.of(255, 255, 0));
      this.tail3 = new EnderLordPart(this, "tail", 2.0F, 2.0F, Color.of(255, 255, 0));
      this.wing1 = new EnderLordPart(this, "wing", 4.0F, 2.0F , Color.of(255, 153, 0));
      this.wing2 = new EnderLordPart(this, "wing", 4.0F, 2.0F , Color.of(255, 153, 0));
      this.subEntities = new EnderLordPart[]{this.head, this.neck, this.body, this.tail1, this.tail2, this.tail3, this.wing1, this.wing2};
      this.reallySetDragonHealth(this.getMaxHealth());
      this.noPhysics = true;
      this.noCulling = true;
      this.phaseManager = new EnderDragonPhaseManager(this);
      this.xpReward = 0;
      this.setId(ENTITY_COUNTER.getAndAdd(this.getSubEntities().length + 1) + 1); // Forge: Fix MC-158205: Make sure part ids are successors of parent mob id
   }

   public void setFightOrigin(BlockPos p_287665_) {
      this.getEntityData().set(FIGHT_ORIGIN_POS, p_287665_);
   }

   public BlockPos getFightOrigin() {
      return this.getEntityData().get(FIGHT_ORIGIN_POS);
   }

   public void setHangTime(int value) {
   	  this.getEntityData().set(HANG_TIME, value);
   }

   public int getHangTime() {
   	  return this.getEntityData().get(HANG_TIME);
   }

   public void setFlyAttackCooldown(int value) {
   	  this.getEntityData().set(FLY_ATTACK_COOLDOWN, value);
   }

   public int getFlyAttackCooldown() {
   	  return this.getEntityData().get(FLY_ATTACK_COOLDOWN);
   }

   public void setChargingTime(int value) {
   	  this.getEntityData().set(CHARGING_TIME, value);
   }

   public int getChargingTime() {
   	  return this.getEntityData().get(CHARGING_TIME);
   }

   public void setAttackTimes(int value) {
      this.getEntityData().set(ATTACK_TIMES, Mth.clamp(value, 0, 30));
   }

   public int getAttackTimes() {
      return this.getEntityData().get(ATTACK_TIMES);
   }

   public void setCanBeDead(boolean value) {
      this.getEntityData().set(CAN_BE_DEAD, value);
   }

   public boolean getCanBeDead() {
      return this.getEntityData().get(CAN_BE_DEAD);
   }

   public void setProgressTwo(boolean value) {
      this.getEntityData().set(PROGRESS_TWO, value);
   }

   public boolean getProgressTwo() {
      return this.getEntityData().get(PROGRESS_TWO);
   }

   public void setShield(float value) {
      this.getEntityData().set(DATA_SHIELD, Mth.clamp(value, 0.0F, this.getMaxShield()));
   }

   public float getShield() {
      return this.getEntityData().get(DATA_SHIELD);
   }

   public float getMaxShield() {
      return ModCommonConfig.ENDER_LORD_MAX_SHIELD.get();
   }

   public boolean hasShield() {
      return this.getShield() > 0.0F;
   }

   public void setAttackTarget(int value) {
      this.getEntityData().set(DATA_ATTACK_TARGET, value);
   }

   public int getAttackTarget() {
      return this.getEntityData().get(DATA_ATTACK_TARGET);
   }

   public boolean hasAttackTarget() {
      return this.getEntityData().get(DATA_ATTACK_TARGET) != 0;
   }

   @Nullable
   public LivingEntity getActiveAttackTarget() {
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
         return this.attackTarget;
      }
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 600.0D);
   }

   public boolean isFlapping() {
      float f = Mth.cos(this.flapTime * ((float)Math.PI * 2F));
      float f1 = Mth.cos(this.oFlapTime * ((float)Math.PI * 2F));
      return f1 <= -0.3F && f >= -0.3F;
   }

   public void onFlap() {
      if (this.level().isClientSide && !this.isSilent()) {
         this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ENDER_DRAGON_FLAP, this.getSoundSource(), 5.0F, 0.8F + this.random.nextFloat() * 0.3F, false);
      }

   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.getEntityData().define(DATA_PHASE, EnderDragonPhase.HOLDING_PATTERN.getId());
      this.getEntityData().define(FIGHT_ORIGIN_POS, BlockPos.ZERO);
      this.getEntityData().define(HANG_TIME, 0);
      this.getEntityData().define(FLY_ATTACK_COOLDOWN, 0);
      this.getEntityData().define(CHARGING_TIME, 0);
      this.getEntityData().define(ATTACK_TIMES, 0);
      this.getEntityData().define(CAN_BE_DEAD, false);
      this.getEntityData().define(PROGRESS_TWO, false);
      this.getEntityData().define(DATA_SHIELD, (float)ModCommonConfig.ENDER_LORD_MAX_SHIELD.get());
      this.getEntityData().define(DATA_HEALTH, (float)ModCommonConfig.ENDER_LORD_MAX_HEALTH.get());
      this.getEntityData().define(DATA_ATTACK_TARGET, 0);
   }

   @Nullable
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_30153_, DifficultyInstance p_30154_, MobSpawnType p_30155_, @Nullable SpawnGroupData p_30156_, @Nullable CompoundTag p_30157_) {
        if (this.level() instanceof ServerLevel serverLevel) {
           this.setFightOrigin(this.blockPosition());
        }
   	  return super.finalizeSpawn(p_30153_, p_30154_, p_30155_, p_30156_, p_30157_);
   }

   public double[] getLatencyPos(int p_31102_, float p_31103_) {
      if (this.isDeadOrDying()) {
         p_31103_ = 0.0F;
      }

      p_31103_ = 1.0F - p_31103_;
      int i = this.posPointer - p_31102_ & 63;
      int j = this.posPointer - p_31102_ - 1 & 63;
      double[] adouble = new double[3];
      double d0 = this.positions[i][0];
      double d1 = Mth.wrapDegrees(this.positions[j][0] - d0);
      adouble[0] = d0 + d1 * (double)p_31103_;
      d0 = this.positions[i][1];
      d1 = this.positions[j][1] - d0;
      adouble[1] = d0 + d1 * (double)p_31103_;
      adouble[2] = Mth.lerp((double)p_31103_, this.positions[i][2], this.positions[j][2]);
      return adouble;
   }

   public boolean displayFireAnimation() {
      return false;
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

   public void tick() {
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
      
      if (!this.level().isClientSide) {
         this.dragonEvent.setProgress(this.getHealth() / this.getMaxHealth());

         if (this.getPhaseManager().getCurrentPhase().getPhase() == EnderDragonPhase.TRACKING_ENTITY || this.getPhaseManager().getCurrentPhase().getPhase() == EnderDragonPhase.CLUSTER_STRAFE_ENTITY || this.getPhaseManager().getCurrentPhase().getPhase() == EnderDragonPhase.EXPLOSION || this.getChargingTime() > 0) {
            this.dragonEvent.setDarkenScreen(true);
         } else {
            this.dragonEvent.setDarkenScreen(false);
         }

         this.dragonAttackTimes.setProgress(this.getAttackTimes() / 30.0F);
         this.dragonAttackTimes.setName(Component.empty());
         this.dragonShieldEvent.setProgress(this.getShield() / this.getMaxShield());
         if (this.getProgressTwo()) {
            this.dragonShieldEvent.setRenderType(2);
            this.dragonShieldEvent.setColor(BossEvent.BossBarColor.PURPLE);

            if (this.hasShield()) {
               this.reallySetDragonHealth(this.getMaxHealth() / 2);
            }
         } else {
            this.dragonShieldEvent.setRenderType(1);
            this.dragonShieldEvent.setColor(BossEvent.BossBarColor.WHITE);

            if (this.hasShield()) {
               this.reallySetDragonHealth(this.getMaxHealth());
            }
         }
         
         if (this.getHealth() < 0.0F || Float.isNaN(this.getHealth())) {
            this.setDragonHealth(0.0F);
         }
      }

      if (this.getPhaseManager().getCurrentPhase().getPhase() != EnderDragonPhase.EXPLOSION) {
         if (this.level().isClientSide) {
            this.chargingTime = 0;
         } else {
            this.setChargingTime(0);
         }
      }

      super.tick();
   }

   public void startSeenByPlayer(ServerPlayer player) {
   	super.startSeenByPlayer(player);
   	this.dragonEvent.addPlayer(player);
      this.dragonAttackTimes.addPlayer(player);
      this.dragonShieldEvent.addPlayer(player);
   }

   public void stopSeenByPlayer(ServerPlayer player) {
   	super.stopSeenByPlayer(player);
   	this.dragonEvent.removePlayer(player);
      this.dragonAttackTimes.removePlayer(player);
      this.dragonShieldEvent.removePlayer(player);
   }

   public void setNoAi(boolean value) {
   	this.phaseManager.setPhase(value ? EnderDragonPhase.NO_AI : EnderDragonPhase.HOLDING_PATTERN);
   }

   public boolean isNoAi() {
   	return false;
   }

   @org.jetbrains.annotations.Nullable private Player unlimitedLastHurtByPlayer = null;
   public void aiStep() {
      if (!this.level().isClientSide) {
         if (this.level().getDifficulty() == Difficulty.PEACEFUL) {
            this.attackTarget = null;
            this.setAttackTarget(0);
         } else {
            if (this.attackTarget == null || !this.attackTarget.isAlive() || this.attackTarget.isRemoved()) {
               BlockPos blockpos = this.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, this.getFightOrigin());
               LivingEntity entity = this.level().getNearestEntity(LivingEntity.class, NEW_TARGET_TARGETING, this, (double) blockpos.getX(), (double) blockpos.getY(), (double) blockpos.getZ(), new AABB(new Vec3(this.getX(), this.getY(), this.getZ()), new Vec3(this.getX(), this.getY(), this.getZ())).inflate(200 / 2d));
               this.attackTarget = entity;
               if (entity != null) {
                  this.setAttackTarget(entity.getId());
               }
            }
         }

         if (this.getAttackTimes() >= 30 && this.phaseManager.getCurrentPhase().getPhase() != EnderDragonPhase.EXPLOSION) {
            if (this.phaseManager.getCurrentPhase().getPhase() != EnderDragonPhase.DYING) {
               this.getPhaseManager().setPhase(EnderDragonPhase.EXPLOSION);
            }
         }
      }
      
      // lastHurtByPlayer is cleared after 100 ticks, capture it indefinitely in unlimitedLastHurtByPlayer for LivingExperienceDropEvent
      if (this.lastHurtByPlayer != null) this.unlimitedLastHurtByPlayer = lastHurtByPlayer;
      if (this.unlimitedLastHurtByPlayer != null && this.unlimitedLastHurtByPlayer.isRemoved()) this.unlimitedLastHurtByPlayer = null;
      this.processFlappingMovement();
      if (this.level().isClientSide) {
         this.reallySetDragonHealth(this.getHealth());
         if (!this.isSilent() && !this.phaseManager.getCurrentPhase().isSitting() && --this.growlTime < 0) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ENDER_DRAGON_GROWL, this.getSoundSource(), 2.5F, 0.8F + this.random.nextFloat() * 0.3F, false);
            this.growlTime = 200 + this.random.nextInt(200);
         }
      }


      this.oFlapTime = this.flapTime;
      if (this.isDeadOrDying()) {
         float f8 = (this.random.nextFloat() - 0.5F) * 8.0F;
         float f10 = (this.random.nextFloat() - 0.5F) * 4.0F;
         float f11 = (this.random.nextFloat() - 0.5F) * 8.0F;
         this.level().addParticle(ParticleTypes.EXPLOSION, this.getX() + (double)f8, this.getY() + 2.0D + (double)f10, this.getZ() + (double)f11, 0.0D, 0.0D, 0.0D);
      } else {
         Vec3 vec34 = this.getDeltaMovement();
         float f9 = 0.2F / ((float)vec34.horizontalDistance() * 10.0F + 1.0F);
         f9 *= (float)Math.pow(2.0D, vec34.y);
         if (this.phaseManager.getCurrentPhase().isSitting()) {
            this.flapTime += 0.1F;
         } else if (this.inWall) {
            this.flapTime += f9 * 0.5F;
         } else {
            this.flapTime += f9;
         }

         this.setYRot(Mth.wrapDegrees(this.getYRot()));
         if (this.isNoAi()) {
            this.flapTime = 0.5F;
         } else {
            if (this.posPointer < 0) {
               for(int i = 0; i < this.positions.length; ++i) {
                  this.positions[i][0] = (double)this.getYRot();
                  this.positions[i][1] = this.getY();
               }
            }

            if (++this.posPointer == this.positions.length) {
               this.posPointer = 0;
            }

            this.positions[this.posPointer][0] = (double)this.getYRot();
            this.positions[this.posPointer][1] = this.getY();
            if (this.level().isClientSide) {
               if (this.lerpSteps > 0) {
                  double d6 = this.getX() + (this.lerpX - this.getX()) / (double)this.lerpSteps;
                  double d0 = this.getY() + (this.lerpY - this.getY()) / (double)this.lerpSteps;
                  double d1 = this.getZ() + (this.lerpZ - this.getZ()) / (double)this.lerpSteps;
                  double d2 = Mth.wrapDegrees(this.lerpYRot - (double)this.getYRot());
                  this.setYRot(this.getYRot() + (float)d2 / (float)this.lerpSteps);
                  this.setXRot(this.getXRot() + (float)(this.lerpXRot - (double)this.getXRot()) / (float)this.lerpSteps);
                  --this.lerpSteps;
                  this.setPos(d6, d0, d1);
                  this.setRot(this.getYRot(), this.getXRot());
               }

               this.phaseManager.getCurrentPhase().doClientTick();
            } else {
               DragonPhaseInstance dragonphaseinstance = this.phaseManager.getCurrentPhase();
               dragonphaseinstance.doServerTick();
               if (this.phaseManager.getCurrentPhase() != dragonphaseinstance) {
                  dragonphaseinstance = this.phaseManager.getCurrentPhase();
                  dragonphaseinstance.doServerTick();
               }

               Vec3 vec3 = dragonphaseinstance.getFlyTargetLocation();
               if (vec3 != null) {
                  double d7 = vec3.x - this.getX();
                  double d8 = vec3.y - this.getY();
                  double d9 = vec3.z - this.getZ();
                  double d3 = d7 * d7 + d8 * d8 + d9 * d9;
                  float f4 = dragonphaseinstance.getFlySpeed();
                  double d4 = Math.sqrt(d7 * d7 + d9 * d9);
                  if (d4 > 0.0D) {
                     d8 = Mth.clamp(d8 / d4, (double)(-f4), (double)f4);
                  }

                  this.setDeltaMovement(this.getDeltaMovement().add(0.0D, d8 * 0.01D, 0.0D));
                  this.setYRot(Mth.wrapDegrees(this.getYRot()));
                  Vec3 vec31 = vec3.subtract(this.getX(), this.getY(), this.getZ()).normalize();
                  Vec3 vec32 = (new Vec3((double)Mth.sin(this.getYRot() * ((float)Math.PI / 180F)), this.getDeltaMovement().y, (double)(-Mth.cos(this.getYRot() * ((float)Math.PI / 180F))))).normalize();
                  float f5 = Math.max(((float)vec32.dot(vec31) + 0.5F) / 1.5F, 0.0F);
                  if (Math.abs(d7) > (double)1.0E-5F || Math.abs(d9) > (double)1.0E-5F) {
                     float f6 = Mth.clamp(Mth.wrapDegrees(180.0F - (float)Mth.atan2(d7, d9) * (180F / (float)Math.PI) - this.getYRot()), -50.0F, 50.0F);
                     this.yRotA *= 0.8F;
                     this.yRotA += f6 * dragonphaseinstance.getTurnSpeed();
                     this.setYRot(this.getYRot() + this.yRotA * 0.1F);
                  }

                  float f19 = (float)(2.0D / (d3 + 1.0D));
                  float f7 = 0.06F;
                  this.moveRelative(0.06F * (f5 * f19 + (1.0F - f19)), new Vec3(0.0D, 0.0D, -1.0D));
                  if (this.inWall) {
                     this.move(MoverType.SELF, this.getDeltaMovement().scale((double)0.8F).scale((double)dragonphaseinstance.getMoveSpeed()));
                  } else {
                     this.move(MoverType.SELF, this.getDeltaMovement().scale((double)dragonphaseinstance.getMoveSpeed()));
                  }

                  Vec3 vec33 = this.getDeltaMovement().normalize();
                  double d5 = 0.8D + 0.15D * (vec33.dot(vec32) + 1.0D) / 2.0D;
                  this.setDeltaMovement(this.getDeltaMovement().multiply(d5, (double)0.91F, d5));
               }
            }

            this.yBodyRot = this.getYRot();
            Vec3[] avec3 = new Vec3[this.subEntities.length];

            for(int j = 0; j < this.subEntities.length; ++j) {
               avec3[j] = new Vec3(this.subEntities[j].getX(), this.subEntities[j].getY(), this.subEntities[j].getZ());
            }

            float f12 = (float)(this.getLatencyPos(5, 1.0F)[1] - this.getLatencyPos(10, 1.0F)[1]) * 10.0F * ((float)Math.PI / 180F);
            float f13 = Mth.cos(f12);
            float f = Mth.sin(f12);
            float f14 = this.getYRot() * ((float)Math.PI / 180F);
            float f1 = Mth.sin(f14);
            float f15 = Mth.cos(f14);
            this.tickPart(this.body, (double)(f1 * 0.5F), 0.0D, (double)(-f15 * 0.5F));
            this.tickPart(this.wing1, (double)(f15 * 4.5F), 2.0D, (double)(f1 * 4.5F));
            this.tickPart(this.wing2, (double)(f15 * -4.5F), 2.0D, (double)(f1 * -4.5F));
            if (!this.level().isClientSide && this.hurtTime == 0) {
               this.knockBack(this.level().getEntities(this, this.wing1.getBoundingBox().inflate(6.0D, 4.0D, 6.0D).move(0.0D, -4.0D, 0.0D), EntitySelector.NO_CREATIVE_OR_SPECTATOR));
               this.knockBack(this.level().getEntities(this, this.wing2.getBoundingBox().inflate(6.0D, 4.0D, 6.0D).move(0.0D, -4.0D, 0.0D), EntitySelector.NO_CREATIVE_OR_SPECTATOR));
               this.hurt(this.level().getEntities(this, this.head.getBoundingBox().inflate(2.0D), EntitySelector.NO_CREATIVE_OR_SPECTATOR));
               this.hurt(this.level().getEntities(this, this.neck.getBoundingBox().inflate(2.0D), EntitySelector.NO_CREATIVE_OR_SPECTATOR));
            }

            float f2 = Mth.sin(this.getYRot() * ((float)Math.PI / 180F) - this.yRotA * 0.01F);
            float f16 = Mth.cos(this.getYRot() * ((float)Math.PI / 180F) - this.yRotA * 0.01F);
            float f3 = this.getHeadYOffset();
            this.tickPart(this.head, (double)(f2 * 6.5F * f13), (double)(f3 + f * 6.5F), (double)(-f16 * 6.5F * f13));
            this.tickPart(this.neck, (double)(f2 * 5.5F * f13), (double)(f3 + f * 5.5F), (double)(-f16 * 5.5F * f13));
            double[] adouble = this.getLatencyPos(5, 1.0F);

            for(int k = 0; k < 3; ++k) {
               EnderLordPart EnderLordPart = null;
               if (k == 0) {
                  EnderLordPart = this.tail1;
               }

               if (k == 1) {
                  EnderLordPart = this.tail2;
               }

               if (k == 2) {
                  EnderLordPart = this.tail3;
               }

               double[] adouble1 = this.getLatencyPos(12 + k * 2, 1.0F);
               float f17 = this.getYRot() * ((float)Math.PI / 180F) + this.rotWrap(adouble1[0] - adouble[0]) * ((float)Math.PI / 180F);
               float f18 = Mth.sin(f17);
               float f20 = Mth.cos(f17);
               float f21 = 1.5F;
               float f22 = (float)(k + 1) * 2.0F;
               this.tickPart(EnderLordPart, (double)(-(f1 * 1.5F + f18 * f22) * f13), adouble1[1] - adouble[1] - (double)((f22 + 1.5F) * f) + 1.5D, (double)((f15 * 1.5F + f20 * f22) * f13));
            }

            if (!this.level().isClientSide) {
               this.inWall = this.checkWalls(this.head.getBoundingBox()) | this.checkWalls(this.neck.getBoundingBox()) | this.checkWalls(this.body.getBoundingBox());

            }

            for(int l = 0; l < this.subEntities.length; ++l) {
               this.subEntities[l].xo = avec3[l].x;
               this.subEntities[l].yo = avec3[l].y;
               this.subEntities[l].zo = avec3[l].z;
               this.subEntities[l].xOld = avec3[l].x;
               this.subEntities[l].yOld = avec3[l].y;
               this.subEntities[l].zOld = avec3[l].z;
            }

         }
      }
   }

   private void tickPart(EnderLordPart p_31116_, double p_31117_, double p_31118_, double p_31119_) {
      p_31116_.setPos(this.getX() + p_31117_, this.getY() + p_31118_, this.getZ() + p_31119_);
   }

   private float getHeadYOffset() {
      if (this.phaseManager.getCurrentPhase().isSitting()) {
         return -1.0F;
      } else {
         double[] adouble = this.getLatencyPos(5, 1.0F);
         double[] adouble1 = this.getLatencyPos(0, 1.0F);
         return (float)(adouble[1] - adouble1[1]);
      }
   }

   private void knockBack(List<Entity> p_31132_) {
      double d0 = (this.body.getBoundingBox().minX + this.body.getBoundingBox().maxX) / 2.0D;
      double d1 = (this.body.getBoundingBox().minZ + this.body.getBoundingBox().maxZ) / 2.0D;

      for(Entity entity : p_31132_) {
         if (entity instanceof LivingEntity) {
            double d2 = entity.getX() - d0;
            double d3 = entity.getZ() - d1;
            double d4 = Math.max(d2 * d2 + d3 * d3, 0.1D);
            entity.push(d2 / d4 * 4.0D, (double)0.2F, d3 / d4 * 4.0D);
            if (!this.phaseManager.getCurrentPhase().isSitting() && ((LivingEntity)entity).getLastHurtByMobTimestamp() < entity.tickCount - 1) {
               entity.hurt(enderLordAttack(this), entity instanceof Player ? 3.5F : entity instanceof LivingEntity ? Math.max(((LivingEntity) entity).getMaxHealth() / 20.0F, 10.0F) : 10.0F);
               this.doEnchantDamageEffects(this, entity);
            }
         }
      }

   }

   private void hurt(List<Entity> p_31142_) {
      for(Entity entity : p_31142_) {
         if (entity instanceof LivingEntity && ((LivingEntity)entity).getLastHurtByMobTimestamp() < entity.tickCount - 2) {
            entity.hurt(enderLordAttack(this), entity instanceof Player ? 6.0F : entity instanceof LivingEntity ? Math.max(((LivingEntity) entity).getMaxHealth() / 10.0F, 20.0F) : 20.0F);
            this.doEnchantDamageEffects(this, entity);
         }
      }

   }

   public static DamageSource enderLordAttack(Entity entity) {
      return new DamageSource(entity.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("endless_deep_space:ender_lord_attack"))), entity);
   }

   private float rotWrap(double p_31165_) {
      return (float)Mth.wrapDegrees(p_31165_);
   }

   private boolean checkWalls(AABB p_31140_) {
      int i = Mth.floor(p_31140_.minX);
      int j = Mth.floor(p_31140_.minY);
      int k = Mth.floor(p_31140_.minZ);
      int l = Mth.floor(p_31140_.maxX);
      int i1 = Mth.floor(p_31140_.maxY);
      int j1 = Mth.floor(p_31140_.maxZ);
      boolean flag = false;
      boolean flag1 = false;

      for(int k1 = i; k1 <= l; ++k1) {
         for(int l1 = j; l1 <= i1; ++l1) {
            for(int i2 = k; i2 <= j1; ++i2) {
               BlockPos blockpos = new BlockPos(k1, l1, i2);
               BlockState blockstate = this.level().getBlockState(blockpos);
               if (this.level().getLevelData().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && ModCommonConfig.ENDER_LORD_BREAK_BLOCKS.get()) {
                  flag1 = this.level().removeBlock(blockpos, false) || flag1;
               } else {
                  flag = true;
               }
            }
         }
      }

      if (flag1) {
         BlockPos blockpos1 = new BlockPos(i + this.random.nextInt(l - i + 1), j + this.random.nextInt(i1 - j + 1), k + this.random.nextInt(j1 - k + 1));
         this.level().levelEvent(2008, blockpos1, 0);
      }

      return flag;
   }

   public boolean hurt(EnderLordPart p_31121_, DamageSource p_31122_, float p_31123_) {
      if (p_31122_.getEntity() != null) {
      	  if (p_31122_.getEntity() instanceof LivingEntity && !this.level().isClientSide) {
      	  	 LivingEntity entity = (LivingEntity)p_31122_.getEntity();
      	  	 if (entity != this) {
      	  	 	this.attackTarget = entity;
                this.setAttackTarget(entity.getId());
      	  	 }
      	  }
      }
      
      if (this.phaseManager.getCurrentPhase().getPhase() == EnderDragonPhase.DYING || this.phaseManager.getCurrentPhase().getPhase() == EnderDragonPhase.EXPLOSION) {
         return false;
      } else {
         p_31123_ = this.phaseManager.getCurrentPhase().onHurt(p_31122_, p_31123_);
         if (p_31121_ == this.head && (p_31122_.getDirectEntity() == null || p_31122_.getEntity() != p_31122_.getDirectEntity())) {
            p_31123_ = p_31123_ * 4.0F;
         } else {
         	 p_31123_ = p_31123_ / 4.0F + Math.min(p_31123_, 1.0F);
         }

         if (p_31123_ < 0.01F) {
            return false;
         } else {
            float f = this.getHealth();
            if (p_31122_.getEntity() != null) {
               
               if (p_31122_.getDirectEntity() != null && p_31122_.getEntity() != p_31122_.getDirectEntity()) {
                  this.setAttackTimes(this.getAttackTimes() + 3);
                  p_31123_ = p_31123_ / 4.0F;
               }
               
               if (p_31122_.getEntity() instanceof Player) {
               	   this.reallyHurt(p_31122_, p_31123_);
                   if (this.getProgressTwo() && this.hasShield()) {
                      ColorfulLightningBolt lightningBolt = new ColorfulLightningBolt(this.level(), p_31122_.getEntity().getX(), p_31122_.getEntity().getY(), p_31122_.getEntity().getZ(), 0.5F, false);
                      this.level().addFreshEntity(lightningBolt);
                   }
                   if (!this.hasShield() && this.getProgressTwo() && this.getHealth() - p_31123_ <= 0.0F) {
                     this.reallySetDragonHealth(1.0F);
                     this.phaseManager.setPhase(EnderDragonPhase.DYING);
                   }
               } else if (!ModCommonConfig.ENDER_LORD_BYPASS_NONPLAYER_DAMAGE.get()) {
                  if (this.invulnerableTime <= 0 && this.phaseManager.getCurrentPhase().getPhase() != EnderDragonPhase.DYING) {
                     this.reallyHurt(p_31122_, Math.min(20, p_31123_ / 4.0F));
                     if (this.getProgressTwo() && this.hasShield()) {
                        ColorfulLightningBolt lightningBolt = new ColorfulLightningBolt(this.level(), p_31122_.getEntity().getX(), p_31122_.getEntity().getY(), p_31122_.getEntity().getZ(), p_31123_ / 4.0F, false);
                        this.level().addFreshEntity(lightningBolt);
                     }
                     this.invulnerableTime = 40;
                     p_31122_.getEntity().hurt(enderLordAttack(this), p_31123_ / 2.0F);
                     p_31122_.getEntity().invulnerableTime = 0;
                  } else {
                     return false;
                  }
               } else {
                  return false;
               }
            } else {
               return false;
            }

            if (this.phaseManager.getCurrentPhase().isSitting()) {
               this.sittingDamageReceived = this.sittingDamageReceived + f - this.getHealth();
               if (this.sittingDamageReceived > 0.25F * this.getMaxHealth()) {
                  this.sittingDamageReceived = 0.0F;
                  this.phaseManager.setPhase(EnderDragonPhase.TAKEOFF);
               }
            }

            return true;
         }
      }
   }

   public boolean hurt(DamageSource p_31113_, float p_31114_) {
      return !this.level().isClientSide && this.hurt(this.body, p_31113_, p_31114_);
   }

   protected boolean reallyHurt(DamageSource p_31162_, float p_31163_) {
      if (p_31162_.getEntity() != null) {
      	  if (p_31162_.getEntity() == this) {
      	  	  return false;
      	  }
      }

      if (this.phaseManager.getCurrentPhase().getPhase() == EnderDragonPhase.TRACKING_ENTITY) {
      	 p_31163_ = p_31163_ * 4.0F;
      }

      return super.hurt(p_31162_, p_31163_);
   }

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
            this.setDragonHealth(this.getHealth() - f1);
            this.setAbsorptionAmount(this.getAbsorptionAmount() - f1);
            this.gameEvent(GameEvent.ENTITY_DAMAGE);
         }
      }
   }

   public void heal(float p_21116_) {
      p_21116_ = net.minecraftforge.event.ForgeEventFactory.onLivingHeal(this, p_21116_);
      if (p_21116_ <= 0) return;
      float f = this.getHealth();
      if (f > 0.0F) {
         this.setDragonHealth(f + p_21116_);
      }

   }

   public boolean isAlive() {
      return !this.isRemoved() && (!this.getCanBeDead() || this.hasShield());
   }

   public boolean isDeadOrDying() {
      return this.getHealth() <= 0.0F && this.getCanBeDead() && !this.hasShield();
   }

   public void die(DamageSource source) {
      if (this.getCanBeDead()) {
         this.dragonEvent.setProgress(0.0F);
         this.setChargingTime(0);
         this.kill();
      }
   }

   public void setDragonHealth(float value) {
      if (this.getPhaseManager() != null && (value < this.getHealth() || (this.hasShield() && (this.getHealth() - value) < this.getShield() && (this.getHealth() - value) > 0.0F))) {
         this.setAttackTimes(this.getAttackTimes() + 1);
      }

   	  if (this.phaseManager != null && (this.phaseManager.getCurrentPhase().getPhase() == EnderDragonPhase.DYING || this.phaseManager.getCurrentPhase().getPhase() == EnderDragonPhase.EXPLOSION)) {
            return;
   	  } else {
         if (this.hasShield() && this.getPhaseManager() != null) {
            this.setShield(this.getShield() - Math.max(this.getHealth() - value, 0.0F));
            return;
         } else {
            this.reallySetDragonHealth(value);
            if (!this.getProgressTwo() && value <= this.getMaxHealth() / 2) {
               this.getPhaseManager().setPhase(EnderDragonPhase.EXPLOSION);
               this.reallySetDragonHealth(this.getMaxHealth() / 2);
               this.setAttackTimes(30);
            }
            return;
         }
   	  }
   }

   public void reallySetDragonHealth(float value) {
      this.getEntityData().set(DATA_HEALTH, Mth.clamp(value, 0.0F, this.getMaxHealth()));
   }

   public void setHealth(float value) {
      return;
   }

   public float getHealth() {
      if (this.hasShield()) {
         if (this.getProgressTwo()) {
            return this.getMaxHealth() / 2.0F;
         } else {
            return this.getMaxHealth();
         }
      }
      return this.getEntityData().get(DATA_HEALTH);
   }

   public void kill() {
      if (this.phaseManager.getCurrentPhase().getPhase() != EnderDragonPhase.DYING) {
         this.phaseManager.setPhase(EnderDragonPhase.DYING);
      }
   }

   protected void tickDeath() {
      ++this.dragonDeathTime;
      if (this.dragonDeathTime >= 780 && this.dragonDeathTime <= 800) {
         float f = (this.random.nextFloat() - 0.5F) * 8.0F;
         float f1 = (this.random.nextFloat() - 0.5F) * 4.0F;
         float f2 = (this.random.nextFloat() - 0.5F) * 8.0F;
         this.level().addParticle(ParticleTypes.EXPLOSION_EMITTER, this.getX() + (double) f, this.getY() + 2.0D + (double) f1, this.getZ() + (double) f2, 0.0D, 0.0D, 0.0D);
      }

      boolean flag = this.level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT);
      int i = 1000000;

      if (this.level() instanceof ServerLevel) {
         TrackingUtil.forceEffect(this, LivingEntity.class, true, 200, (double) this.dragonDeathTime / 4, 200.0D);
         List<Entity> _entfound = this.level().getEntitiesOfClass(Entity.class, new AABB(new Vec3(this.getX(), this.getY(), this.getZ()), new Vec3(this.getX(), this.getY(), this.getZ())).inflate(200 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(new Vec3(this.getX(), this.getY(), this.getZ())))).toList();
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

                  if (entity instanceof LivingEntity && !(entity instanceof Player) && !(entity instanceof EnderLord)) {
                     LivingEntity livingentity = (LivingEntity) entity;
                     for (int j = 0; j < 20; j++) {
                        livingentity.hurt(EnderLord.enderLordAttack(this), Float.MAX_VALUE);
                        OceanDefender.actuallyHurt(livingentity, EnderLord.enderLordAttack(this), Float.MAX_VALUE, true);;
                        livingentity.invulnerableTime = 0;
                        livingentity.removeAllEffects();
                        livingentity.setHealth(0);
                        OceanDefender.setIsDying(livingentity, true);
                        livingentity.die(EnderLord.enderLordAttack(this));
                     }
                  }

               }
            }
         }
         if (this.dragonDeathTime > 750 && this.dragonDeathTime % 5 == 0 && flag) {
            int award = net.minecraftforge.event.ForgeEventFactory.getExperienceDrop(this, this.unlimitedLastHurtByPlayer, Mth.floor((float) i * 0.08F));
            ExperienceOrb.award((ServerLevel) this.level(), this.position(), award);
         }

         if (this.dragonDeathTime == 601 && !this.isSilent()) {
            this.level().globalLevelEvent(1028, this.blockPosition(), 0);
         }
      }

      this.move(MoverType.SELF, new Vec3(0.0D, (double) 0.016F, 0.0D));
      if (this.dragonDeathTime == 800 && this.level() instanceof ServerLevel) {
         if (flag) {
            int award = net.minecraftforge.event.ForgeEventFactory.getExperienceDrop(this, this.unlimitedLastHurtByPlayer, Mth.floor((float) i * 0.2F));
            ExperienceOrb.award((ServerLevel) this.level(), this.position(), award);
            this.dropAllDeathLoot(this.getLastDamageSource() == null ? EnderLord.enderLordAttack(this) : this.getLastDamageSource());
         }

         List<LivingEntity> _entfound = this.level().getEntitiesOfClass(LivingEntity.class, new AABB(new Vec3(this.getX(), this.getY(), this.getZ()), new Vec3(this.getX(), this.getY(), this.getZ())).inflate(400 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(new Vec3(this.getX(), this.getY(), this.getZ())))).toList();
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
            } else if (entityiterator instanceof ServerPlayer player) {
                ((ServerPlayerAccessor)player).setSeenCredits(false);
             }
         }

         this.reallyRemove(Entity.RemovalReason.KILLED);
         this.gameEvent(GameEvent.ENTITY_DIE);
      }

   }
   
   @Override
   public boolean canUpdate() {
      return true;
   }
   
   public void remove(Entity.RemovalReason p_146834_) {
      if (!p_146834_.shouldDestroy()) {
         this.reallyRemove(p_146834_);
      }
   }

   public void reallyRemove(Entity.RemovalReason p_146834_) {
      super.remove(p_146834_);
   }

   public int findClosestNode() {
      if (this.nodes[0] == null) {
         for(int i = 0; i < 24; ++i) {
            int j = 5;
            int l;
            int i1;
            if (i < 12) {
               l = Mth.floor(60.0F * Mth.cos(2.0F * (-(float)Math.PI + 0.2617994F * (float)i)));
               i1 = Mth.floor(60.0F * Mth.sin(2.0F * (-(float)Math.PI + 0.2617994F * (float)i)));
            } else if (i < 20) {
               int $$2 = i - 12;
               l = Mth.floor(40.0F * Mth.cos(2.0F * (-(float)Math.PI + ((float)Math.PI / 8F) * (float)$$2)));
               i1 = Mth.floor(40.0F * Mth.sin(2.0F * (-(float)Math.PI + ((float)Math.PI / 8F) * (float)$$2)));
               j += 10;
            } else {
               int k1 = i - 20;
               l = Mth.floor(20.0F * Mth.cos(2.0F * (-(float)Math.PI + ((float)Math.PI / 4F) * (float)k1)));
               i1 = Mth.floor(20.0F * Mth.sin(2.0F * (-(float)Math.PI + ((float)Math.PI / 4F) * (float)k1)));
            }

            int j1 = Math.max(this.level().getSeaLevel() + 10 + ModCommonConfig.ENDER_LORD_FLIGHT_HEIGHT.get(), this.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(l, 0, i1)).getY() + j + ModCommonConfig.ENDER_LORD_FLIGHT_HEIGHT.get());
            this.nodes[i] = new Node(this.getFightOrigin().getX() + l, j1, this.getFightOrigin().getZ() + i1);
            LOGGER.debug("Path {} generation completed. {}", i, this.nodes[i]);
         }

         this.nodeAdjacency[0] = 6146;
         this.nodeAdjacency[1] = 8197;
         this.nodeAdjacency[2] = 8202;
         this.nodeAdjacency[3] = 16404;
         this.nodeAdjacency[4] = 32808;
         this.nodeAdjacency[5] = 32848;
         this.nodeAdjacency[6] = 65696;
         this.nodeAdjacency[7] = 131392;
         this.nodeAdjacency[8] = 131712;
         this.nodeAdjacency[9] = 263424;
         this.nodeAdjacency[10] = 526848;
         this.nodeAdjacency[11] = 525313;
         this.nodeAdjacency[12] = 1581057;
         this.nodeAdjacency[13] = 3166214;
         this.nodeAdjacency[14] = 2138120;
         this.nodeAdjacency[15] = 6373424;
         this.nodeAdjacency[16] = 4358208;
         this.nodeAdjacency[17] = 12910976;
         this.nodeAdjacency[18] = 9044480;
         this.nodeAdjacency[19] = 9706496;
         this.nodeAdjacency[20] = 15216640;
         this.nodeAdjacency[21] = 13688832;
         this.nodeAdjacency[22] = 11763712;
         this.nodeAdjacency[23] = 8257536;

         
      }

      return this.findClosestNode(this.getX(), this.getY(), this.getZ());
   }

   public int findClosestNode(double p_31171_, double p_31172_, double p_31173_) {
      float f = 10000.0F;
      int i = 0;
      Node node = new Node(Mth.floor(p_31171_), Mth.floor(p_31172_), Mth.floor(p_31173_));
      int j = 0;
      if (this.getProgressTwo()) {
         j = 12;
      }

      for(int k = j; k < 24; ++k) {
         if (this.nodes[k] != null) {
            float f1 = this.nodes[k].distanceToSqr(node);
            if (f1 < f) {
               f = f1;
               i = k;
            }
         }
      }

      return i;
   }

   @Nullable
   public Path findPath(int p_31105_, int p_31106_, @Nullable Node p_31107_) {
      for(int i = 0; i < 24; ++i) {
         Node node = this.nodes[i];
         node.closed = false;
         node.f = 0.0F;
         node.g = 0.0F;
         node.h = 0.0F;
         node.cameFrom = null;
         node.heapIdx = -1;
      }

      Node node4 = this.nodes[p_31105_];
      Node node5 = this.nodes[p_31106_];
      node4.g = 0.0F;
      node4.h = node4.distanceTo(node5);
      node4.f = node4.h;
      this.openSet.clear();
      this.openSet.insert(node4);
      Node node1 = node4;
      int j = 0;
      if (this.getProgressTwo()) {
         j = 12;
      }

      while(!this.openSet.isEmpty()) {
         Node node2 = this.openSet.pop();
         if (node2.equals(node5)) {
            if (p_31107_ != null) {
               p_31107_.cameFrom = node5;
               node5 = p_31107_;
            }

            return this.reconstructPath(node4, node5);
         }

         if (node2.distanceTo(node5) < node1.distanceTo(node5)) {
            node1 = node2;
         }

         node2.closed = true;
         int k = 0;

         for(int l = 0; l < 24; ++l) {
            if (this.nodes[l] == node2) {
               k = l;
               break;
            }
         }

         for(int i1 = j; i1 < 24; ++i1) {
            if ((this.nodeAdjacency[k] & 1 << i1) > 0) {
               Node node3 = this.nodes[i1];
               if (!node3.closed) {
                  float f = node2.g + node2.distanceTo(node3);
                  if (!node3.inOpenSet() || f < node3.g) {
                     node3.cameFrom = node2;
                     node3.g = f;
                     node3.h = node3.distanceTo(node5);
                     if (node3.inOpenSet()) {
                        this.openSet.changeCost(node3, node3.g + node3.h);
                     } else {
                        node3.f = node3.g + node3.h;
                        this.openSet.insert(node3);
                     }
                  }
               }
            }
         }
      }

      if (node1 == node4) {
         return null;
      } else {
         LOGGER.debug("Failed to find path from {} to {}", p_31105_, p_31106_);
         if (p_31107_ != null) {
            p_31107_.cameFrom = node1;
            node1 = p_31107_;
         }

         return this.reconstructPath(node4, node1);
      }
   }

   private Path reconstructPath(Node p_31129_, Node p_31130_) {
      List<Node> list = Lists.newArrayList();
      Node node = p_31130_;
      list.add(0, p_31130_);

      while(node.cameFrom != null) {
         node = node.cameFrom;
         list.add(0, node);
      }

      return new Path(list, new BlockPos(p_31130_.x, p_31130_.y, p_31130_.z), true);
   }

   public void addAdditionalSaveData(CompoundTag p_31144_) {
      super.addAdditionalSaveData(p_31144_);
      p_31144_.putInt("DragonPhase", this.phaseManager.getCurrentPhase().getPhase().getId());
      p_31144_.putInt("DragonDeathTime", this.dragonDeathTime);
      p_31144_.put("FightOrigin", NbtUtils.writeBlockPos(this.getFightOrigin()));
      p_31144_.putInt("HangTime", this.getHangTime());
      p_31144_.putInt("FlyAttackCooldown", this.getFlyAttackCooldown());
      p_31144_.putInt("DragomChargingTime", this.getChargingTime());
      p_31144_.putInt("AttackTimes", this.getAttackTimes());
      p_31144_.putBoolean("CanBeDead", this.getCanBeDead());
      p_31144_.putBoolean("ProgressTwo", this.getProgressTwo());
      p_31144_.putFloat("Shield", this.getShield());
      p_31144_.putFloat("DragonHealth", this.getHealth());
   }

   public void readAdditionalSaveData(CompoundTag p_31134_) {
      super.readAdditionalSaveData(p_31134_);
      if (p_31134_.contains("DragonPhase")) {
         this.phaseManager.setPhase(EnderDragonPhase.getById(p_31134_.getInt("DragonPhase")));
      }

      if (p_31134_.contains("DragonDeathTime")) {
         this.dragonDeathTime = p_31134_.getInt("DragonDeathTime");
      }

      if (p_31134_.contains("FightOrigin")) {
      	  this.setFightOrigin(NbtUtils.readBlockPos(p_31134_.getCompound("FightOrigin")));
      }

      if (p_31134_.contains("HangTime")) {
      	  this.setHangTime(p_31134_.getInt("HangTime"));
      }

      if (p_31134_.contains("FlyAttackCooldown")) {
      	  this.setFlyAttackCooldown(p_31134_.getInt("FlyAttackCooldown"));
      }

      if (p_31134_.contains("DragomChargingTime")) {
         this.setChargingTime(p_31134_.getInt("DragomChargingTime"));
      }

      if (p_31134_.contains("AttackTimes")) {
         this.setAttackTimes(p_31134_.getInt("AttackTimes"));
      }

      if (p_31134_.contains("CanBeDead")) {
         this.setCanBeDead(p_31134_.getBoolean("CanBeDead"));
      }

      if (p_31134_.contains("ProgressTwo")) {
         this.setProgressTwo(p_31134_.getBoolean("ProgressTwo"));
      }

      if (p_31134_.contains("Shield")) {
         this.setShield(p_31134_.getFloat("Shield"));
      }

      if (p_31134_.contains("DragonHealth", 99)) {
         this.reallySetDragonHealth(p_31134_.getFloat("DragonHealth"));
      }

   }

   @Nullable
   public LivingEntity getTarget() {
      if (this.level().isClientSide) {
         return this.getActiveAttackTarget();
      }

      return this.attackTarget;
   }

   public EnderLordPart[] getSubEntities() {
      return this.subEntities;
   }

   public boolean isPickable() {
      return false;
   }

   public SoundSource getSoundSource() {
      return SoundSource.HOSTILE;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENDER_DRAGON_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource p_31154_) {
      return SoundEvents.ENDER_DRAGON_HURT;
   }

   protected float getSoundVolume() {
      return 5.0F;
   }

   public float getHeadPartYOffset(int p_31109_, double[] p_31110_, double[] p_31111_) {
      DragonPhaseInstance dragonphaseinstance = this.phaseManager.getCurrentPhase();
      EnderDragonPhase<? extends DragonPhaseInstance> enderdragonphase = dragonphaseinstance.getPhase();
      double d0;
      if (enderdragonphase != EnderDragonPhase.LANDING && enderdragonphase != EnderDragonPhase.TAKEOFF) {
         if (dragonphaseinstance.isSitting()) {
            d0 = (double)p_31109_;
         } else if (p_31109_ == 6) {
            d0 = 0.0D;
         } else {
            d0 = p_31111_[1] - p_31110_[1];
         }
      } else {
         BlockPos blockpos = this.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, this.getFightOrigin());
         double d1 = Math.max(Math.sqrt(blockpos.distToCenterSqr(this.position())) / 4.0D, 1.0D);
         d0 = (double)p_31109_ / d1;
      }

      return (float)d0;
   }

   public Vec3 getHeadLookVector(float p_31175_) {
      DragonPhaseInstance dragonphaseinstance = this.phaseManager.getCurrentPhase();
      EnderDragonPhase<? extends DragonPhaseInstance> enderdragonphase = dragonphaseinstance.getPhase();
      Vec3 vec3;
      if (enderdragonphase != EnderDragonPhase.LANDING && enderdragonphase != EnderDragonPhase.TAKEOFF) {
         if (dragonphaseinstance.isSitting()) {
            float f4 = this.getXRot();
            float f5 = 1.5F;
            this.setXRot(-45.0F);
            vec3 = this.getViewVector(p_31175_);
            this.setXRot(f4);
         } else {
            vec3 = this.getViewVector(p_31175_);
         }
      } else {
         BlockPos blockpos = this.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, this.getFightOrigin());
         float f = Math.max((float)Math.sqrt(blockpos.distToCenterSqr(this.position())) / 4.0F, 1.0F);
         float f1 = 6.0F / f;
         float f2 = this.getXRot();
         float f3 = 1.5F;
         this.setXRot(-f1 * 1.5F * 5.0F);
         vec3 = this.getViewVector(p_31175_);
         this.setXRot(f2);
      }

      return vec3;
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> p_31136_) {
      if (DATA_PHASE.equals(p_31136_) && this.level().isClientSide) {
         this.phaseManager.setPhase(EnderDragonPhase.getById(this.getEntityData().get(DATA_PHASE)));
      }

      if (DATA_ATTACK_TARGET.equals(p_31136_)) {
         this.clientSideAttackTarget = null;
      }

      super.onSyncedDataUpdated(p_31136_);
   }

   public EnderDragonPhaseManager getPhaseManager() {
      return this.phaseManager;
   }

   public boolean canAttack(LivingEntity p_149576_) {
      return p_149576_.canBeSeenAsEnemy();
   }

   public double getPassengersRidingOffset() {
      return (double)this.body.getBbHeight();
   }

   public boolean isPushable() {
      return false;
   }

   protected Entity.MovementEmission getMovementEmission() {
      return Entity.MovementEmission.NONE;
   }

   public boolean isPushedByFluid() {
      return false;
   }

   public boolean ignoreExplosion() {
      return true;
   }

}
