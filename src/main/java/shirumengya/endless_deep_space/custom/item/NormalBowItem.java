package shirumengya.endless_deep_space.custom.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import shirumengya.endless_deep_space.custom.entity.projectile.Arrow;
import shirumengya.endless_deep_space.init.EndlessDeepSpaceModEnchantments;

import java.util.function.Predicate;

public class NormalBowItem extends ModBowItem {
   public static final Predicate<ItemStack> ARROW_ONLY = (p_43017_) -> {
      return p_43017_.is(ItemTags.ARROWS);
   };
   
   public NormalBowItem(Properties p_40660_) {
      super(p_40660_);
   }
   
   @Override
   public Predicate<ItemStack> getAllSupportedProjectiles() {
      return ARROW_ONLY;
   }
   
   @Override
   public void releaseUsing(ItemStack p_40667_, Level p_40668_, LivingEntity p_40669_, int p_40670_) {
      if (p_40669_ instanceof Player player) {
         boolean flag = player.getAbilities().instabuild || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, p_40667_) > 0;
         ItemStack itemstack = player.getProjectile(p_40667_);
         
         int i = this.getUseDuration(p_40667_) - p_40670_;
         i = net.minecraftforge.event.ForgeEventFactory.onArrowLoose(p_40667_, p_40668_, player, i, !itemstack.isEmpty() || flag);
         if (i < 0) return;
         
         if (!itemstack.isEmpty() || flag) {
            if (itemstack.isEmpty()) {
               itemstack = new ItemStack(Items.ARROW);
            }
            
            float f = getPowerForTime(i);
            if (!((double)f < 0.1D)) {
               boolean flag1 = player.getAbilities().instabuild || (itemstack.getItem() == Items.ARROW && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, p_40667_) > 0);
               if (!p_40668_.isClientSide) {
                  Arrow abstractarrow = new Arrow(p_40669_, p_40668_, Arrow.ArrowType.NORMAL);
                  abstractarrow.setEffectsFromItem(itemstack);
                  int airSpeed = EnchantmentHelper.getItemEnchantmentLevel(EndlessDeepSpaceModEnchantments.SPEED.get(), p_40667_);
                  int waterSpeed = EnchantmentHelper.getItemEnchantmentLevel(EndlessDeepSpaceModEnchantments.RIPTIDE.get(), p_40667_);
                  abstractarrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, f * 3.0F + (player.isInWater() ? waterSpeed : airSpeed) / 2.0F, 1.0F);
                  
                  if (f == 1.0F) {
                     abstractarrow.setCritArrow(true);
                  }
                  
                  int j = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, p_40667_);
                  if (j > 0) {
                     abstractarrow.setBaseDamage(abstractarrow.getBaseDamage() + (double)j * 0.5D + 0.5D);
                  }
                  
                  int k = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, p_40667_);
                  if (k > 0) {
                     abstractarrow.setKnockback(k);
                  }
                  
                  if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, p_40667_) > 0) {
                     abstractarrow.setSecondsOnFire(100);
                  }
                  
                  if (waterSpeed > 0) {
                     abstractarrow.setWaterSpeed(abstractarrow.getWaterSpeed() + ((0.99F - 0.6F) / 4.0F) * waterSpeed);
                  }
                  
                  if (airSpeed > 0) {
                     abstractarrow.setAirSpeed(abstractarrow.getAirSpeed() + ((0.99F - 0.6F) / 8.0F) * airSpeed);
                  }
                  
                  abstractarrow.setBaseDamage(abstractarrow.getBaseDamage() + Math.max(airSpeed, waterSpeed));
                  
                  int resistance = EnchantmentHelper.getItemEnchantmentLevel(EndlessDeepSpaceModEnchantments.REDUCE_RESISTANCE.get(), p_40667_);
                  if (resistance > 0) {
                     abstractarrow.setResistance(Math.max(0.0F, abstractarrow.getResistance() - resistance * 0.01F));
                  }
                  
                  int explosionPower = EnchantmentHelper.getItemEnchantmentLevel(EndlessDeepSpaceModEnchantments.EXPLOSION.get(), p_40667_);
                  if (explosionPower > 0) {
                     abstractarrow.addExplosion(explosionPower * 2.0F, explosionPower * 20.0F, 0.1F);
                  }
                  
                  p_40667_.hurtAndBreak(Math.max(explosionPower * 5, Math.max(1 + resistance, Math.max(1 + waterSpeed, 1 + airSpeed))), player, (p_289501_) -> {
                     p_289501_.broadcastBreakEvent(player.getUsedItemHand());
                  });
                  if (flag1 || player.getAbilities().instabuild && (itemstack.is(Items.SPECTRAL_ARROW) || itemstack.is(Items.TIPPED_ARROW))) {
                     abstractarrow.pickup = Arrow.Pickup.CREATIVE_ONLY;
                  }
                  
                  p_40668_.addFreshEntity(abstractarrow);
               }
               
               p_40668_.playSound((Player)null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F / (p_40668_.getRandom().nextFloat() * 0.4F + 1.2F) + f * 0.5F);
               if (!flag1 && !player.getAbilities().instabuild) {
                  itemstack.shrink(1);
                  if (itemstack.isEmpty()) {
                     player.getInventory().removeItem(itemstack);
                  }
               }
               
               player.awardStat(Stats.ITEM_USED.get(this));
            }
         }
      }
   }
   
   public static float getPowerForTime(int p_40662_) {
      float f = (float)p_40662_ / 20.0F;
      f = (f * f + f * 2.0F) / 3.0F;
      if (f > 1.0F) {
         f = 1.0F;
      }
      
      return f;
   }
   
   @Override
   public int getUseDuration(ItemStack p_40680_) {
      return 72000;
   }
   
   @Override
   public UseAnim getUseAnimation(ItemStack p_40678_) {
      return UseAnim.BOW;
   }
   
   @Override
   public InteractionResultHolder<ItemStack> use(Level p_40672_, Player p_40673_, InteractionHand p_40674_) {
      ItemStack itemstack = p_40673_.getItemInHand(p_40674_);
      boolean flag = !p_40673_.getProjectile(itemstack).isEmpty();
      
      InteractionResultHolder<ItemStack> ret = net.minecraftforge.event.ForgeEventFactory.onArrowNock(itemstack, p_40672_, p_40673_, p_40674_, flag);
      if (ret != null) return ret;
      
      if (!p_40673_.getAbilities().instabuild && !flag) {
         return InteractionResultHolder.fail(itemstack);
      } else {
         p_40673_.startUsingItem(p_40674_);
         return InteractionResultHolder.consume(itemstack);
      }
   }
   
   @Override
   public int getDefaultProjectileRange() {
      return 15;
   }
}
