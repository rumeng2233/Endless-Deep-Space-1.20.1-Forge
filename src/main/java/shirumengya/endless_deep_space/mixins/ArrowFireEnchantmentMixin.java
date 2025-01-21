package shirumengya.endless_deep_space.mixins;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ArrowFireEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import shirumengya.endless_deep_space.custom.item.ModBowItem;

@Mixin({ArrowFireEnchantment.class})
public abstract class ArrowFireEnchantmentMixin extends Enchantment {
	public ArrowFireEnchantmentMixin(Enchantment.Rarity p_44568_, EquipmentSlot... p_44569_) {
        super(p_44568_, EnchantmentCategory.BOW, p_44569_);
    }

	@Unique
	public boolean canEnchant(ItemStack p_44642_) {
		return p_44642_.getItem() instanceof ModBowItem || super.canEnchant(p_44642_);
	}
}