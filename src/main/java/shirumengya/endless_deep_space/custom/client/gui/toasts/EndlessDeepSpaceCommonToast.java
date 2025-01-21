package shirumengya.endless_deep_space.custom.client.gui.toasts;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class EndlessDeepSpaceCommonToast implements Toast {
   ResourceLocation TOAST_TEXTURE = new ResourceLocation("endless_deep_space:textures/gui/endless_deep_space_toasts/common_toast.png");
   private Component title;
   private List<FormattedCharSequence> messageLines;
   private long lastChanged;
   private boolean changed;
   private int width;
   @Nullable
   private ItemStack items;
   private int titlecolors;
   private int descriptioncolors;
   private long times;

   public EndlessDeepSpaceCommonToast(@Nullable ItemStack item, Component p_94833_, @Nullable Component p_94834_, long time, int titlecolor, int descriptioncolor) {
      this(item, p_94833_, nullToEmpty(p_94834_), Math.max(160, 30 + Math.max(Minecraft.getInstance().font.width(p_94833_), p_94834_ == null ? 0 : Minecraft.getInstance().font.width(p_94834_))), time, titlecolor, descriptioncolor);
   }

   public static EndlessDeepSpaceCommonToast multiline(Minecraft p_94848_, @Nullable ItemStack item, Component p_94850_, Component p_94851_, long time, int titlecolor, int descriptioncolor) {
      Font font = p_94848_.font;
      int titleWidth = font.width(p_94850_);
      List<FormattedCharSequence> list = font.split(p_94851_, 130);
      int i = Math.max(160, list.stream().mapToInt(font::width).max().orElse(160));
      return new EndlessDeepSpaceCommonToast(item, p_94850_, list, Math.max(i, titleWidth), time, titlecolor, descriptioncolor);
   }

   public EndlessDeepSpaceCommonToast(@Nullable ItemStack item, Component p_94828_, List<FormattedCharSequence> p_94829_, int p_94830_, long time, int titlecolor, int descriptioncolor) {
      this.items = item;
      this.title = p_94828_;
      this.messageLines = p_94829_;
      this.width = p_94830_;
      this.times = time;
      this.titlecolors = titlecolor;
      this.descriptioncolors = descriptioncolor;
   }

   private static ImmutableList<FormattedCharSequence> nullToEmpty(@Nullable Component p_94861_) {
      return p_94861_ == null ? ImmutableList.of() : ImmutableList.of(p_94861_.getVisualOrderText());
   }

   public int width() {
      return this.width;
   }

   public int height() {
      return 20 + Math.max(this.messageLines.size(), 1) * 12;
   }

   public Toast.Visibility render(GuiGraphics p_281624_, ToastComponent p_282333_, long p_282762_) {
      if (this.changed) {
         this.lastChanged = p_282762_;
         this.changed = false;
      }

      int i = this.width();
      if (i == 160 && this.messageLines.size() <= 1) {
         p_281624_.blit(TOAST_TEXTURE, 0, 0, 0, 128, i, this.height());
      } else {
         int j = this.height();
         int k = 28;
         int l = Math.min(4, j - 28);
         this.renderBackgroundRow(p_281624_, p_282333_, i, 0, 0, 28);

         for(int i1 = 28; i1 < j - l; i1 += 10) {
            this.renderBackgroundRow(p_281624_, p_282333_, i, 16, i1, Math.min(16, j - i1 - l));
         }

         this.renderBackgroundRow(p_281624_, p_282333_, i, 32 - l, j - l, l);
      }

      if (this.messageLines == null) {
         p_281624_.drawString(p_282333_.getMinecraft().font, this.title, this.items != null ? 30 : 5, 12, this.titlecolors, false);
      } else {
         p_281624_.drawString(p_282333_.getMinecraft().font, this.title, this.items != null ? 30 : 5, 7, this.titlecolors, false);

         for(int j1 = 0; j1 < this.messageLines.size(); ++j1) {
            p_281624_.drawString(p_282333_.getMinecraft().font, this.messageLines.get(j1), this.items != null ? 30 : 5, 18 + j1 * 12, this.descriptioncolors, false);
         }
      }

       if (this.items != null) {
           p_281624_.renderFakeItem(this.items, 8, 8);
       }

       return p_282762_ - this.lastChanged < times ? Visibility.SHOW : Visibility.HIDE;
   }

   private void renderBackgroundRow(GuiGraphics p_281840_, ToastComponent p_281283_, int p_281750_, int p_282371_, int p_283613_, int p_282880_) {
      int i = p_282371_ == 0 ? 20 : 5;
      int j = Math.min(60, p_281750_ - i);
      p_281840_.blit(TOAST_TEXTURE, 0, p_283613_, 0, 128 + p_282371_, i, p_282880_);

      for(int k = i; k < p_281750_ - j; k += 64) {
         p_281840_.blit(TOAST_TEXTURE, k, p_283613_, 32, 128 + p_282371_, Math.min(64, p_281750_ - k - j), p_282880_);
      }

      p_281840_.blit(TOAST_TEXTURE, p_281750_ - j, p_283613_, 160 - j, 128 + p_282371_, j, p_282880_);
   }

   public void reset(@Nullable ItemStack item, Component p_94863_, @Nullable Component p_94864_, long time, int titlecolor, int descriptioncolor) {
      this.items = item;
      this.title = p_94863_;
      Font font = Minecraft.getInstance().font;
      int titleWidth = font.width(this.title);
      List<FormattedCharSequence> list = font.split(p_94864_ != null ? p_94864_ : Component.empty(), 130);
      int i = Math.max(160, list.stream().mapToInt(font::width).max().orElse(160));
      this.messageLines = list;
      this.width = Math.max(i, titleWidth);
      this.times = time;
      this.titlecolors = titlecolor;
      this.descriptioncolors = descriptioncolor;
      this.changed = true;
   }

   public static void add(ToastComponent p_94856_, @Nullable ItemStack item, Component p_94858_, @Nullable Component p_94859_, long time, int titlecolor, int descriptioncolor) {
      p_94856_.addToast(EndlessDeepSpaceCommonToast.multiline(Minecraft.getInstance(), item, p_94858_, p_94859_ != null ? p_94859_ : Component.empty(), time, titlecolor, descriptioncolor));
   }

   public static void addOrUpdate(ToastComponent p_94870_, @Nullable ItemStack item, Component p_94872_, @Nullable Component p_94873_, long time, int titlecolor, int descriptioncolor) {
      EndlessDeepSpaceCommonToast systemtoast = p_94870_.getToast(EndlessDeepSpaceCommonToast.class, NO_TOKEN);
      if (systemtoast == null) {
         add(p_94870_, item, p_94872_, p_94873_, time, titlecolor, descriptioncolor);
      } else {
         systemtoast.reset(item, p_94872_, p_94873_, time, titlecolor, descriptioncolor);
      }

   }
}