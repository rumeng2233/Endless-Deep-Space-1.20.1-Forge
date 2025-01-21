package shirumengya.endless_deep_space.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.world.BossEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shirumengya.endless_deep_space.custom.client.gui.CustomBossBar;
import shirumengya.endless_deep_space.custom.config.ModClientConfig;

import java.util.Map;
import java.util.UUID;

@Mixin({BossHealthOverlay.class})
public abstract class BossHealthOverlayMixin {
	@Shadow @Final private Map<UUID, LerpingBossEvent> events;

	@Shadow @Final private Minecraft minecraft;

	public BossHealthOverlayMixin() {
		
	}

	@Inject(method = {"render"}, at = {@At("HEAD")}, cancellable = true)
	public void render(GuiGraphics p_283175_, CallbackInfo ci) {
		if (ModClientConfig.CUSTOM_BOSSBAR.get()) {
			if (!this.events.isEmpty()) {
				int i = p_283175_.guiWidth();
				int j = 12;

				for(LerpingBossEvent lerpingbossevent : this.events.values()) {
					int k = i / 2 - 91;
					var event = net.minecraftforge.client.ForgeHooksClient.onCustomizeBossEventProgress(p_283175_, this.minecraft.getWindow(), lerpingbossevent, k, j, 10 + this.minecraft.font.lineHeight);
					if (!event.isCanceled()) {
						CustomBossBar customBossBar = CustomBossBar.customBossBars.get(-2);
						customBossBar.renderBossBar(event, Component.empty(), -2);
					}
					j += event.getIncrement();
					if (j >= p_283175_.guiHeight() / 3) {
						break;
					}
				}
			}
			ci.cancel();
		}
	}

	@Inject(method = {"update"}, at = {@At("HEAD")}, cancellable = true)
	public void update(ClientboundBossEventPacket p_93712_, CallbackInfo ci) {
		p_93712_.dispatch(new ClientboundBossEventPacket.Handler() {
			public void add(UUID p_168824_, Component p_168825_, float p_168826_, BossEvent.BossBarColor p_168827_, BossEvent.BossBarOverlay p_168828_, boolean p_168829_, boolean p_168830_, boolean p_168831_) {
				BossHealthOverlayMixin.this.events.put(p_168824_, new LerpingBossEvent(p_168824_, p_168825_, p_168826_, p_168827_, p_168828_, p_168829_, p_168830_, p_168831_));
				CustomBossBar.customBossBarsLastProgress.put(p_168824_, 0.0D);
				CustomBossBar.customBossBarsLastBufferTime.put(p_168824_, 0.0D);
			}

			public void remove(UUID p_168812_) {
				BossHealthOverlayMixin.this.events.remove(p_168812_);
				CustomBossBar.customBossBarsLastProgress.remove(p_168812_);
				CustomBossBar.customBossBarsLastBufferTime.remove(p_168812_);
			}

			public void updateProgress(UUID p_168814_, float p_168815_) {
				BossHealthOverlayMixin.this.events.get(p_168814_).setProgress(p_168815_);
			}

			public void updateName(UUID p_168821_, Component p_168822_) {
				BossHealthOverlayMixin.this.events.get(p_168821_).setName(p_168822_);
			}

			public void updateStyle(UUID p_168817_, BossEvent.BossBarColor p_168818_, BossEvent.BossBarOverlay p_168819_) {
				LerpingBossEvent lerpingbossevent = BossHealthOverlayMixin.this.events.get(p_168817_);
				lerpingbossevent.setColor(p_168818_);
				lerpingbossevent.setOverlay(p_168819_);
			}

			public void updateProperties(UUID p_168833_, boolean p_168834_, boolean p_168835_, boolean p_168836_) {
				LerpingBossEvent lerpingbossevent = BossHealthOverlayMixin.this.events.get(p_168833_);
				lerpingbossevent.setDarkenScreen(p_168834_);
				lerpingbossevent.setPlayBossMusic(p_168835_);
				lerpingbossevent.setCreateWorldFog(p_168836_);
			}
		});

		ci.cancel();
	}

	@Inject(method = {"reset"}, at = {@At("HEAD")}, cancellable = true)
	public void reset(CallbackInfo ci) {
		this.events.clear();
		CustomBossBar.customBossBarsLastProgress.clear();
		CustomBossBar.customBossBarsLastBufferTime.clear();

		ci.cancel();
	}
}
