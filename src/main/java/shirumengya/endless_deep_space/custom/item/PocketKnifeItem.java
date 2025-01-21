package shirumengya.endless_deep_space.custom.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber
public class PocketKnifeItem extends SwordItem implements Vanishable {
   private final float attackDamage;
   private final Multimap<Attribute, AttributeModifier> defaultModifiers;
   protected static final UUID BASE_BLOCK_ATTACK_REACH_UUID = UUID.fromString("8b6138f2-0e28-4981-a368-c516ad975000");
   protected static final UUID BASE_ENTITY_ATTACK_REACH_UUID = UUID.fromString("d0edab5a-f12c-40a3-8ab9-4ada2d7582e6");

   public PocketKnifeItem(Tier p_43269_, int p_43270_, float p_43271_, Item.Properties p_43272_) {
      super(p_43269_, p_43270_, p_43271_ / 6.0F * 3.0F, p_43272_.defaultDurability(p_43269_.getUses() / 5 * 3));
      this.attackDamage = (float)p_43270_ + (p_43269_.getAttackDamageBonus() / 5 * 3);
      ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
      builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", (double)this.attackDamage, AttributeModifier.Operation.ADDITION));
      builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", (double)p_43271_ / 6.0F * 3.0F, AttributeModifier.Operation.ADDITION));
      builder.put(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(BASE_BLOCK_ATTACK_REACH_UUID, "Weapon modifier", -0.5D, AttributeModifier.Operation.ADDITION));
      builder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(BASE_ENTITY_ATTACK_REACH_UUID, "Weapon modifier", -0.5D, AttributeModifier.Operation.ADDITION));
      this.defaultModifiers = builder.build();
   }

   @Override
   public float getDamage() {
      return this.attackDamage;
   }

   @Override
   public boolean canAttackBlock(BlockState p_43291_, Level p_43292_, BlockPos p_43293_, Player p_43294_) {
      return !p_43294_.isCreative();
   }

   @Override
   public float getDestroySpeed(ItemStack p_43288_, BlockState p_43289_) {
      if (p_43289_.is(Blocks.COBWEB)) {
         return 20.0F;
      } else {
         return p_43289_.is(BlockTags.SWORD_EFFICIENT) ? 1.5F : 1.0F;
      }
   }

   @Override
   public boolean hurtEnemy(ItemStack p_43278_, LivingEntity p_43279_, LivingEntity p_43280_) {
      p_43278_.hurtAndBreak(1, p_43280_, (p_43296_) -> {
         p_43296_.broadcastBreakEvent(EquipmentSlot.MAINHAND);
      });

      p_43279_.invulnerableTime = Math.max(0, p_43279_.invulnerableTime / 2);
      return true;
   }

   @Override
   public boolean mineBlock(ItemStack p_43282_, Level p_43283_, BlockState p_43284_, BlockPos p_43285_, LivingEntity p_43286_) {
      if (p_43284_.getDestroySpeed(p_43283_, p_43285_) != 0.0F) {
         p_43282_.hurtAndBreak(4, p_43286_, (p_43276_) -> {
            p_43276_.broadcastBreakEvent(EquipmentSlot.MAINHAND);
         });
      }

      return true;
   }

   @Override
   public boolean isCorrectToolForDrops(BlockState p_43298_) {
      return p_43298_.is(Blocks.COBWEB);
   }

   @Override
   public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot p_43274_) {
      return p_43274_ == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(p_43274_);
   }

   @Override
   public boolean canPerformAction(ItemStack stack, net.minecraftforge.common.ToolAction toolAction) {
      return net.minecraftforge.common.ToolActions.DEFAULT_SWORD_ACTIONS.contains(toolAction);
   }

   @Override
   public int getBarColor(ItemStack p_150901_) {
     float f = Math.max(0.0F, ((float)this.getMaxDamage() - (float)p_150901_.getDamageValue()) / (float)this.getMaxDamage());
     return Mth.hsvToRgb(f / 2.0F, 1.0F, 1.0F);
   }

   @SubscribeEvent
   public static void onPlayerCriticalHit(CriticalHitEvent event) {
      if (event.getEntity().getMainHandItem().getItem() instanceof PocketKnifeItem) {
         event.setDamageModifier(event.getDamageModifier() * 2.0F);
         if (!event.getEntity().onGround()) {
            event.setResult(Event.Result.ALLOW);
         }
      }
   }
}