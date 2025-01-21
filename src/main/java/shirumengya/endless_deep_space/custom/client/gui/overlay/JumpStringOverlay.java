package shirumengya.endless_deep_space.custom.client.gui.overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Timer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import shirumengya.endless_deep_space.custom.client.gui.CustomBossBar;
import shirumengya.endless_deep_space.custom.client.renderer.entity.EnderLordRenderer;
import shirumengya.endless_deep_space.custom.util.java.color.RGBtoTen;

import java.awt.*;

public class JumpStringOverlay {
   private static Minecraft minecraft = Minecraft.getInstance();
   public static  final Timer timer = new Timer(20.0F, 0L);
   public static Component BigJumpString = Component.empty();
   public static int BigJumpTime = 0;
   public static int BigJumpStayTime = 0;
   public static int BigJumpFadeOutTime = 0;
   public static int BigJumpFadeInTime = 0;
   public static boolean BigAnimateJumpMessageColor = false;
   public static int BigJumpStringX = 0;
   public static int BigJumpStringY = 0;
   public static int BigJumpStringWidths = 0;
   public static int BigJumpStringHeights = 0;
   public static boolean BigJumpStringCanUpdate = true;
   public static Component JumpString = Component.empty();
   public static int JumpTime = 0;
   public static int JumpStayTime = 0;
   public static int JumpFadeOutTime = 0;
   public static int JumpFadeInTime = 0;
   public static boolean AnimateJumpMessageColor = false;
   public static int JumpStringX = 0;
   public static int JumpStringY = 0;
   public static int JumpStringWidths = 0;
   public static int JumpStringHeights = 0;
   public static boolean JumpStringCanUpdate = true;
   public static Component SmallJumpString = Component.empty();
   public static int SmallJumpTime = 0;
   public static int SmallJumpStayTime = 0;
   public static int SmallJumpFadeOutTime = 0;
   public static int SmallJumpFadeInTime = 0;
   public static boolean SmallAnimateJumpMessageColor = false;
   public static int SmallJumpStringX = 0;
   public static int SmallJumpStringY = 0;
   public static int SmallJumpStringWidths = 0;
   public static int SmallJumpStringHeights = 0;
   public static boolean SmallJumpStringCanUpdate = true;
   
   public static final IGuiOverlay BIG_JUMP_STRING = ((gui, poseStack, partialTick, width, height) -> {
      renderBigJumpString(poseStack, timer.partialTick, width, height);
   });
   
   public static void renderBigJumpString(GuiGraphics poseStack, float partialTick, int width, int height) {
      BigJumpStringWidths = width;
      BigJumpStringHeights = height;
      Player entity = Minecraft.getInstance().player;
      minecraft = Minecraft.getInstance();
      int i = minecraft.getWindow().getGuiScaledWidth();
      
      //方法弃用，由于每个玩家客户端帧率的不同，导致此方法无法平等每个玩家客户端跳字的显示时间
		/*if (BigJumpTime > 0) {
	   		BigJumpTime--;
	   		if (BigJumpTime <= 0) {
	   			BigJumpString = null;
	   			BigAnimateJumpMessageColor = false;
	   			BigJumpStringY = BigJumpStringHeights / 6 + 15;
	   			BigJumpStringCanUpdate = true;
	   		}
	   	}*/
      
      if (BigJumpTime > 0 && BigJumpString != null) {
         int l = minecraft.font.width(BigJumpString);
         int i1 = i / 2 - l;
         float f4 = (float)BigJumpTime - partialTick;
         int alpha = 255;
         if (BigJumpTime > BigJumpFadeOutTime + BigJumpStayTime) {
            float f6 = (float)(BigJumpFadeInTime + BigJumpStayTime + BigJumpFadeOutTime) - f4;
            alpha = (int)(f6 * 255.0F / (float)BigJumpFadeInTime);
            //BigJumpStringY--;
         }
         
         if (BigJumpTime <= BigJumpFadeOutTime) {
            alpha = (int)(f4 * 255.0F / (float)BigJumpFadeOutTime);
            //BigJumpStringY--;
         }
         
         alpha = Mth.clamp(alpha, 0, 255);
         poseStack.pose().pushPose();
         poseStack.pose().scale(2, 2, 2);
         if (alpha > 8) {
            int k1 = 16777215;
            if (BigAnimateJumpMessageColor) {
               //k1 = Mth.hsvToRgb(f4 / 50.0F, 0.7F, 0.6F) & 16777215;
               Color color = EnderLordRenderer.rainbow(4000, 0.5F, 1.0F);
               k1 = RGBtoTen.OutputResult(color.getRed(), color.getGreen(), color.getBlue());
            }
            int k = alpha << 24 & -16777216;
            drawBackdrop(poseStack, minecraft.font, -10, l, 16777215 | k);
            //minecraft.font.draw(poseStack, BigJumpString.copy().withStyle(BigJumpString.getStyle().withFont(new ResourceLocation("endless_deep_space:genshin_impact_font"))), i1 / 2, BigJumpStringY / 2, k1 | k);
            poseStack.drawString(minecraft.font, BigJumpString, i1 / 2, BigJumpStringY / 2, k1 | k, false);
         }
         poseStack.pose().popPose();
      }
   }
   
   public static void UpdateBigJumpString(Component string, int time, boolean animateColor, boolean canUpdate) {
      if (BigJumpStringCanUpdate) {
         BigJumpString = string.copy().withStyle(CustomBossBar.GENSHIN_IMPACT_FONT);
         BigJumpFadeInTime = 10;
         BigJumpFadeOutTime = 10;
         BigJumpStayTime = time;
         BigJumpTime = BigJumpFadeInTime + BigJumpStayTime + BigJumpFadeOutTime;
         BigAnimateJumpMessageColor = animateColor;
         BigJumpStringY = BigJumpStringHeights / 6 + 15;
         BigJumpStringX = BigJumpStringWidths / 2;
         BigJumpStringCanUpdate = canUpdate;
      }
   }
   
   public static final IGuiOverlay JUMP_STRING = ((gui, poseStack, partialTick, width, height) -> {
      renderJumpString(poseStack, timer.partialTick, width, height);
   });
   
   public static void renderJumpString(GuiGraphics poseStack, float partialTick, int width, int height) {
      JumpStringWidths = width;
      JumpStringHeights = height;
      Player entity = Minecraft.getInstance().player;
      minecraft = Minecraft.getInstance();
      int i = minecraft.getWindow().getGuiScaledWidth();
      
      //方法弃用，由于每个玩家客户端帧率的不同，导致此方法无法平等每个玩家客户端跳字的显示时间
		/*if (JumpTime > 0) {
	   		JumpTime--;
	   		if (JumpTime <= 0) {
	   			JumpString = null;
	   			AnimateJumpMessageColor = false;
	   			JumpStringY = JumpStringHeights / 6 + 30;
	   			JumpStringCanUpdate = true;
	   		}
	   	}*/
      
      if (JumpTime > 0 && JumpString != null) {
         int l = minecraft.font.width(JumpString);
         int i1 = i / 2 - l / 2;
         float f4 = (float)JumpTime - partialTick;
         int alpha = 255;
         if (JumpTime > JumpFadeOutTime + JumpStayTime) {
            float f6 = (float)(JumpFadeInTime + JumpStayTime + JumpFadeOutTime) - f4;
            alpha = (int)(f6 * 255.0F / (float)JumpFadeInTime);
            //JumpStringY--;
         }
         
         if (JumpTime <= JumpFadeOutTime) {
            alpha = (int)(f4 * 255.0F / (float)JumpFadeOutTime);
            //JumpStringY--;
         }
         
         alpha = Mth.clamp(alpha, 0, 255);
         poseStack.pose().pushPose();
         poseStack.pose().scale(1, 1, 1);
         if (alpha > 8) {
            int k1 = 16777215;
            if (AnimateJumpMessageColor) {
               //k1 = Mth.hsvToRgb(f4 / 50.0F, 0.7F, 0.6F) & 16777215;
               Color color = EnderLordRenderer.rainbow(4000, 0.5F, 1.0F);
               k1 = RGBtoTen.OutputResult(color.getRed(), color.getGreen(), color.getBlue());
            }
            int k = alpha << 24 & -16777216;
            drawBackdrop(poseStack, minecraft.font, -10, l, 16777215 | k);
            poseStack.drawString(minecraft.font, JumpString, i1, JumpStringY, k1 | k, false);
         }
         poseStack.pose().popPose();
      }
   }
   
   public static void UpdateJumpString(Component string, int time, boolean animateColor, boolean canUpdate) {
      if (JumpStringCanUpdate) {
         JumpString = string.copy().withStyle(CustomBossBar.GENSHIN_IMPACT_FONT);
         JumpFadeInTime = 10;
         JumpFadeOutTime = 10;
         JumpStayTime = time;
         JumpTime = JumpFadeInTime + JumpStayTime + JumpFadeOutTime;
         AnimateJumpMessageColor = animateColor;
         JumpStringY = JumpStringHeights / 6 + 30;
         JumpStringX = JumpStringWidths / 2;
         JumpStringCanUpdate = canUpdate;
      }
   }
   
   public static final IGuiOverlay SMALL_JUMP_STRING = ((gui, poseStack, partialTick, width, height) -> {
      renderSmallJumpString(poseStack, timer.partialTick, width, height);
   });
   
   public static void renderSmallJumpString(GuiGraphics poseStack, float partialTick, int width, int height) {
      SmallJumpStringWidths = width;
      SmallJumpStringHeights = height;
      Player entity = Minecraft.getInstance().player;
      minecraft = Minecraft.getInstance();
      int i = minecraft.getWindow().getGuiScaledWidth();
      
      //方法弃用，由于每个玩家客户端帧率的不同，导致此方法无法平等每个玩家客户端跳字的显示时间
		/*if (SmallJumpTime > 0) {
	   		SmallJumpTime--;
	   		if (SmallJumpTime <= 0) {
	   			SmallJumpString = null;
	   			SmallAnimateJumpMessageColor = false;
	   			SmallJumpStringY = SmallJumpStringHeights / 6;
	   			SmallJumpStringCanUpdate = true;
	   		}
	   	}*/
      
      if (SmallJumpTime > 0 && SmallJumpString != null) {
         int l = minecraft.font.width(SmallJumpString);
         int i1 = i / 2 - l / 4;
         float f4 = (float)SmallJumpTime - partialTick;
         int alpha = 255;
         if (SmallJumpTime > SmallJumpFadeOutTime + SmallJumpStayTime) {
            float f6 = (float)(SmallJumpFadeInTime + SmallJumpStayTime + SmallJumpFadeOutTime) - f4;
            alpha = (int)(f6 * 255.0F / (float)SmallJumpFadeInTime);
         }
         
         if (SmallJumpTime <= SmallJumpFadeOutTime) {
            alpha = (int)(f4 * 255.0F / (float)SmallJumpFadeOutTime);
         }
         
         alpha = Mth.clamp(alpha, 0, 255);
         poseStack.pose().pushPose();
         poseStack.pose().scale(0.5F, 0.5F, 0.5F);
         if (alpha > 8) {
            int k1 = 16777215;
            if (SmallAnimateJumpMessageColor) {
               //k1 = Mth.hsvToRgb(f4 / 50.0F, 0.7F, 0.6F) & 16777215;
               Color color = EnderLordRenderer.rainbow(4000, 0.5F, 1.0F);
               k1 = RGBtoTen.OutputResult(color.getRed(), color.getGreen(), color.getBlue());
            }
            int k = alpha << 24 & -16777216;
            drawBackdrop(poseStack, minecraft.font, -10, l, 16777215 | k);
            poseStack.drawString(minecraft.font, SmallJumpString, i1 * 2, SmallJumpStringY * 2, k1 | k, false);
         }
         poseStack.pose().popPose();
      }
   }
   
   public static void UpdateSmallJumpString(Component string, int time, boolean animateColor, boolean canUpdate) {
      if (SmallJumpStringCanUpdate) {
         SmallJumpString = string.copy().withStyle(CustomBossBar.GENSHIN_IMPACT_FONT);
         SmallJumpFadeInTime = 10;
         SmallJumpFadeOutTime = 10;
         SmallJumpStayTime = time;
         SmallJumpTime = SmallJumpFadeInTime + SmallJumpStayTime + SmallJumpFadeOutTime;
         SmallAnimateJumpMessageColor = animateColor;
         SmallJumpStringY = SmallJumpStringHeights / 6;
         SmallJumpStringX = SmallJumpStringWidths / 2;
         SmallJumpStringCanUpdate = canUpdate;
      }
   }
   
   public static void renderAllJumpString(GuiGraphics poseStack, float partialTick, int width, int height) {
      renderBigJumpString(poseStack, partialTick, width, height);
      renderJumpString(poseStack, partialTick, width, height);
      renderSmallJumpString(poseStack, partialTick, width, height);
   }
   
   public static void updateAllJumpString() {
      if (!Minecraft.getInstance().isPaused()) {
         Minecraft.getInstance().getProfiler().push("BigJumpStringOverlayUpdate");
         if (BigJumpTime > 0) {
            BigJumpTime--;
            if (BigJumpTime <= 0) {
               BigJumpString = null;
               BigAnimateJumpMessageColor = false;
               BigJumpStringY = BigJumpStringHeights / 6 + 15;
               BigJumpStringCanUpdate = true;
            }
         }
         
         if (BigJumpTime > 0 && BigJumpString != null) {
            if (BigJumpTime > BigJumpFadeOutTime + BigJumpStayTime) {
               BigJumpStringY--;
            }
            
            if (BigJumpTime <= BigJumpFadeOutTime) {
               BigJumpStringY--;
            }
         }
         Minecraft.getInstance().getProfiler().pop();
         
         
         Minecraft.getInstance().getProfiler().push("JumpStringOverlayUpdate");
         if (JumpTime > 0) {
            JumpTime--;
            if (JumpTime <= 0) {
               JumpString = null;
               AnimateJumpMessageColor = false;
               JumpStringY = JumpStringHeights / 6 + 15;
               JumpStringCanUpdate = true;
            }
         }
         
         if (JumpTime > 0 && JumpString != null) {
            if (JumpTime > JumpFadeOutTime + JumpStayTime) {
               JumpStringY--;
            }
            
            if (JumpTime <= JumpFadeOutTime) {
               JumpStringY--;
            }
         }
         Minecraft.getInstance().getProfiler().pop();
         
         
         Minecraft.getInstance().getProfiler().push("SmallJumpStringOverlayUpdate");
         if (SmallJumpTime > 0) {
            SmallJumpTime--;
            if (SmallJumpTime <= 0) {
               SmallJumpString = null;
               SmallAnimateJumpMessageColor = false;
               SmallJumpStringY = SmallJumpStringHeights / 6;
               SmallJumpStringCanUpdate = true;
            }
         }
         Minecraft.getInstance().getProfiler().pop();
      }
   }
   
   public static void drawBackdrop(GuiGraphics p_282548_, Font p_93041_, int p_93042_, int p_93043_, int p_93044_) {
      int i = Minecraft.getInstance().options.getBackgroundColor(0.0F);
      if (i != 0) {
         int j = -p_93043_ / 2;
         p_282548_.fill(j - 2, p_93042_ - 2, j + p_93043_ + 2, p_93042_ + 9 + 2, FastColor.ARGB32.multiply(i, p_93044_));
      }
   }
}
