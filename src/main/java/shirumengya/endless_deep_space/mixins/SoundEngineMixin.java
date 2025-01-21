package shirumengya.endless_deep_space.mixins;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.audio.Channel;
import com.mojang.blaze3d.audio.Library;
import com.mojang.blaze3d.audio.Listener;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Options;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundEngineExecutor;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shirumengya.endless_deep_space.custom.client.sounds.InteractiveMusicTickableSoundInstance;
import shirumengya.endless_deep_space.custom.client.sounds.LoopTickableSoundInstance;
import shirumengya.endless_deep_space.custom.client.sounds.ModMusicManager;
import shirumengya.endless_deep_space.custom.init.ModSounds;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Mixin({SoundEngine.class})
public abstract class SoundEngineMixin {
	@Shadow
	private int tickCount;
	@Final
	@Shadow
	private static Logger LOGGER = LogUtils.getLogger();
	@Final
	@Shadow
	private List<TickableSoundInstance> queuedTickableSounds;
	@Final
	@Shadow
	private List<TickableSoundInstance> tickingSounds;
	@Final
	@Shadow
	private Map<SoundInstance, ChannelAccess.ChannelHandle> instanceToChannel;

	@Shadow
	protected abstract float calculateVolume(SoundInstance p_120328_);

	@Shadow
	protected abstract float calculatePitch(SoundInstance p_120325_);

	@Final
	@Shadow
	private Options options;
	@Final
	@Shadow
	private Map<SoundInstance, Integer> soundDeleteTime = Maps.newHashMap();
	@Final
	@Shadow
	private Map<SoundInstance, Integer> queuedSounds = Maps.newHashMap();
	@Final
	@Shadow
	private static Marker MARKER = MarkerFactory.getMarker("SOUNDS");
	@Final
	@Shadow
	private Multimap<SoundSource, SoundInstance> instanceBySource = HashMultimap.create();
	@Shadow
	private boolean loaded;
	@Final
	@Shadow
	private ChannelAccess channelAccess = new ChannelAccess(this.library, this.executor);
	@Final
	@Shadow
	private Library library;
	@Final
	@Shadow
	private SoundEngineExecutor executor;

	@Shadow @Final private Listener listener;

	@Shadow protected abstract float getVolume(@Nullable SoundSource p_120259_);

	public SoundEngineMixin() {

	}

	@ModifyVariable(method = "tick", at = @At("HEAD"), argsOnly = true)
	public boolean battleMusicPlayingTick(boolean pause) {
		return false;
	}

	private static boolean shouldLoopManually(SoundInstance p_120319_) {
		return p_120319_.isLooping() && requiresManualLooping(p_120319_);
	}

	private static boolean requiresManualLooping(SoundInstance p_120316_) {
		return p_120316_.getDelay() > 0;
	}

	@Inject(method = "tickNonPaused", at = @At("HEAD"), cancellable = true)
	private void tick(CallbackInfo ci) {
		SoundEngine engine = ((SoundEngine) (Object) this);
		++this.tickCount;
		this.queuedTickableSounds.stream().filter(SoundInstance::canPlaySound).forEach(engine::play);
		this.queuedTickableSounds.clear();

		for (TickableSoundInstance tickablesoundinstance : this.tickingSounds) {
			if (!tickablesoundinstance.canPlaySound()) {
				engine.stop(tickablesoundinstance);
			}

			tickablesoundinstance.tick();
			if (tickablesoundinstance.isStopped()) {
				engine.stop(tickablesoundinstance);
			} else {
				float f = this.calculateVolume(tickablesoundinstance);
				float f1 = this.calculatePitch(tickablesoundinstance);
				Vec3 vec3 = new Vec3(tickablesoundinstance.getX(), tickablesoundinstance.getY(), tickablesoundinstance.getZ());
				ChannelAccess.ChannelHandle channelaccess$channelhandle = this.instanceToChannel.get(tickablesoundinstance);
				if (channelaccess$channelhandle != null) {
					channelaccess$channelhandle.execute((p_194478_) -> {
						if (ModMusicManager.incandescentOdeOfResurrection && (tickablesoundinstance.getSource() == SoundSource.MUSIC || tickablesoundinstance.getSource() == SoundSource.RECORDS)) {
							if (tickablesoundinstance instanceof LoopTickableSoundInstance loopTickableSoundInstance) {
								if (loopTickableSoundInstance.getLocation().equals(ModSounds.MUSIC_INCANDESCENT_ODE_OF_RESURRECTION.get().getLocation()) || loopTickableSoundInstance.isStopping()) {
									p_194478_.setVolume(Math.max(f / 4.0F, Math.min(ModMusicManager.pauseDelay / 40.0F, f)));
								} else {
									p_194478_.setVolume(0.001F);
								}
							} else {
								p_194478_.setVolume(0.001F);
							}
						} else {
							if (tickablesoundinstance instanceof LoopTickableSoundInstance) {
								p_194478_.setVolume(Math.max(f / 4.0F, Math.min(ModMusicManager.pauseDelay / 40.0F, f)));
							} else {
								p_194478_.setVolume(f);
							}
						}
						p_194478_.setPitch(f1);
						p_194478_.setSelfPosition(vec3);
					});
				}
			}
		}

		Iterator<Map.Entry<SoundInstance, ChannelAccess.ChannelHandle>> iterator = this.instanceToChannel.entrySet().iterator();

		while (iterator.hasNext()) {
			Map.Entry<SoundInstance, ChannelAccess.ChannelHandle> entry = iterator.next();
			ChannelAccess.ChannelHandle channelaccess$channelhandle1 = entry.getValue();
			SoundInstance soundinstance = entry.getKey();
			float f2 = this.options.getSoundSourceVolume(soundinstance.getSource());
			if (f2 <= 0.0F) {
				channelaccess$channelhandle1.execute(Channel::stop);
				iterator.remove();
			} else if (channelaccess$channelhandle1.isStopped()) {
				int i = this.soundDeleteTime.get(soundinstance);
				if (i <= this.tickCount) {
					if (shouldLoopManually(soundinstance)) {
						this.queuedSounds.put(soundinstance, this.tickCount + soundinstance.getDelay());
					}

					iterator.remove();
					LOGGER.debug(MARKER, "Removed channel {} because it's not playing anymore", (Object) channelaccess$channelhandle1);
					this.soundDeleteTime.remove(soundinstance);

					try {
						this.instanceBySource.remove(soundinstance.getSource(), soundinstance);
					} catch (RuntimeException runtimeexception) {
					}

					if (soundinstance instanceof TickableSoundInstance) {
						this.tickingSounds.remove(soundinstance);
					}
				}
			}
		}

		Iterator<Map.Entry<SoundInstance, Integer>> iterator1 = this.queuedSounds.entrySet().iterator();

		while (iterator1.hasNext()) {
			Map.Entry<SoundInstance, Integer> entry1 = iterator1.next();
			if (this.tickCount >= entry1.getValue()) {
				SoundInstance soundinstance1 = entry1.getKey();
				if (soundinstance1 instanceof TickableSoundInstance) {
					((TickableSoundInstance) soundinstance1).tick();
				}

				engine.play(soundinstance1);
				iterator1.remove();
			}
		}

		ci.cancel();
	}

	@Inject(method = "resume", at = @At("HEAD"), cancellable = true)
	private void resume(CallbackInfo ci) {
		if (this.loaded) {
			this.channelAccess.executeOnChannels((p_194508_) -> {
				p_194508_.forEach(Channel::unpause);
			});
		}

		ci.cancel();
	}

	@Inject(method = "updateCategoryVolume", at = @At("HEAD"), cancellable = true)
	public void updateCategoryVolume(SoundSource p_120261_, float p_120262_, CallbackInfo ci) {
		if (this.loaded) {
			if (p_120261_ == SoundSource.MASTER) {
				this.listener.setGain(p_120262_);
			} else {
				this.instanceToChannel.forEach((p_120280_, p_120281_) -> {
					float f = this.calculateVolume(p_120280_);
					p_120281_.execute((p_174990_) -> {
						if (f <= 0.0F) {
							if (p_120280_ instanceof InteractiveMusicTickableSoundInstance interactiveMusicTickableSoundInstance && !interactiveMusicTickableSoundInstance.isStopped && this.getVolume(SoundSource.MUSIC) > 0) {
								p_174990_.setVolume(f);
							} else {
								p_174990_.stop();
							}
						} else {
							if (p_120280_ instanceof LoopTickableSoundInstance) {
								p_174990_.setVolume(Math.max(f / 4.0F, Math.min(ModMusicManager.pauseDelay / 40.0F, f)));
							} else {
								p_174990_.setVolume(f);
							}
						}

					});
				});
			}
		}

		ci.cancel();
	}

	/*@Redirect(method = "shouldChangeDevice", at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;getMillis()J"))
	private long getMillis() {
		return Util.getNanos() / 1000000L;
	}*/
}
