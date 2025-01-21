
package shirumengya.endless_deep_space.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class BlockStrengthenedEnchantment extends Enchantment {

	public BlockStrengthenedEnchantment() {
		super(Rarity.RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
	}

	@Override
	public int getMinCost(int level) {
		return 1 + level * 10;
	}

	@Override
	public int getMaxCost(int level) {
		return 6 + level * 10;
	}

	@Override
	public int getMaxLevel() {
		return 4;
	}

	public boolean checkCompatibility(Enchantment p_44644_) {
		return !(p_44644_ instanceof BlockStrengthenedEnchantment);
	}

	public boolean canEnchant(ItemStack p_44642_) {
		return p_44642_.getItem() instanceof SwordItem;
	}
}
