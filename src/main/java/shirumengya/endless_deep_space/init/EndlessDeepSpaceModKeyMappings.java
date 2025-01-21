
/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package shirumengya.endless_deep_space.init;

import shirumengya.endless_deep_space.network.NoFogMessage;
import shirumengya.endless_deep_space.network.NightVisionMessage;
import shirumengya.endless_deep_space.network.InvincibleKeyMessage;
import shirumengya.endless_deep_space.EndlessDeepSpaceMod;

import org.lwjgl.glfw.GLFW;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = {Dist.CLIENT})
public class EndlessDeepSpaceModKeyMappings {
	public static final KeyMapping INVINCIBLE_KEY = new KeyMapping("key.endless_deep_space.invincible_key", GLFW.GLFW_KEY_I, "key.categories.endless_deep_space") {
		private boolean isDownOld = false;

		@Override
		public void setDown(boolean isDown) {
			super.setDown(isDown);
			if (isDownOld != isDown && isDown) {
				EndlessDeepSpaceMod.PACKET_HANDLER.sendToServer(new InvincibleKeyMessage(0, 0));
				InvincibleKeyMessage.pressAction(Minecraft.getInstance().player, 0, 0);
			}
			isDownOld = isDown;
		}
	};
	public static final KeyMapping NIGHT_VISION = new KeyMapping("key.endless_deep_space.night_vision", GLFW.GLFW_KEY_N, "key.categories.endless_deep_space") {
		private boolean isDownOld = false;

		@Override
		public void setDown(boolean isDown) {
			super.setDown(isDown);
			if (isDownOld != isDown && isDown) {
				EndlessDeepSpaceMod.PACKET_HANDLER.sendToServer(new NightVisionMessage(0, 0));
				NightVisionMessage.pressAction(Minecraft.getInstance().player, 0, 0);
			}
			isDownOld = isDown;
		}
	};
	public static final KeyMapping NO_FOG = new KeyMapping("key.endless_deep_space.no_fog", GLFW.GLFW_KEY_G, "key.categories.endless_deep_space") {
		private boolean isDownOld = false;

		@Override
		public void setDown(boolean isDown) {
			super.setDown(isDown);
			if (isDownOld != isDown && isDown) {
				EndlessDeepSpaceMod.PACKET_HANDLER.sendToServer(new NoFogMessage(0, 0));
				NoFogMessage.pressAction(Minecraft.getInstance().player, 0, 0);
			}
			isDownOld = isDown;
		}
	};

	@SubscribeEvent
	public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
		event.register(INVINCIBLE_KEY);
		event.register(NIGHT_VISION);
		event.register(NO_FOG);
	}

	@Mod.EventBusSubscriber({Dist.CLIENT})
	public static class KeyEventListener {
		@SubscribeEvent
		public static void onClientTick(TickEvent.ClientTickEvent event) {
			if (Minecraft.getInstance().screen == null) {
				INVINCIBLE_KEY.consumeClick();
				NIGHT_VISION.consumeClick();
				NO_FOG.consumeClick();
			}
		}
	}
}
