package shirumengya.endless_deep_space.custom.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import shirumengya.endless_deep_space.custom.client.model.BlaznanaShulkerTrickModel;
import shirumengya.endless_deep_space.custom.entity.miniboss.BlaznanaShulkerTrick;

@OnlyIn(Dist.CLIENT)
public class BlaznanaShulkerTrickHeadLayer extends RenderLayer<BlaznanaShulkerTrick, BlaznanaShulkerTrickModel<BlaznanaShulkerTrick>> {
   public BlaznanaShulkerTrickHeadLayer(RenderLayerParent<BlaznanaShulkerTrick, BlaznanaShulkerTrickModel<BlaznanaShulkerTrick>> p_117432_) {
      super(p_117432_);
   }

   public void render(PoseStack p_117445_, MultiBufferSource p_117446_, int p_117447_, BlaznanaShulkerTrick p_117448_, float p_117449_, float p_117450_, float p_117451_, float p_117452_, float p_117453_, float p_117454_) {
      ResourceLocation resourcelocation = new ResourceLocation("endless_deep_space:textures/entities/blaznana_shulker_trick/blaznana_shulker_trick.png");
      VertexConsumer vertexconsumer = p_117446_.getBuffer(RenderType.entitySolid(resourcelocation));
      this.getParentModel().getHead().render(p_117445_, vertexconsumer, p_117447_, LivingEntityRenderer.getOverlayCoords(p_117448_, 0.0F));
   }
}