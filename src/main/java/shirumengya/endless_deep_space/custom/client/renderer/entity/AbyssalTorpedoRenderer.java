package shirumengya.endless_deep_space.custom.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import shirumengya.endless_deep_space.custom.client.renderer.ModRenderType;
import shirumengya.endless_deep_space.custom.entity.projectile.AbyssalTorpedo;

public class AbyssalTorpedoRenderer extends EntityRenderer<AbyssalTorpedo> {
   public AbyssalTorpedoRenderer(EntityRendererProvider.Context p_174008_) {
      super(p_174008_);
   }
   
   @Override
   protected int getBlockLightLevel(AbyssalTorpedo p_114496_, BlockPos p_114497_) {
      return 15;
   }
   
   @Override
   public void render(AbyssalTorpedo p_114485_, float p_114486_, float p_114487_, PoseStack p_114488_, MultiBufferSource p_114489_, int p_114490_) {
      p_114488_.pushPose();
      EnderLordRenderer.renderCube(0.5F, -0.5F, 0.5F, p_114488_.last().pose(), p_114489_.getBuffer(ModRenderType.EndGateway(EnderLordRenderer.END_SKY_LOCATION, EnderLordRenderer.END_PORTAL_LOCATION, false)));
      p_114488_.popPose();
      super.render(p_114485_, p_114486_, p_114487_, p_114488_, p_114489_, p_114490_);
   }
   
   @Override
   public ResourceLocation getTextureLocation(AbyssalTorpedo p_114482_) {
      return TextureAtlas.LOCATION_BLOCKS;
   }
}
