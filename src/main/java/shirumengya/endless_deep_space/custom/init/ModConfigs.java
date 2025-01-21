package shirumengya.endless_deep_space.custom.init;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import shirumengya.endless_deep_space.EndlessDeepSpaceMod;
import shirumengya.endless_deep_space.custom.config.ModClientConfig;
import shirumengya.endless_deep_space.custom.config.ModCommonConfig;

@Mod.EventBusSubscriber(modid = EndlessDeepSpaceMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModConfigs {
	@SubscribeEvent
	public static void register(FMLConstructModEvent event) {
		event.enqueueWork(() -> {
			ModLoadingContext.get().registerConfig(Type.COMMON, ModCommonConfig.SPEC, "endless_deep_space-common.toml");
			ModLoadingContext.get().registerConfig(Type.CLIENT, ModClientConfig.SPEC, "endless_deep_space-client.toml");
		});
	}
}
