package shirumengya.endless_deep_space.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import shirumengya.endless_deep_space.custom.item.SwordItemBlock;

import java.util.function.Consumer;

@Mixin({SwordItem.class})
public abstract class SwordItemMixin extends TieredItem implements Vanishable {
	public SwordItemMixin(Tier tier, Item.Properties p_43272_) {
		super(tier, p_43272_);
	}

	@Unique
	public UseAnim getUseAnimation(ItemStack p_43105_) {
		return UseAnim.CUSTOM;
	}

	@Unique
	public int getUseDuration(ItemStack p_43107_) {
		return 72000;
	}

	@Unique
	public InteractionResultHolder<ItemStack> use(Level p_43099_, Player p_43100_, InteractionHand p_43101_) {
		ItemStack itemstack = p_43100_.getItemInHand(p_43101_);
		p_43100_.startUsingItem(p_43101_);
		return InteractionResultHolder.consume(itemstack);
	}

	@Unique
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(new IClientItemExtensions() {
			@Override
			public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack itemInHand, float partialTick, float equipProcess, float swingProcess) {
				return SwordItemBlock.applyForgeHandTransform(poseStack, player, arm, itemInHand, partialTick, equipProcess, swingProcess);
			}

			@Override
			public HumanoidModel.@NotNull ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
				return entityLiving.isUsingItem() && entityLiving.getUseItem() == itemStack ? SwordItemBlock.SWORD_BLOCK : HumanoidModel.ArmPose.ITEM;
			}
		});
	}
}
