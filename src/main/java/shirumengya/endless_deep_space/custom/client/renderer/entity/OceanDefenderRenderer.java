package shirumengya.endless_deep_space.custom.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import shirumengya.endless_deep_space.custom.client.model.OceanDefenderModel;
import shirumengya.endless_deep_space.custom.client.renderer.ModRenderType;
import shirumengya.endless_deep_space.custom.client.renderer.entity.layers.OceanDefenderArmorLayer;
import shirumengya.endless_deep_space.custom.client.renderer.entity.layers.OceanDefenderProgressTwoLayer;
import shirumengya.endless_deep_space.custom.entity.boss.oceandefenders.OceanDefender;
import shirumengya.endless_deep_space.custom.init.ModModels;

import static shirumengya.endless_deep_space.custom.client.renderer.entity.EnderLordRenderer.*;

@OnlyIn(Dist.CLIENT)
public class OceanDefenderRenderer extends MobRenderer<OceanDefender, OceanDefenderModel> {
   public static final ResourceLocation GUARDIAN_LOCATION = new ResourceLocation("endless_deep_space:textures/entities/oceandefender/guardian.png");
   public static final ResourceLocation GUARDIAN_ELDER_LOCATION = new ResourceLocation("endless_deep_space:textures/entities/oceandefender/guardian_elder.png");
   private static final ResourceLocation GUARDIAN_BEAM_LOCATION = new ResourceLocation("endless_deep_space:textures/entities/oceandefender/guardian_beam.png");
   private static final RenderType BEAM_RENDER_TYPE = RenderType.entityCutoutNoCull(GUARDIAN_BEAM_LOCATION);
   private static final RenderType OUTLINE_BEAM_RENDER_TYPE = RenderType.outline(GUARDIAN_BEAM_LOCATION);
   public static final Material ATTRITION_FIRE_0 = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("block/attrition_fire_0"));
   public static final Material ATTRITION_FIRE_1 = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("block/attrition_fire_1"));

   public OceanDefenderRenderer(EntityRendererProvider.Context p_173966_) {
      super(p_173966_, new OceanDefenderModel(p_173966_.bakeLayer(ModModels.OCEAN_DEFENDER)), 1.2F);
      this.addLayer(new OceanDefenderProgressTwoLayer(this, p_173966_.getModelSet()));
      this.addLayer(new OceanDefenderArmorLayer(this, p_173966_.getModelSet()));
   }
   
   @Override
   public boolean shouldRender(OceanDefender p_115468_, Frustum p_115469_, double p_115470_, double p_115471_, double p_115472_) {
      return true;
   }
   
   private Vec3 getPosition(LivingEntity p_114803_, double p_114804_, float p_114805_) {
      double d0 = Mth.lerp((double)p_114805_, p_114803_.xOld, p_114803_.getX());
      double d1 = Mth.lerp((double)p_114805_, p_114803_.yOld, p_114803_.getY()) + p_114804_;
      double d2 = Mth.lerp((double)p_114805_, p_114803_.zOld, p_114803_.getZ());
      return new Vec3(d0, d1, d2);
   }
   
   public void render(OceanDefender p_114829_, float p_114830_, float p_114831_, PoseStack p_114832_, MultiBufferSource p_114833_, int p_114834_) {
      p_114832_.pushPose();
      if (p_114829_.oceanDefenderDeathTime > 0) {
         p_114832_.scale(1.0F - (float) p_114829_.oceanDefenderDeathTime / 1800, 1.0F - (float) p_114829_.oceanDefenderDeathTime / 1800, 1.0F - (float) p_114829_.oceanDefenderDeathTime / 1800);
      }
      super.render(p_114829_, p_114830_, p_114831_, p_114832_, p_114833_, p_114834_);
      p_114832_.popPose();
      
      if (p_114829_.oceanDefenderDeathTime > 0) {
         float f5 = ((float)p_114829_.oceanDefenderDeathTime + p_114831_) / 1800.0F;
         float f7 = Math.min(f5 > 0.8F ? (f5 - 0.8F) / 0.2F : 0.0F, 1.0F);
         RandomSource randomsource = RandomSource.create(432L);
         VertexConsumer vertexconsumer2 = p_114833_.getBuffer(RenderType.lightning());
         p_114832_.pushPose();
         p_114832_.translate(0.0F, p_114829_.size.height / 2.0F, 0.0F);
         p_114832_.scale(p_114829_.oceanDefenderDeathTime / 30.0F, p_114829_.oceanDefenderDeathTime / 30.0F, p_114829_.oceanDefenderDeathTime / 30.0F);
         
         for(int i = 0; (float)i < (f5 + f5 * f5) / 2.0F * 60.0F; ++i) {
            p_114832_.mulPose(Axis.XP.rotationDegrees(randomsource.nextFloat() * 360.0F));
            p_114832_.mulPose(Axis.YP.rotationDegrees(randomsource.nextFloat() * 360.0F));
            p_114832_.mulPose(Axis.ZP.rotationDegrees(randomsource.nextFloat() * 360.0F));
            p_114832_.mulPose(Axis.XP.rotationDegrees(randomsource.nextFloat() * 360.0F));
            p_114832_.mulPose(Axis.YP.rotationDegrees(randomsource.nextFloat() * 360.0F));
            p_114832_.mulPose(Axis.ZP.rotationDegrees(randomsource.nextFloat() * 360.0F + f5 * 90.0F));
            float f3 = randomsource.nextFloat() * 20.0F + 5.0F + f7 * 10.0F;
            float f4 = randomsource.nextFloat() * 2.0F + 1.0F + f7 * 2.0F;
            Matrix4f matrix4f = p_114832_.last().pose();
            int j = (int)(255.0F * (1.0F - f7));
            vertex01(vertexconsumer2, matrix4f, j, 255, 255, 255);
            vertex2(vertexconsumer2, matrix4f, f3, f4, 255, 255, 255);
            vertex3(vertexconsumer2, matrix4f, f3, f4, 255, 255, 255);
            vertex01(vertexconsumer2, matrix4f, j, 255, 255, 255);
            vertex3(vertexconsumer2, matrix4f, f3, f4, 255, 255, 255);
            vertex4(vertexconsumer2, matrix4f, f3, f4, 255, 255, 255);
            vertex01(vertexconsumer2, matrix4f, j, 255, 255, 255);
            vertex4(vertexconsumer2, matrix4f, f3, f4, 255, 255, 255);
            vertex2(vertexconsumer2, matrix4f, f3, f4, 255, 255, 255);
         }
         
         p_114832_.popPose();
      }
      
      if (p_114829_.oceanDefenderChargingTime > 0) {
         float f5 = ((float)p_114829_.oceanDefenderChargingTime + p_114831_) / 200.0F;
         float f7 = Math.min(f5 > 0.8F ? (f5 - 0.8F) / 0.2F : 0.0F, 1.0F);
         RandomSource randomsource = RandomSource.create(432L);
         VertexConsumer vertexconsumer2 = p_114833_.getBuffer(RenderType.lightning());
         p_114832_.pushPose();
         p_114832_.translate(0.0F, p_114829_.size.height / 2.0F, 0.0F);
         p_114832_.scale(20.0F, 20.0F, 20.0F);
         
         for(int i = 0; (float)i < (f5 + f5 * f5) / 2.0F * 60.0F; ++i) {
            p_114832_.mulPose(Axis.XP.rotationDegrees(randomsource.nextFloat() * 360.0F));
            p_114832_.mulPose(Axis.YP.rotationDegrees(randomsource.nextFloat() * 360.0F));
            p_114832_.mulPose(Axis.ZP.rotationDegrees(randomsource.nextFloat() * 360.0F));
            p_114832_.mulPose(Axis.XP.rotationDegrees(randomsource.nextFloat() * 360.0F));
            p_114832_.mulPose(Axis.YP.rotationDegrees(randomsource.nextFloat() * 360.0F));
            p_114832_.mulPose(Axis.ZP.rotationDegrees(randomsource.nextFloat() * 360.0F + f5 * 90.0F));
            float f3 = randomsource.nextFloat() * 20.0F + 5.0F + f7 * 10.0F;
            float f4 = randomsource.nextFloat() * 2.0F + 1.0F + f7 * 2.0F;
            Matrix4f matrix4f = p_114832_.last().pose();
            int j = (int)(255.0F * (1.0F - f7));
            vertex01(vertexconsumer2, matrix4f, j, 255, 255, 255);
            vertex2(vertexconsumer2, matrix4f, f3, f4, 255, 0, 255);
            vertex3(vertexconsumer2, matrix4f, f3, f4, 255, 0, 255);
            vertex01(vertexconsumer2, matrix4f, j, 255, 255, 255);
            vertex3(vertexconsumer2, matrix4f, f3, f4, 255, 0, 255);
            vertex4(vertexconsumer2, matrix4f, f3, f4, 255, 0, 255);
            vertex01(vertexconsumer2, matrix4f, j, 255, 255, 255);
            vertex4(vertexconsumer2, matrix4f, f3, f4, 255, 0, 255);
            vertex2(vertexconsumer2, matrix4f, f3, f4, 255, 0, 255);
         }
         
         p_114832_.popPose();
      }
      
      LivingEntity livingentity = p_114829_.getActiveAttackTarget();
      if (livingentity != null) {
         float f = p_114829_.getAttackAnimationScale(p_114831_);
         float f1 = p_114829_.tickCount + p_114831_;
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
         VertexConsumer vertexconsumer = p_114833_.getBuffer(OUTLINE_BEAM_RENDER_TYPE);
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
      
      OceanDefender defender = p_114829_.getOtherOceanDefender();
      if (defender != null) {
         float f = 1;
         float f1 = p_114829_.tickCount + p_114831_;
         float f2 = f1 * 0.5F % 1.0F;
         float f3 = p_114829_.getEyeHeight();
         p_114832_.pushPose();
         p_114832_.translate(0.0F, f3, 0.0F);
         Vec3 vec3 = this.getPosition(defender, (double)defender.size.height * 0.5D, p_114831_);
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
         int j = 0;
         int k = 255;
         int l = 255;
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
         VertexConsumer vertexconsumer = p_114833_.getBuffer(OUTLINE_BEAM_RENDER_TYPE);
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
      
      LivingEntity attackTarget = p_114829_.getAttackTarget();
      if (attackTarget != null) {
         float f = 1;
         float f1 = p_114829_.tickCount + p_114831_;
         float f2 = f1 * 0.5F % 1.0F;
         float f3 = p_114829_.getEyeHeight();
         p_114832_.pushPose();
         p_114832_.translate(0.0F, f3, 0.0F);
         Vec3 vec3 = this.getPosition(attackTarget, (double) attackTarget.getBbHeight() * 0.5D, p_114831_);
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
         int j = 255;
         int k = 255;
         int l = 255;
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
         VertexConsumer vertexconsumer = p_114833_.getBuffer(p_114829_.isTypeOne() ? ModRenderType.EnderLordAttackTargetBeam(END_SKY_LOCATION, END_PORTAL_LOCATION, false) : ModRenderType.EnderLordDeath(END_SKY_LOCATION, END_PORTAL_LOCATION, false));
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
   }
   
   private static void vertex(VertexConsumer p_253637_, Matrix4f p_253920_, Matrix3f p_253881_, float p_253994_, float p_254492_, float p_254474_, int p_254080_, int p_253655_, int p_254133_, float p_254233_, float p_253939_) {
      p_253637_.vertex(p_253920_, p_253994_, p_254492_, p_254474_).color(p_254080_, p_253655_, p_254133_, 255).uv(p_254233_, p_253939_).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(p_253881_, 0.0F, 1.0F, 0.0F).endVertex();
   }

   protected void scale(OceanDefender p_114129_, PoseStack p_114130_, float p_114131_) {
      p_114130_.scale(OceanDefender.ELDER_SIZE_SCALE * (p_114129_.isTypeOne() ? 4.0F : 2.0F), OceanDefender.ELDER_SIZE_SCALE * (p_114129_.isTypeOne() ? 4.0F : 2.0F), OceanDefender.ELDER_SIZE_SCALE * (p_114129_.isTypeOne() ? 4.0F : 2.0F));
   }
   
   @Override
   protected int getBlockLightLevel(OceanDefender p_114496_, BlockPos p_114497_) {
      return Math.max(15 / (p_114496_.isTypeOne() ? 1 : 2), super.getBlockLightLevel(p_114496_, p_114497_));
   }
   
   public ResourceLocation getTextureLocation(OceanDefender p_114127_) {
      return getStaticTextureLocation(p_114127_);
   }
   
   public static ResourceLocation getStaticTextureLocation(OceanDefender p_114127_) {
      return p_114127_.isTypeOne() ? GUARDIAN_ELDER_LOCATION : GUARDIAN_LOCATION;
   }
}