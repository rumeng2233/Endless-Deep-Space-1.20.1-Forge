package shirumengya.endless_deep_space.custom.event;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.ItemHandlerHelper;
import shirumengya.endless_deep_space.custom.config.ModCommonConfig;
import shirumengya.endless_deep_space.custom.networking.ModMessages;
import shirumengya.endless_deep_space.custom.networking.packet.UpdateIsDyingS2CPacket;
import shirumengya.endless_deep_space.custom.networking.packet.UpdateVertigoTimeS2CPacket;
import shirumengya.endless_deep_space.init.EndlessDeepSpaceModEnchantments;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber
public class SwordBlockEvent {
   public static final Map<Player, Long> PLAYER_LAST_BLOCK_SWORD = new HashMap<>();
   public static final Map<LivingEntity, Integer> livingEntityVertigoTime = new HashMap<>();
   
   public static final Map<LivingEntity, Integer> clientSideLivingEntityVertigoTime = new HashMap<>();
   
   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void swordBlock(LivingDamageEvent event) {
      if (!event.getEntity().level().isClientSide && event.getEntity() instanceof Player player && player.isUsingItem() && player.getUseItem().getItem() instanceof SwordItem) {
         Vec3 vec32 = event.getSource().getSourcePosition();
         if (vec32 != null) {
            Vec3 vec3 = player.getViewVector(1.0F);
            Vec3 vec31 = vec32.vectorTo(player.position()).normalize();
            vec31 = new Vec3(vec31.x, 0.0D, vec31.z);
            if (vec31.dot(vec3) < 0.0D) {
               Entity entity = event.getSource().getEntity();
               if (entity != null) {
                  long respondtime = player.level().getGameTime() - PLAYER_LAST_BLOCK_SWORD.get(player);
                  if (respondtime <= ModCommonConfig.SWORD_BLOCK_RESPOND_TIME.get()) {
                     if (!(entity instanceof Player player1 && player1.getUseItem().getItem() instanceof SwordItem) && !event.getSource().is(DamageTypes.THORNS)) {
                        entity.hurt(swordBlockAttack(player), event.getAmount() * 1.5F + ((SwordItem) player.getUseItem().getItem()).getDamage() + (event.getAmount() / 2.0F * (float) player.getUseItem().getEnchantmentLevel(EndlessDeepSpaceModEnchantments.BLOCK_STRENGTHENED.get())));
                        player.doEnchantDamageEffects(player, entity);
                     }
                     float yaw = player.yHeadRot;
                     float f = 2.0F;
                     double mz = Mth.cos(yaw / 180.0F * (float) Math.PI) * f / 2.0D;
                     double mx = -Mth.sin(yaw / 180.0F * (float) Math.PI) * f / 2.0D;
                     entity.setDeltaMovement(entity.getDeltaMovement().add(mx, 0.1D, mz));
                     event.setCanceled(true);
                     if (entity instanceof LivingEntity livingEntity) {
                        if (!immuneVertigo(livingEntity)) {
                           addVertigoTime(livingEntity, 60 + player.getUseItem().getEnchantmentLevel(EndlessDeepSpaceModEnchantments.BLOCK_STRENGTHENED.get()) * 10);
                        }
                        ItemStack stealItem = livingEntity.getMainHandItem() != ItemStack.EMPTY ? livingEntity.getMainHandItem() : (livingEntity.getOffhandItem() != ItemStack.EMPTY ? livingEntity.getOffhandItem() : livingEntity.getUseItem());
                        if (stealItem != ItemStack.EMPTY) {
                           if (player.getRandom().nextFloat() < (float) player.getUseItem().getEnchantmentLevel(EndlessDeepSpaceModEnchantments.BLOCK_STRENGTHENED.get()) / 4.0F) {
                              ItemHandlerHelper.giveItemToPlayer(player, stealItem.copyAndClear());
                           }
                        }
                        if (livingEntity instanceof Player player1) {
                           player1.disableShield(true);
                        }
                     }
                     player.getUseItem().hurtAndBreak(Math.max(0, (int) (6 + ((SwordItem) player.getUseItem().getItem()).getDamage() / 4.0F) - player.getUseItem().getEnchantmentLevel(EndlessDeepSpaceModEnchantments.BLOCK_STRENGTHENED.get())), player, (p_43296_) -> {
                        p_43296_.broadcastBreakEvent(player.getUsedItemHand());
                     });
                     player.level().broadcastEntityEvent(player, (byte)-1);
                     player.awardStat(Stats.ITEM_USED.get(player.getUseItem().getItem()));
                  } else {
                     if (ModCommonConfig.SWORD_BLOCK_COST.get() && respondtime > ModCommonConfig.SWORD_BLOCK_MAX_RESPOND_TIME.get()) {
                        event.setAmount(Math.max(0, event.getAmount() * (2.0F + ((SwordItem) player.getUseItem().getItem()).getDamage() / 20.0F - player.getUseItem().getEnchantmentLevel(EndlessDeepSpaceModEnchantments.BLOCK_STRENGTHENED.get()))));
                        player.getUseItem().hurtAndBreak(Math.max(0, (int) (event.getAmount() - ((SwordItem) player.getUseItem().getItem()).getDamage() / 4.0F) - player.getUseItem().getEnchantmentLevel(EndlessDeepSpaceModEnchantments.BLOCK_STRENGTHENED.get())), player, (p_43296_) -> {
                           p_43296_.broadcastBreakEvent(player.getUsedItemHand());
                        });
                        player.invulnerableTime = 0;
                        player.awardStat(Stats.ITEM_USED.get(player.getUseItem().getItem()));
                     } else {
                        if (!(entity instanceof Player player1 && player1.getUseItem().getItem() instanceof SwordItem) && !event.getSource().is(DamageTypes.THORNS)) {
                           entity.hurt(swordBlockAttack(player), event.getAmount() / 2.0F + ((SwordItem) player.getUseItem().getItem()).getDamage() / 4.0F + (event.getAmount() / 4.0F * (float) player.getUseItem().getEnchantmentLevel(EndlessDeepSpaceModEnchantments.BLOCK_STRENGTHENED.get())));
                           player.doEnchantDamageEffects(player, entity);
                        }
                        event.setAmount(Math.max(0, event.getAmount() / (2.0F + ((SwordItem) player.getUseItem().getItem()).getDamage() / 20.0F + player.getUseItem().getEnchantmentLevel(EndlessDeepSpaceModEnchantments.BLOCK_STRENGTHENED.get()))));
                        player.getUseItem().hurtAndBreak(Math.max(0, (int) (4 + ((SwordItem) player.getUseItem().getItem()).getDamage() / 4.0F) - player.getUseItem().getEnchantmentLevel(EndlessDeepSpaceModEnchantments.BLOCK_STRENGTHENED.get())), player, (p_43296_) -> {
                           p_43296_.broadcastBreakEvent(player.getUsedItemHand());
                        });
                        player.level().broadcastEntityEvent(player, (byte)-1);
                        player.awardStat(Stats.ITEM_USED.get(player.getUseItem().getItem()));
                     }
                  }
               }
            }
         }
      }
   }
   
   @SubscribeEvent
   public static void onPlayerBlockSword(PlayerInteractEvent.RightClickItem event) {
      if (!event.getLevel().isClientSide) {
         if(event.getItemStack().getItem() instanceof SwordItem) {
            PLAYER_LAST_BLOCK_SWORD.put(event.getEntity(), event.getLevel().getGameTime());
         }
      }
   }
   
   @SubscribeEvent
   public static void onPlayerAttack(AttackEntityEvent event) {
      if (event.isCancelable() && hasVertigoTime(event.getEntity())) {
         event.setCanceled(true);
      }
   }
   
   @SubscribeEvent
   public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
      LivingEntity entity = event.getEntity();
      if (hasVertigoTime(entity)) {
         entity.setDeltaMovement(Vec3.ZERO);
      }
   }
   
   @SubscribeEvent
   public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
      if (event.phase == TickEvent.Phase.END) {
         LivingEntity entity = event.player;
         if (hasVertigoTime(entity)) {
            entity.setDeltaMovement(Vec3.ZERO);
            if (entity instanceof ServerPlayer player) {
               player.connection.send(new ClientboundSetEntityMotionPacket(player.getId(), Vec3.ZERO));
            }
         }
      }
   }
   
   @SubscribeEvent
   public static void onEntityTick(LivingEvent.LivingTickEvent event) {
      if (hasVertigoTime(event.getEntity())) {
         event.getEntity().setDeltaMovement(Vec3.ZERO);
      }
   }
   
   @SubscribeEvent
   public static void onPlayerLeftClick(PlayerInteractEvent.LeftClickBlock event) {
      Player player = event.getEntity();
      if (event.isCancelable() && hasVertigoTime(player)) {
         event.setCanceled(true);
      }
   }
   
   @SubscribeEvent
   public static void onUseItem(LivingEntityUseItemEvent event) {
      LivingEntity living = event.getEntity();
      if (event.isCancelable() && hasVertigoTime(living)) {
         event.setCanceled(true);
      }
   }
   
   @SubscribeEvent
   public static void onPlaceBlock(BlockEvent.EntityPlaceEvent event) {
      Entity entity = event.getEntity();
      if (entity instanceof LivingEntity) {
         LivingEntity living = (LivingEntity) entity;
         if (event.isCancelable() && hasVertigoTime(living)) {
            event.setCanceled(true);
         }
      }
   }
   
   @SubscribeEvent
   public static void onFillBucket(FillBucketEvent event) {
      LivingEntity living = event.getEntity();
      if (living != null) {
         if (event.isCancelable() && hasVertigoTime(living)) {
            event.setCanceled(true);
         }
      }
   }
   
   @SubscribeEvent
   public static void onBreakBlock(BlockEvent.BreakEvent event) {
      if (event.isCancelable() && hasVertigoTime(event.getPlayer())) {
         event.setCanceled(true);
      }
   }
   
   @SubscribeEvent
   public static void onPlayerInteract(PlayerInteractEvent.RightClickEmpty event) {
      if (event.isCancelable() && hasVertigoTime(event.getEntity())) {
         event.setCanceled(true);
      }
   }
   
   @SubscribeEvent
   public static void onPlayerInteract(PlayerInteractEvent.LeftClickEmpty event) {
      if (event.isCancelable() && hasVertigoTime(event.getEntity())) {
         event.setCanceled(true);
      }
   }
   
   @SubscribeEvent
   public static void onPlayerInteract(PlayerInteractEvent.EntityInteract event) {
      if (event.isCancelable() && hasVertigoTime(event.getEntity())) {
         event.setCanceled(true);
      }
   }
   
   @SubscribeEvent
   public static void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
      if (event.isCancelable() && hasVertigoTime(event.getEntity())) {
         event.setCanceled(true);
      }
   }
   
   @SubscribeEvent
   public static void onPlayerInteract(PlayerInteractEvent.LeftClickBlock event) {
      if (event.isCancelable() && hasVertigoTime(event.getEntity())) {
         event.setCanceled(true);
      }
   }
   
   @SubscribeEvent
   public static void onPlayerInteract(PlayerInteractEvent.RightClickItem event) {
      if (event.isCancelable() && hasVertigoTime(event.getEntity())) {
         event.setCanceled(true);
      }
   }
   
   @SubscribeEvent
   public static void onEntityAttacked(LivingAttackEvent event) {
      if (event.isCancelable() && event.getSource().getEntity() != null && event.getSource().getEntity() instanceof LivingEntity livingEntity && hasVertigoTime(livingEntity)) {
         event.setCanceled(true);
      }
   }
   
   public static DamageSource swordBlockAttack(Entity entity) {
      return new DamageSource(entity.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("endless_deep_space:sword_block"))), entity);
   }
   
   public static void addVertigoTime(LivingEntity entity, int value) {
      SwordBlockEvent.setVertigoTime(entity, SwordBlockEvent.getVertigoTime(entity) + value);
   }
   
   public static void setVertigoTime(LivingEntity entity, int value) {
      if (!entity.level().isClientSide) {
         SwordBlockEvent.livingEntityVertigoTime.put(entity, Math.max(-1, value));
         
         ModMessages.sendToAllPlayers(new UpdateVertigoTimeS2CPacket(entity, false, getVertigoTime(entity)));
      } else {
         SwordBlockEvent.clientSideLivingEntityVertigoTime.put(entity, Math.max(-1, value));
      }
   }
   
   public static void removeVertigoTime(LivingEntity entity) {
      if (!entity.level().isClientSide) {
         SwordBlockEvent.livingEntityVertigoTime.remove(entity);
         
         ModMessages.sendToAllPlayers(new UpdateVertigoTimeS2CPacket(entity, true, getVertigoTime(entity)));
      } else {
         SwordBlockEvent.clientSideLivingEntityVertigoTime.remove(entity);
      }
   }
   
   public static int getVertigoTime(LivingEntity entity) {
      if (!entity.level().isClientSide) {
         return SwordBlockEvent.livingEntityVertigoTime.getOrDefault(entity, 0);
      } else {
         return SwordBlockEvent.clientSideLivingEntityVertigoTime.getOrDefault(entity, 0);
      }
   }
   
   public static boolean hasVertigoTime(LivingEntity entity) {
      return getVertigoTime(entity) > 0 || getVertigoTime(entity) == MobEffectInstance.INFINITE_DURATION;
   }
   
   public static boolean immuneVertigo(LivingEntity entity) {
      return entity.getType().is(TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("endless_deep_space.custom:vertigo_immune")));
   }
}
