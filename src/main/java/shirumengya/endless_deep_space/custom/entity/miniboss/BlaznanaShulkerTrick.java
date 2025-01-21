package shirumengya.endless_deep_space.custom.entity.miniboss;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import shirumengya.endless_deep_space.custom.client.event.CustomServerBossEvent;
import shirumengya.endless_deep_space.custom.entity.AttackTimesShieldEntity;
import shirumengya.endless_deep_space.custom.entity.boss.enderlord.EnderLord;
import shirumengya.endless_deep_space.custom.entity.boss.oceandefenders.OceanDefender;
import shirumengya.endless_deep_space.custom.entity.projectile.BlaznanaShulkerTrickBullet;
import shirumengya.endless_deep_space.custom.event.SwordBlockEvent;
import shirumengya.endless_deep_space.custom.util.entity.TrackingUtil;
import shirumengya.endless_deep_space.custom.world.explosion.CustomExplosion;

import javax.annotation.Nullable;
import java.util.*;

public class BlaznanaShulkerTrick extends AbstractGolem implements Enemy , AttackTimesShieldEntity {
   private static final UUID COVERED_ARMOR_MODIFIER_UUID = UUID.fromString("7E0292F2-9434-48D5-A29F-9583AF7DF27F");
   private static final AttributeModifier COVERED_ARMOR_MODIFIER = new AttributeModifier(COVERED_ARMOR_MODIFIER_UUID, "Covered armor bonus", 20.0D, AttributeModifier.Operation.ADDITION);
   protected static final EntityDataAccessor<Direction> DATA_ATTACH_FACE_ID = SynchedEntityData.defineId(BlaznanaShulkerTrick.class, EntityDataSerializers.DIRECTION);
   protected static final EntityDataAccessor<Byte> DATA_PEEK_ID = SynchedEntityData.defineId(BlaznanaShulkerTrick.class, EntityDataSerializers.BYTE);
   public static final EntityDataAccessor<Integer> DATA_SHIELD_ATTACK_TIMES = SynchedEntityData.defineId(BlaznanaShulkerTrick.class, EntityDataSerializers.INT);
   private static final int TELEPORT_STEPS = 6;
   private static final byte NO_COLOR = 16;
   private static final byte DEFAULT_COLOR = 16;
   private static final int MAX_TELEPORT_DISTANCE = 8;
   private static final int OTHER_SHULKER_SCAN_RADIUS = 8;
   private static final int OTHER_SHULKER_LIMIT = 5;
   private static final float PEEK_PER_TICK = 0.05F;
   static final Vector3f FORWARD = Util.make(() -> {
      Vec3i vec3i = Direction.SOUTH.getNormal();
      return new Vector3f((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ());
   });
   private float currentPeekAmountO;
   private float currentPeekAmount;
   @Nullable
   private BlockPos clientOldAttachPosition;
   private int clientSideTeleportInterpolation;
   private static final float MAX_LID_OPEN = 1.0F;
   private final CustomServerBossEvent bossEvent = new CustomServerBossEvent(this, this.getDisplayName(), BossEvent.BossBarColor.RED, false, 11);
   private final CustomServerBossEvent bossEventShieldAttackTimes = new CustomServerBossEvent(Component.empty(), Component.empty(), BossEvent.BossBarColor.RED, false, 12);
   public int blaznanaShulkerTrickDeathTime;

   public BlaznanaShulkerTrick(EntityType<? extends BlaznanaShulkerTrick> p_33404_, Level p_33405_) {
      super(p_33404_, p_33405_);
      this.xpReward = 0;
      this.lookControl = new BlaznanaShulkerTrick.ShulkerLookControl(this);
      this.noCulling = true;
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F, 0.02F, true));
      this.goalSelector.addGoal(4, new BlaznanaShulkerTrick.ShulkerAttackGoal());
      this.goalSelector.addGoal(7, new BlaznanaShulkerTrick.ShulkerPeekGoal());
      this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
      this.targetSelector.addGoal(1, (new HurtByTargetGoal(this, this.getClass())).setAlertOthers());
      this.targetSelector.addGoal(2, new BlaznanaShulkerTrick.ShulkerNearestAttackGoal(this));
      this.targetSelector.addGoal(3, new BlaznanaShulkerTrick.ShulkerDefenseAttackGoal(this));
   }

   protected Entity.MovementEmission getMovementEmission() {
      return Entity.MovementEmission.NONE;
   }

   public SoundSource getSoundSource() {
      return SoundSource.HOSTILE;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.SHULKER_AMBIENT;
   }

   public void playAmbientSound() {
      if (!this.isClosed()) {
         super.playAmbientSound();
      }

   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.SHULKER_DEATH;
   }

   protected SoundEvent getHurtSound(DamageSource p_33457_) {
      return this.isClosed() ? SoundEvents.SHULKER_HURT_CLOSED : SoundEvents.SHULKER_HURT;
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_ATTACH_FACE_ID, Direction.DOWN);
      this.entityData.define(DATA_PEEK_ID, (byte)0);
      this.entityData.define(DATA_SHIELD_ATTACK_TIMES, 0);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 5750056.0F * 2.0F);
   }

   protected BodyRotationControl createBodyControl() {
      return new BlaznanaShulkerTrick.ShulkerBodyRotationControl(this);
   }

   public void readAdditionalSaveData(CompoundTag p_33432_) {
      super.readAdditionalSaveData(p_33432_);
      this.setAttachFace(Direction.from3DDataValue(p_33432_.getByte("AttachFace")));
      this.entityData.set(DATA_PEEK_ID, p_33432_.getByte("Peek"));
      this.setShield(p_33432_.getInt("ShieldAttackTimes"));
      this.blaznanaShulkerTrickDeathTime = p_33432_.getInt("BlaznanaShulkerTrickDeathTime");
      if (this.hasCustomName()) {
         this.bossEvent.setName(this.getDisplayName());
      }
   }

   public void addAdditionalSaveData(CompoundTag p_33443_) {
      super.addAdditionalSaveData(p_33443_);
      p_33443_.putByte("AttachFace", (byte)this.getAttachFace().get3DDataValue());
      p_33443_.putByte("Peek", this.entityData.get(DATA_PEEK_ID));
      p_33443_.putInt("ShieldAttackTimes", this.getShield());
      p_33443_.putInt("BlaznanaShulkerTrickDeathTime", this.blaznanaShulkerTrickDeathTime);
   }

   public void setCustomName(@Nullable Component p_31476_) {
      super.setCustomName(p_31476_);
      this.bossEvent.setName(this.getDisplayName());
   }

   public void tick() {
      super.tick();
      if (!this.level().isClientSide) {
         this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
         if (this.hasCustomName() && (("奇想林中的蕉研组".equals(this.getName().getString())) || ("心蕉如火的猴把戏".equals(this.getName().getString())))) {
            this.bossEvent.setDescription(Component.translatable("entity.endless_deep_space.blaznana_shulker_trick.description.easter_egg"));
         } else {
            this.bossEvent.setDescription(Component.translatable(this.getType().getDescriptionId() + ".description"));
         }
         this.bossEventShieldAttackTimes.setProgress((float) this.getShield() / this.getMaxShield());
      }

      if (!this.level().isClientSide && !this.isPassenger() && !this.canStayAt(this.blockPosition(), this.getAttachFace())) {
         this.findNewAttachment();
      }

      if (this.updatePeekAmount()) {
         this.onPeekAmountChange();
      }

      if (this.level().isClientSide) {
         if (this.clientSideTeleportInterpolation > 0) {
            --this.clientSideTeleportInterpolation;
         } else {
            this.clientOldAttachPosition = null;
         }
      }

   }

   @Override
   public void startSeenByPlayer(ServerPlayer p_20119_) {
      super.startSeenByPlayer(p_20119_);
      this.bossEvent.addPlayer(p_20119_);
      this.bossEventShieldAttackTimes.addPlayer(p_20119_);
   }

   @Override
   public void stopSeenByPlayer(ServerPlayer p_20174_) {
      super.stopSeenByPlayer(p_20174_);
      this.bossEvent.removePlayer(p_20174_);
      this.bossEventShieldAttackTimes.removePlayer(p_20174_);
   }

   private void findNewAttachment() {
      Direction direction = this.findAttachableSurface(this.blockPosition());
      if (direction != null) {
         this.setAttachFace(direction);
      } else {
         this.teleportSomewhere();
      }

   }

   protected AABB makeBoundingBox() {
      float f = getPhysicalPeek(this.currentPeekAmount);
      Direction direction = this.getAttachFace().getOpposite();
      float f1 = this.getType().getWidth() / 2.0F;
      return getProgressAabb(direction, f).move(this.getX() - (double)f1, this.getY(), this.getZ() - (double)f1);
   }

   private static float getPhysicalPeek(float p_149769_) {
      return 0.5F - Mth.sin((0.5F + p_149769_) * (float)Math.PI) * 0.5F;
   }

   private boolean updatePeekAmount() {
      this.currentPeekAmountO = this.currentPeekAmount;
      float f = (float)this.getRawPeekAmount() * 0.01F;
      if (this.currentPeekAmount == f) {
         return false;
      } else {
         if (this.currentPeekAmount > f) {
            this.currentPeekAmount = Mth.clamp(this.currentPeekAmount - 0.05F, f, 1.0F);
         } else {
            this.currentPeekAmount = Mth.clamp(this.currentPeekAmount + 0.05F, 0.0F, f);
         }

         return true;
      }
   }

   private void onPeekAmountChange() {
      this.reapplyPosition();
      float f = getPhysicalPeek(this.currentPeekAmount);
      float f1 = getPhysicalPeek(this.currentPeekAmountO);
      Direction direction = this.getAttachFace().getOpposite();
      float f2 = f - f1;
      if (!(f2 <= 0.0F)) {
         for(Entity entity : this.level().getEntities(this, getProgressDeltaAabb(direction, f1, f).move(this.getX() - 0.5D, this.getY(), this.getZ() - 0.5D), EntitySelector.NO_SPECTATORS.and((p_149771_) -> {
            return !p_149771_.isPassengerOfSameVehicle(this);
         }))) {
            if (!(entity instanceof Shulker || entity instanceof BlaznanaShulkerTrick) && !entity.noPhysics) {
               entity.move(MoverType.SHULKER, new Vec3((double)(f2 * (float)direction.getStepX()), (double)(f2 * (float)direction.getStepY()), (double)(f2 * (float)direction.getStepZ())));
            }
         }

      }
   }

   public static AABB getProgressAabb(Direction p_149791_, float p_149792_) {
      return getProgressDeltaAabb(p_149791_, -1.0F, p_149792_);
   }

   public static AABB getProgressDeltaAabb(Direction p_149794_, float p_149795_, float p_149796_) {
      double d0 = (double)Math.max(p_149795_, p_149796_);
      double d1 = (double)Math.min(p_149795_, p_149796_);
      return (new AABB(BlockPos.ZERO)).expandTowards((double)p_149794_.getStepX() * d0, (double)p_149794_.getStepY() * d0, (double)p_149794_.getStepZ() * d0).contract((double)(-p_149794_.getStepX()) * (1.0D + d1), (double)(-p_149794_.getStepY()) * (1.0D + d1), (double)(-p_149794_.getStepZ()) * (1.0D + d1));
   }

   public double getMyRidingOffset() {
      EntityType<?> entitytype = this.getVehicle().getType();
      return !(this.getVehicle() instanceof Boat) && entitytype != EntityType.MINECART ? super.getMyRidingOffset() : 0.1875D - this.getVehicle().getPassengersRidingOffset();
   }

   public boolean startRiding(Entity p_149773_, boolean p_149774_) {
      if (this.level().isClientSide()) {
         this.clientOldAttachPosition = null;
         this.clientSideTeleportInterpolation = 0;
      }

      this.setAttachFace(Direction.DOWN);
      return super.startRiding(p_149773_, p_149774_);
   }

   public void stopRiding() {
      super.stopRiding();
      if (this.level().isClientSide) {
         this.clientOldAttachPosition = this.blockPosition();
      }

      this.yBodyRotO = 0.0F;
      this.yBodyRot = 0.0F;
   }

   @Nullable
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_149780_, DifficultyInstance p_149781_, MobSpawnType p_149782_, @Nullable SpawnGroupData p_149783_, @Nullable CompoundTag p_149784_) {
      this.setYRot(0.0F);
      this.yHeadRot = this.getYRot();
      this.setOldPosAndRot();
      if (RandomSource.create().nextFloat() < 1.0E-4D) {
         this.setCustomName(Component.literal(RandomSource.create().nextFloat() < 0.5F ? "奇想林中的蕉研组" : "心蕉如火的猴把戏"));
      }
      return super.finalizeSpawn(p_149780_, p_149781_, p_149782_, p_149783_, p_149784_);
   }

   public void move(MoverType p_33424_, Vec3 p_33425_) {
      if (p_33424_ == MoverType.SHULKER_BOX) {
         this.teleportSomewhere();
      } else {
         super.move(p_33424_, p_33425_);
      }

   }

   public Vec3 getDeltaMovement() {
      return Vec3.ZERO;
   }

   public void setDeltaMovement(Vec3 p_149804_) {
   }

   public void setPos(double p_33449_, double p_33450_, double p_33451_) {
      BlockPos blockpos = this.blockPosition();
      if (this.isPassenger()) {
         super.setPos(p_33449_, p_33450_, p_33451_);
      } else {
         super.setPos((double)Mth.floor(p_33449_) + 0.5D, (double)Mth.floor(p_33450_ + 0.5D), (double)Mth.floor(p_33451_) + 0.5D);
      }

      if (this.tickCount != 0) {
         BlockPos blockpos1 = this.blockPosition();
         if (!blockpos1.equals(blockpos)) {
            this.entityData.set(DATA_PEEK_ID, (byte)0);
            this.hasImpulse = true;
            if (this.level().isClientSide && !this.isPassenger() && !blockpos1.equals(this.clientOldAttachPosition)) {
               this.clientOldAttachPosition = blockpos;
               this.clientSideTeleportInterpolation = 6;
               this.xOld = this.getX();
               this.yOld = this.getY();
               this.zOld = this.getZ();
            }
         }

      }
   }

   @Nullable
   protected Direction findAttachableSurface(BlockPos p_149811_) {
      for(Direction direction : Direction.values()) {
         if (this.canStayAt(p_149811_, direction)) {
            return direction;
         }
      }

      return null;
   }

   boolean canStayAt(BlockPos p_149786_, Direction p_149787_) {
      if (this.isPositionBlocked(p_149786_)) {
         return false;
      } else {
         Direction direction = p_149787_.getOpposite();
         if (!this.level().loadedAndEntityCanStandOnFace(p_149786_.relative(p_149787_), this, direction)) {
            return false;
         } else {
            AABB aabb = getProgressAabb(direction, 1.0F).move(p_149786_).deflate(1.0E-6D);
            return this.level().noCollision(this, aabb);
         }
      }
   }

   private boolean isPositionBlocked(BlockPos p_149813_) {
      BlockState blockstate = this.level().getBlockState(p_149813_);
      if (blockstate.isAir()) {
         return false;
      } else {
         boolean flag = blockstate.is(Blocks.MOVING_PISTON) && p_149813_.equals(this.blockPosition());
         return !flag;
      }
   }

   protected boolean teleportSomewhere() {
      if (!this.isNoAi() && this.isAlive()) {
         BlockPos blockpos = this.blockPosition();

         for(int i = 0; i < 5; ++i) {
            BlockPos blockpos1 = blockpos.offset(Mth.randomBetweenInclusive(this.random, -8, 8), Mth.randomBetweenInclusive(this.random, -8, 8), Mth.randomBetweenInclusive(this.random, -8, 8));
            if (blockpos1.getY() > this.level().getMinBuildHeight() && this.level().isEmptyBlock(blockpos1) && this.level().getWorldBorder().isWithinBounds(blockpos1) && this.level().noCollision(this, (new AABB(blockpos1)).deflate(1.0E-6D))) {
               Direction direction = this.findAttachableSurface(blockpos1);
               if (direction != null) {
                  net.minecraftforge.event.entity.EntityTeleportEvent.EnderEntity event = net.minecraftforge.event.ForgeEventFactory.onEnderTeleport(this, blockpos1.getX(), blockpos1.getY(), blockpos1.getZ());
                  if (event.isCanceled()) direction = null;
                  blockpos1 = BlockPos.containing(event.getTargetX(), event.getTargetY(), event.getTargetZ());
               }

               if (direction != null) {
                  this.unRide();
                  this.setAttachFace(direction);
                  this.playSound(SoundEvents.SHULKER_TELEPORT, 1.0F, 1.0F);
                  this.setPos((double)blockpos1.getX() + 0.5D, (double)blockpos1.getY(), (double)blockpos1.getZ() + 0.5D);
                  this.level().gameEvent(GameEvent.TELEPORT, blockpos, GameEvent.Context.of(this));
                  this.entityData.set(DATA_PEEK_ID, (byte)0);
                  this.setTarget((LivingEntity)null);
                  return true;
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public void lerpTo(double p_33411_, double p_33412_, double p_33413_, float p_33414_, float p_33415_, int p_33416_, boolean p_33417_) {
      this.lerpSteps = 0;
      this.setPos(p_33411_, p_33412_, p_33413_);
      this.setRot(p_33414_, p_33415_);
   }

   public boolean hurt(DamageSource p_33421_, float p_33422_) {
      if (this.isClosed()) {
         return false;
      }
      if (p_33421_.getEntity() != null && p_33421_.getEntity() == this) {
         return false;
      }

      if (!super.hurt(p_33421_, p_33422_)) {
         return false;
      } else {
         if ((double)this.getHealth() < (double)this.getMaxHealth() * 0.5D && this.random.nextInt(4) == 0) {
            this.teleportSomewhere();
         } else if (p_33421_.is(DamageTypeTags.IS_PROJECTILE)) {
            Entity entity1 = p_33421_.getDirectEntity();
            if (entity1 != null && entity1.getType() == EntityType.SHULKER_BULLET) {
               this.hitByShulkerBullet();
            }
         }

         return true;
      }
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
            this.setShield(this.getShield() + 1);
            if (this.getShield() >= this.getMaxShield()) {
               this.accumulateAttacksFull(p_21240_);
               this.setShield(0);
            }
            this.setAbsorptionAmount(this.getAbsorptionAmount() - f1);
            this.gameEvent(GameEvent.ENTITY_DAMAGE);
         }
      }
   }

   @Override
   public void kill() {
      this.reallyActuallyHurt(this.damageSources().genericKill(), Float.MAX_VALUE);
   }

   @Override
   protected void tickDeath() {
      ++this.blaznanaShulkerTrickDeathTime;
      if (!this.level().isClientSide) {
         TrackingUtil.forceEffect(this, LivingEntity.class, false, 32, this.blaznanaShulkerTrickDeathTime / 10.0D, 32);
      }
      if (this.blaznanaShulkerTrickDeathTime >= 80 && !this.level().isClientSide() && !this.isRemoved()) {
         CustomExplosion.nukeExplode(this.level(), this, this.getX(), this.getY(), this.getZ(), 32.0F, this.level().getLevelData().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING), this.level().getLevelData().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) ? CustomExplosion.BlockInteraction.DESTROY : CustomExplosion.BlockInteraction.KEEP, 1300.0D, 1.0F);
         int i = 500;
         boolean flag = this.level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT);
         if (flag) {
            int award = net.minecraftforge.event.ForgeEventFactory.getExperienceDrop(this, this.lastHurtByPlayer, i);
            ExperienceOrb.award((ServerLevel) this.level(), this.position(), award);
         }
         List<LivingEntity> _entfound = this.level().getEntitiesOfClass(LivingEntity.class, new AABB(new Vec3(this.getX(), this.getY(), this.getZ()), new Vec3(this.getX(), this.getY(), this.getZ())).inflate(32 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(new Vec3(this.getX(), this.getY(), this.getZ())))).toList();
         for (LivingEntity entity : _entfound) {
            if (!(entity instanceof Player)) {
               for (int k = 0; k < 100; k++) {
                  entity.invulnerableTime = 0;
                  OceanDefender.actuallyHurt(entity, EnderLord.enderLordAttack(this), this.getMaxHealth() * k, true);
               }
            } else if (this.hasCustomName() && (("奇想林中的蕉研组".equals(this.getName().getString())) || ("心蕉如火的猴把戏".equals(this.getName().getString())))) {
               for (int k = 0; k < 100; k++) {
                  entity.invulnerableTime = 0;
                  OceanDefender.actuallyHurt(entity, EnderLord.enderLordAttack(this), this.getMaxHealth() * k, true);
               }
            }
         }
         this.level().broadcastEntityEvent(this, (byte)60);
         this.remove(Entity.RemovalReason.KILLED);
      }
   }

   private boolean isClosed() {
      return this.getRawPeekAmount() == 0;
   }

   private void hitByShulkerBullet() {
      Vec3 vec3 = this.position();
      AABB aabb = this.getBoundingBox();
      if (!this.isClosed() && this.teleportSomewhere()) {
         int i = this.level().getEntities(EntityType.SHULKER, aabb.inflate(8.0D), Entity::isAlive).size();
         float f = (float)(i - 1) / 5.0F;
         if (!(this.level().random.nextFloat() < f)) {
            Shulker shulker = EntityType.SHULKER.create(this.level());
            if (shulker != null) {
               shulker.setVariant(Optional.of(DyeColor.RED));
               shulker.moveTo(vec3);
               this.level().addFreshEntity(shulker);
               if (this.getTarget() != null) {
                  shulker.setTarget(this.getTarget());
               }
            }

         }
      }
   }

   public boolean canBeCollidedWith() {
      return true;
   }

   @Override
   public boolean addEffect(MobEffectInstance p_147208_, @Nullable Entity p_147209_) {
      return false;
   }

   public Direction getAttachFace() {
      return this.entityData.get(DATA_ATTACH_FACE_ID);
   }

   private void setAttachFace(Direction p_149789_) {
      this.entityData.set(DATA_ATTACH_FACE_ID, p_149789_);
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> p_33434_) {
      if (DATA_ATTACH_FACE_ID.equals(p_33434_)) {
         this.setBoundingBox(this.makeBoundingBox());
      }

      super.onSyncedDataUpdated(p_33434_);
   }

   private int getRawPeekAmount() {
      return this.entityData.get(DATA_PEEK_ID);
   }

   public void setRawPeekAmount(int p_33419_) {
      if (!this.level().isClientSide) {
         this.getAttribute(Attributes.ARMOR).removeModifier(COVERED_ARMOR_MODIFIER);
         if (p_33419_ == 0) {
            this.getAttribute(Attributes.ARMOR).addPermanentModifier(COVERED_ARMOR_MODIFIER);
            this.playSound(SoundEvents.SHULKER_CLOSE, 1.0F, 1.0F);
            this.gameEvent(GameEvent.CONTAINER_CLOSE);
         } else {
            this.playSound(SoundEvents.SHULKER_OPEN, 1.0F, 1.0F);
            this.gameEvent(GameEvent.CONTAINER_OPEN);
         }
      }

      this.entityData.set(DATA_PEEK_ID, (byte)p_33419_);
   }

   public float getClientPeekAmount(float p_33481_) {
      return Mth.lerp(p_33481_, this.currentPeekAmountO, this.currentPeekAmount);
   }

   protected float getStandingEyeHeight(Pose p_33438_, EntityDimensions p_33439_) {
      return 0.5F;
   }

   public void recreateFromPacket(ClientboundAddEntityPacket p_219067_) {
      super.recreateFromPacket(p_219067_);
      this.yBodyRot = 0.0F;
      this.yBodyRotO = 0.0F;
   }

   public int getMaxHeadXRot() {
      return 180;
   }

   public int getMaxHeadYRot() {
      return 180;
   }

   public void push(Entity p_33474_) {
   }

   public float getPickRadius() {
      return 0.0F;
   }

   public Optional<Vec3> getRenderPosition(float p_149767_) {
      if (this.clientOldAttachPosition != null && this.clientSideTeleportInterpolation > 0) {
         double d0 = (double)((float)this.clientSideTeleportInterpolation - p_149767_) / 6.0D;
         d0 *= d0;
         BlockPos blockpos = this.blockPosition();
         double d1 = (double)(blockpos.getX() - this.clientOldAttachPosition.getX()) * d0;
         double d2 = (double)(blockpos.getY() - this.clientOldAttachPosition.getY()) * d0;
         double d3 = (double)(blockpos.getZ() - this.clientOldAttachPosition.getZ()) * d0;
         return Optional.of(new Vec3(-d1, -d2, -d3));
      } else {
         return Optional.empty();
      }
   }

   @Override
   public int getShield() {
      return this.entityData.get(DATA_SHIELD_ATTACK_TIMES);
   }

   @Override
   public int getMaxShield() {
      return 25;
   }

   @Override
   public boolean hasShield() {
      return this.getShield() > 0;
   }

   @Override
   public void setShield(int value) {
      this.entityData.set(DATA_SHIELD_ATTACK_TIMES, Mth.clamp(value, 0, this.getMaxShield()));
   }

   protected void reallyActuallyHurt(DamageSource p_21240_, float p_21241_) {
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
            super.setHealth(this.getHealth() - f1);
            this.setAbsorptionAmount(this.getAbsorptionAmount() - f1);
            this.gameEvent(GameEvent.ENTITY_DAMAGE);
         }
      }
   }

   public void accumulateAttacksFull(DamageSource damageSource) {
      this.reallyActuallyHurt(damageSource, this.getMaxHealth() / 10.0F);
      if (this.getHealth() <= 1.0F) {
         this.reallyActuallyHurt(damageSource, Float.MAX_VALUE);
      }

      if (damageSource.getEntity() != null) {
         damageSource.getEntity().invulnerableTime = 0;
         if (damageSource.getEntity() instanceof LivingEntity livingEntity) {
            livingEntity.hurt(EnderLord.enderLordAttack(this), livingEntity.getMaxHealth() / 5.0F);
         } else {
            damageSource.getEntity().hurt(EnderLord.enderLordAttack(this), this.getMaxHealth() / 5.0F);
         }
      }

      SwordBlockEvent.addVertigoTime(this, 80);
   }

   class ShulkerAttackGoal extends Goal {
      private int attackTime;

      public ShulkerAttackGoal() {
         this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
      }

      public boolean canUse() {
         LivingEntity livingentity = BlaznanaShulkerTrick.this.getTarget();
         if (livingentity != null && livingentity.isAlive()) {
            return BlaznanaShulkerTrick.this.level().getDifficulty() != Difficulty.PEACEFUL;
         } else {
            return false;
         }
      }

      public void start() {
         this.attackTime = 20;
         BlaznanaShulkerTrick.this.setRawPeekAmount(100);
      }

      public void stop() {
         BlaznanaShulkerTrick.this.setRawPeekAmount(0);
      }

      public boolean requiresUpdateEveryTick() {
         return true;
      }

      public void tick() {
         if (BlaznanaShulkerTrick.this.level().getDifficulty() != Difficulty.PEACEFUL) {
            --this.attackTime;
            LivingEntity livingentity = BlaznanaShulkerTrick.this.getTarget();
            if (livingentity != null) {
               BlaznanaShulkerTrick.this.getLookControl().setLookAt(livingentity, 180.0F, 180.0F);
               double d0 = BlaznanaShulkerTrick.this.distanceToSqr(livingentity);
               if (d0 < 4096.0D) {//25
                  if (this.attackTime <= 0) {
                     this.attackTime = 20 + BlaznanaShulkerTrick.this.random.nextInt(10) * 20 / 2;
                     Vec3 vec32 = BlaznanaShulkerTrick.this.getViewVector(1.0F);
                     double d6 = BlaznanaShulkerTrick.this.getX() - vec32.x;
                     double d7 = BlaznanaShulkerTrick.this.getY(0.5D) + 0.5D;
                     double d8 = BlaznanaShulkerTrick.this.getZ() - vec32.z;
                     double d9 = livingentity.getX() - d6;
                     double d10 = livingentity.getY(0.5D) - d7;
                     double d11 = livingentity.getZ() - d8;
                     BlaznanaShulkerTrickBullet bullet = new BlaznanaShulkerTrickBullet(BlaznanaShulkerTrick.this.level(), BlaznanaShulkerTrick.this);
                     bullet.setPos(BlaznanaShulkerTrick.this.getX(), BlaznanaShulkerTrick.this.getY(), BlaznanaShulkerTrick.this.getZ());
                     bullet.shoot(d9, d10, d11, 2, 0);
                     BlaznanaShulkerTrick.this.level().addFreshEntity(bullet);
                     BlaznanaShulkerTrick.this.playSound(SoundEvents.SHULKER_SHOOT, 2.0F, (BlaznanaShulkerTrick.this.random.nextFloat() - BlaznanaShulkerTrick.this.random.nextFloat()) * 0.2F + 1.0F);

                     BlaznanaShulkerTrick.this.playSound(SoundEvents.EVOKER_PREPARE_ATTACK, 2.0F, (BlaznanaShulkerTrick.this.random.nextFloat() - BlaznanaShulkerTrick.this.random.nextFloat()) * 0.2F + 1.0F);
                     double d3 = Math.min(livingentity.getY(), BlaznanaShulkerTrick.this.getY());
                     double d1 = Math.max(livingentity.getY(), BlaznanaShulkerTrick.this.getY()) + 1.0D;
                     float f = (float)Mth.atan2(livingentity.getZ() - BlaznanaShulkerTrick.this.getZ(), livingentity.getX() - BlaznanaShulkerTrick.this.getX());
                     if (BlaznanaShulkerTrick.this.distanceToSqr(livingentity) < 9.0D) {
                        for(int i = 0; i < 5; ++i) {
                           float f1 = f + (float)i * (float)Math.PI * 0.4F;
                           this.createSpellEntity(BlaznanaShulkerTrick.this.getX() + (double)Mth.cos(f1) * 1.5D, BlaznanaShulkerTrick.this.getZ() + (double)Mth.sin(f1) * 1.5D, d3, d1, f1, 0);
                        }

                        for(int k = 0; k < 8; ++k) {
                           float f2 = f + (float)k * (float)Math.PI * 2.0F / 8.0F + 1.2566371F;
                           this.createSpellEntity(BlaznanaShulkerTrick.this.getX() + (double)Mth.cos(f2) * 2.5D, BlaznanaShulkerTrick.this.getZ() + (double)Mth.sin(f2) * 2.5D, d3, d1, f2, 3);
                        }
                     } else {
                        for(int l = 0; l < 16; ++l) {
                           double d2 = 1.25D * (double)(l + 1);
                           int j = 1 * l;
                           this.createSpellEntity(BlaznanaShulkerTrick.this.getX() + (double)Mth.cos(f) * d2, BlaznanaShulkerTrick.this.getZ() + (double)Mth.sin(f) * d2, d3, d1, f, j);
                        }
                     }

                  }
               }

               super.tick();
            }
         }
      }

      public void createSpellEntity(double p_32673_, double p_32674_, double p_32675_, double p_32676_, float p_32677_, int p_32678_) {
         BlockPos blockpos = BlockPos.containing(p_32673_, p_32676_, p_32674_);
         boolean flag = false;
         double d0 = 0.0D;

         do {
            BlockPos blockpos1 = blockpos.below();
            BlockState blockstate = BlaznanaShulkerTrick.this.level().getBlockState(blockpos1);
            if (blockstate.isFaceSturdy(BlaznanaShulkerTrick.this.level(), blockpos1, Direction.UP)) {
               if (!BlaznanaShulkerTrick.this.level().isEmptyBlock(blockpos)) {
                  BlockState blockstate1 = BlaznanaShulkerTrick.this.level().getBlockState(blockpos);
                  VoxelShape voxelshape = blockstate1.getCollisionShape(BlaznanaShulkerTrick.this.level(), blockpos);
                  if (!voxelshape.isEmpty()) {
                     d0 = voxelshape.max(Direction.Axis.Y);
                  }
               }

               flag = true;
               break;
            }

            blockpos = blockpos.below();
         } while(blockpos.getY() >= Mth.floor(p_32675_) - 1);

         if (flag) {
            BlaznanaShulkerTrick.this.level().addFreshEntity(new EvokerFangs(BlaznanaShulkerTrick.this.level(), p_32673_, (double)blockpos.getY() + d0, p_32674_, p_32677_, p_32678_, BlaznanaShulkerTrick.this));
         }

      }
   }

   static class ShulkerBodyRotationControl extends BodyRotationControl {
      public ShulkerBodyRotationControl(Mob p_149816_) {
         super(p_149816_);
      }

      public void clientTick() {
      }
   }

   static class ShulkerDefenseAttackGoal extends NearestAttackableTargetGoal<LivingEntity> {
      public ShulkerDefenseAttackGoal(BlaznanaShulkerTrick p_33496_) {
         super(p_33496_, LivingEntity.class, 10, true, false, (p_33501_) -> {
            return p_33501_ instanceof Enemy;
         });
      }

      public boolean canUse() {
         return this.mob.getTeam() == null ? false : super.canUse();
      }

      protected AABB getTargetSearchArea(double p_33499_) {
         Direction direction = ((BlaznanaShulkerTrick)this.mob).getAttachFace();
         if (direction.getAxis() == Direction.Axis.X) {
            return this.mob.getBoundingBox().inflate(4.0D, p_33499_, p_33499_);
         } else {
            return direction.getAxis() == Direction.Axis.Z ? this.mob.getBoundingBox().inflate(p_33499_, p_33499_, 4.0D) : this.mob.getBoundingBox().inflate(p_33499_, 4.0D, p_33499_);
         }
      }
   }

   class ShulkerLookControl extends LookControl {
      public ShulkerLookControl(Mob p_149820_) {
         super(p_149820_);
      }

      protected void clampHeadRotationToBody() {
      }

      protected Optional<Float> getYRotD() {
         Direction direction = BlaznanaShulkerTrick.this.getAttachFace().getOpposite();
         Vector3f vector3f = direction.getRotation().transform(new Vector3f((Vector3fc)BlaznanaShulkerTrick.FORWARD));
         Vec3i vec3i = direction.getNormal();
         Vector3f vector3f1 = new Vector3f((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ());
         vector3f1.cross(vector3f);
         double d0 = this.wantedX - this.mob.getX();
         double d1 = this.wantedY - this.mob.getEyeY();
         double d2 = this.wantedZ - this.mob.getZ();
         Vector3f vector3f2 = new Vector3f((float)d0, (float)d1, (float)d2);
         float f = vector3f1.dot(vector3f2);
         float f1 = vector3f.dot(vector3f2);
         return !(Math.abs(f) > 1.0E-5F) && !(Math.abs(f1) > 1.0E-5F) ? Optional.empty() : Optional.of((float)(Mth.atan2((double)(-f), (double)f1) * (double)(180F / (float)Math.PI)));
      }

      protected Optional<Float> getXRotD() {
         return Optional.of(0.0F);
      }
   }

   class ShulkerNearestAttackGoal extends NearestAttackableTargetGoal<Player> {
      public ShulkerNearestAttackGoal(BlaznanaShulkerTrick p_33505_) {
         super(p_33505_, Player.class, true);
      }

      public boolean canUse() {
         return BlaznanaShulkerTrick.this.level().getDifficulty() == Difficulty.PEACEFUL ? false : super.canUse();
      }

      protected AABB getTargetSearchArea(double p_33508_) {
         Direction direction = ((BlaznanaShulkerTrick)this.mob).getAttachFace();
         if (direction.getAxis() == Direction.Axis.X) {
            return this.mob.getBoundingBox().inflate(4.0D, p_33508_, p_33508_);
         } else {
            return direction.getAxis() == Direction.Axis.Z ? this.mob.getBoundingBox().inflate(p_33508_, p_33508_, 4.0D) : this.mob.getBoundingBox().inflate(p_33508_, 4.0D, p_33508_);
         }
      }
   }

   class ShulkerPeekGoal extends Goal {
      private int peekTime;

      public boolean canUse() {
         return BlaznanaShulkerTrick.this.getTarget() == null && BlaznanaShulkerTrick.this.random.nextInt(reducedTickDelay(40)) == 0 && BlaznanaShulkerTrick.this.canStayAt(BlaznanaShulkerTrick.this.blockPosition(), BlaznanaShulkerTrick.this.getAttachFace());
      }

      public boolean canContinueToUse() {
         return BlaznanaShulkerTrick.this.getTarget() == null && this.peekTime > 0;
      }

      public void start() {
         this.peekTime = this.adjustedTickDelay(20 * (1 + BlaznanaShulkerTrick.this.random.nextInt(3)));
         BlaznanaShulkerTrick.this.setRawPeekAmount(30);
      }

      public void stop() {
         if (BlaznanaShulkerTrick.this.getTarget() == null) {
            BlaznanaShulkerTrick.this.setRawPeekAmount(0);
         }

      }

      public void tick() {
         --this.peekTime;
      }
   }
}
