
package shirumengya.endless_deep_space.enchantment;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.DamageEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import shirumengya.endless_deep_space.init.EndlessDeepSpaceModEnchantments;

public class DecapitateEnchantment extends Enchantment {

	public DecapitateEnchantment() {
		super(Enchantment.Rarity.UNCOMMON, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
	}

	public int getMinCost(int p_44633_) {
		return 1 + (p_44633_ - 1) * 11;
	}

	public int getMaxCost(int p_44646_) {
		return this.getMinCost(p_44646_) + 20;
	}

	public int getMaxLevel() {
		return 10;
	}

	public float getDamageBonus(int level, MobType mobType, ItemStack enchantedItem) {
		int killEntityTimes = enchantedItem.getOrCreateTag().getInt("KillEntityTimes");
		return (1.0F + (float)Math.max(0, level - 1)) * Math.max(0.1F, (killEntityTimes / 40.0F));
	}

	public boolean checkCompatibility(Enchantment p_44644_) {
		return !(p_44644_ instanceof DamageEnchantment) && !(p_44644_ instanceof DecapitateEnchantment);
	}

	public void doPostAttack(LivingEntity p_44638_, Entity p_44639_, int p_44640_) {
		if (p_44639_ instanceof LivingEntity livingentity) {
			if (!livingentity.isAlive()) {
				int enchantmentLevel = p_44638_.getMainHandItem().getEnchantmentLevel(EndlessDeepSpaceModEnchantments.DECAPITATE.get());
				if (enchantmentLevel > 0) {
					p_44638_.getMainHandItem().getOrCreateTag().putInt("KillEntityTimes", p_44638_.getMainHandItem().getOrCreateTag().getInt("KillEntityTimes") + (int) ((enchantmentLevel / 2.0F) + 1.0F));
					p_44638_.getMainHandItem().hurtAndBreak((int) ((enchantmentLevel / 2.0F) + 1.0F), p_44638_, (p_43296_) -> {
						p_43296_.broadcastBreakEvent(EquipmentSlot.MAINHAND);
					});
				} else {
					enchantmentLevel = p_44638_.getOffhandItem().getEnchantmentLevel(EndlessDeepSpaceModEnchantments.DECAPITATE.get());
					if (enchantmentLevel > 0) {
						p_44638_.getOffhandItem().getOrCreateTag().putInt("KillEntityTimes", p_44638_.getOffhandItem().getOrCreateTag().getInt("KillEntityTimes") + (int) ((enchantmentLevel / 2.0F) + 1.0F));
						p_44638_.getOffhandItem().hurtAndBreak((int) ((enchantmentLevel / 2.0F) + 1.0F), p_44638_, (p_43296_) -> {
							p_43296_.broadcastBreakEvent(EquipmentSlot.MAINHAND);
						});
					}
				}
			}
		}
	}

	public boolean canEnchant(ItemStack p_44642_) {
		return p_44642_.getItem() instanceof AxeItem || super.canEnchant(p_44642_);
	}
}
