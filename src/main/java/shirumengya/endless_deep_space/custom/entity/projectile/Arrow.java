package shirumengya.endless_deep_space.custom.entity.projectile;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
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
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import shirumengya.endless_deep_space.custom.init.ModEntities;
import shirumengya.endless_deep_space.custom.init.ModItems;
import shirumengya.endless_deep_space.custom.util.entity.TrackingUtil;
import shirumengya.endless_deep_space.custom.world.explosion.CustomExplosion;
import shirumengya.endless_deep_space.init.EndlessDeepSpaceModGameRules;

import javax.annotation.Nullable;
import java.util.*;

public class Arrow extends Projectile {
   public static final double ARROW_BASE_DAMAGE = 2.0D;
   public static final EntityDataAccessor<Byte> ID_FLAGS = SynchedEntityData.defineId(Arrow.class, EntityDataSerializers.BYTE);
   public static final EntityDataAccessor<Byte> PIERCE_LEVEL = SynchedEntityData.defineId(Arrow.class, EntityDataSerializers.BYTE);
   public static final EntityDataAccessor<Integer> ARROW_TYPE = SynchedEntityData.defineId(Arrow.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Float> AIR_SPEED = SynchedEntityData.defineId(Arrow.class, EntityDataSerializers.FLOAT);
   public static final EntityDataAccessor<Float> WATER_SPEED = SynchedEntityData.defineId(Arrow.class, EntityDataSerializers.FLOAT);
   public static final EntityDataAccessor<Float> RESISTANCE = SynchedEntityData.defineId(Arrow.class, EntityDataSerializers.FLOAT);
   public static final int FLAG_CRIT = 1;
   public static final int FLAG_NOPHYSICS = 2;
   public static final int FLAG_CROSSBOW = 4;
   @Nullable
   public BlockState lastState;
   public boolean inGround;
   public int inGroundTime;
   public Arrow.Pickup pickup = Arrow.Pickup.DISALLOWED;
   public int shakeTime;
   public int life;
   public double baseDamage = 2.0D;
   public int knockback;
   public SoundEvent soundEvent = this.getDefaultHitGroundSoundEvent();
   @Nullable
   public IntOpenHashSet piercingIgnoreEntityIds;
   @Nullable
   public List<Entity> piercedAndKilledEntities;
   public final IntOpenHashSet ignoredEntities = new IntOpenHashSet();
   public boolean canTracking;
   public static final int EXPOSED_POTION_DECAY_TIME = 600;
   public static final int NO_EFFECT_COLOR = -1;
   public static final EntityDataAccessor<Integer> ID_EFFECT_COLOR = SynchedEntityData.defineId(Arrow.class, EntityDataSerializers.INT);
   public static final byte EVENT_POTION_PUFF = 0;
   public Potion potion = Potions.EMPTY;
   public final Set<MobEffectInstance> effects = Sets.newHashSet();
   public boolean fixedColor;
   public final Map<UUID, ResourceKey<DamageType>> additionalDamageSource = new HashMap<>();
   public final Map<UUID, Float> additionalDamageValue = new HashMap<>();
   public static final Logger LOGGER = LogUtils.getLogger();
   public final Map<UUID, Float> explosionPower = new HashMap<>();
   public final Map<UUID, Float> explosionCapacity = new HashMap<>();
   public final Map<UUID, Float> explosionDamage = new HashMap<>();
   
   public Arrow(EntityType<? extends Arrow> p_36721_, Level p_36722_) {
      super(p_36721_, p_36722_);
      this.noCulling = true;
   }
   
   public Arrow(EntityType<? extends Arrow> p_36711_, double p_36712_, double p_36713_, double p_36714_, Level p_36715_) {
      this(p_36711_, p_36715_);
      this.setPos(p_36712_, p_36713_, p_36714_);
   }
   
   public Arrow(EntityType<? extends Arrow> p_36717_, Entity p_36718_, Level p_36719_) {
      this(p_36717_, p_36718_.getX(), p_36718_.getEyeY() - (double)0.1F, p_36718_.getZ(), p_36719_);
      this.setOwner(p_36718_);
      if (p_36718_ instanceof Player) {
         this.pickup = Arrow.Pickup.ALLOWED;
      }
   }
   
   public Arrow(Entity p_36718_, Level p_36719_, ArrowType type) {
      this(ModEntities.ARROW.get(), p_36718_, p_36719_);
      this.setArrowType(type);
      switch (this.getArrowType()) {
         case TRACKING_ARROW:
            this.canTracking = true;
            break;
         
         default:
            break;
      }
   }
   
   public static DamageSource arrowAttack(Entity entity, Entity sourceentity) {
      return new DamageSource(entity.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.ARROW), entity, sourceentity);
   }
   
   public void setSoundEvent(SoundEvent p_36741_) {
      this.soundEvent = p_36741_;
   }
   
   public boolean shouldRenderAtSqrDistance(double p_36726_) {
      double d0 = this.getBoundingBox().getSize() * 10.0D;
      if (Double.isNaN(d0)) {
         d0 = 1.0D;
      }
      
      d0 *= 64.0D * getViewScale();
      return p_36726_ < d0 * d0;
   }
   
   public void defineSynchedData() {
      this.entityData.define(ID_FLAGS, (byte)0);
      this.entityData.define(PIERCE_LEVEL, (byte)0);
      this.entityData.define(ARROW_TYPE, ArrowType.NORMAL.ordinal());
      this.entityData.define(AIR_SPEED, 0.99F);
      this.entityData.define(WATER_SPEED, 0.6F);
      this.entityData.define(RESISTANCE, 0.05F);
      this.entityData.define(ID_EFFECT_COLOR, -1);
   }
   
   public void shoot(double p_36775_, double p_36776_, double p_36777_, float p_36778_, float p_36779_) {
      super.shoot(p_36775_, p_36776_, p_36777_, p_36778_, p_36779_);
      this.life = 0;
   }
   
   public void lerpTo(double p_36728_, double p_36729_, double p_36730_, float p_36731_, float p_36732_, int p_36733_, boolean p_36734_) {
      this.setPos(p_36728_, p_36729_, p_36730_);
      this.setRot(p_36731_, p_36732_);
   }
   
   public void lerpMotion(double p_36786_, double p_36787_, double p_36788_) {
      super.lerpMotion(p_36786_, p_36787_, p_36788_);
      this.life = 0;
   }
   
   public void tick() {
      super.tick();
      boolean flag = this.isNoPhysics();
      Vec3 vec3 = this.getDeltaMovement();
      if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
         double d0 = vec3.horizontalDistance();
         this.setYRot((float)(Mth.atan2(vec3.x, vec3.z) * (double)(180F / (float)Math.PI)));
         this.setXRot((float)(Mth.atan2(vec3.y, d0) * (double)(180F / (float)Math.PI)));
         this.yRotO = this.getYRot();
         this.xRotO = this.getXRot();
      }
      
      BlockPos blockpos = this.blockPosition();
      BlockState blockstate = this.level().getBlockState(blockpos);
      if (!blockstate.isAir() && !flag) {
         VoxelShape voxelshape = blockstate.getCollisionShape(this.level(), blockpos);
         if (!voxelshape.isEmpty()) {
            Vec3 vec31 = this.position();
            
            for(AABB aabb : voxelshape.toAabbs()) {
               if (aabb.move(blockpos).contains(vec31)) {
                  this.inGround = true;
                  break;
               }
            }
         }
      }
      
      if (this.shakeTime > 0) {
         --this.shakeTime;
      }
      
      if (this.isInWaterOrRain() || blockstate.is(Blocks.POWDER_SNOW) || this.isInFluidType((fluidType, height) -> this.canFluidExtinguish(fluidType))) {
         this.clearFire();
      }
      
      if ((this.inGround || (this.getResistance() <= 0.01F && this.getDeltaMovement().lengthSqr() < 0.25 && this.tickCount >= 200)) && !flag) {
         if (this.lastState != blockstate && this.shouldFall()) {
            this.startFalling();
         } else if (!this.level().isClientSide) {
            this.tickDespawn();
         }
         
         ++this.inGroundTime;
      } else {
         this.inGroundTime = 0;
         Vec3 vec32 = this.position();
         Vec3 vec33 = vec32.add(vec3);
         HitResult hitresult = this.level().clip(new ClipContext(vec32, vec33, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
         if (hitresult.getType() != HitResult.Type.MISS) {
            vec33 = hitresult.getLocation();
         }
         
         while(!this.isRemoved()) {
            EntityHitResult entityhitresult = this.findHitEntity(vec32, vec33);
            if (entityhitresult != null) {
               hitresult = entityhitresult;
            }
            
            if (hitresult != null && hitresult.getType() == HitResult.Type.ENTITY) {
               Entity entity = ((EntityHitResult)hitresult).getEntity();
               Entity entity1 = this.getOwner();
               if (entity instanceof Player && entity1 instanceof Player && !((Player)entity1).canHarmPlayer((Player)entity)) {
                  hitresult = null;
                  entityhitresult = null;
               }
            }
            
            if (hitresult != null && hitresult.getType() != HitResult.Type.MISS && !flag) {
               switch (net.minecraftforge.event.ForgeEventFactory.onProjectileImpactResult(this, hitresult)) {
                  case SKIP_ENTITY:
                     if (hitresult.getType() != HitResult.Type.ENTITY) { // If there is no entity, we just return default behaviour
                        this.onHit(hitresult);
                        this.hasImpulse = true;
                        break;
                     }
                     ignoredEntities.add(entityhitresult.getEntity().getId());
                     entityhitresult = null; // Don't process any further
                     break;
                  case STOP_AT_CURRENT_NO_DAMAGE:
                     this.discard();
                     entityhitresult = null; // Don't process any further
                     break;
                  case STOP_AT_CURRENT:
                     this.setPierceLevel((byte) 0);
                  case DEFAULT:
                     this.onHit(hitresult);
                     this.hasImpulse = true;
                     break;
               }
            }
            
            if (entityhitresult == null || this.getPierceLevel() <= 0) {
               break;
            }
            
            hitresult = null;
         }
         
         if (this.isRemoved())
            return;
         
         vec3 = this.getDeltaMovement();
         double d5 = vec3.x;
         double d6 = vec3.y;
         double d1 = vec3.z;
         if (this.isCritArrow()) {
            for(int i = 0; i < 4; ++i) {
               this.level().addParticle(ParticleTypes.CRIT, this.getX() + d5 * (double)i / 4.0D, this.getY() + d6 * (double)i / 4.0D, this.getZ() + d1 * (double)i / 4.0D, -d5, -d6 + 0.2D, -d1);
            }
         }
         
         double d7 = this.getX() + d5;
         double d2 = this.getY() + d6;
         double d3 = this.getZ() + d1;
         double d4 = vec3.horizontalDistance();
         if (flag) {
            this.setYRot((float)(Mth.atan2(-d5, -d1) * (double)(180F / (float)Math.PI)));
         } else {
            this.setYRot((float)(Mth.atan2(d5, d1) * (double)(180F / (float)Math.PI)));
         }
         
         this.setXRot((float)(Mth.atan2(d6, d4) * (double)(180F / (float)Math.PI)));
         this.setXRot(lerpRotation(this.xRotO, this.getXRot()));
         this.setYRot(lerpRotation(this.yRotO, this.getYRot()));
         float f = Math.min(0.99F, this.getAirSpeed());
         float f1 = this.getResistance();
         if (this.isInWater()) {
            for(int j = 0; j < 4; ++j) {
               float f2 = 0.25F;
               this.level().addParticle(ParticleTypes.BUBBLE, d7 - d5 * 0.25D, d2 - d6 * 0.25D, d3 - d1 * 0.25D, d5, d6, d1);
            }
            
            f = Math.min(0.99F, this.getWaterSpeed());
         }
         
         this.setDeltaMovement(vec3.scale(f));
         if (!this.isNoGravity() && !flag) {
            Vec3 vec34 = this.getDeltaMovement();
            this.setDeltaMovement(vec34.x, vec34.y - f1, vec34.z);
         }
         
         this.setPos(d7, d2, d3);
         this.checkInsideBlocks();
      }
      
      if (this.level().isClientSide) {
         if (this.inGround) {
            if (this.inGroundTime % 5 == 0) {
               this.makeParticle(1);
            }
         } else {
            this.makeParticle(2);
         }
      } else if (this.inGround && this.inGroundTime != 0 && !this.effects.isEmpty() && this.inGroundTime >= 600) {
         this.level().broadcastEntityEvent(this, (byte)0);
         this.potion = Potions.EMPTY;
         this.effects.clear();
         this.entityData.set(ID_EFFECT_COLOR, -1);
      }
      
      this.arrowTypeTick();
   }
   
   public void arrowTypeTick() {
      switch (this.getArrowType()) {
         case TRACKING_ARROW:
            Vec3 vec3 = this.getDeltaMovement();
            float v0 = (float) vec3.length();
            float maxTurningAngleCos = Mth.cos(7.1619724F * v0 * Mth.DEG_TO_RAD);
            float maxTurningAngleSin = Mth.sin(7.1619724F * v0 * Mth.DEG_TO_RAD);
            if (!this.level().isClientSide && this.canTracking) {
               if (this.getOwner() instanceof Mob) {
                  TrackingUtil.TrackingEntity(this, maxTurningAngleCos, maxTurningAngleSin, false);
               } else {
                  TrackingUtil.TrackingEntityClass(this, Player.class, 40.0D, true, maxTurningAngleCos, maxTurningAngleSin, false);
                  TrackingUtil.TrackingEntityClass(this, Shulker.class, 40.0D, true, maxTurningAngleCos, maxTurningAngleSin, false);
                  TrackingUtil.TrackingEntityClass(this, Ghast.class, 40.0D, true, maxTurningAngleCos, maxTurningAngleSin, false);
                  TrackingUtil.TrackingEntityClass(this, Slime.class, 40.0D, true, maxTurningAngleCos, maxTurningAngleSin, false);
                  TrackingUtil.TrackingEntityClass(this, Monster.class, 40.0D, true, maxTurningAngleCos, maxTurningAngleSin, false);
                  TrackingUtil.TrackingEntityClass(this, PartEntity.class, 40.0D, true, maxTurningAngleCos, maxTurningAngleSin, false);
                  if (this.level() instanceof ServerLevel level) {
                     if (level.dimension() == Level.END && level.getDragonFight() != null && level.getDragonFight().getDragonUUID() != null && level.getDragonFight().getCrystalsAlive() > 0) {
                        TrackingUtil.TrackingEntityClass(this, EndCrystal.class, 40.0D, true, maxTurningAngleCos, maxTurningAngleSin, false);
                     }
                  }
               }
            }
            break;
         
         default:
            break;
      }
   }
   
   public void arrowTypeOnHitEntity(EntityHitResult p_36757_) {
      switch (this.getArrowType()) {
         case TRACKING_ARROW:
            break;
         
         default:
            break;
      }
   }
   
   public void arrowTypeOnHitBlock(BlockHitResult p_36757_) {
      switch (this.getArrowType()) {
         case TRACKING_ARROW:
            this.canTracking = false;
            break;
         
         default:
            break;
      }
   }
   
   public void arrowTypeOnHit(HitResult p_36757_) {
      switch (this.getArrowType()) {
         case TRACKING_ARROW:
            break;
         
         default:
            break;
      }
   }
   
   public void arrowTypeDoPostHurtEffects(LivingEntity p_36757_) {
      switch (this.getArrowType()) {
         case TRACKING_ARROW:
            break;
         
         default:
            break;
      }
   }
   
   public boolean shouldFall() {
      return this.inGround && this.level().noCollision((new AABB(this.position(), this.position())).inflate(0.06D));
   }
   
   public void startFalling() {
      this.inGround = false;
      Vec3 vec3 = this.getDeltaMovement();
      this.setDeltaMovement(vec3.multiply((double)(this.random.nextFloat() * 0.2F), (double)(this.random.nextFloat() * 0.2F), (double)(this.random.nextFloat() * 0.2F)));
      this.life = 0;
   }
   
   public void move(MoverType p_36749_, Vec3 p_36750_) {
      super.move(p_36749_, p_36750_);
      if (p_36749_ != MoverType.SELF && this.shouldFall()) {
         this.startFalling();
      }
   }
   
   public void tickDespawn() {
      ++this.life;
      if (this.life >= 1200) {
         this.discard();
      }
      
   }
   
   public void resetPiercedEntities() {
      if (this.piercedAndKilledEntities != null) {
         this.piercedAndKilledEntities.clear();
      }
      
      if (this.piercingIgnoreEntityIds != null) {
         this.piercingIgnoreEntityIds.clear();
      }
      
   }
   
   public boolean doAdditionalDamage(Entity entity, DamageSource originalDamageSource, float originalDamage) {
      boolean flag = entity.hurt(originalDamageSource, originalDamage);
      
      if (!this.additionalDamageSource.isEmpty() && !this.additionalDamageValue.isEmpty() && this.additionalDamageSource.size() == this.additionalDamageValue.size()) {
         this.additionalDamageSource.forEach((uuid, type) -> {
            entity.invulnerableTime = 0;
            ResourceKey<DamageType> damageTypeResourceKey = ResourceKey.create(Registries.DAMAGE_TYPE, type.location());
            Registry<DamageType> damageTypeRegistry = entity.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
            Optional<Holder.Reference<DamageType>> damageTypeHolder = damageTypeRegistry.getHolder(damageTypeResourceKey);
            entity.hurt(new DamageSource(damageTypeHolder.orElseGet(() -> {
               LOGGER.error("Missing key in {}: {}", damageTypeRegistry.key(), damageTypeResourceKey);
               return entity.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.ARROW);
            }), this, this.getOwner() != null ? this.getOwner() : this), this.additionalDamageValue.getOrDefault(uuid, 0.0F));
         });
      } else {
         this.additionalDamageSource.clear();
         this.additionalDamageValue.clear();
      }
      
      return flag;
   }
   
   public void doExplosion(@Nullable Entity entity) {
      if (!this.explosionPower.isEmpty() && !this.explosionCapacity.isEmpty() && this.explosionPower.size() == this.explosionCapacity.size()) {
         this.explosionPower.forEach((uuid, power) -> {
            if (entity != null) {
               entity.invulnerableTime = 0;
            }
            
            if (!this.level().isClientSide) {
               CustomExplosion.nukeExplode(this.level(), this.getOwner() != null ? this.getOwner() : this, this.getX(), this.getY(), this.getZ(), power, this.explosionDamage.getOrDefault(uuid, 1.0F), false, this.level().getLevelData().getGameRules().getBoolean(EndlessDeepSpaceModGameRules.ARROWS_EXPLOSION_DROP_BLOCKS) ? CustomExplosion.BlockInteraction.DESTROY : CustomExplosion.BlockInteraction.KEEP, this.explosionCapacity.getOrDefault(uuid, 0.0F), 1.0F);
            }
         });
      }
      
      this.explosionPower.clear();
      this.explosionCapacity.clear();
      this.explosionDamage.clear();
   }
   
   public void onHitEntity(EntityHitResult p_36757_) {
      super.onHitEntity(p_36757_);
      Entity entity = p_36757_.getEntity();
      float f = (float)this.getDeltaMovement().length();
      int i = Mth.ceil(Mth.clamp((double)f * this.baseDamage, 0.0D, (double)Integer.MAX_VALUE));
      if (this.getPierceLevel() > 0) {
         if (this.piercingIgnoreEntityIds == null) {
            this.piercingIgnoreEntityIds = new IntOpenHashSet(5);
         }
         
         if (this.piercedAndKilledEntities == null) {
            this.piercedAndKilledEntities = Lists.newArrayListWithCapacity(5);
         }
         
         if (this.piercingIgnoreEntityIds.size() >= this.getPierceLevel() + 1) {
            this.discard();
            return;
         }
         
         this.piercingIgnoreEntityIds.add(entity.getId());
      }
      
      if (this.isCritArrow()) {
         long j = (long)this.random.nextInt(i / 2 + 2);
         i = (int)Math.min(j + (long)i, 2147483647L);
      }
      
      Entity entity1 = this.getOwner();
      DamageSource damagesource;
      if (entity1 == null) {
         damagesource = arrowAttack(this, this);
      } else {
         damagesource = arrowAttack(this, entity1);
         if (entity1 instanceof LivingEntity) {
            ((LivingEntity)entity1).setLastHurtMob(entity);
         }
      }
      
      boolean flag = entity.getType() == EntityType.ENDERMAN;
      int k = entity.getRemainingFireTicks();
      if (this.isOnFire() && !flag) {
         entity.setSecondsOnFire(5);
      }
      
      if (this.getArrowType() == ArrowType.TRACKING_ARROW) {
         entity.invulnerableTime = 0;
      }
      
      if (this.doAdditionalDamage(entity, damagesource, (float)i)) {
         if (flag) {
            return;
         }
         
         if (entity instanceof LivingEntity) {
            LivingEntity livingentity = (LivingEntity)entity;
            if (!this.level().isClientSide && this.getPierceLevel() <= 0) {
               livingentity.setArrowCount(livingentity.getArrowCount() + 1);
            }
            
            if (this.knockback > 0) {
               double d0 = Math.max(0.0D, 1.0D - livingentity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
               Vec3 vec3 = this.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D).normalize().scale((double)this.knockback * 0.6D * d0);
               if (vec3.lengthSqr() > 0.0D) {
                  livingentity.push(vec3.x, 0.1D, vec3.z);
               }
            }
            
            if (!this.level().isClientSide && entity1 instanceof LivingEntity) {
               EnchantmentHelper.doPostHurtEffects(livingentity, entity1);
               EnchantmentHelper.doPostDamageEffects((LivingEntity)entity1, livingentity);
            }
            
            this.doPostHurtEffects(livingentity);
            if (entity1 != null && livingentity != entity1 && livingentity instanceof Player && entity1 instanceof ServerPlayer && !this.isSilent()) {
               ((ServerPlayer)entity1).connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0F));
            }
            
            if (!entity.isAlive() && this.piercedAndKilledEntities != null) {
               this.piercedAndKilledEntities.add(livingentity);
            }
            
            if (!this.level().isClientSide && entity1 instanceof ServerPlayer) {
               ServerPlayer serverplayer = (ServerPlayer)entity1;
               if (this.piercedAndKilledEntities != null && this.shotFromCrossbow()) {
                  CriteriaTriggers.KILLED_BY_CROSSBOW.trigger(serverplayer, this.piercedAndKilledEntities);
               } else if (!entity.isAlive() && this.shotFromCrossbow()) {
                  CriteriaTriggers.KILLED_BY_CROSSBOW.trigger(serverplayer, Arrays.asList(entity));
               }
            }
         }
         
         this.playSound(this.soundEvent, 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
         if (this.getPierceLevel() <= 0) {
            this.discard();
         }
      } else {
         entity.setRemainingFireTicks(k);
         this.setDeltaMovement(this.getDeltaMovement().scale(-0.1D));
         this.setYRot(this.getYRot() + 180.0F);
         this.yRotO += 180.0F;
         if (!this.level().isClientSide && this.getDeltaMovement().lengthSqr() < 1.0E-7D) {
            if (this.pickup == Arrow.Pickup.ALLOWED) {
               this.spawnAtLocation(this.getPickupItem(), 0.1F);
            }
            
            this.discard();
         }
      }
      
      this.doExplosion(entity);
      
      this.arrowTypeOnHitEntity(p_36757_);
   }
   
   public void onHitBlock(BlockHitResult p_36755_) {
      this.lastState = this.level().getBlockState(p_36755_.getBlockPos());
      super.onHitBlock(p_36755_);
      Vec3 vec3 = p_36755_.getLocation().subtract(this.getX(), this.getY(), this.getZ());
      this.setDeltaMovement(vec3);
      Vec3 vec31 = vec3.normalize().scale((double)0.05F);
      this.setPosRaw(this.getX() - vec31.x, this.getY() - vec31.y, this.getZ() - vec31.z);
      this.playSound(this.getHitGroundSoundEvent(), 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
      this.inGround = true;
      this.shakeTime = 7;
      this.setCritArrow(false);
      this.setPierceLevel((byte)0);
      this.setSoundEvent(SoundEvents.ARROW_HIT);
      this.setShotFromCrossbow(false);
      this.resetPiercedEntities();
      
      this.doExplosion(null);
      
      this.arrowTypeOnHitBlock(p_36755_);
   }
   
   @Override
   protected void onHit(HitResult p_37260_) {
      super.onHit(p_37260_);
      
      this.arrowTypeOnHit(p_37260_);
   }
   
   @Override
   public boolean ignoreExplosion() {
      return !this.explosionPower.isEmpty() && !this.explosionCapacity.isEmpty();
   }
   
   public SoundEvent getDefaultHitGroundSoundEvent() {
      return SoundEvents.ARROW_HIT;
   }
   
   public final SoundEvent getHitGroundSoundEvent() {
      return this.soundEvent;
   }
   
   public void doPostHurtEffects(LivingEntity p_36744_) {
      Entity entity = this.getEffectSource();
      
      for(MobEffectInstance mobeffectinstance : this.potion.getEffects()) {
         p_36744_.addEffect(new MobEffectInstance(mobeffectinstance.getEffect(), Math.max(mobeffectinstance.mapDuration((p_268168_) -> {
            return p_268168_ / 8;
         }), 1), mobeffectinstance.getAmplifier(), mobeffectinstance.isAmbient(), mobeffectinstance.isVisible()), entity);
      }
      
      if (!this.effects.isEmpty()) {
         for(MobEffectInstance mobeffectinstance1 : this.effects) {
            p_36744_.addEffect(mobeffectinstance1, entity);
         }
      }
      
      this.arrowTypeDoPostHurtEffects(p_36744_);
   }
   
   @Nullable
   public EntityHitResult findHitEntity(Vec3 p_36758_, Vec3 p_36759_) {
      return ProjectileUtil.getEntityHitResult(this.level(), this, p_36758_, p_36759_, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0D), this::canHitEntity);
   }
   
   public boolean canHitEntity(Entity p_36743_) {
      return super.canHitEntity(p_36743_) && (this.piercingIgnoreEntityIds == null || !this.piercingIgnoreEntityIds.contains(p_36743_.getId())) && !this.ignoredEntities.contains(p_36743_.getId());
   }
   
   public void addAdditionalSaveData(CompoundTag p_36772_) {
      super.addAdditionalSaveData(p_36772_);
      p_36772_.putShort("life", (short)this.life);
      if (this.lastState != null) {
         p_36772_.put("inBlockState", NbtUtils.writeBlockState(this.lastState));
      }
      
      p_36772_.putByte("shake", (byte)this.shakeTime);
      p_36772_.putBoolean("inGround", this.inGround);
      p_36772_.putByte("pickup", (byte)this.pickup.ordinal());
      p_36772_.putDouble("damage", this.baseDamage);
      p_36772_.putBoolean("crit", this.isCritArrow());
      p_36772_.putByte("PierceLevel", this.getPierceLevel());
      p_36772_.putString("SoundEvent", ForgeRegistries.SOUND_EVENTS.getKey(this.soundEvent).toString());
      p_36772_.putBoolean("ShotFromCrossbow", this.shotFromCrossbow());
      
      p_36772_.putInt("ArrowType", this.getArrowType().ordinal());
      p_36772_.putFloat("AirSpeed", this.getAirSpeed());
      p_36772_.putFloat("WaterSpeed", this.getWaterSpeed());
      p_36772_.putFloat("Resistance", this.getResistance());
      
      if (this.potion != Potions.EMPTY) {
         p_36772_.putString("Potion", ForgeRegistries.POTIONS.getKey(this.potion).toString());
      }
      
      if (this.fixedColor) {
         p_36772_.putInt("Color", this.getColor());
      }
      
      if (!this.effects.isEmpty()) {
         ListTag listtag = new ListTag();
         
         for(MobEffectInstance mobeffectinstance : this.effects) {
            listtag.add(mobeffectinstance.save(new CompoundTag()));
         }
         
         p_36772_.put("CustomPotionEffects", listtag);
      }
      
      if (!this.additionalDamageSource.isEmpty() && !this.additionalDamageValue.isEmpty() && this.additionalDamageSource.size() == this.additionalDamageValue.size()) {
         ListTag damageSourceList = new ListTag();
         
         this.additionalDamageSource.forEach((uuid, type) -> {
            CompoundTag tag = new CompoundTag();
            tag.putString("DamageType", type.location().toString());
            tag.putString("Damage", String.valueOf(this.additionalDamageValue.getOrDefault(uuid, 0.0F)));
            damageSourceList.add(tag);
         });
         
         p_36772_.put("AdditionalDamage", damageSourceList);
      }
      
      if (!this.explosionPower.isEmpty() && !this.explosionCapacity.isEmpty() && !this.explosionDamage.isEmpty() && this.explosionPower.size() == this.explosionCapacity.size() && this.explosionPower.size() == this.explosionDamage.size()) {
         ListTag explosionList = new ListTag();
         
         this.explosionPower.forEach((uuid, power) -> {
            CompoundTag tag = new CompoundTag();
            tag.putFloat("Power", power);
            tag.putFloat("Capacity", this.explosionCapacity.getOrDefault(uuid, 0.0F));
            tag.putFloat("Damage", this.explosionDamage.getOrDefault(uuid, 1.0F));
            explosionList.add(tag);
         });
         
         p_36772_.put("Explosion", explosionList);
      }
   }
   
   public void readAdditionalSaveData(CompoundTag p_36761_) {
      super.readAdditionalSaveData(p_36761_);
      this.life = p_36761_.getShort("life");
      if (p_36761_.contains("inBlockState", 10)) {
         this.lastState = NbtUtils.readBlockState(this.level().holderLookup(Registries.BLOCK), p_36761_.getCompound("inBlockState"));
      }
      
      this.shakeTime = p_36761_.getByte("shake") & 255;
      this.inGround = p_36761_.getBoolean("inGround");
      if (p_36761_.contains("damage", 99)) {
         this.baseDamage = p_36761_.getDouble("damage");
      }
      
      this.pickup = Arrow.Pickup.byOrdinal(p_36761_.getByte("pickup"));
      this.setCritArrow(p_36761_.getBoolean("crit"));
      this.setPierceLevel(p_36761_.getByte("PierceLevel"));
      if (p_36761_.contains("SoundEvent", 8)) {
         SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(p_36761_.getString("SoundEvent")));
         this.soundEvent = sound != null ? sound : this.getDefaultHitGroundSoundEvent();
      }
      
      this.setShotFromCrossbow(p_36761_.getBoolean("ShotFromCrossbow"));
      
      this.setArrowType(Arrow.ArrowType.byOrdinal(p_36761_.getInt("ArrowType")));
      this.setAirSpeed(p_36761_.getFloat("AirSpeed"));
      this.setWaterSpeed(p_36761_.getFloat("WaterSpeed"));
      this.setResistance(p_36761_.getFloat("Resistance"));
      
      if (p_36761_.contains("Potion", 8)) {
         this.potion = PotionUtils.getPotion(p_36761_);
      }
      
      for(MobEffectInstance mobeffectinstance : PotionUtils.getCustomEffects(p_36761_)) {
         this.addEffect(mobeffectinstance);
      }
      
      if (p_36761_.contains("Color", 99)) {
         this.setFixedColor(p_36761_.getInt("Color"));
      } else {
         this.updateColor();
      }
      
      ListTag damageSourceList = p_36761_.getList("AdditionalDamage", 10);
      for(int i = 0; i < damageSourceList.size(); i++) {
         CompoundTag tag = damageSourceList.getCompound(i);
         this.addAdditionalDamage(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(tag.getString("DamageType"))), tag.getFloat("Damage"));
      }
      
      ListTag explosionList = p_36761_.getList("Explosion", 10);
      for(int i = 0; i < explosionList.size(); i++) {
         CompoundTag tag = explosionList.getCompound(i);
         this.addExplosion(tag.getFloat("Power"), tag.getFloat("Capacity"), tag.getFloat("Damage"));
      }
   }
   
   public void addExplosion(float power, float capacity, float damage) {
      this.addExplosion(Mth.createInsecureUUID(), power, capacity, damage);
   }
   
   public void addExplosion(UUID uuid, float power, float capacity, float damage) {
      this.setExplosionPower(uuid, power);
      this.setExplosionCapacity(uuid, capacity);
      this.setExplosionDamage(uuid, damage);
   }
   
   public void setExplosionDamage(UUID uuid, float damage) {
      this.explosionDamage.put(uuid, damage);
   }
   
   public void setExplosionCapacity(UUID uuid, float capacity) {
      this.explosionCapacity.put(uuid, capacity);
   }
   
   public void setExplosionPower(UUID uuid, float power) {
      this.explosionPower.put(uuid, power);
   }
   
   public void addAdditionalDamage(ResourceKey<DamageType> type, float damage) {
      this.addAdditionalDamage(Mth.createInsecureUUID(), type, damage);
   }
   
   public void addAdditionalDamage(UUID uuid, ResourceKey<DamageType> type, float damage) {
      this.setAdditionalDamageSource(uuid, type);
      this.setAdditionalDamageValue(uuid, damage);
   }
   
   public void setAdditionalDamageSource(UUID uuid, ResourceKey<DamageType> type) {
      this.additionalDamageSource.put(uuid, type);
   }
   
   public void setAdditionalDamageValue(UUID uuid, float damage) {
      this.additionalDamageValue.put(uuid, damage);
   }
   
   public void setEffectsFromItem(ItemStack p_36879_) {
      if (p_36879_.is(Items.TIPPED_ARROW)) {
         this.potion = PotionUtils.getPotion(p_36879_);
         Collection<MobEffectInstance> collection = PotionUtils.getCustomEffects(p_36879_);
         if (!collection.isEmpty()) {
            for(MobEffectInstance mobeffectinstance : collection) {
               this.effects.add(new MobEffectInstance(mobeffectinstance));
            }
         }
         
         int i = getCustomColor(p_36879_);
         if (i == -1) {
            this.updateColor();
         } else {
            this.setFixedColor(i);
         }
      } else if (p_36879_.is(Items.ARROW)) {
         this.potion = Potions.EMPTY;
         this.effects.clear();
         this.entityData.set(ID_EFFECT_COLOR, -1);
      }
      
   }
   
   public static int getCustomColor(ItemStack p_36885_) {
      CompoundTag compoundtag = p_36885_.getTag();
      return compoundtag != null && compoundtag.contains("CustomPotionColor", 99) ? compoundtag.getInt("CustomPotionColor") : -1;
   }
   
   private void updateColor() {
      this.fixedColor = false;
      if (this.potion == Potions.EMPTY && this.effects.isEmpty()) {
         this.entityData.set(ID_EFFECT_COLOR, -1);
      } else {
         this.entityData.set(ID_EFFECT_COLOR, PotionUtils.getColor(PotionUtils.getAllEffects(this.potion, this.effects)));
      }
      
   }
   
   public void addEffect(MobEffectInstance p_36871_) {
      this.effects.add(p_36871_);
      this.entityData.set(ID_EFFECT_COLOR, PotionUtils.getColor(PotionUtils.getAllEffects(this.potion, this.effects)));
   }
   
   private void makeParticle(int p_36877_) {
      int i = this.getColor();
      if (i != -1 && p_36877_ > 0) {
         double d0 = (double)(i >> 16 & 255) / 255.0D;
         double d1 = (double)(i >> 8 & 255) / 255.0D;
         double d2 = (double)(i >> 0 & 255) / 255.0D;
         
         for(int j = 0; j < p_36877_; ++j) {
            this.level().addParticle(ParticleTypes.ENTITY_EFFECT, this.getRandomX(0.5D), this.getRandomY(), this.getRandomZ(0.5D), d0, d1, d2);
         }
         
      }
   }
   
   public int getColor() {
      return this.entityData.get(ID_EFFECT_COLOR);
   }
   
   private void setFixedColor(int p_36883_) {
      this.fixedColor = true;
      this.entityData.set(ID_EFFECT_COLOR, p_36883_);
   }
   
   public float getResistance() {
      return this.entityData.get(RESISTANCE);
   }
   
   public void setResistance(float resistance) {
      this.entityData.set(RESISTANCE, resistance);
   }
   
   public float getAirSpeed() {
      return this.entityData.get(AIR_SPEED);
   }
   
   public void setAirSpeed(float speed) {
      this.entityData.set(AIR_SPEED, speed);
   }
   
   public float getWaterSpeed() {
      return this.entityData.get(WATER_SPEED);
   }
   
   public void setWaterSpeed(float speed) {
      this.entityData.set(WATER_SPEED, speed);
   }
   
   public Arrow.ArrowType getArrowType() {
      return Arrow.ArrowType.byOrdinal(this.entityData.get(ARROW_TYPE));
   }
   
   public void setArrowType(Arrow.ArrowType type) {
      this.entityData.set(ARROW_TYPE, type.ordinal());
   }
   
   public void setOwner(@Nullable Entity p_36770_) {
      super.setOwner(p_36770_);
      if (p_36770_ instanceof Player) {
         this.pickup = ((Player)p_36770_).getAbilities().instabuild ? Arrow.Pickup.CREATIVE_ONLY : Arrow.Pickup.ALLOWED;
      }
      
   }
   
   public void playerTouch(Player p_36766_) {
      if (!this.level().isClientSide && (this.inGround || this.isNoPhysics() || (this.getResistance() <= 0.01F && this.getDeltaMovement().lengthSqr() < 0.25 && this.tickCount >= 200)) && this.shakeTime <= 0) {
         if (this.tryPickup(p_36766_)) {
            p_36766_.take(this, 1);
            this.discard();
         }
         
      }
   }
   
   public boolean tryPickup(Player p_150121_) {
      switch (this.pickup) {
         case ALLOWED:
            p_150121_.level().playSound(null, p_150121_.getX(), p_150121_.getY() + 0.5, p_150121_.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((p_150121_.level().random.nextFloat() - p_150121_.level().random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            return p_150121_.getInventory().add(this.getPickupItem());
         case CREATIVE_ONLY:
            if (p_150121_.getAbilities().instabuild) {
               p_150121_.level().playSound(null, p_150121_.getX(), p_150121_.getY() + 0.5, p_150121_.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((p_150121_.level().random.nextFloat() - p_150121_.level().random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            }
            return p_150121_.getAbilities().instabuild;
         default:
            return false;
      }
   }
   
   public ItemStack getPickupItem() {
      switch (this.getArrowType()) {
         case TRACKING_ARROW:
            return new ItemStack(ModItems.TRACKING_ARROW.get());
         
         default:
            if (this.effects.isEmpty() && this.potion == Potions.EMPTY) {
               return new ItemStack(Items.ARROW);
            } else {
               ItemStack itemstack = new ItemStack(Items.TIPPED_ARROW);
               PotionUtils.setPotion(itemstack, this.potion);
               PotionUtils.setCustomEffects(itemstack, this.effects);
               if (this.fixedColor) {
                  itemstack.getOrCreateTag().putInt("CustomPotionColor", this.getColor());
               }
               
               return itemstack;
            }
      }
   }
   
   public void handleEntityEvent(byte p_36869_) {
      if (p_36869_ == 0) {
         int i = this.getColor();
         if (i != -1) {
            double d0 = (double)(i >> 16 & 255) / 255.0D;
            double d1 = (double)(i >> 8 & 255) / 255.0D;
            double d2 = (double)(i >> 0 & 255) / 255.0D;
            
            for(int j = 0; j < 20; ++j) {
               this.level().addParticle(ParticleTypes.ENTITY_EFFECT, this.getRandomX(0.5D), this.getRandomY(), this.getRandomZ(0.5D), d0, d1, d2);
            }
         }
      } else {
         super.handleEntityEvent(p_36869_);
      }
      
   }
   
   @Override
   public ItemStack getPickResult() {
      return getPickupItem();
   }
   
   public Entity.MovementEmission getMovementEmission() {
      return Entity.MovementEmission.NONE;
   }
   
   public void setBaseDamage(double p_36782_) {
      this.baseDamage = p_36782_;
   }
   
   public double getBaseDamage() {
      return this.baseDamage;
   }
   
   public void setKnockback(int p_36736_) {
      this.knockback = p_36736_;
   }
   
   public int getKnockback() {
      return this.knockback;
   }
   
   public boolean isAttackable() {
      return false;
   }
   
   public float getEyeHeight(Pose p_36752_, EntityDimensions p_36753_) {
      return 0.13F;
   }
   
   public void setCritArrow(boolean p_36763_) {
      this.setFlag(1, p_36763_);
   }
   
   public void setPierceLevel(byte p_36768_) {
      this.entityData.set(PIERCE_LEVEL, p_36768_);
   }
   
   public void setFlag(int p_36738_, boolean p_36739_) {
      byte b0 = this.entityData.get(ID_FLAGS);
      if (p_36739_) {
         this.entityData.set(ID_FLAGS, (byte)(b0 | p_36738_));
      } else {
         this.entityData.set(ID_FLAGS, (byte)(b0 & ~p_36738_));
      }
      
   }
   
   public boolean isCritArrow() {
      byte b0 = this.entityData.get(ID_FLAGS);
      return (b0 & 1) != 0;
   }
   
   public boolean shotFromCrossbow() {
      byte b0 = this.entityData.get(ID_FLAGS);
      return (b0 & 4) != 0;
   }
   
   public byte getPierceLevel() {
      return this.entityData.get(PIERCE_LEVEL);
   }
   
   public void setEnchantmentEffectsFromEntity(LivingEntity p_36746_, float p_36747_) {
      int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER_ARROWS, p_36746_);
      int j = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH_ARROWS, p_36746_);
      this.setBaseDamage((double)(p_36747_ * 2.0F) + this.random.triangle((double)this.level().getDifficulty().getId() * 0.11D, 0.57425D));
      if (i > 0) {
         this.setBaseDamage(this.getBaseDamage() + (double)i * 0.5D + 0.5D);
      }
      
      if (j > 0) {
         this.setKnockback(j);
      }
      
      if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAMING_ARROWS, p_36746_) > 0) {
         this.setSecondsOnFire(100);
      }
      
   }
   
   public void setNoPhysics(boolean p_36791_) {
      this.noPhysics = p_36791_;
      this.setFlag(2, p_36791_);
   }
   
   public boolean isNoPhysics() {
      if (!this.level().isClientSide) {
         return this.noPhysics;
      } else {
         return (this.entityData.get(ID_FLAGS) & 2) != 0;
      }
   }
   
   public void setShotFromCrossbow(boolean p_36794_) {
      this.setFlag(4, p_36794_);
   }
   
   public static enum Pickup {
      DISALLOWED,
      ALLOWED,
      CREATIVE_ONLY;
      
      public static Arrow.Pickup byOrdinal(int p_36809_) {
         if (p_36809_ < 0 || p_36809_ > (values().length - 1)) {
            p_36809_ = 0;
         }
         
         return values()[p_36809_];
      }
   }
   
   public static enum ArrowType {
      NORMAL,
      TRACKING_ARROW;
      
      public static Arrow.ArrowType byOrdinal(int p_36809_) {
         if (p_36809_ < 0 || p_36809_ > (values().length - 1)) {
            p_36809_ = 0;
         }
         
         return values()[p_36809_];
      }
      
      public ResourceLocation getLocation() {
         return new ResourceLocation("endless_deep_space:textures/entities/arrow/arrow_" + this.toString().toLowerCase() + ".png");
      }
   }
}
