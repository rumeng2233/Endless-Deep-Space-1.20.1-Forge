package shirumengya.endless_deep_space.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import shirumengya.endless_deep_space.custom.client.gui.overlay.AttritionOverlay;
import shirumengya.endless_deep_space.custom.client.gui.screens.components.wheel.Color;
import shirumengya.endless_deep_space.custom.client.renderer.entity.OceanDefenderRenderer;
import shirumengya.endless_deep_space.custom.entity.boss.ColoredEntityPart;
import shirumengya.endless_deep_space.custom.entity.boss.PartBoss;
import shirumengya.endless_deep_space.custom.entity.boss.oceandefenders.OceanDefender;
import shirumengya.endless_deep_space.custom.event.SwordBlockEvent;

@Mixin({EntityRenderDispatcher.class})
public abstract class EntityRenderDispatcherMixin {
   @Unique
   private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("endless_deep_space:textures/mob_effect/invincible.png");
   @Unique
   private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(TEXTURE_LOCATION);
   
   @Shadow
   private boolean shouldRenderShadow;
   @Shadow
   private static void renderShadow(PoseStack p_114458_, MultiBufferSource p_114459_, Entity p_114460_, float p_114461_, float p_114462_, LevelReader p_114463_, float p_114464_) {
   }
   @Shadow
   private Level level;
   
   @Shadow
   private static void renderHitbox(PoseStack p_114442_, VertexConsumer p_114443_, Entity p_114444_, float p_114445_) {
   }
   
   @Shadow private Quaternionf cameraOrientation;
   
   @Shadow public abstract Quaternionf cameraOrientation();
   
   @Shadow public abstract double distanceToSqr(Entity p_114472_);
   
   @Shadow @Final private BlockRenderDispatcher blockRenderDispatcher;
   
   public EntityRenderDispatcherMixin() {
   
   }
   
   @Inject(method = {"shouldRender"}, at = {@At("HEAD")}, cancellable = true)
   public <E extends Entity> void shouldRender(E p_114398_, Frustum p_114399_, double p_114400_, double p_114401_, double p_114402_, CallbackInfoReturnable<Boolean> ci) {
      if (p_114398_.displayFireAnimation()) {
         ci.setReturnValue(true);
      }
      
      if (p_114398_ instanceof LivingEntity livingEntity) {
         if (OceanDefender.getAttrition(livingEntity) > 0.0F || SwordBlockEvent.hasVertigoTime(livingEntity)) {
            ci.setReturnValue(true);
         }
      }
   }
   
   @Inject(method = {"render"}, at = {@At("HEAD")}, cancellable = true)
   private <E extends Entity> void renderEntity(E p_114385_, double p_114386_, double p_114387_, double p_114388_, float p_114389_, float p_114390_, PoseStack p_114391_, MultiBufferSource p_114392_, int p_114393_, CallbackInfo ci) {
      EntityRenderDispatcher entityRenderDispatcher = ((EntityRenderDispatcher)(Object)this);
      
      EntityRenderer<? super E> entityrenderer = entityRenderDispatcher.getRenderer(p_114385_);
      
      try {
         Vec3 vec3 = entityrenderer.getRenderOffset(p_114385_, p_114390_);
         double d2 = p_114386_ + vec3.x();
         double d3 = p_114387_ + vec3.y();
         double d0 = p_114388_ + vec3.z();
         p_114391_.pushPose();
         p_114391_.translate(d2, d3, d0);
         entityrenderer.render(p_114385_, p_114389_, p_114390_, p_114391_, p_114392_, p_114393_);
         if (p_114385_.displayFireAnimation()) {
            this.renderFlame(p_114391_, p_114392_, p_114385_, entityRenderDispatcher.camera, ModelBakery.FIRE_0.sprite(), ModelBakery.FIRE_1.sprite(), Color.of(255, 255, 255));
         }
         
         if (p_114385_ instanceof LivingEntity livingEntity) {
            if (OceanDefender.getAttrition(livingEntity) > 0.0F) {
               float color = Math.max(0.1F, 1.0F - (float) OceanDefender.getAttritionTick(livingEntity) / OceanDefender.getAttritionMaxTick(livingEntity));
               this.renderFlame(p_114391_, p_114392_, p_114385_, entityRenderDispatcher.camera, OceanDefenderRenderer.ATTRITION_FIRE_0.sprite(), OceanDefenderRenderer.ATTRITION_FIRE_1.sprite(), Color.of(color, color, color, color));
            }
            
            if (SwordBlockEvent.hasVertigoTime(livingEntity)) {
               this.renderVertigoTime(livingEntity, p_114391_, p_114392_, p_114393_);
               AttritionOverlay.renderBlockModel(p_114385_, Blocks.ICE.defaultBlockState(), p_114391_, p_114392_, p_114393_);
            }
         }
         
         p_114391_.translate(-vec3.x(), -vec3.y(), -vec3.z());
         if (entityRenderDispatcher.options.entityShadows().get() && this.shouldRenderShadow && ((EntityRendererAccessor)entityrenderer).getShadowRadius() > 0.0F && !p_114385_.isInvisible()) {
            double d1 = entityRenderDispatcher.distanceToSqr(p_114385_.getX(), p_114385_.getY(), p_114385_.getZ());
            float f = (float)((1.0D - d1 / 256.0D) * (double)((EntityRendererAccessor)entityrenderer).getShadowStrength());
            if (f > 0.0F) {
               renderShadow(p_114391_, p_114392_, p_114385_, f, p_114390_, this.level, Math.min(((EntityRendererAccessor)entityrenderer).getShadowRadius(), 32.0F));
            }
         }
         
         if (entityRenderDispatcher.shouldRenderHitBoxes()) {
            renderHitbox(p_114391_, p_114392_.getBuffer(RenderType.lines()), p_114385_, p_114390_);
         }
         
         p_114391_.popPose();
      } catch (Throwable throwable) {
         CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering entity in world");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Entity being rendered");
         p_114385_.fillCrashReportCategory(crashreportcategory);
         CrashReportCategory crashreportcategory1 = crashreport.addCategory("Renderer details");
         crashreportcategory1.setDetail("Assigned renderer", entityrenderer);
         crashreportcategory1.setDetail("Location", CrashReportCategory.formatLocation(this.level, p_114386_, p_114387_, p_114388_));
         crashreportcategory1.setDetail("Rotation", p_114389_);
         crashreportcategory1.setDetail("Delta", p_114390_);
         throw new ReportedException(crashreport);
      }
      
      ci.cancel();
   }
   
   @Inject(method = {"renderHitbox"}, at = {@At("TAIL")})
   private static void renderColoredEntityPartHitbox(PoseStack p_114442_, VertexConsumer p_114443_, Entity p_114444_, float p_114445_, CallbackInfo ci) {
      if (p_114444_ instanceof PartBoss) {
         PartBoss dragon = (PartBoss) p_114444_;
         double d0 = -Mth.lerp((double) p_114445_, dragon.xOld, dragon.getX());
         double d1 = -Mth.lerp((double) p_114445_, dragon.yOld, dragon.getY());
         double d2 = -Mth.lerp((double) p_114445_, dragon.zOld, dragon.getZ());
         
         for (ColoredEntityPart<?> enderdragonpart : dragon.getSubEntities()) {
            p_114442_.pushPose();
            double d3 = d0 + Mth.lerp((double) p_114445_, enderdragonpart.xOld, enderdragonpart.getX());
            double d4 = d1 + Mth.lerp((double) p_114445_, enderdragonpart.yOld, enderdragonpart.getY());
            double d5 = d2 + Mth.lerp((double) p_114445_, enderdragonpart.zOld, enderdragonpart.getZ());
            p_114442_.translate(d3, d4, d5);
            LevelRenderer.renderLineBox(p_114442_, p_114443_, enderdragonpart.getBoundingBox().move(-enderdragonpart.getX(), -enderdragonpart.getY(), -enderdragonpart.getZ()), enderdragonpart.hitboxColor.red, enderdragonpart.hitboxColor.green, enderdragonpart.hitboxColor.blue, enderdragonpart.hitboxColor.alpha);
            p_114442_.popPose();
         }
      }
   }
   
   @Unique
   private void renderFlame(PoseStack p_114454_, MultiBufferSource p_114455_, Entity p_114456_, Camera camera, TextureAtlasSprite textureAtlasSpriteOne, TextureAtlasSprite textureAtlasSpriteTwo, Color color) {
      TextureAtlasSprite textureatlassprite = textureAtlasSpriteOne;
      TextureAtlasSprite textureatlassprite1 = textureAtlasSpriteTwo;
      p_114454_.pushPose();
      float f = p_114456_.getBbWidth() * 1.4F;
      if (p_114456_ instanceof OceanDefender defender) {
         f = defender.size.width * 1.4F;
      }
      p_114454_.scale(f, f, f);
      float f1 = 0.5F;
      float f2 = 0.0F;
      float f3 = p_114456_.getBbHeight() / f;
      if (p_114456_ instanceof OceanDefender defender) {
         f3 = defender.size.height / f;
      }
      float f4 = 0.0F;
      p_114454_.mulPose(Axis.YP.rotationDegrees(-camera.getYRot()));
      p_114454_.translate(0.0F, 0.0F, -0.3F + (float)((int)f3) * 0.02F);
      float f5 = 0.0F;
      int i = 0;
      VertexConsumer vertexconsumer = p_114455_.getBuffer(/*Sheets.cutoutBlockSheet()*/RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
      
      for(PoseStack.Pose posestack$pose = p_114454_.last(); f3 > 0.0F; ++i) {
         TextureAtlasSprite textureatlassprite2 = i % 2 == 0 ? textureatlassprite : textureatlassprite1;
         float f6 = textureatlassprite2.getU0();
         float f7 = textureatlassprite2.getV0();
         float f8 = textureatlassprite2.getU1();
         float f9 = textureatlassprite2.getV1();
         if (i / 2 % 2 == 0) {
            float f10 = f8;
            f8 = f6;
            f6 = f10;
         }
         
         fireVertex(posestack$pose, vertexconsumer, f1 - 0.0F, 0.0F - f4, f5, f8, f9, color);
         fireVertex(posestack$pose, vertexconsumer, -f1 - 0.0F, 0.0F - f4, f5, f6, f9, color);
         fireVertex(posestack$pose, vertexconsumer, -f1 - 0.0F, 1.4F - f4, f5, f6, f7, color);
         fireVertex(posestack$pose, vertexconsumer, f1 - 0.0F, 1.4F - f4, f5, f8, f7, color);
         f3 -= 0.45F;
         f4 -= 0.45F;
         f1 *= 0.9F;
         f5 += 0.03F;
      }
      
      p_114454_.popPose();
   }
   
   @Unique
   private static void fireVertex(PoseStack.Pose p_114415_, VertexConsumer p_114416_, float p_114417_, float p_114418_, float p_114419_, float p_114420_, float p_114421_, Color color) {
      p_114416_.vertex(p_114415_.pose(), p_114417_, p_114418_, p_114419_).color(color.red, color.green, color.blue, color.alpha).uv(p_114420_, p_114421_).overlayCoords(0, 10).uv2(240).normal(p_114415_.normal(), 0.0F, 1.0F, 0.0F).endVertex();
   }
   
   @Unique
   private void renderVertigoTime(LivingEntity p_114080_, PoseStack p_114083_, MultiBufferSource p_114084_, int p_114085_) {
      EntityRenderDispatcher entityRenderDispatcher = ((EntityRenderDispatcher)(Object)this);
      p_114083_.pushPose();
      float f = p_114080_.getBbWidth() * 1.4F;
      float f1 = p_114080_.getBbHeight();
      if (p_114080_ instanceof OceanDefender defender) {
         f = defender.size.width * 1.4F;
         f1 = defender.size.height;
      }
      p_114083_.translate(0.0F, f1, 0.0F);
      p_114083_.scale(f, f, f);
      p_114083_.mulPose(entityRenderDispatcher.cameraOrientation());
      p_114083_.mulPose(Axis.YP.rotationDegrees(180.0F));
      PoseStack.Pose posestack$pose = p_114083_.last();
      Matrix4f matrix4f = posestack$pose.pose();
      Matrix3f matrix3f = posestack$pose.normal();
      VertexConsumer vertexconsumer = p_114084_.getBuffer(RENDER_TYPE);
      vertex(vertexconsumer, matrix4f, matrix3f, p_114085_, 0.0F, 0, 0, 1);
      vertex(vertexconsumer, matrix4f, matrix3f, p_114085_, 1.0F, 0, 1, 1);
      vertex(vertexconsumer, matrix4f, matrix3f, p_114085_, 1.0F, 1, 1, 0);
      vertex(vertexconsumer, matrix4f, matrix3f, p_114085_, 0.0F, 1, 0, 0);
      p_114083_.popPose();
   }
   
   private static void vertex(VertexConsumer p_254095_, Matrix4f p_254477_, Matrix3f p_253948_, int p_253829_, float p_253995_, int p_254031_, int p_253641_, int p_254243_) {
      p_254095_.vertex(p_254477_, p_253995_ - 0.5F, (float)p_254031_ - 0.25F, 0.0F).color(255, 255, 255, 255).uv((float)p_253641_, (float)p_254243_).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(p_253829_).normal(p_253948_, 0.0F, 1.0F, 0.0F).endVertex();
   }
}
