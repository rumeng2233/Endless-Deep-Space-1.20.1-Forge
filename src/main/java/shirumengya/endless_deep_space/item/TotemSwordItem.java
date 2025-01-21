package shirumengya.endless_deep_space.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import shirumengya.endless_deep_space.procedures.TotemSwordEWaiDeDiaoLuoTiaoJianProcedure;
import shirumengya.endless_deep_space.procedures.TotemSwordShengWuBeiGongJuJiZhongShiProcedure;
import shirumengya.endless_deep_space.procedures.TotemSwordYouJiKongQiShiShiTiDeWeiZhiProcedure;

public class TotemSwordItem extends SwordItem {
	public TotemSwordItem() {
		super(new Tier() {
			public int getUses() {
				return 0;
			}

			public float getSpeed() {
				return Float.MAX_VALUE;
			}

			public float getAttackDamageBonus() {
				return Float.MAX_VALUE;
			}

			public int getLevel() {
				return Integer.MAX_VALUE;
			}

			public int getEnchantmentValue() {
				return Integer.MAX_VALUE;
			}

			public Ingredient getRepairIngredient() {
				return Ingredient.of();
			}
		}, 3, -2.4f, new Item.Properties().fireResistant().rarity(Rarity.EPIC));
	}

	@Override
	public boolean isCorrectToolForDrops(ItemStack itemstack, BlockState blockstate) {
		return super.isCorrectToolForDrops(itemstack, blockstate) && TotemSwordEWaiDeDiaoLuoTiaoJianProcedure.execute();
	}

	@Override
	public boolean hurtEnemy(ItemStack itemstack, LivingEntity entity, LivingEntity sourceentity) {
		boolean retval = super.hurtEnemy(itemstack, entity, sourceentity);
		TotemSwordShengWuBeiGongJuJiZhongShiProcedure.execute(sourceentity.level(), entity, sourceentity);
		return retval;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player entity, InteractionHand hand) {
		InteractionResultHolder<ItemStack> ar = super.use(world, entity, hand);
		TotemSwordYouJiKongQiShiShiTiDeWeiZhiProcedure.RightClick(world, entity);
		return ar;
	}

	public boolean canBeHurtBy(DamageSource p_41387_) {
		return false;
	}

	public float getDestroySpeed(ItemStack p_43288_, BlockState p_43289_) {
		return Float.MAX_VALUE;
	}

	public boolean isCorrectToolForDrops(BlockState p_43298_) {
		return true;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isFoil(ItemStack itemstack) {
		return true;
	}
}