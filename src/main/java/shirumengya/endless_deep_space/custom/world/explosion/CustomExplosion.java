package shirumengya.endless_deep_space.custom.world.explosion;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import shirumengya.endless_deep_space.custom.networking.ModMessages;
import shirumengya.endless_deep_space.custom.networking.packet.MushroomCloudS2CPacket;

import javax.annotation.Nullable;
import java.util.*;

public class CustomExplosion extends Explosion {
   private static final CustomExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new CustomExplosionDamageCalculator();
   private static final int MAX_DROPS_PER_COMBINED_STACK = 16;
   private final boolean fire;
   private final CustomExplosion.BlockInteraction blockInteraction;
   private final RandomSource random = RandomSource.create();
   private final Level level;
   private final double x;
   private final double y;
   private final double z;
   @Nullable
   private final Entity source;
   private final float radius;
   private final DamageSource damageSource;
   private final CustomExplosionDamageCalculator damageCalculator;
   private final ObjectArrayList<BlockPos> toBlow = new ObjectArrayList<>();
   private final Map<Player, Vec3> hitPlayers = Maps.newHashMap();
   private float damageReduction = 1.0F;
   
   public CustomExplosion(Level p_151471_, @Nullable Entity p_151472_, double p_151473_, double p_151474_, double p_151475_, float p_151476_) {
      this(p_151471_, p_151472_, p_151473_, p_151474_, p_151475_, p_151476_, false, BlockInteraction.DESTROY_WITH_DECAY);
   }
   
   public CustomExplosion(Level p_46024_, @Nullable Entity p_46025_, double p_46026_, double p_46027_, double p_46028_, float p_46029_, List<BlockPos> p_46030_) {
      this(p_46024_, p_46025_, p_46026_, p_46027_, p_46028_, p_46029_, false, BlockInteraction.DESTROY, p_46030_);
   }
   
   public CustomExplosion(Level p_46041_, @Nullable Entity p_46042_, double p_46043_, double p_46044_, double p_46045_, float p_46046_, boolean p_46047_, BlockInteraction p_46048_, List<BlockPos> p_46049_) {
      this(p_46041_, p_46042_, p_46043_, p_46044_, p_46045_, p_46046_, p_46047_, p_46048_);
      this.toBlow.addAll(p_46049_);
   }
   
   public CustomExplosion(Level p_46032_, @Nullable Entity p_46033_, double p_46034_, double p_46035_, double p_46036_, float p_46037_, boolean p_46038_, BlockInteraction p_46039_) {
      this(p_46032_, p_46033_, (DamageSource)null, (CustomExplosionDamageCalculator)null, p_46034_, p_46035_, p_46036_, p_46037_, p_46038_, p_46039_);
   }
   
   public CustomExplosion(Level p_46051_, @Nullable Entity p_46052_, @Nullable DamageSource p_46053_, @Nullable CustomExplosionDamageCalculator p_46054_, double p_46055_, double p_46056_, double p_46057_, float p_46058_, boolean p_46059_, BlockInteraction p_46060_, List<BlockPos> p_46061_) {
      this(p_46051_, p_46052_, p_46053_, p_46054_, p_46055_, p_46056_, p_46057_, p_46058_, p_46059_, p_46060_);
      this.toBlow.addAll(p_46061_);
   }
   
   public CustomExplosion(Level p_46051_, @Nullable Entity p_46052_, @Nullable DamageSource p_46053_, @Nullable CustomExplosionDamageCalculator p_46054_, double p_46055_, double p_46056_, double p_46057_, float p_46058_, boolean p_46059_, BlockInteraction p_46060_) {
      super(p_46051_, p_46052_, p_46053_, new ExplosionDamageCalculator(), p_46055_, p_46056_, p_46057_, p_46058_, p_46059_, Explosion.BlockInteraction.DESTROY);
      this.level = p_46051_;
      this.source = p_46052_;
      this.radius = p_46058_;
      this.x = p_46055_;
      this.y = p_46056_;
      this.z = p_46057_;
      this.fire = p_46059_;
      this.blockInteraction = p_46060_;
      this.damageSource = p_46053_ == null ? p_46051_.damageSources().explosion(this) : p_46053_;
      this.damageCalculator = p_46054_ == null ? this.makeDamageCalculator(p_46052_) : p_46054_;
   }
   
   private CustomExplosionDamageCalculator makeDamageCalculator(@Nullable Entity p_46063_) {
      return (CustomExplosionDamageCalculator)(p_46063_ == null ? EXPLOSION_DAMAGE_CALCULATOR : new EntityBasedCustomExplosionDamageCalculator(p_46063_));
   }
   
   public static float getSeenPercent(Vec3 p_46065_, Entity p_46066_) {
      AABB aabb = p_46066_.getBoundingBox();
      double d0 = 1.0D / ((aabb.maxX - aabb.minX) * 2.0D + 1.0D);
      double d1 = 1.0D / ((aabb.maxY - aabb.minY) * 2.0D + 1.0D);
      double d2 = 1.0D / ((aabb.maxZ - aabb.minZ) * 2.0D + 1.0D);
      double d3 = (1.0D - Math.floor(1.0D / d0) * d0) / 2.0D;
      double d4 = (1.0D - Math.floor(1.0D / d2) * d2) / 2.0D;
      if (!(d0 < 0.0D) && !(d1 < 0.0D) && !(d2 < 0.0D)) {
         int i = 0;
         int j = 0;
         
         for(double d5 = 0.0D; d5 <= 1.0D; d5 += d0) {
            for(double d6 = 0.0D; d6 <= 1.0D; d6 += d1) {
               for(double d7 = 0.0D; d7 <= 1.0D; d7 += d2) {
                  double d8 = Mth.lerp(d5, aabb.minX, aabb.maxX);
                  double d9 = Mth.lerp(d6, aabb.minY, aabb.maxY);
                  double d10 = Mth.lerp(d7, aabb.minZ, aabb.maxZ);
                  Vec3 vec3 = new Vec3(d8 + d3, d9, d10 + d4);
                  if (p_46066_.level().clip(new ClipContext(vec3, p_46065_, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p_46066_)).getType() == HitResult.Type.MISS) {
                     ++i;
                  }
                  
                  ++j;
               }
            }
         }
         
         return (float)i / (float)j;
      } else {
         return 0.0F;
      }
   }
   
   public void explode() {
      this.level.gameEvent(this.source, GameEvent.EXPLODE, new Vec3(this.x, this.y, this.z));
      if (this.toBlow.isEmpty()) {
         Set<BlockPos> set = Sets.newHashSet();
         int i = 16;
         
         for(int j = 0; j < 16; ++j) {
            for(int k = 0; k < 16; ++k) {
               for(int l = 0; l < 16; ++l) {
                  if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
                     double d0 = (double)((float)j / 15.0F * 2.0F - 1.0F);
                     double d1 = (double)((float)k / 15.0F * 2.0F - 1.0F);
                     double d2 = (double)((float)l / 15.0F * 2.0F - 1.0F);
                     double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                     d0 /= d3;
                     d1 /= d3;
                     d2 /= d3;
                     float f = this.radius * (0.7F + this.level.random.nextFloat() * 0.6F);
                     double d4 = this.x;
                     double d6 = this.y;
                     double d8 = this.z;
                     
                     for(float f1 = 0.3F; f > 0.0F; f -= 0.22500001F) {
                        BlockPos blockpos = BlockPos.containing(d4, d6, d8);
                        BlockState blockstate = this.level.getBlockState(blockpos);
                        FluidState fluidstate = this.level.getFluidState(blockpos);
                        if (!this.level.isInWorldBounds(blockpos)) {
                           break;
                        }
                        
                        Optional<Float> optional = this.damageCalculator.getBlockExplosionResistance(this, this.level, blockpos, blockstate, fluidstate);
                        if (optional.isPresent()) {
                           f -= (optional.get() + 0.3F) * 0.3F;
                        }
                        
                        if (f > 0.0F && this.damageCalculator.shouldBlockExplode(this, this.level, blockpos, blockstate, f)) {
                           set.add(blockpos);
                        }
                        
                        d4 += d0 * (double)0.3F;
                        d6 += d1 * (double)0.3F;
                        d8 += d2 * (double)0.3F;
                     }
                  }
               }
            }
         }
         
         this.toBlow.addAll(set);
      }
      float f2 = this.radius * 2.0F;
      int k1 = Mth.floor(this.x - (double)f2 - 1.0D);
      int l1 = Mth.floor(this.x + (double)f2 + 1.0D);
      int i2 = Mth.floor(this.y - (double)f2 - 1.0D);
      int i1 = Mth.floor(this.y + (double)f2 + 1.0D);
      int j2 = Mth.floor(this.z - (double)f2 - 1.0D);
      int j1 = Mth.floor(this.z + (double)f2 + 1.0D);
      List<Entity> list = this.level.getEntities(this.source, new AABB((double)k1, (double)i2, (double)j2, (double)l1, (double)i1, (double)j1));
      net.minecraftforge.event.ForgeEventFactory.onExplosionDetonate(this.level, this, list, f2);
      Vec3 vec3 = new Vec3(this.x, this.y, this.z);
      
      for(int k2 = 0; k2 < list.size(); ++k2) {
         Entity entity = list.get(k2);
         if (!entity.ignoreExplosion()) {
            double d12 = Math.sqrt(entity.distanceToSqr(vec3)) / (double)f2;
            if (d12 <= 1.0D) {
               double d5 = entity.getX() - this.x;
               double d7 = (entity instanceof PrimedTnt ? entity.getY() : entity.getEyeY()) - this.y;
               double d9 = entity.getZ() - this.z;
               double d13 = Math.sqrt(d5 * d5 + d7 * d7 + d9 * d9);
               if (d13 != 0.0D) {
                  d5 /= d13;
                  d7 /= d13;
                  d9 /= d13;
                  double d14 = (double)getSeenPercent(vec3, entity);
                  double d10 = (1.0D - d12) * d14;
                  entity.hurt(this.getDamageSource(), (float)((int)((d10 * d10 + d10) / 2.0D * 7.0D * (double)f2 + 1.0D)) * this.damageReduction);
                  double d11 = d10;
                  if (entity instanceof LivingEntity) {
                     d11 = ProtectionEnchantment.getExplosionKnockbackAfterDampener((LivingEntity)entity, d10);
                  }
                  
                  entity.setDeltaMovement(entity.getDeltaMovement().add(d5 * d11, d7 * d11, d9 * d11));
                  if (entity instanceof Player) {
                     Player player = (Player)entity;
                     if (!player.isSpectator() && (!player.isCreative() || !player.getAbilities().flying)) {
                        this.hitPlayers.put(player, new Vec3(d5 * d10, d7 * d10, d9 * d10));
                     }
                  }
               }
            }
         }
      }
      
   }
   
   public void finalizeExplosion(boolean p_46076_) {
      if (this.level.isClientSide) {
         this.level.playLocalSound(this.x, this.y, this.z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 4.0F, (1.0F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F) * 0.7F, false);
      }
      
      boolean flag = this.blockInteraction != BlockInteraction.KEEP;
      if (p_46076_) {
         if (!(this.radius < 2.0F) && flag) {
            this.level.addParticle(ParticleTypes.EXPLOSION_EMITTER, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
         } else {
            this.level.addParticle(ParticleTypes.EXPLOSION, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
         }
      }
      
      if (flag) {
         ObjectArrayList<Pair<ItemStack, BlockPos>> objectarraylist = new ObjectArrayList<>();
         boolean flag1 = this.getSourceMob() instanceof Player;
         Util.shuffle(this.toBlow, this.level.random);
         
         for(BlockPos blockpos : this.toBlow) {
            BlockState blockstate = this.level.getBlockState(blockpos);
            Block block = blockstate.getBlock();
            if (!blockstate.isAir()) {
               BlockPos blockpos1 = blockpos.immutable();
               this.level.getProfiler().push("explosion_blocks");
               if (this.blockInteraction != BlockInteraction.DESTROY_NO_LOOT && blockstate.canDropFromExplosion(this.level, blockpos, this)) {
                  Level $$9 = this.level;
                  if ($$9 instanceof ServerLevel) {
                     ServerLevel serverlevel = (ServerLevel)$$9;
                     BlockEntity blockentity = blockstate.hasBlockEntity() ? this.level.getBlockEntity(blockpos) : null;
                     LootParams.Builder lootparams$builder = (new LootParams.Builder(serverlevel)).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockpos)).withParameter(LootContextParams.TOOL, ItemStack.EMPTY).withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockentity).withOptionalParameter(LootContextParams.THIS_ENTITY, this.source);
                     if (this.blockInteraction == CustomExplosion.BlockInteraction.DESTROY_WITH_DECAY) {
                        lootparams$builder.withParameter(LootContextParams.EXPLOSION_RADIUS, this.radius);
                     }
                     
                     blockstate.spawnAfterBreak(serverlevel, blockpos, ItemStack.EMPTY, flag1);
                     blockstate.getDrops(lootparams$builder).forEach((p_46074_) -> {
                        addBlockDrops(objectarraylist, p_46074_, blockpos1);
                     });
                  }
               }
               
               this.level.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 3);
               block.wasExploded(this.level, blockpos, this);
               this.level.getProfiler().pop();
            }
         }
         
         for(Pair<ItemStack, BlockPos> pair : objectarraylist) {
            Block.popResource(this.level, pair.getSecond(), pair.getFirst());
         }
      }
      
      if (this.fire) {
         for(BlockPos blockpos2 : this.toBlow) {
            if (this.random.nextInt(3) == 0 && this.level.getBlockState(blockpos2).isAir() && this.level.getBlockState(blockpos2.below()).isSolidRender(this.level, blockpos2.below())) {
               this.level.setBlockAndUpdate(blockpos2, BaseFireBlock.getState(this.level, blockpos2));
            }
         }
      }
      
   }
   
   private static void addBlockDrops(ObjectArrayList<Pair<ItemStack, BlockPos>> p_46068_, ItemStack p_46069_, BlockPos p_46070_) {
      int i = p_46068_.size();
      
      for(int j = 0; j < i; ++j) {
         Pair<ItemStack, BlockPos> pair = p_46068_.get(j);
         ItemStack itemstack = pair.getFirst();
         if (ItemEntity.areMergable(itemstack, p_46069_)) {
            ItemStack itemstack1 = ItemEntity.merge(itemstack, p_46069_, 16);
            p_46068_.set(j, Pair.of(itemstack1, pair.getSecond()));
            if (p_46069_.isEmpty()) {
               return;
            }
         }
      }
      
      p_46068_.add(Pair.of(p_46069_, p_46070_));
   }
   
   public DamageSource getDamageSource() {
      return this.damageSource;
   }
   
   public Map<Player, Vec3> getHitPlayers() {
      return this.hitPlayers;
   }
   
   @Nullable
   public LivingEntity getSourceMob() {
      if (this.source == null) {
         return null;
      } else if (this.source instanceof PrimedTnt) {
         return ((PrimedTnt)this.source).getOwner();
      } else if (this.source instanceof LivingEntity) {
         return (LivingEntity)this.source;
      } else {
         if (this.source instanceof Projectile) {
            Entity entity = ((Projectile)this.source).getOwner();
            if (entity instanceof LivingEntity) {
               return (LivingEntity)entity;
            }
         }
         
         return null;
      }
   }
   
   public void clearToBlow() {
      this.toBlow.clear();
   }
   
   public List<BlockPos> getToBlow() {
      return this.toBlow;
   }
   
   public static CustomExplosion explode(Level p_46511, @Nullable Entity p_46512_, double p_46513_, double p_46514_, double p_46515_, float p_46516_, CustomExplosion.BlockInteraction p_46517_) {
      return explode(p_46511, p_46512_, (DamageSource)null, (CustomExplosionDamageCalculator)null, p_46513_, p_46514_, p_46515_, p_46516_, false, p_46517_);
   }
   
   public static CustomExplosion explode(Level p_46518, @Nullable Entity p_46519_, double p_46520_, double p_46521_, double p_46522_, float p_46523_, boolean p_46524_, CustomExplosion.BlockInteraction p_46525_) {
      return explode(p_46518, p_46519_, (DamageSource)null, (CustomExplosionDamageCalculator)null, p_46520_, p_46521_, p_46522_, p_46523_, p_46524_, p_46525_);
   }
   
   public static CustomExplosion explode(Level p_46525, @Nullable Entity p_46526_, @Nullable DamageSource p_46527_, @Nullable CustomExplosionDamageCalculator p_46528_, double p_46529_, double p_46530_, double p_46531_, float p_46532_, boolean p_46533_, CustomExplosion.BlockInteraction p_46534_) {
      CustomExplosion explosion = new CustomExplosion(p_46525, p_46526_, p_46527_, p_46528_, p_46529_, p_46530_, p_46531_, p_46532_, p_46533_, p_46534_);
      explosion.explode();
      explosion.finalizeExplosion(true);
      if (p_46534_ == BlockInteraction.KEEP) {
         explosion.clearToBlow();
      }
      
      for(Player player : p_46525.players()) {
         if (player instanceof ServerPlayer) {
            ServerPlayer serverplayer = (ServerPlayer)player;
            if (serverplayer.distanceToSqr(p_46529_, p_46530_, p_46531_) < 4096.0D) {
               serverplayer.connection.send(new ClientboundExplodePacket(p_46529_, p_46530_, p_46531_, p_46532_, explosion.getToBlow(), explosion.getHitPlayers().get(serverplayer)));
            }
         }
      }
      
      return explosion;
   }
   
   public static CustomExplosion explode(Level p_46511, @Nullable Entity p_46512_, double p_46513_, double p_46514_, double p_46515_, float p_46516_, CustomExplosion.BlockInteraction p_46517_, List<BlockPos> p_46518_) {
      return explode(p_46511, p_46512_, (DamageSource)null, (CustomExplosionDamageCalculator)null, p_46513_, p_46514_, p_46515_, p_46516_, false, p_46517_, p_46518_);
   }
   
   public static CustomExplosion explode(Level p_46518, @Nullable Entity p_46519_, double p_46520_, double p_46521_, double p_46522_, float p_46523_, boolean p_46524_, CustomExplosion.BlockInteraction p_46525_, List<BlockPos> p_46526_) {
      return explode(p_46518, p_46519_, (DamageSource)null, (CustomExplosionDamageCalculator)null, p_46520_, p_46521_, p_46522_, p_46523_, p_46524_, p_46525_, p_46526_);
   }
   
   public static CustomExplosion explode(Level p_46525, @Nullable Entity p_46526_, @Nullable DamageSource p_46527_, @Nullable CustomExplosionDamageCalculator p_46528_, double p_46529_, double p_46530_, double p_46531_, float p_46532_, boolean p_46533_, CustomExplosion.BlockInteraction p_46534_, List<BlockPos> p_46535_) {
      CustomExplosion explosion = new CustomExplosion(p_46525, p_46526_, p_46527_, p_46528_, p_46529_, p_46530_, p_46531_, p_46532_, p_46533_, p_46534_, p_46535_);
      explosion.explode();
      explosion.finalizeExplosion(true);
      if (p_46534_ == BlockInteraction.KEEP) {
         explosion.clearToBlow();
      }
      
      for(Player player : p_46525.players()) {
         if (player instanceof ServerPlayer) {
            ServerPlayer serverplayer = (ServerPlayer)player;
            if (serverplayer.distanceToSqr(p_46529_, p_46530_, p_46531_) < 4096.0D) {
               serverplayer.connection.send(new ClientboundExplodePacket(p_46529_, p_46530_, p_46531_, p_46532_, explosion.getToBlow(), explosion.getHitPlayers().get(serverplayer)));
            }
         }
      }
      
      return explosion;
   }
   
   public static double getBlastPower(double dist, double radius){
      double decay_rd = radius * 0.95;
      if(dist < decay_rd){
         return 1.1D;
      }
      else {
         return -(1 / (radius - decay_rd)) * (dist - decay_rd) + 1;
      }
   }
   
   public static List<BlockPos> getAffectedBlockPositions(Level world, double x, double y, double z, float radius, double max_blast_power, float yShorten){
      List<BlockPos> affectedBlockPositions = Lists.newArrayList();
      int radius_int = (int)Math.ceil(radius);
      for (int dx = -radius_int; dx < radius_int + 1; dx++) {
         int y_lim = (int)(Math.sqrt(radius_int * radius_int - dx * dx) / yShorten);
         for (int dy = -y_lim; dy < y_lim + 1; dy++) {
            int z_lim = (int)Math.sqrt(radius_int * radius_int - dx * dx - dy * dy * yShorten * yShorten);
            for (int dz = -z_lim; dz < z_lim + 1; dz++) {
               BlockPos blockPos = BlockPos.containing(x + dx, y + dy, z + dz);
               BlockState blockState = world.getBlockState(blockPos);
               double power = getBlastPower(Math.sqrt(dx * dx + dy * dy * yShorten * yShorten + dz * dz), radius);
               if (blockState != Blocks.AIR.defaultBlockState() && ((power > 1) ||(power > new Random().nextDouble()))){
                  float resistance = blockState.getBlock().getExplosionResistance();
                  if (resistance < max_blast_power) {
                     affectedBlockPositions.add(blockPos);
                  }
               }
            }
         }
      }
      
      return affectedBlockPositions;
   }
   
   public static void shockWave(Level level, double initialX, double initialY, double initialZ, float blastRadius) {
      if(!level.isClientSide()) {
         float multiplier = 2.0f;
         int effectiveHeight = 15;
         int effectRadius = (int)(multiplier * blastRadius);
         for (int dx = -effectRadius; dx <= effectRadius; dx++){
            int yMax = (int)Math.sqrt(effectRadius * effectRadius - dx * dx);
            for(int dz = -yMax; dz <= yMax; dz++){
               int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, (int)initialX + dx, (int)initialZ + dz);
               BlockPos interested = new BlockPos((int)initialX + dx, y - 1, (int)initialZ + dz);
               if(y - initialY < effectiveHeight || y - initialY > -effectiveHeight){
                  while (y - initialY > -effectiveHeight){
                     BlockPos interested1 = new BlockPos((int)initialX + dx, y - 1, (int)initialZ + dz);
                     BlockState blockState = level.getBlockState(interested);
                     level.destroyBlock(interested1, false);
                     y--;
                  }
               }
            }
         }
      }
   }
   
   public static void effectCore(Level level, double initialX, double initialY, double initialZ, float blastRadius) {
      if(!level.isClientSide()) {
         float multiplier = 2.0f;
         int effectiveHeight=15;
         int effectRadius = (int)(multiplier * blastRadius);
         RandomSource random = RandomSource.create();
         for (int dx = -effectRadius; dx <= effectRadius; dx++) {
            int yMax = (int)Math.sqrt(effectRadius * effectRadius - dx * dx);
            for(int dz = -yMax; dz <= yMax; dz++){
               int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, (int)initialX + dx, (int)initialZ + dz);
               BlockPos interested = new BlockPos((int)initialX + dx, y - 1, (int)initialZ + dz);
               if(dx * dx + dz * dz < blastRadius * blastRadius){
                  if(random.nextFloat() < 0.5){
                     level.setBlockAndUpdate(interested, Blocks.NETHERRACK.defaultBlockState());
                     if(random.nextFloat() < 0.2){
                        level.setBlockAndUpdate(interested.relative(Direction.UP), Blocks.FIRE.defaultBlockState());
                     }
                  }
                  if(random.nextFloat() < 0.05){
                     level.setBlockAndUpdate(interested, Blocks.LAVA.defaultBlockState());
                  }
               }
            }
         }
      }
   }
   
   public static void mushroomCloud(double x, double y, double z, double radius){
      ClientLevel world = Minecraft.getInstance().level;
      if (world.isClientSide) {
         int MAX_STEP_Y = (int)Math.ceil(1.25 * radius);
         double step_y = 2.986 * (radius) / MAX_STEP_Y;
         for(int step = 0; step < MAX_STEP_Y; step++) {
            double this_r = 0d;
            double delta_y = step * step_y;
            if (delta_y < radius / 3) {
               this_r = radius - delta_y * 2;
            } else if (delta_y < radius) {
               this_r = radius / 3;
            } else if (delta_y < 2.98 * radius) {
               this_r = Math.sqrt(radius * radius - (1.986 * radius - delta_y) * (1.986 * radius - delta_y));
            }
            int n_render = (int)Math.ceil(0.75 * this_r);
            double theta_step = 6.28 / n_render;
            for (int i = 0; i < n_render; i++){
               double theta = theta_step * i;
               double delta_x = this_r * Math.cos(theta);
               double delta_z = this_r * Math.sin(theta);
               if (delta_y > radius) {
                  //world.addParticle((ParticleOptions)ParticleTypeInit.NUKE_PARTICLE_FIRE.get(), true, x + delta_x, y + delta_y, z + delta_z, 0d, 0.07d, 0d);
                  world.addParticle((ParticleOptions)ParticleTypes.EXPLOSION_EMITTER, true, x + delta_x, y + delta_y, z + delta_z, 0d, 0.07d, 0d);
               } else {
                  world.addParticle((ParticleOptions)ParticleTypes.EXPLOSION_EMITTER, true, x + delta_x, y + delta_y, z + delta_z, 0d, 0.07d, 0d);
               }
            }
         }
      }
   }
   
   public static CustomExplosion nukeExplode(Level p_46511, @Nullable Entity p_46512_, double p_46513_, double p_46514_, double p_46515_, float p_46516_, CustomExplosion.BlockInteraction p_46517_, double p_46518_, float p_46519_) {
      return nukeExplode(p_46511, p_46512_, (DamageSource)null, (CustomExplosionDamageCalculator)null, p_46513_, p_46514_, p_46515_, p_46516_, 1.0F, false, p_46517_, p_46518_, p_46519_);
   }
   
   public static CustomExplosion nukeExplode(Level p_46518, @Nullable Entity p_46519_, double p_46520_, double p_46521_, double p_46522_, float p_46523_, boolean p_46524_, CustomExplosion.BlockInteraction p_46525_, double p_46526_, float p_46527_) {
      return nukeExplode(p_46518, p_46519_, (DamageSource)null, (CustomExplosionDamageCalculator)null, p_46520_, p_46521_, p_46522_, p_46523_, 1.0F, p_46524_, p_46525_, p_46526_, p_46527_);
   }
   
   public static CustomExplosion nukeExplode(Level p_46511, @Nullable Entity p_46512_, @Nullable DamageSource source, double p_46513_, double p_46514_, double p_46515_, float p_46516_, CustomExplosion.BlockInteraction p_46517_, double p_46518_, float p_46519_) {
      return nukeExplode(p_46511, p_46512_, source, (CustomExplosionDamageCalculator)null, p_46513_, p_46514_, p_46515_, p_46516_, 1.0F, false, p_46517_, p_46518_, p_46519_);
   }
   
   public static CustomExplosion nukeExplode(Level p_46518, @Nullable Entity p_46519_, @Nullable DamageSource source, double p_46520_, double p_46521_, double p_46522_, float p_46523_, boolean p_46524_, CustomExplosion.BlockInteraction p_46525_, double p_46526_, float p_46527_) {
      return nukeExplode(p_46518, p_46519_, source, (CustomExplosionDamageCalculator)null, p_46520_, p_46521_, p_46522_, p_46523_, 1.0F, p_46524_, p_46525_, p_46526_, p_46527_);
   }
   
   public static CustomExplosion nukeExplode(Level p_46518, @Nullable Entity p_46519_, double p_46520_, double p_46521_, double p_46522_, float p_46523_, float damageReduction, boolean p_46524_, CustomExplosion.BlockInteraction p_46525_, double p_46526_, float p_46527_) {
      return nukeExplode(p_46518, p_46519_, (DamageSource)null, (CustomExplosionDamageCalculator)null, p_46520_, p_46521_, p_46522_, p_46523_, damageReduction, p_46524_, p_46525_, p_46526_, p_46527_);
   }
   
   public static CustomExplosion nukeExplode(Level p_46511, @Nullable Entity p_46512_, @Nullable DamageSource source, double p_46513_, double p_46514_, double p_46515_, float p_46516_, float damageReduction, CustomExplosion.BlockInteraction p_46517_, double p_46518_, float p_46519_) {
      return nukeExplode(p_46511, p_46512_, source, (CustomExplosionDamageCalculator)null, p_46513_, p_46514_, p_46515_, p_46516_, damageReduction, false, p_46517_, p_46518_, p_46519_);
   }
   
   public static CustomExplosion nukeExplode(Level p_46518, @Nullable Entity p_46519_, @Nullable DamageSource source, double p_46520_, double p_46521_, double p_46522_, float p_46523_, float damageReduction, boolean p_46524_, CustomExplosion.BlockInteraction p_46525_, double p_46526_, float p_46527_) {
      return nukeExplode(p_46518, p_46519_, source, (CustomExplosionDamageCalculator)null, p_46520_, p_46521_, p_46522_, p_46523_, damageReduction, p_46524_, p_46525_, p_46526_, p_46527_);
   }
   
   public static CustomExplosion nukeExplode(Level p_46525, @Nullable Entity p_46526_, @Nullable DamageSource p_46527_, @Nullable CustomExplosionDamageCalculator p_46528_, double p_46529_, double p_46530_, double p_46531_, float p_46532_, float damageReduction, boolean p_46533_, CustomExplosion.BlockInteraction p_46534_, double p_46535_, float p_46536_) {
      List<BlockPos> affectedBlockPositions = getAffectedBlockPositions(p_46525, p_46529_, p_46530_, p_46531_, p_46532_, p_46535_, p_46536_);
      CustomExplosion explosion = new CustomExplosion(p_46525, p_46526_, p_46527_, p_46528_, p_46529_, p_46530_, p_46531_, p_46532_, p_46533_, p_46534_, affectedBlockPositions);
      explosion.damageReduction = damageReduction;
      if (net.minecraftforge.event.ForgeEventFactory.onExplosionStart(p_46525, explosion)) return explosion;
      explosion.explode();
      explosion.finalizeExplosion(true);
      if (p_46534_ == BlockInteraction.KEEP) {
         explosion.clearToBlow();
      }
      
      for(Player player : p_46525.players()) {
         if (player instanceof ServerPlayer) {
            ServerPlayer serverplayer = (ServerPlayer)player;
            if (serverplayer.distanceToSqr(p_46529_, p_46530_, p_46531_) < 4096.0D) {
               serverplayer.connection.send(new ClientboundExplodePacket(p_46529_, p_46530_, p_46531_, p_46532_, explosion.getToBlow(), explosion.getHitPlayers().get(serverplayer)));
               ModMessages.sendToPlayer(new MushroomCloudS2CPacket(p_46529_, p_46530_, p_46531_, p_46532_), serverplayer);
            }
         }
      }
      
      return explosion;
   }
   
   public static enum BlockInteraction {
      KEEP,
      DESTROY,
      DESTROY_WITH_DECAY,
      DESTROY_NO_LOOT;
   }
}