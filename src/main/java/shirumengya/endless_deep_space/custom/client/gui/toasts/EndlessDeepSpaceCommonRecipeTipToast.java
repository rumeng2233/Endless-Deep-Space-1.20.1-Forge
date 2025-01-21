package shirumengya.endless_deep_space.custom.client.gui.toasts;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class EndlessDeepSpaceCommonRecipeTipToast implements Toast {
	ResourceLocation TOAST_TEXTURE = new ResourceLocation("textures/gui/endless_deep_space_toasts/common_toast.png");
	private final ItemStack items;
	private final ItemStack recipeitems;
	private final Component titles;
	private final Component descriptions;
	private final long timeleft;
	private final int titlecolors;
	private final int descriptioncolors;

	public EndlessDeepSpaceCommonRecipeTipToast(ItemStack item, ItemStack recipeitem,@Nullable Component title, @Nullable Component description, long time, int titlecolor, int descriptioncolor) {
		this.items = item;
		this.recipeitems = recipeitem;
		this.titles = title;
		this.descriptions = description;
		this.timeleft = time;
		this.titlecolors = titlecolor;
		this.descriptioncolors = descriptioncolor;
	}

	public static void add(ToastComponent toastcomponent, ItemStack item, ItemStack recipeitem,@Nullable Component title, @Nullable Component description, long time, int titlecolor, int descriptioncolor) {
      	toastcomponent.addToast(new EndlessDeepSpaceCommonRecipeTipToast(item, recipeitem, title, description, time, titlecolor, descriptioncolor));
   	}

	@Override
	public Toast.Visibility render(GuiGraphics p_94814_, ToastComponent p_94815_, long p_94816_) {
         p_94814_.blit(TOAST_TEXTURE, 0, 0, 0, 32, this.width(), this.height());
		 p_94814_.drawString(p_94815_.getMinecraft().font, this.titles, 30, 7, this.titlecolors, false);
		 p_94814_.drawString(p_94815_.getMinecraft().font, this.descriptions, 30, 18, this.descriptioncolors, false);
		 p_94814_.pose().pushPose();
		 p_94814_.pose().scale(0.6F, 0.6F, 1.0F);
		 p_94814_.renderFakeItem(this.items, 3, 3);
		 p_94814_.pose().popPose();
		 p_94814_.renderFakeItem(this.recipeitems, 8, 8);
//         p_94815_.getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F));
         return p_94816_ >= this.timeleft ? Visibility.HIDE : Visibility.SHOW;
   	}
}
