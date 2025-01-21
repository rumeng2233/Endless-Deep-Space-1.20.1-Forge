package shirumengya.endless_deep_space.custom.client.event;

import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import shirumengya.endless_deep_space.custom.client.gui.overlay.JumpStringOverlay;
import shirumengya.endless_deep_space.custom.config.ModClientConfig;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class JumpStringInBiome {
	public static Biome biome;
	
	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			execute(event, event.player.level(), event.player);
		}
	}

	public static void execute(LevelAccessor world, Entity entity) {
		execute(null, world, entity);
	}

	private static void execute(@Nullable Event event, LevelAccessor world, Entity entity) {
		if (entity == null || world == null)
			return;
		if (world instanceof Level && entity.level().isClientSide && ModClientConfig.BIOME_JUMP_STRING.get()) {
			Level level = (Level)world;
			if (biome != world.getBiome(entity.blockPosition()).get()) {
				biome = world.getBiome(entity.blockPosition()).get();
				JumpStringOverlay.UpdateBigJumpString(getBiomeName(biome, level), 60, false, true);
			}
		}
	}

	public static Component getBiomeName(Biome biome,Level world) {
		ResourceLocation biomeBaseKey = world.registryAccess().registryOrThrow(Registries.BIOME).getKey(biome);
		String key = Util.makeDescriptionId("biome", biomeBaseKey);
		return Component.translatable(key);
	}
}
