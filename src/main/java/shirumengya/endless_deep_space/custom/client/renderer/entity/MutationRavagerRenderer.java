package shirumengya.endless_deep_space.custom.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import shirumengya.endless_deep_space.custom.client.model.MutationRavagerModel;
import shirumengya.endless_deep_space.custom.client.renderer.ModRenderType;
import shirumengya.endless_deep_space.custom.entity.miniboss.MutationRavager;
import shirumengya.endless_deep_space.custom.init.ModItems;
import shirumengya.endless_deep_space.custom.init.ModModels;

import static net.minecraft.client.renderer.entity.LivingEntityRenderer.getOverlayCoords;
import static net.minecraft.client.renderer.entity.LivingEntityRenderer.isEntityUpsideDown;

@OnlyIn(Dist.CLIENT)
public class MutationRavagerRenderer extends EntityRenderer<MutationRavager> {
   private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/illager/ravager.png");
   private static final ResourceLocation MUTATION_RAVAGER_BEAM_LOCATION = new ResourceLocation("textures/entity/guardian_beam.png");
   private static final RenderType BEAM_RENDER_TYPE = RenderType.entityCutoutNoCull(MUTATION_RAVAGER_BEAM_LOCATION);
   private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(TEXTURE_LOCATION);
   private final MutationRavagerModel model;

   public MutationRavagerRenderer(EntityRendererProvider.Context p_174362_) {
      super(p_174362_);
      this.model = new MutationRavagerModel(p_174362_.bakeLayer(ModModels.MUTATION_RAVAGER));
      this.shadowRadius = 1.1F;
   }

   public ResourceLocation getTextureLocation(MutationRavager p_115811_) {
      return TEXTURE_LOCATION;
   }

   @Override
   public void render(MutationRavager p_115308_, float p_115309_, float p_115310_, PoseStack p_115311_, MultiBufferSource p_115312_, int p_115313_) {
      ModRenderType.setTargetStack(new ItemStack(ModItems.MUTATION_RAVAGER_SPAWN_EGG.get()));
      p_115311_.pushPose();
      this.model.attackTime = p_115308_.getAttackAnim(p_115310_);

      boolean shouldSit = p_115308_.isPassenger() && (p_115308_.getVehicle() != null && p_115308_.getVehicle().shouldRiderSit());
      this.model.riding = shouldSit;
      this.model.young = p_115308_.isBaby();
      float f = Mth.rotLerp(p_115310_, p_115308_.yBodyRotO, p_115308_.yBodyRot);
      float f1 = Mth.rotLerp(p_115310_, p_115308_.yHeadRotO, p_115308_.yHeadRot);
      float f2 = f1 - f;
      if (shouldSit && p_115308_.getVehicle() instanceof LivingEntity) {
         LivingEntity livingentity = (LivingEntity)p_115308_.getVehicle();
         f = Mth.rotLerp(p_115310_, livingentity.yBodyRotO, livingentity.yBodyRot);
         f2 = f1 - f;
         float f3 = Mth.wrapDegrees(f2);
         if (f3 < -85.0F) {
            f3 = -85.0F;
         }

         if (f3 >= 85.0F) {
            f3 = 85.0F;
         }

         f = f1 - f3;
         if (f3 * f3 > 2500.0F) {
            f += f3 * 0.2F;
         }

         f2 = f1 - f;
      }

      float f6 = Mth.lerp(p_115310_, p_115308_.xRotO, p_115308_.getXRot());
      if (isEntityUpsideDown(p_115308_)) {
         f6 *= -1.0F;
         f2 *= -1.0F;
      }

      if (p_115308_.hasPose(Pose.SLEEPING)) {
         Direction direction = p_115308_.getBedOrientation();
         if (direction != null) {
            float f4 = p_115308_.getEyeHeight(Pose.STANDING) - 0.1F;
            p_115311_.translate((float)(-direction.getStepX()) * f4, 0.0F, (float)(-direction.getStepZ()) * f4);
         }
      }

      float f7 = p_115308_.tickCount + p_115310_;
      this.setupRotations(p_115308_, p_115311_, f7, f, p_115310_);
      p_115311_.scale(-1.0F, -1.0F, 1.0F);
      p_115311_.translate(0.0F, -1.501F, 0.0F);
      float f8 = 0.0F;
      float f5 = 0.0F;
      if (!shouldSit && p_115308_.isAlive()) {
         f8 = p_115308_.walkAnimation.speed(p_115310_);
         f5 = p_115308_.walkAnimation.position(p_115310_);
         if (p_115308_.isBaby()) {
            f5 *= 3.0F;
         }

         if (f8 > 1.0F) {
            f8 = 1.0F;
         }
      }

      this.model.prepareMobModel(p_115308_, f5, f8, p_115310_);
      this.model.setupAnim(p_115308_, f5, f8, f7, f2, f6);
      VertexConsumer vertexconsumer = ItemRenderer.getFoilBufferDirect(p_115312_, RENDER_TYPE, false, p_115308_.hasShield());
      int i = getOverlayCoords(p_115308_, 0.0F);
      this.model.renderToBuffer(p_115311_, vertexconsumer, p_115313_, i, 1.0F, 1.0F, 1.0F, 1.0F);

      p_115311_.popPose();

      super.render(p_115308_, p_115309_, p_115310_, p_115311_, p_115312_, p_115313_);

      LivingEntity livingentity = p_115308_.getActiveAttackTarget();
      if (livingentity != null) {
         this.renderAttackBeam(p_115308_, livingentity, p_115310_, p_115311_, p_115312_);
      }
   }

   protected void setupRotations(MutationRavager p_115317_, PoseStack p_115318_, float p_115319_, float p_115320_, float p_115321_) {
      if (p_115317_.isFullyFrozen()) {
         p_115320_ += (float)(Math.cos((double)p_115317_.tickCount * 3.25D) * Math.PI * (double)0.4F);
      }

      if (!p_115317_.hasPose(Pose.SLEEPING)) {
         p_115318_.mulPose(Axis.YP.rotationDegrees(180.0F - p_115320_));
      }

      if (p_115317_.deathTime > 0) {
         float f = ((float)p_115317_.deathTime + p_115321_ - 1.0F) / 20.0F * 1.6F;
         f = Mth.sqrt(f);
         if (f > 1.0F) {
            f = 1.0F;
         }

         p_115318_.mulPose(Axis.ZP.rotationDegrees(f * 90.0F));
      } else if (p_115317_.isAutoSpinAttack()) {
         p_115318_.mulPose(Axis.XP.rotationDegrees(-90.0F - p_115317_.getXRot()));
         p_115318_.mulPose(Axis.YP.rotationDegrees(((float)p_115317_.tickCount + p_115321_) * -75.0F));
      } else if (p_115317_.hasPose(Pose.SLEEPING)) {
         Direction direction = p_115317_.getBedOrientation();
         float f1 = direction != null ? sleepDirectionToRotation(direction) : p_115320_;
         p_115318_.mulPose(Axis.YP.rotationDegrees(f1));
         p_115318_.mulPose(Axis.ZP.rotationDegrees(90.0F));
         p_115318_.mulPose(Axis.YP.rotationDegrees(270.0F));
      } else if (isEntityUpsideDown(p_115317_)) {
         p_115318_.translate(0.0F, p_115317_.getBbHeight() + 0.1F, 0.0F);
         p_115318_.mulPose(Axis.ZP.rotationDegrees(180.0F));
      }

   }

   private static float sleepDirectionToRotation(Direction p_115329_) {
      switch (p_115329_) {
         case SOUTH:
            return 90.0F;
         case WEST:
            return 0.0F;
         case NORTH:
            return 270.0F;
         case EAST:
            return 180.0F;
         default:
            return 0.0F;
      }
   }

   private void renderAttackBeam(MutationRavager p_114829_, LivingEntity livingentity, float p_114831_, PoseStack p_114832_, MultiBufferSource p_114833_) {
      float f1 = p_114829_.tickCount + p_114831_;
      float f = p_114829_.getAttackAnimationScale(p_114831_);
      //float f1 = p_114829_.getClientSideAttackTime() + p_114831_;
      float f2 = f1 * 0.5F % 1.0F;
      float f3 = p_114829_.getEyeHeight();
      p_114832_.pushPose();
      p_114832_.translate(0.0F, f3, 0.0F);
      Vec3 vec3 = this.getPosition(livingentity, (double)livingentity.getBbHeight() * 0.5D, p_114831_);
      Vec3 vec31 = this.getPosition(p_114829_, (double)f3, p_114831_);
      Vec3 vec32 = vec3.subtract(vec31);
      float f4 = (float)(vec32.length() + 1.0D);
      vec32 = vec32.normalize();
      float f5 = (float)Math.acos(vec32.y);
      float f6 = (float)Math.atan2(vec32.z, vec32.x);
      p_114832_.mulPose(Axis.YP.rotationDegrees((((float)Math.PI / 2F) - f6) * (180F / (float)Math.PI)));
      p_114832_.mulPose(Axis.XP.rotationDegrees(f5 * (180F / (float)Math.PI)));
      int i = 1;
      float f7 = f1 * 0.05F * -1.5F;
      float f8 = f * f;
      int j = 64 + (int)(f8 * 191.0F);
      int k = 32 + (int)(f8 * 191.0F);
      int l = 128 - (int)(f8 * 64.0F);
      float f9 = 0.2F;
      float f10 = 0.282F;
      float f11 = Mth.cos(f7 + 2.3561945F) * 0.282F;
      float f12 = Mth.sin(f7 + 2.3561945F) * 0.282F;
      float f13 = Mth.cos(f7 + ((float)Math.PI / 4F)) * 0.282F;
      float f14 = Mth.sin(f7 + ((float)Math.PI / 4F)) * 0.282F;
      float f15 = Mth.cos(f7 + 3.926991F) * 0.282F;
      float f16 = Mth.sin(f7 + 3.926991F) * 0.282F;
      float f17 = Mth.cos(f7 + 5.4977875F) * 0.282F;
      float f18 = Mth.sin(f7 + 5.4977875F) * 0.282F;
      float f19 = Mth.cos(f7 + (float)Math.PI) * 0.2F;
      float f20 = Mth.sin(f7 + (float)Math.PI) * 0.2F;
      float f21 = Mth.cos(f7 + 0.0F) * 0.2F;
      float f22 = Mth.sin(f7 + 0.0F) * 0.2F;
      float f23 = Mth.cos(f7 + ((float)Math.PI / 2F)) * 0.2F;
      float f24 = Mth.sin(f7 + ((float)Math.PI / 2F)) * 0.2F;
      float f25 = Mth.cos(f7 + ((float)Math.PI * 1.5F)) * 0.2F;
      float f26 = Mth.sin(f7 + ((float)Math.PI * 1.5F)) * 0.2F;
      float f27 = 0.0F;
      float f28 = 0.4999F;
      float f29 = -1.0F + f2;
      float f30 = f4 * 2.5F + f29;
      VertexConsumer vertexconsumer = p_114833_.getBuffer(BEAM_RENDER_TYPE);
      PoseStack.Pose posestack$pose = p_114832_.last();
      Matrix4f matrix4f = posestack$pose.pose();
      Matrix3f matrix3f = posestack$pose.normal();
      vertex(vertexconsumer, matrix4f, matrix3f, f19, f4, f20, j, k, l, 0.4999F, f30);
      vertex(vertexconsumer, matrix4f, matrix3f, f19, 0.0F, f20, j, k, l, 0.4999F, f29);
      vertex(vertexconsumer, matrix4f, matrix3f, f21, 0.0F, f22, j, k, l, 0.0F, f29);
      vertex(vertexconsumer, matrix4f, matrix3f, f21, f4, f22, j, k, l, 0.0F, f30);
      vertex(vertexconsumer, matrix4f, matrix3f, f23, f4, f24, j, k, l, 0.4999F, f30);
      vertex(vertexconsumer, matrix4f, matrix3f, f23, 0.0F, f24, j, k, l, 0.4999F, f29);
      vertex(vertexconsumer, matrix4f, matrix3f, f25, 0.0F, f26, j, k, l, 0.0F, f29);
      vertex(vertexconsumer, matrix4f, matrix3f, f25, f4, f26, j, k, l, 0.0F, f30);
      float f31 = 0.0F;
      if (p_114829_.tickCount % 2 == 0) {
         f31 = 0.5F;
      }

      vertex(vertexconsumer, matrix4f, matrix3f, f11, f4, f12, j, k, l, 0.5F, f31 + 0.5F);
      vertex(vertexconsumer, matrix4f, matrix3f, f13, f4, f14, j, k, l, 1.0F, f31 + 0.5F);
      vertex(vertexconsumer, matrix4f, matrix3f, f17, f4, f18, j, k, l, 1.0F, f31);
      vertex(vertexconsumer, matrix4f, matrix3f, f15, f4, f16, j, k, l, 0.5F, f31);
      p_114832_.popPose();
   }

   private Vec3 getPosition(LivingEntity p_114803_, double p_114804_, float p_114805_) {
      double d0 = Mth.lerp((double)p_114805_, p_114803_.xOld, p_114803_.getX());
      double d1 = Mth.lerp((double)p_114805_, p_114803_.yOld, p_114803_.getY()) + p_114804_;
      double d2 = Mth.lerp((double)p_114805_, p_114803_.zOld, p_114803_.getZ());
      return new Vec3(d0, d1, d2);
   }

   private static void vertex(VertexConsumer p_253637_, Matrix4f p_253920_, Matrix3f p_253881_, float p_253994_, float p_254492_, float p_254474_, int p_254080_, int p_253655_, int p_254133_, float p_254233_, float p_253939_) {
      p_253637_.vertex(p_253920_, p_253994_, p_254492_, p_254474_).color(p_254080_, p_253655_, p_254133_, 255).uv(p_254233_, p_253939_).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(p_253881_, 0.0F, 1.0F, 0.0F).endVertex();
   }
}