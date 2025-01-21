package shirumengya.endless_deep_space.custom.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.WitherBossModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import shirumengya.endless_deep_space.custom.client.model.OceanDefenderModel;
import shirumengya.endless_deep_space.custom.entity.boss.oceandefenders.OceanDefender;
import shirumengya.endless_deep_space.custom.init.ModModels;

public class OceanDefenderArmorLayer extends RenderLayer<OceanDefender, OceanDefenderModel> {
   private static final ResourceLocation ARMOR_LOCATION = new ResourceLocation("endless_deep_space:textures/entities/oceandefender/defender_armor.png");
   private final OceanDefenderModel model;
   
   public OceanDefenderArmorLayer(RenderLayerParent<OceanDefender, OceanDefenderModel> p_117346_, EntityModelSet p_174555_) {
      super(p_117346_);
      this.model = new OceanDefenderModel(p_174555_.bakeLayer(ModModels.OCEAN_DEFENDER_ARMOR));
   }
   
   @Override
   public void render(PoseStack p_116970_, MultiBufferSource p_116971_, int p_116972_, OceanDefender p_116973_, float p_116974_, float p_116975_, float p_116976_, float p_116977_, float p_116978_, float p_116979_) {
      if (p_116973_.isDying() || (p_116973_.getOtherOceanDefender() != null && p_116973_.isProgressTwo() && !p_116973_.getOtherOceanDefender().isProgressTwo()) || p_116973_.getChargingTimer() >= 100 || p_116973_.getPhase() == OceanDefender.PHASE_LASER_ATTACK) {
         float f = (float)p_116973_.tickCount + p_116976_;
         EntityModel<OceanDefender> entitymodel = this.model;
         entitymodel.prepareMobModel(p_116973_, p_116974_, p_116975_, p_116976_);
         this.getParentModel().copyPropertiesTo(entitymodel);
         VertexConsumer vertexconsumer = p_116971_.getBuffer(RenderType.energySwirl(ARMOR_LOCATION, this.xOffset(f) % 1.0F, f * 0.01F % 1.0F));
         entitymodel.setupAnim(p_116973_, p_116974_, p_116975_, p_116977_, p_116978_, p_116979_);
         entitymodel.renderToBuffer(p_116970_, vertexconsumer, p_116972_, OverlayTexture.NO_OVERLAY, 0.5F, 0.5F, 0.5F, 1.0F);
      }
   }
   
   protected float xOffset(float p_117702_) {
      return Mth.cos(p_117702_ * 0.02F) * 3.0F;
   }
}
