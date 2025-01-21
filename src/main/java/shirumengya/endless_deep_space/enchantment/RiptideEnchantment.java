
package shirumengya.endless_deep_space.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import shirumengya.endless_deep_space.custom.item.ModBowItem;

public class RiptideEnchantment extends Enchantment {
	public static final EnchantmentCategory ENCHANTMENT_CATEGORY_MOD_BOW = EnchantmentCategory.create("endless_deep_space_bow", item -> {
		return item instanceof ModBowItem;
	});

	public RiptideEnchantment() {
		super(Rarity.COMMON, ENCHANTMENT_CATEGORY_MOD_BOW, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
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
