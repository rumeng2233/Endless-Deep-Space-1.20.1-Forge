package shirumengya.endless_deep_space.custom.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.ShulkerBulletModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import shirumengya.endless_deep_space.custom.entity.projectile.BlaznanaShulkerTrickBullet;

@OnlyIn(Dist.CLIENT)
public class BlaznanaShulkerTrickBulletRenderer extends EntityRenderer<BlaznanaShulkerTrickBullet> {
   private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("endless_deep_space:textures/entities/blaznana_shulker_trick/spark.png");
   private static final RenderType RENDER_TYPE = RenderType.entityTranslucent(TEXTURE_LOCATION);
   private final ShulkerBulletModel<BlaznanaShulkerTrickBullet> model;

   public BlaznanaShulkerTrickBulletRenderer(EntityRendererProvider.Context p_174368_) {
      super(p_174368_);
      this.model = new ShulkerBulletModel<>(p_174368_.bakeLayer(ModelLayers.SHULKER_BULLET));
   }

   protected int getBlockLightLevel(BlaznanaShulkerTrickBullet p_115869_, BlockPos p_115870_) {
      return 15;
   }

   public void render(BlaznanaShulkerTrickBullet p_115862_, float p_115863_, float p_115864_, PoseStack p_115865_, MultiBufferSource p_115866_, int p_115867_) {
      p_115865_.pushPose();
      float f = Mth.rotLerp(p_115864_, p_115862_.yRotO, p_115862_.getYRot());
      float f1 = Mth.lerp(p_115864_, p_115862_.xRotO, p_115862_.getXRot());
      float f2 = (float)p_115862_.tickCount + p_115864_;
      p_115865_.translate(0.0F, 0.15F, 0.0F);
      p_115865_.mulPose(Axis.YP.rotationDegrees(Mth.sin(f2 * 0.1F) * 180.0F));
      p_115865_.mulPose(Axis.XP.rotationDegrees(Mth.cos(f2 * 0.1F) * 180.0F));
      p_115865_.mulPose(Axis.ZP.rotationDegrees(Mth.sin(f2 * 0.15F) * 360.0F));
      p_115865_.scale(-0.5F, -0.5F, 0.5F);
      this.model.setupAnim(p_115862_, 0.0F, 0.0F, 0.0F, f, f1);
      VertexConsumer vertexconsumer = p_115866_.getBuffer(this.model.renderType(TEXTURE_LOCATION));
      this.model.renderToBuffer(p_115865_, vertexconsumer, p_115867_, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      p_115865_.scale(1.5F, 1.5F, 1.5F);
      VertexConsumer vertexconsumer1 = p_115866_.getBuffer(RENDER_TYPE);
      this.model.renderToBuffer(p_115865_, vertexconsumer1, p_115867_, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 0.15F);
      p_115865_.popPose();
      super.render(p_115862_, p_115863_, p_115864_, p_115865_, p_115866_, p_115867_);
   }

   public ResourceLocation getTextureLocation(BlaznanaShulkerTrickBullet p_115860_) {
      return TEXTURE_LOCATION;
   }
}