package shirumengya.endless_deep_space.custom.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;
import shirumengya.endless_deep_space.custom.client.model.OceanDefenderModel;
import shirumengya.endless_deep_space.custom.client.renderer.ModRenderType;
import shirumengya.endless_deep_space.custom.client.renderer.entity.OceanDefenderRenderer;
import shirumengya.endless_deep_space.custom.entity.boss.oceandefenders.OceanDefender;
import shirumengya.endless_deep_space.custom.init.ModItems;
import shirumengya.endless_deep_space.custom.init.ModModels;

public class OceanDefenderProgressTwoLayer extends RenderLayer<OceanDefender, OceanDefenderModel> {
   private final OceanDefenderModel model;
   
   public OceanDefenderProgressTwoLayer(RenderLayerParent<OceanDefender, OceanDefenderModel> p_117346_, EntityModelSet p_174555_) {
      super(p_117346_);
      this.model = new OceanDefenderModel(p_174555_.bakeLayer(ModModels.OCEAN_DEFENDER_PROGRESS_TWO));
   }
   
   @Override
   public void render(PoseStack p_116970_, MultiBufferSource p_116971_, int p_116972_, OceanDefender p_116973_, float p_116974_, float p_116975_, float p_116976_, float p_116977_, float p_116978_, float p_116979_) {
      ModRenderType.setTargetStack(new ItemStack(ModItems.OCEAN_DEFENDER_SPAWN_EGG.get()));
      EntityModel<OceanDefender> entitymodel = this.model;
      entitymodel.prepareMobModel(p_116973_, p_116974_, p_116975_, p_116976_);
      this.getParentModel().copyPropertiesTo(entitymodel);
      VertexConsumer vertexconsumer = ItemRenderer.getFoilBufferDirect(p_116971_, this.model.renderType(OceanDefenderRenderer.getStaticTextureLocation(p_116973_)), false, p_116973_.isProgressTwo());
      entitymodel.setupAnim(p_116973_, p_116974_, p_116975_, p_116977_, p_116978_, p_116979_);
      entitymodel.renderToBuffer(p_116970_, vertexconsumer, p_116972_, OverlayTexture.pack(0.0F, p_116973_.hurtTime > 0), 1.0F, 1.0F, 1.0F, 1.0F);
   }
}