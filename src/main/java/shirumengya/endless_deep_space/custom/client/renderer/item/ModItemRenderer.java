package shirumengya.endless_deep_space.custom.client.renderer.item;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

@OnlyIn(Dist.CLIENT)
public class ModItemRenderer extends BlockEntityWithoutLevelRenderer {
   public static int ticksExisted = 0;

   public ModItemRenderer() {
      super(null, null);
   }

   public static ModItemRenderer getInstance() {
      return new ModItemRenderer();
   }

   public static void drawEntityOnScreen(PoseStack matrixstack, int posX, int posY, float scale, boolean follow, double xRot, double yRot, double zRot, float mouseX, float mouseY, Entity entity) {
      float f = (float) Math.atan(-mouseX / 40.0F);
      float f1 = (float) Math.atan(mouseY / 40.0F);
      matrixstack.scale(scale, scale, scale);
      entity.setOnGround(false);
      float partialTicks = Minecraft.getInstance().getFrameTime();
      Quaternionf quaternion = Axis.ZP.rotationDegrees(180.0F);
      Quaternionf quaternion1 = Axis.XP.rotationDegrees(20.0F);
      float partialTicksForRender = Minecraft.getInstance().isPaused() ? 0 : partialTicks;
      int tick;
      if (Minecraft.getInstance().player == null || Minecraft.getInstance().isPaused()) {
         tick = ticksExisted;
      } else {
         tick = Minecraft.getInstance().player.tickCount;
      }
      if (follow) {
         float yaw = f * 45.0F;
         entity.setYRot(yaw);
         entity.tickCount = tick;
         if (entity instanceof LivingEntity) {
            ((LivingEntity) entity).yBodyRot = yaw;
            ((LivingEntity) entity).yBodyRotO = yaw;
            ((LivingEntity) entity).yHeadRot = yaw;
            ((LivingEntity) entity).yHeadRotO = yaw;
         }

         quaternion1 = Axis.XP.rotationDegrees(f1 * 20.0F);
         quaternion.mul(quaternion1);
      }

      matrixstack.mulPose(quaternion);
      matrixstack.mulPose(Axis.XP.rotationDegrees((float) (-xRot)));
      matrixstack.mulPose(Axis.YP.rotationDegrees((float) yRot));
      matrixstack.mulPose(Axis.ZP.rotationDegrees((float) zRot));
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      quaternion1.conjugate();
      entityrenderdispatcher.overrideCameraOrientation(quaternion1);
      entityrenderdispatcher.setRenderShadow(false);
      MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
      RenderSystem.runAsFancy(() -> {
         entityrenderdispatcher.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, partialTicksForRender, matrixstack, multibuffersource$buffersource, 15728880);
      });
      multibuffersource$buffersource.endBatch();
      entityrenderdispatcher.setRenderShadow(true);
      entity.setYRot(0.0F);
      entity.setXRot(0.0F);
      if (entity instanceof LivingEntity) {
         ((LivingEntity) entity).yBodyRot = 0.0F;
         ((LivingEntity) entity).yHeadRotO = 0.0F;
         ((LivingEntity) entity).yHeadRot = 0.0F;
      }
      RenderSystem.applyModelViewMatrix();
      Lighting.setupFor3DItems();
   }

   public void renderCube(float size1, float size2, float size3, Matrix4f p_173692_, VertexConsumer p_173693_) {
      this.renderFace(p_173692_, p_173693_, size2, size1, size2 + size3, size1 + size3, size1, size1, size1, size1, Direction.SOUTH);
      this.renderFace(p_173692_, p_173693_, size2, size1, size1 + size3, size2 + size3, size2, size2, size2, size2, Direction.NORTH);
      this.renderFace(p_173692_, p_173693_, size1, size1, size1 + size3, size2 + size3, size2, size1, size1, size2, Direction.EAST);
      this.renderFace(p_173692_, p_173693_, size2, size2, size2 + size3, size1 + size3, size2, size1, size1, size2, Direction.WEST);
      this.renderFace(p_173692_, p_173693_, size2, size1, size2 + size3, size2 + size3, size2, size2, size1, size1, Direction.DOWN);
      this.renderFace(p_173692_, p_173693_, size2, size1, size1 + size3, size1 + size3, size1, size1, size2, size2, Direction.UP);
   }

   public void renderFace(Matrix4f p_254247_, VertexConsumer p_254390_, float p_254147_, float p_253639_, float p_254107_, float p_254109_, float p_254021_, float p_254458_, float p_254086_, float p_254310_, Direction p_253619_) {
      p_254390_.vertex(p_254247_, p_254147_, p_254107_, p_254021_).endVertex();
      p_254390_.vertex(p_254247_, p_253639_, p_254107_, p_254458_).endVertex();
      p_254390_.vertex(p_254247_, p_253639_, p_254109_, p_254086_).endVertex();
      p_254390_.vertex(p_254247_, p_254147_, p_254109_, p_254310_).endVertex();
   }

   public void renderByItem(ItemStack itemStackIn, ItemDisplayContext transformType, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
   }
}