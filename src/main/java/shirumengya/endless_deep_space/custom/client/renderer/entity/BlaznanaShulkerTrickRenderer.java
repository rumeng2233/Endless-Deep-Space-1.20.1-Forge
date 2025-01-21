package shirumengya.endless_deep_space.custom.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import shirumengya.endless_deep_space.custom.client.model.BlaznanaShulkerTrickModel;
import shirumengya.endless_deep_space.custom.client.renderer.entity.layers.BlaznanaShulkerTrickHeadLayer;
import shirumengya.endless_deep_space.custom.entity.miniboss.BlaznanaShulkerTrick;
import shirumengya.endless_deep_space.custom.init.ModModels;

@OnlyIn(Dist.CLIENT)
public class BlaznanaShulkerTrickRenderer extends MobRenderer<BlaznanaShulkerTrick, BlaznanaShulkerTrickModel<BlaznanaShulkerTrick>> {
   private static final float HALF_SQRT_3 = (float)(Math.sqrt(3.0D) / 2.0D);

   public BlaznanaShulkerTrickRenderer(EntityRendererProvider.Context p_174370_) {
      super(p_174370_, new BlaznanaShulkerTrickModel<>(p_174370_.bakeLayer(ModModels.BLAZNANA_SHULKER_TRICK)), 0.0F);
      this.addLayer(new BlaznanaShulkerTrickHeadLayer(this));
   }

   public Vec3 getRenderOffset(BlaznanaShulkerTrick p_115904_, float p_115905_) {
      return p_115904_.getRenderPosition(p_115905_).orElse(super.getRenderOffset(p_115904_, p_115905_));
   }

   @Override
   public void render(BlaznanaShulkerTrick p_115455_, float p_115456_, float p_115457_, PoseStack p_115458_, MultiBufferSource p_115459_, int p_115460_) {
      super.render(p_115455_, p_115456_, p_115457_, p_115458_, p_115459_, p_115460_);

      if (p_115455_.blaznanaShulkerTrickDeathTime > 0) {
         float f5 = ((float)p_115455_.blaznanaShulkerTrickDeathTime + p_115457_) / 80.0F;
         float f7 = Math.min(f5 > 0.8F ? (f5 - 0.8F) / 0.2F : 0.0F, 1.0F);
         RandomSource randomsource = RandomSource.create(432L);
         VertexConsumer vertexconsumer2 = p_115459_.getBuffer(RenderType.lightning());
         p_115458_.pushPose();
         p_115458_.translate(0.0F, 0.5F, 0.0F);
         p_115458_.scale(0.5F, 0.5F, 0.5F);

         for(int i = 0; (float)i < (f5 + f5 * f5) / 2.0F * 60.0F; ++i) {
            p_115458_.mulPose(Axis.XP.rotationDegrees(randomsource.nextFloat() * 360.0F));
            p_115458_.mulPose(Axis.YP.rotationDegrees(randomsource.nextFloat() * 360.0F));
            p_115458_.mulPose(Axis.ZP.rotationDegrees(randomsource.nextFloat() * 360.0F));
            p_115458_.mulPose(Axis.XP.rotationDegrees(randomsource.nextFloat() * 360.0F));
            p_115458_.mulPose(Axis.YP.rotationDegrees(randomsource.nextFloat() * 360.0F));
            p_115458_.mulPose(Axis.ZP.rotationDegrees(randomsource.nextFloat() * 360.0F + f5 * 90.0F));
            float f3 = randomsource.nextFloat() * 20.0F + 5.0F + f7 * 10.0F;
            float f4 = randomsource.nextFloat() * 2.0F + 1.0F + f7 * 2.0F;
            Matrix4f matrix4f = p_115458_.last().pose();
            int j = (int)(255.0F * (1.0F - f7));
            vertex01(vertexconsumer2, matrix4f, j, 252, 100, 100);
            vertex2(vertexconsumer2, matrix4f, f3, f4, 252, 100, 100);
            vertex3(vertexconsumer2, matrix4f, f3, f4, 252, 100, 100);
            vertex01(vertexconsumer2, matrix4f, j, 252, 100, 100);
            vertex3(vertexconsumer2, matrix4f, f3, f4, 252, 100, 100);
            vertex4(vertexconsumer2, matrix4f, f3, f4, 252, 100, 100);
            vertex01(vertexconsumer2, matrix4f, j, 252, 100, 100);
            vertex4(vertexconsumer2, matrix4f, f3, f4, 252, 100, 100);
            vertex2(vertexconsumer2, matrix4f, f3, f4, 252, 100, 100);
         }

         p_115458_.popPose();
      }
   }

   private static void vertex01(VertexConsumer p_254498_, Matrix4f p_253891_, int p_254278_, int red, int green, int blue) {
      p_254498_.vertex(p_253891_, 0.0F, 0.0F, 0.0F).color(red, green, blue, p_254278_).endVertex();
   }

   private static void vertex2(VertexConsumer p_253956_, Matrix4f p_254053_, float p_253704_, float p_253701_, int red, int green, int blue) {
      p_253956_.vertex(p_254053_, -HALF_SQRT_3 * p_253701_, p_253704_, -0.5F * p_253701_).color(red, green, blue, 0).endVertex();
   }

   private static void vertex3(VertexConsumer p_253850_, Matrix4f p_254379_, float p_253729_, float p_254030_, int red, int green, int blue) {
      p_253850_.vertex(p_254379_, HALF_SQRT_3 * p_254030_, p_253729_, -0.5F * p_254030_).color(red, green, blue, 0).endVertex();
   }

   private static void vertex4(VertexConsumer p_254184_, Matrix4f p_254082_, float p_253649_, float p_253694_, int red, int green, int blue) {
      p_254184_.vertex(p_254082_, 0.0F, p_253649_, 1.0F * p_253694_).color(red, green, blue, 0).endVertex();
   }

   public ResourceLocation getTextureLocation(BlaznanaShulkerTrick p_115902_) {
      return new ResourceLocation("endless_deep_space:textures/entities/blaznana_shulker_trick/blaznana_shulker_trick.png");
   }

   protected void setupRotations(BlaznanaShulkerTrick p_115907_, PoseStack p_115908_, float p_115909_, float p_115910_, float p_115911_) {
      super.setupRotations(p_115907_, p_115908_, p_115909_, p_115910_ + 180.0F, p_115911_);
      p_115908_.translate(0.0D, 0.5D, 0.0D);
      p_115908_.mulPose(p_115907_.getAttachFace().getOpposite().getRotation());
      p_115908_.translate(0.0D, -0.5D, 0.0D);
   }
}