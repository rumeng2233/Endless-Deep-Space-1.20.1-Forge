package shirumengya.endless_deep_space.custom.client.renderer.entity;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class NullRenderer<T extends Entity> extends EntityRenderer<T> {
	public NullRenderer(EntityRendererProvider.Context manager) {
		super(manager);
	}

	public ResourceLocation getTextureLocation(T entity) {
      	return TextureAtlas.LOCATION_BLOCKS;
   	}
}
