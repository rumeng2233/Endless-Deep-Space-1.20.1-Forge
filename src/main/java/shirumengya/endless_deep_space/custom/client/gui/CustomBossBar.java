package shirumengya.endless_deep_space.custom.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import shirumengya.endless_deep_space.EndlessDeepSpaceMod;
import shirumengya.endless_deep_space.custom.client.gui.screens.components.wheel.Color;
import shirumengya.endless_deep_space.custom.client.gui.screens.components.wheel.WheelConstants;
import shirumengya.endless_deep_space.custom.client.gui.screens.components.wheel.WheelRenderer;
import shirumengya.endless_deep_space.custom.client.renderer.entity.EnderLordRenderer;
import shirumengya.endless_deep_space.custom.util.java.color.RGBtoTen;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CustomBossBar {
   public static Map<Integer, CustomBossBar> customBossBars = new HashMap<>();
   public static final WheelRenderer wheel = new WheelRenderer();
   public static Map<UUID, Double> customBossBarsLastProgress = new HashMap<>();
   public static Map<UUID, Double> customBossBarsLastBufferTime = new HashMap<>();
   public static long renderTime;
   public static Style GENSHIN_IMPACT_FONT = Style.EMPTY.withFont(new ResourceLocation("endless_deep_space:genshin_impact_font"));
   
   static {
      //Normal
      customBossBars.put(-2, new CustomBossBar(
            new ResourceLocation(EndlessDeepSpaceMod.MODID, "textures/gui/endless_deep_space_common_bossbar.png"),
            null,
            5, 0, 10, 5, 256, 0,0, 0, 0, 256, 256, 19, 182, false, null));
      
      //Ender Lord
      customBossBars.put(0, new CustomBossBar(
            new ResourceLocation(EndlessDeepSpaceMod.MODID, "textures/gui/endless_deep_space_common_bossbar.png"),
            null,
            5, 0, 10, 5, 256, 0,0, 0, 0, 256, 256, -5, 182, true, ChatFormatting.AQUA));
      
      //Ender Lord - Shield
      customBossBars.put(1, new CustomBossBar(
            new ResourceLocation(EndlessDeepSpaceMod.MODID, "textures/gui/endless_deep_space_common_bossbar.png"),
            null,
            3, 15, 21, 3, 256, 4,5 + 5, 0, 0, 256, 256, 17 + 5, 174, true, ChatFormatting.AQUA));
      
      //Ender Lord - Shield Progress Two
      customBossBars.put(2, new CustomBossBar(
            new ResourceLocation(EndlessDeepSpaceMod.MODID, "textures/gui/endless_deep_space_common_bossbar.png"),
            null,
            3, 15, 21, 12, 256, 4,5 + 5, 0, 0, 256, 256, 17 + 5, 174, true, ChatFormatting.AQUA));
      
      //Ender Lord - Attack Times
      customBossBars.put(3, new CustomBossBar(
            new ResourceLocation(EndlessDeepSpaceMod.MODID, "textures/gui/endless_deep_space_common_bossbar.png"),
            null,
            0, 0, 0, 0, 0, 0,5 + 5, 0, 0, 0, 0, 0, 0, true, ChatFormatting.AQUA));
      
      //Warden
      customBossBars.put(4, new CustomBossBar(
            new ResourceLocation(EndlessDeepSpaceMod.MODID, "textures/gui/endless_deep_space_common_bossbar.png"),
            null,
            5, 0, 10, 5, 256, 0,0, 0, 0, 256, 256, 19, 182, false, ChatFormatting.DARK_GRAY));
      
      //Normal Mini Boss
      customBossBars.put(5, new CustomBossBar(
            new ResourceLocation(EndlessDeepSpaceMod.MODID, "textures/gui/endless_deep_space_common_bossbar.png"),
            null,
            5, 45, 55, 5, 256, 10,0, 0, 0, 256, 256, 19, 162, false, null));
      
      //Mutation Ravager
      customBossBars.put(6, new CustomBossBar(
            new ResourceLocation(EndlessDeepSpaceMod.MODID, "textures/gui/endless_deep_space_common_bossbar.png"),
            null,
            5, 45, 55, 5, 256, 10,0, 0, 0, 256, 256, -5, 162, false, ChatFormatting.LIGHT_PURPLE));
      
      //Mutation Ravager - Shield
      customBossBars.put(7, new CustomBossBar(
            new ResourceLocation(EndlessDeepSpaceMod.MODID, "textures/gui/endless_deep_space_common_bossbar.png"),
            null,
            3, 60, 66, 3, 256, 14,5 + 5, 0, 0, 256, 256, 17 + 5, 154, false, ChatFormatting.LIGHT_PURPLE));
      
      //Mutation Ravager Has Passenger - Mutation Ravager
      customBossBars.put(8, new CustomBossBar(
            new ResourceLocation(EndlessDeepSpaceMod.MODID, "textures/gui/endless_deep_space_common_bossbar.png"),
            null,
            5, 90, 100, 5, 256, 50 - 45,0, 0, 0, 256, 256, -5, 82, false, ChatFormatting.LIGHT_PURPLE));
      
      //Mutation Ravager Has Passenger - Mutation Ravager - Shield
      customBossBars.put(9, new CustomBossBar(
            new ResourceLocation(EndlessDeepSpaceMod.MODID, "textures/gui/endless_deep_space_common_bossbar.png"),
            null,
            3, 105, 111, 3, 256, 54 - 45,5 + 5, 0, 0, 256, 256, 0, 74, false, ChatFormatting.LIGHT_PURPLE));
      
      //Mutation Ravager Has Passenger - Passenger
      customBossBars.put(10, new CustomBossBar(
            new ResourceLocation(EndlessDeepSpaceMod.MODID, "textures/gui/endless_deep_space_common_bossbar.png"),
            null,
            5, 90, 100, 5, 256, 50 + 45,5, 0, 0, 256, 256, 17 + 5, 82, false, ChatFormatting.LIGHT_PURPLE));
      
      //Blaznana Shulker Trick
      customBossBars.put(11, new CustomBossBar(
            new ResourceLocation(EndlessDeepSpaceMod.MODID, "textures/gui/endless_deep_space_common_bossbar.png"),
            null,
            5, 45, 55, 5, 256, 10,0, 0, 0, 256, 256, -5, 162, false, ChatFormatting.RED));
      
      //Blaznana Shulker Trick - Attack Times
      customBossBars.put(12, new CustomBossBar(
            new ResourceLocation(EndlessDeepSpaceMod.MODID, "textures/gui/endless_deep_space_common_bossbar.png"),
            null,
            0, 0, 0, 0, 0, 0,5 + 5, 0, 0, 0, 0, 19, 0, false, ChatFormatting.RED));
      
      //Ocean Defender
      customBossBars.put(13, new CustomBossBar(
            new ResourceLocation(EndlessDeepSpaceMod.MODID, "textures/gui/endless_deep_space_common_bossbar.png"),
            null,
            5, 0, 10, 5, 256, 0,0, 0, 0, 256, 256, -5, 182, false, ChatFormatting.BLUE));
      
      //Coral Defenders - Ocean Defender
      customBossBars.put(14, new CustomBossBar(
            new ResourceLocation(EndlessDeepSpaceMod.MODID, "textures/gui/endless_deep_space_common_bossbar.png"),
            null,
            5, 90, 100, 5, 256, 50 - 45,0, 0, 0, 256, 256, -5, 82, false, ChatFormatting.BLUE));
      
      //Coral Defenders - Other Ocean Defender
      customBossBars.put(15, new CustomBossBar(
            new ResourceLocation(EndlessDeepSpaceMod.MODID, "textures/gui/endless_deep_space_common_bossbar.png"),
            null,
            5, 90, 100, 5, 256, 50 + 45,5, 0, 0, 256, 256, 0, 82, false, ChatFormatting.BLUE));
      
      //Coral Defenders - Charging Timer
      customBossBars.put(16, new CustomBossBar(
            new ResourceLocation(EndlessDeepSpaceMod.MODID, "textures/gui/endless_deep_space_common_bossbar.png"),
            null,
            0, 0, 0, 0, 0, 0,5 + 5, 0, 0, 0, 0, 19, 0, true, ChatFormatting.BLUE));
   }
   
   private final ResourceLocation baseTexture;
   private final ResourceLocation overlayTexture;
   private final boolean hasOverlay;
   
   private final int baseHeight;
   private final int baseOffsetHight;
   private final int baseBufferHight;
   private final int offsetHight;
   private final int baseTextureHeight;
   private final int baseOffsetX;
   private final int baseOffsetY;
   private final int overlayOffsetX;
   private final int overlayOffsetY;
   private final int overlayWidth;
   private final int overlayHeight;
   
   private final int verticalIncrement;
   
   private final int getProgress;
   
   private final boolean dynamicColor;
   
   @Nullable
   private final ChatFormatting textColor;
   
   public CustomBossBar(ResourceLocation baseTexture, ResourceLocation overlayTexture, int baseHeight, int baseOffsetHight, int baseBufferHight, int offsetHight, int baseTextureHeight, int baseOffsetX, int baseOffsetY, int overlayOffsetX, int overlayOffsetY, int overlayWidth, int overlayHeight, int verticalIncrement, int getProgress, boolean dynamicColor, ChatFormatting textColor) {
      this.baseTexture = baseTexture;
      this.overlayTexture = overlayTexture;
      this.hasOverlay = overlayTexture != null;
      this.baseHeight = baseHeight;
      this.baseOffsetHight = baseOffsetHight;
      this.baseBufferHight = baseBufferHight;
      this.offsetHight = offsetHight;
      this.baseTextureHeight = baseTextureHeight;
      this.baseOffsetX = baseOffsetX;
      this.baseOffsetY = baseOffsetY;
      this.overlayOffsetX = overlayOffsetX;
      this.overlayOffsetY = overlayOffsetY;
      this.overlayWidth = overlayWidth;
      this.overlayHeight = overlayHeight;
      this.verticalIncrement = verticalIncrement;
      this.getProgress = getProgress;
      this.dynamicColor = dynamicColor;
      this.textColor = textColor;
   }
   
   public ResourceLocation getBaseTexture() {
      return baseTexture;
   }
   
   public ResourceLocation getOverlayTexture() {
      return overlayTexture;
   }
   
   public boolean hasOverlay() {
      return hasOverlay;
   }
   
   public int getBaseHeight() {
      return baseHeight;
   }
   
   public int getBaseOffsetHeight() {
      return baseOffsetHight;
   }
   
   public int getBaseBufferHeight() {
      return baseBufferHight;
   }
   
   public int getOffsetHeight() {
      return offsetHight;
   }
   
   public int getBaseTextureHeight() {
      return baseTextureHeight;
   }
   
   public int getBaseOffsetX() {
      return baseOffsetX;
   }
   
   public int getBaseOffsetY() {
      return baseOffsetY;
   }
   
   public int getOverlayOffsetX() {
      return overlayOffsetX;
   }
   
   public int getOverlayOffsetY() {
      return overlayOffsetY;
   }
   
   public int getOverlayWidth() {
      return overlayWidth;
   }
   
   public int getOverlayHeight() {
      return overlayHeight;
   }
   
   public int getProgress() {
      return getProgress;
   }
   
   public int getVerticalIncrement() {
      return verticalIncrement;
   }
   
   public boolean getDynamicColor() {
      return dynamicColor;
   }
   
   @Nullable
   public ChatFormatting getTextColor() {
      return textColor;
   }
   
   public void renderBossBar(CustomizeGuiOverlayEvent.BossEventProgress event, Component description, int renderType) {
      GuiGraphics guiGraphics = event.getGuiGraphics();
      int y = event.getY();
      int i = Minecraft.getInstance().getWindow().getGuiScaledWidth();
      int j = y - 9;
      Component component = event.getBossEvent().getName().copy();
      component = component.copy().withStyle(GENSHIN_IMPACT_FONT);
      
      Minecraft.getInstance().getProfiler().push("EndlessDeepSpaceCustomBossBarBase");
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.setShaderTexture(0, getBaseTexture());
      drawBar(guiGraphics, event.getX() + getBaseOffsetX(), y + getBaseOffsetY() + ((!description.equals(Component.empty()) && !description.toString().isEmpty()) ? 5 : 0), event.getBossEvent());
      if (!getDynamicColor()) {
         if (getTextColor() != null) {
            component = component.copy().withStyle(getTextColor());
         }
      }
      Minecraft.getInstance().getProfiler().pop();
      
      int l = Minecraft.getInstance().font.width(component);
      int i1 = i / 2 - l / 2;
      java.awt.Color color = EnderLordRenderer.rainbow(4000.0F, 0.5F, 1.0F);
      Color dangerous = WheelConstants.DEPLETED_1.blend(WheelConstants.DEPLETED_2, WheelConstants.cycle(System.currentTimeMillis(), WheelConstants.DEPLETED_BLINK));
      guiGraphics.drawString(Minecraft.getInstance().font, component, i1, j, getDynamicColor() ? RGBtoTen.OutputResult(color.getRed(), color.getGreen(), color.getBlue()) : 16777215, false);
      
      if (!description.equals(Component.empty()) && !description.toString().isEmpty()) {
         description = description.copy().withStyle(GENSHIN_IMPACT_FONT);
         int l1 = Minecraft.getInstance().font.width(description);
         int i2 = i / 2 - l1 / 4;
         guiGraphics.pose().pushPose();
         guiGraphics.pose().scale(0.5F, 0.5F, 0.5F);
         guiGraphics.drawString(Minecraft.getInstance().font, description, i2 * 2, event.getY() * 2, 11184810, false);
         guiGraphics.pose().popPose();
      }
      
      if (hasOverlay()) {
         Minecraft.getInstance().getProfiler().push("EndlessDeepSpaceCustomBossBarOverlay");
         RenderSystem.setShaderTexture(0, getOverlayTexture());
         event.getGuiGraphics().blit(getOverlayTexture(), event.getX() + getBaseOffsetX() + getOverlayOffsetX(), y + getOverlayOffsetY() + getBaseOffsetY(), 0, 0, getOverlayWidth(), getOverlayHeight(), getOverlayWidth(), getOverlayHeight());
         Minecraft.getInstance().getProfiler().pop();
      }
      
      Minecraft.getInstance().getProfiler().push("EndlessDeepSpaceCustomBossBarWhell");
      switch (renderType) {
         case 3:
            wheel.renderWheel(guiGraphics, event.getX() + 188, y + getBaseOffsetY() - 1, 0, 5, false, WheelRenderer.WheelLevel.FULL, 0, 1, Color.of(50, 50, 50));
            wheel.renderWheel(guiGraphics, event.getX() + 188, y + getBaseOffsetY() - 1, 0, 6, false, WheelRenderer.WheelLevel.SECOND, 0, 1, Color.of(85, 12, 32));
            wheel.renderWheel(guiGraphics, event.getX() + 188, y + getBaseOffsetY() - 1, 0, 6, false, WheelRenderer.WheelLevel.SECOND, 1 - event.getBossEvent().getProgress(), 1, Color.of(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()));
            break;
         
         case 12:
            wheel.renderWheel(guiGraphics, event.getX() + 178, y + getBaseOffsetY() - 2.5, 0, 5, false, WheelRenderer.WheelLevel.FULL, 0, 1, Color.of(50, 50, 50));
            wheel.renderWheel(guiGraphics, event.getX() + 178, y + getBaseOffsetY() - 2.5, 0, 6, false, WheelRenderer.WheelLevel.SECOND, 0, 1, Color.of(85, 12, 32));
            wheel.renderWheel(guiGraphics, event.getX() + 178, y + getBaseOffsetY() - 2.5, 0, 6, false, WheelRenderer.WheelLevel.SECOND, 1 - event.getBossEvent().getProgress(), 1, Color.of(252, 100, 100));
            break;
         
         case 16:
            wheel.renderWheel(guiGraphics, event.getX() + 188, y + getBaseOffsetY() - 2.5, 0, 5, false, WheelRenderer.WheelLevel.FULL, 0, 1, Color.of(50, 50, 50));
            wheel.renderWheel(guiGraphics, event.getX() + 188, y + getBaseOffsetY() - 2.5, 0, 6, false, WheelRenderer.WheelLevel.SECOND, 0, 1, Color.of(85, 12, 32));
            wheel.renderWheel(guiGraphics, event.getX() + 188, y + getBaseOffsetY() - 2.5, 0, 6, false, WheelRenderer.WheelLevel.SECOND, 1 - event.getBossEvent().getProgress(), 1, dangerous);
            break;
         
         default:
            break;
      }
      Minecraft.getInstance().getProfiler().pop();
      
      event.setIncrement(getVerticalIncrement() + ((!description.equals(Component.empty()) && !description.toString().isEmpty()) ? 5 : 0));
   }
   
   private void drawBar(GuiGraphics guiGraphics, int x, int y, LerpingBossEvent event) {
      guiGraphics.blit(getBaseTexture(), x, y, 0, getBaseOffsetHeight(), getProgress(), getBaseHeight(), 256, getBaseTextureHeight());
      
      double lastProgress = customBossBarsLastProgress.get(event.getId()) == null ? 0 : customBossBarsLastProgress.get(event.getId());
      double lastBufferTime = customBossBarsLastBufferTime.get(event.getId()) == null ? 0 : customBossBarsLastBufferTime.get(event.getId());
      if (lastProgress > event.getProgress()) {
         if (renderTime % 2 == 0 && !Minecraft.getInstance().isPaused()) {
            lastBufferTime -= 60.0D / Minecraft.getInstance().getFps();
            if (lastBufferTime <= 0) {
               lastProgress -= 60.0D / Minecraft.getInstance().getFps() / 300.0D;
            }
         }
      } else {
         lastProgress = event.getProgress();
         lastBufferTime = 0;
      }
      customBossBarsLastProgress.put(event.getId(), lastProgress);
      customBossBarsLastBufferTime.put(event.getId(), lastBufferTime);
      guiGraphics.blit(getBaseTexture(), x, y, 0, getBaseBufferHeight(), (int) (lastProgress * (double) (getProgress() + 1)), getBaseHeight(), 256, getBaseTextureHeight());
      
      int i = (int)(event.getProgress() * (getProgress() + 1));
      if (i > 0) {
         guiGraphics.blit(getBaseTexture(), x, y, 0, getBaseOffsetHeight() + getOffsetHeight(), i, getBaseHeight(), 256, getBaseTextureHeight());
      }
   }
}