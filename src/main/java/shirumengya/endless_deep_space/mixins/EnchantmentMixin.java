package shirumengya.endless_deep_space.mixins;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import shirumengya.endless_deep_space.custom.util.java.color.RGBtoTen;

@Mixin({Enchantment.class})
public abstract class EnchantmentMixin {
	public EnchantmentMixin() {
		
	}

	@Inject(method = {"getFullname"}, at = {@At("HEAD")}, cancellable = true)
	public void getRuoMaNumber(int p_44701_, CallbackInfoReturnable<Component> ci) {
		Enchantment enchantment = ((Enchantment)(Object)this);
		MutableComponent mutablecomponent = Component.translatable(enchantment.getDescriptionId());
		if (enchantment.isCurse()) {
			mutablecomponent.withStyle(ChatFormatting.RED);
		} else {
			mutablecomponent.withStyle(ChatFormatting.GRAY);
		}

		if (p_44701_ != 1 || enchantment.getMaxLevel() != 1) {
			mutablecomponent.append(CommonComponents.SPACE).append(Component.literal(RGBtoTen.convertToRoman(p_44701_))).append(CommonComponents.SPACE).append(Component.translatable("enchantment.level", p_44701_).withStyle(ChatFormatting.DARK_GRAY));
		}

		ci.setReturnValue(mutablecomponent);
		ci.cancel();
	}
}
