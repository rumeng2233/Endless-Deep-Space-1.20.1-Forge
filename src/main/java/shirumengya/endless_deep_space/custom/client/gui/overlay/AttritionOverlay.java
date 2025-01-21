package shirumengya.endless_deep_space.custom.client.gui.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.joml.Matrix4f;
import shirumengya.endless_deep_space.custom.client.gui.screens.components.wheel.Color;
import shirumengya.endless_deep_space.custom.client.renderer.entity.OceanDefenderRenderer;
import shirumengya.endless_deep_space.custom.entity.boss.oceandefenders.OceanDefender;

public class AttritionOverlay {
   public static final Material ICE = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("block/ice"));
   
   public static void renderBlock(PoseStack p_110730_, TextureAtlasSprite textureAtlasSprite, Color color) {
      BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
      RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
      RenderSystem.depthFunc(519);
      RenderSystem.depthMask(false);
      RenderSystem.enableBlend();
      RenderSystem.setShaderTexture(0, textureAtlasSprite.atlasLocation());
      RenderSystem.setShaderColor(color.red, color.green, color.blue, color.alpha);
      float f = textureAtlasSprite.getU0();
      float f1 = textureAtlasSprite.getU1();
      float f2 = (f + f1) / 2.0F;
      float f3 = textureAtlasSprite.getV0();
      float f4 = textureAtlasSprite.getV1();
      float f5 = (f3 + f4) / 2.0F;
      float f6 = textureAtlasSprite.uvShrinkRatio();
      float f7 = Mth.lerp(f6, f, f2);
      float f8 = Mth.lerp(f6, f1, f2);
      float f9 = Mth.lerp(f6, f3, f5);
      float f10 = Mth.lerp(f6, f4, f5);
      float f11 = 1.0F;
      
      for(int i = 0; i < 2; ++i) {
         p_110730_.pushPose();
         float f12 = -0.5F;
         float f13 = 0.5F;
         float f14 = -0.5F;
         float f15 = 0.5F;
         float f16 = -0.5F;
         p_110730_.translate((float)(-(i * 2 - 1)) * 0.24F, -0.3F, 0.0F);
         p_110730_.mulPose(Axis.YP.rotationDegrees((float)(i * 2 - 1) * 10.0F));
         Matrix4f matrix4f = p_110730_.last().pose();
         bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
         bufferbuilder.vertex(matrix4f, -0.5F, -0.5F, -0.5F).color(1.0F, 1.0F, 1.0F, 0.9F).uv(f8, f10).endVertex();
         bufferbuilder.vertex(matrix4f, 0.5F, -0.5F, -0.5F).color(1.0F, 1.0F, 1.0F, 0.9F).uv(f7, f10).endVertex();
         bufferbuilder.vertex(matrix4f, 0.5F, 0.5F, -0.5F).color(1.0F, 1.0F, 1.0F, 0.9F).uv(f7, f9).endVertex();
         bufferbuilder.vertex(matrix4f, -0.5F, 0.5F, -0.5F).color(1.0F, 1.0F, 1.0F, 0.9F).uv(f8, f9).endVertex();
         BufferUploader.drawWithShader(bufferbuilder.end());
         p_110730_.popPose();
      }
      
      RenderSystem.disableBlend();
      RenderSystem.depthMask(true);
      RenderSystem.depthFunc(515);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }
   
   public static void renderAttritionOverlay(GuiGraphics p_283375_, TextureAtlasSprite textureAtlasSprite, Color color, float p_283296_) {
      if (p_283296_ < 1.0F) {
         p_283296_ *= p_283296_;
         p_283296_ *= p_283296_;
         p_283296_ = p_283296_ * 0.8F + 0.2F;
      }
      
      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      p_283375_.setColor(color.red, color.green, color.blue, p_283296_);
      TextureAtlasSprite textureatlassprite = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.NETHER_PORTAL.defaultBlockState());
      textureatlassprite = textureAtlasSprite;
      p_283375_.blit(0, 0, -90, p_283375_.guiWidth(), p_283375_.guiHeight(), textureatlassprite);
      RenderSystem.depthMask(true);
      RenderSystem.enableDepthTest();
      p_283375_.setColor(1.0F, 1.0F, 1.0F, 1.0F);
   }
   
   public static void renderBlockModel(Entity p_114634_, BlockState blockstate, PoseStack stack, MultiBufferSource buffer, int light) {
      float boundingBoxWidth = p_114634_.getBbWidth();
      float boundingBoxHeight = p_114634_.getBbHeight();
      if (p_114634_ instanceof OceanDefender defender) {
         boundingBoxWidth = defender.size.width;
         boundingBoxHeight = defender.size.height;
      }
      float size = Math.max(boundingBoxWidth, boundingBoxHeight);
      stack.pushPose();
      stack.translate(-size / 2.0F, 0.0F, -size / 2.0F);
      stack.scale(size, size, size);
      Minecraft.getInstance().getBlockRenderer().renderSingleBlock(blockstate, stack, buffer, light, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.translucentMovingBlock());
      stack.popPose();
   }
}
