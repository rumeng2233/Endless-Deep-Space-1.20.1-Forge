
package shirumengya.endless_deep_space.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import shirumengya.endless_deep_space.custom.item.ModBowItem;

public class SpeedEnchantment extends Enchantment {
	public SpeedEnchantment() {
		super(Rarity.COMMON, RiptideEnchantment.ENCHANTMENT_CATEGORY_MOD_BOW, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
	}

	public int getMinCost(int p_45254_) {
		return 10 + p_45254_ * 7;
	}

	public int getMaxCost(int p_45258_) {
		return 50;
	}

	public int getMaxLevel() {
		return 4;
	}

	public boolean canEnchant(ItemStack p_44642_) {
		return p_44642_.getItem() instanceof ModBowItem;
	}
}
