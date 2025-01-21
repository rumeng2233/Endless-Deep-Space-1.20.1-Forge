package shirumengya.endless_deep_space.custom.client.sounds;

import com.mojang.logging.LogUtils;
import net.minecraft.Optionull;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.warden.AngerLevel;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import shirumengya.endless_deep_space.EndlessDeepSpaceMod;
import shirumengya.endless_deep_space.custom.IncompatibleModsException;
import shirumengya.endless_deep_space.custom.client.gui.CustomBossBar;
import shirumengya.endless_deep_space.custom.client.gui.overlay.JumpStringOverlay;
import shirumengya.endless_deep_space.custom.client.gui.screens.EndlessDeepSpaceCredits;
import shirumengya.endless_deep_space.custom.client.renderer.ModRenderType;
import shirumengya.endless_deep_space.custom.client.renderer.item.ModItemRenderer;
import shirumengya.endless_deep_space.custom.config.ModClientConfig;
import shirumengya.endless_deep_space.custom.entity.boss.enderlord.EnderLord;
import shirumengya.endless_deep_space.custom.entity.boss.enderlord.phases.EnderDragonPhase;
import shirumengya.endless_deep_space.custom.entity.boss.oceandefenders.OceanDefender;
import shirumengya.endless_deep_space.custom.entity.miniboss.MutationRavager;
import shirumengya.endless_deep_space.custom.init.ModSounds;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@OnlyIn(Dist.CLIENT)
public class ModMusicManager {
	public static final Minecraft minecraft = Minecraft.getInstance();
	@Nullable
	public static LoopTickableSoundInstance currentMusic;
	public static boolean inBattle;
	public static int recordDelay;
	public static int raidDelay;
	public static long lastMillis;
	public static long EndlessDeepSpaceModClientSideTickLastMillis;
	public static long EndlessDeepSpaceModClientSideRenderTypeTickLastMillis;
	public static long EndlessDeepSpaceModClientSideJumpStringOverlayTickLastMillis;
	public static int interactiveMusicNumber;
	public static boolean interactiveMusicPlayOne;
	public static boolean interactiveMusic;
	@Nullable
	public static InteractiveMusicTickableSoundInstance musicOne;
	@Nullable
	public static InteractiveMusicTickableSoundInstance musicTwo;
	public static final Logger LOGGER = LogUtils.getLogger();
	@Nullable
	public static String musicHandedMod;
	public static boolean seenCredits;
	public static int pauseDelay;
	public static boolean incandescentOdeOfResurrection;

	public static long getMillis() {
		return Util.getNanos() / 1000000L;
	}

	public static void EndlessDeepSpaceModClientSideTick() {
		Minecraft.getInstance().getProfiler().push("MusicManagerTick");
		ModMusicManager.tick();
		Minecraft.getInstance().getProfiler().pop();

		Minecraft.getInstance().getProfiler().push("RenderTypeTimerTick");
		ModRenderType.timer.advanceTime(getMillis());
		Minecraft.getInstance().getProfiler().pop();

		Minecraft.getInstance().getProfiler().push("RenderTypeTick");
		ModRenderType.tick();
		Minecraft.getInstance().getProfiler().pop();

		Minecraft.getInstance().getProfiler().push("JumpStringOverlayTimerTick");
		JumpStringOverlay.timer.advanceTime(getMillis());
		Minecraft.getInstance().getProfiler().pop();

		Minecraft.getInstance().getProfiler().push("60fpsSpeedTick");
		if (getMillis() - EndlessDeepSpaceModClientSideJumpStringOverlayTickLastMillis >= 16) {
			Minecraft.getInstance().getProfiler().push("JumpStringOverlayTick");
			JumpStringOverlay.updateAllJumpString();
			Minecraft.getInstance().getProfiler().pop();

			Minecraft.getInstance().getProfiler().push("CustomBossBarTick");
			CustomBossBar.renderTime = CustomBossBar.renderTime + 4L;
			Minecraft.getInstance().getProfiler().pop();

			Minecraft.getInstance().getProfiler().push("60fpsSpeedTickUpdate");
			EndlessDeepSpaceModClientSideJumpStringOverlayTickLastMillis = getMillis();
			Minecraft.getInstance().getProfiler().pop();
		}
		Minecraft.getInstance().getProfiler().pop();

		Minecraft.getInstance().getProfiler().push("30fpsSpeedTick");
		if (getMillis() - EndlessDeepSpaceModClientSideRenderTypeTickLastMillis >= 33) {
			Minecraft.getInstance().getProfiler().push("RenderTypeTick");
			ModRenderType.gameTime = ModRenderType.gameTime + 1L;
			Minecraft.getInstance().getProfiler().pop();

			Minecraft.getInstance().getProfiler().push("30fpsSpeedTickUpdate");
			EndlessDeepSpaceModClientSideRenderTypeTickLastMillis = getMillis();
			Minecraft.getInstance().getProfiler().pop();
		}
		Minecraft.getInstance().getProfiler().pop();

		Minecraft.getInstance().getProfiler().push("GameTickSpeedTick");
		if (getMillis() - EndlessDeepSpaceModClientSideTickLastMillis >= 50) {
			Minecraft.getInstance().getProfiler().push("ItemRendererTickUpdate");
			ModItemRenderer.ticksExisted++;
			Minecraft.getInstance().getProfiler().pop();

			Minecraft.getInstance().getProfiler().push("SeenCreditsUpdate");
			if (Minecraft.getInstance().player == null) {
				seenCredits = false;
			}
			Minecraft.getInstance().getProfiler().pop();

			Minecraft.getInstance().getProfiler().push("LoopTickableSoundInstanceUpdate");
			if (ModClientConfig.PAUSE_SOUND.get() && Minecraft.getInstance().isPaused() && Minecraft.getInstance().player != null && (Optionull.map(Minecraft.getInstance().screen, Screen::getBackgroundMusic) == null || !(Minecraft.getInstance().screen instanceof EndlessDeepSpaceCredits))) {
				if (pauseDelay > 0) {
					pauseDelay--;
				} else {
					pauseDelay = 0;
				}
			} else {
				if (pauseDelay < 40) {
					pauseDelay++;
				} else {
					pauseDelay = 40;
				}
			}
			Minecraft.getInstance().getProfiler().pop();

			Minecraft.getInstance().getProfiler().push("GameTickSpeedTickUpdate");
			EndlessDeepSpaceModClientSideTickLastMillis = getMillis();
			Minecraft.getInstance().getProfiler().pop();
		}
		Minecraft.getInstance().getProfiler().pop();

		Minecraft.getInstance().getProfiler().push("IncompatibleModsCheck");
		for (String mod : EndlessDeepSpaceMod.incompatibleMods) {
			if (ModList.get().isLoaded(mod)) {
				StringBuilder errorMessage = new StringBuilder("Incompatible Mods: ");
				for (String modId : EndlessDeepSpaceMod.incompatibleMods) {
					errorMessage.append(ModList.get().getModContainerById(modId).get().getModInfo().getDisplayName()).append("(").append(modId).append(")").append(modId.equals(EndlessDeepSpaceMod.incompatibleMods.get(EndlessDeepSpaceMod.incompatibleMods.size() - 1)) ? "" : ", ");
				}
				throw new IncompatibleModsException(errorMessage.toString());
			}
		}
		Minecraft.getInstance().getProfiler().pop();
	}

	public static void interactiveMusic() {
		SoundEvent music1 = getInteractiveMusic(interactiveMusicNumber).get(0);
		SoundEvent music2 = getInteractiveMusic(interactiveMusicNumber).get(1);

        if (musicOne != null && musicTwo != null) {
			if (interactiveMusicPlayOne) {
				if (!musicTwo.isStopping()) {
					musicTwo.stopSound();
					musicOne.continueSound();
				}
			} else {
				if (!musicOne.isStopping()) {
					musicOne.stopSound();
					musicTwo.continueSound();
				}
			}

			if (!music1.getLocation().equals(musicOne.getLocation())) {
				musicOne.stopMusic();
			}

			if (!music2.getLocation().equals(musicTwo.getLocation())) {
				musicTwo.stopMusic();
			}

			if (!minecraft.getSoundManager().isActive(musicOne)) {
				musicOne = null;
			}

			if (!minecraft.getSoundManager().isActive(musicTwo)) {
				musicTwo = null;
			}

		} else {
			if (musicOne != null) {
				musicOne.stopMusic();
			}
			if (musicTwo != null) {
				musicTwo.stopMusic();
			}
			musicOne = new InteractiveMusicTickableSoundInstance(music1, SoundSource.MUSIC, true);
			musicTwo = new InteractiveMusicTickableSoundInstance(music2, SoundSource.MUSIC, true);
			minecraft.getSoundManager().play(musicOne);
			minecraft.getSoundManager().play(musicTwo);
		}

	}

	public static void tick() {
		if (ModClientConfig.BATTLE_MUSIC.get()) {
			Minecraft.getInstance().getProfiler().push("GetBattleMusic");
			SoundEvent battleMusic = getBattleMusic();
			inBattle = (battleMusic != null);
			SoundEvent music = inBattle ? battleMusic : minecraft.getSituationalMusic().getEvent().value();
			Minecraft.getInstance().getProfiler().pop();
			interactiveMusicNumber = getInteractiveMusicNumber();

			if (!minecraft.isPaused() && (getMillis() - lastMillis >= 50)) {
				if (recordDelay > 0) {
					recordDelay--;
				}

				if (raidDelay > 0) {
					raidDelay--;
				}

				lastMillis = getMillis();
			}

			if (interactiveMusicNumber != -1 && !getInteractiveMusic(interactiveMusicNumber).isEmpty() && battleMusic != null && battleMusic == ModSounds.EMPTY.get()) {
				Minecraft.getInstance().getProfiler().push("InteractiveMusicTick");
				interactiveMusic = true;
				interactiveMusic();
				recordDelay = 0;
				raidDelay = 0;
				if (currentMusic != null) {
					currentMusic.stopSound();
				}

				Minecraft.getInstance().getProfiler().pop();
			} else {
				Minecraft.getInstance().getProfiler().push("MusicTick");
				interactiveMusicPlayOne = true;
				interactiveMusic = false;
				if (musicOne != null) {
					musicOne.stopMusic();
				}
				if (musicTwo != null) {
					musicTwo.stopMusic();
				}

				if (currentMusic != null) {
					if (!music.getLocation().equals(currentMusic.getLocation()) || (!inBattle && recordDelay > 0)) {
						currentMusic.stopSound();
					} else {
						if (currentMusic.isStopping()) {
							currentMusic.continueSound();
						}
					}

					if (!minecraft.getSoundManager().isActive(currentMusic)) {
						currentMusic = null;
					}

				}

				if (currentMusic == null || currentMusic.isStopped()) {
					if (inBattle) {
						startPlaying(music);
					} else if (recordDelay <= 0) {
						startPlaying(music);
					}
				}
				Minecraft.getInstance().getProfiler().pop();
			}
		} else {
			inBattle = false;
			recordDelay = 0;
			raidDelay = 0;
			interactiveMusicPlayOne = true;
			if (currentMusic != null) {
				currentMusic.stopSound();
			}
			if (musicOne != null) {
				musicOne.stopMusic();
			}
			if (musicTwo != null) {
				musicTwo.stopMusic();
			}
		}
	}

	public static void startPlaying(SoundEvent p_120185_) {
		currentMusic = new LoopTickableSoundInstance(p_120185_, SoundSource.MUSIC, inBattle);
		if (currentMusic.getSound() != SoundManager.EMPTY_SOUND) {
			minecraft.getSoundManager().play(currentMusic);
		}
	}

	public static void stopPlaying(SoundEvent p_278295_) {
		if (isPlayingMusic(p_278295_)) {
			stopPlaying();
		}

	}

	public static void stopPlaying() {
		if (currentMusic != null) {
			currentMusic.stopSound();
			currentMusic = null;
		}
	}

	public static boolean isPlayingMusic(SoundEvent p_120188_) {
		return currentMusic == null ? false : p_120188_.getLocation().equals(currentMusic.getLocation());
	}

	public static int getInteractiveMusicNumber() {
		if (Minecraft.getInstance().player != null) {
			BlockPos pos = Minecraft.getInstance().gameRenderer.getMainCamera().getBlockPosition();
			if (!Minecraft.getInstance().player.level().getEntitiesOfClass(OceanDefender.class, new AABB(pos, pos).inflate(8192 / 2d), e -> true).isEmpty()) {
				List<OceanDefender> entities = Minecraft.getInstance().player.level().getEntitiesOfClass(OceanDefender.class, new AABB(pos, pos).inflate(8192 / 2d), e -> true);
				for (OceanDefender entity : entities) {
					if (entity.getOtherOceanDefender() != null) {
						OceanDefender defenderOne = entity;
						OceanDefender defenderTwo = entity.getOtherOceanDefender();
						if ((defenderOne.getPhase() == OceanDefender.PHASE_SHOOTING_ABYSSAL_TORPEDO && defenderTwo.getPhase() == OceanDefender.PHASE_SHOOTING_ABYSSAL_TORPEDO) || (defenderOne.getChargingTimer() >= 100 && defenderTwo.getChargingTimer() >= 100)) {
							interactiveMusicPlayOne = false;
						} else {
							interactiveMusicPlayOne = true;
						}
					} else {
						if (entity.getPhase() == OceanDefender.PHASE_SHOOTING_ABYSSAL_TORPEDO || entity.getChargingTimer() >= 100) {
							interactiveMusicPlayOne = false;
						} else {
							interactiveMusicPlayOne = true;
						}
					}
				}
				
				return 4;
			}
			
			if (!Minecraft.getInstance().player.level().getEntitiesOfClass(EnderLord.class, new AABB(pos, pos).inflate(8192 / 2d), e -> true).isEmpty()) {
				List<EnderLord> entities = Minecraft.getInstance().player.level().getEntitiesOfClass(EnderLord.class, new AABB(pos, pos).inflate(8192 / 2d), e -> true);
				for (EnderLord entity : entities) {
					if (entity.getPhaseManager() != null && entity.getPhaseManager().getCurrentPhase() != null) {
						if (entity.getProgressTwo()) {
							if (entity.getPhaseManager().getCurrentPhase().getPhase() == EnderDragonPhase.TRACKING_ENTITY || entity.getPhaseManager().getCurrentPhase().getPhase() == EnderDragonPhase.CLUSTER_STRAFE_ENTITY || entity.getPhaseManager().getCurrentPhase().getPhase() == EnderDragonPhase.EXPLOSION || entity.getChargingTime() > 0) {
								interactiveMusicPlayOne = false;
							} else {
								interactiveMusicPlayOne = true;
							}

							return 2;
						}

						if (entity.getPhaseManager().getCurrentPhase().getPhase() == EnderDragonPhase.TRACKING_ENTITY || entity.getPhaseManager().getCurrentPhase().getPhase() == EnderDragonPhase.CLUSTER_STRAFE_ENTITY || entity.getPhaseManager().getCurrentPhase().getPhase() == EnderDragonPhase.EXPLOSION || entity.getChargingTime() > 0) {
							interactiveMusicPlayOne = false;
						} else {
							interactiveMusicPlayOne = true;
						}

						return 1;
					}
				}
			}

			if (!Minecraft.getInstance().player.level().getEntitiesOfClass(WitherBoss.class, new AABB(pos, pos).inflate(8192 / 2d), e -> true).isEmpty()) {
				List<WitherBoss> entities = Minecraft.getInstance().player.level().getEntitiesOfClass(WitherBoss.class, new AABB(pos, pos).inflate(8192 / 2d), e -> true);
				for (WitherBoss entity : entities) {
					if (entity.isPowered() && entity.getInvulnerableTicks() <= 0) {
						interactiveMusicPlayOne = false;
					} else {
						interactiveMusicPlayOne = true;
					}
				}

				return 0;
			}

			if (!Minecraft.getInstance().player.level().getEntitiesOfClass(Warden.class, new AABB(pos, pos).inflate(8192 / 2d), e -> true).isEmpty()) {
				List<Warden> entities = Minecraft.getInstance().player.level().getEntitiesOfClass(Warden.class, new AABB(pos, pos).inflate(8192 / 2d), e -> true);
				for (Warden entity : entities) {
					if (entity.getClientAngerLevel() >= AngerLevel.ANGRY.getMinimumAnger()) {
						interactiveMusicPlayOne = false;
					} else {
						interactiveMusicPlayOne = true;
					}
				}

				return 3;
			}
		}

		return -1;
	}

	public static List<SoundEvent> getInteractiveMusic(int number) {
		switch (number) {
			case 0 -> {
				return Arrays.asList(ModSounds.MUSIC_NARCISSUS_WITHOUT_WATER.get(), ModSounds.MUSIC_ORDEALS_RITUALS_LAWS.get());
			}
			case 1 -> {
				return Arrays.asList(ModSounds.MUSIC_FLICKERING_BRIGHTNESS.get(), ModSounds.MUSIC_BURST_INTO_FLAMES.get());
			}
			case 2 -> {
				return Arrays.asList(ModSounds.MUSIC_THUNDERINGS_OF_THE_MERCILESS.get(), ModSounds.MUSIC_THE_ALMIGHTY_VIOLET_THUNDER.get());
			}
			case 3 -> {
				return Arrays.asList(ModSounds.MUSIC_REALM_OF_ETHEREAL_MURK.get(), ModSounds.MUSIC_GRIM_IS_THE_NIGHT.get());
			}
			case 4 -> {
				return Arrays.asList(ModSounds.MUSIC_BEATS_OF_WATER_DROPS.get(), ModSounds.MUSIC_NO_TURNING_BACK.get());
			}
			default -> {
				return Arrays.asList(ModSounds.MUSIC_INCANDESCENT_ODE_OF_RESURRECTION.get(), ModSounds.MUSIC_INCANDESCENT_ODE_OF_RESURRECTION.get());
			}
		}
	}

	@Nullable
	public static SoundEvent getBattleMusic() {
		musicHandedMod = null;
		incandescentOdeOfResurrection = false;
		
		if (Minecraft.getInstance().player != null) {
			BlockPos pos = Minecraft.getInstance().gameRenderer.getMainCamera().getBlockPosition();
			if (!Minecraft.getInstance().player.level().getEntitiesOfClass(OceanDefender.class, new AABB(pos, pos).inflate(8192 / 2d), e -> true).isEmpty()) {
				List<OceanDefender> entities = Minecraft.getInstance().player.level().getEntitiesOfClass(OceanDefender.class, new AABB(pos, pos).inflate(8192 / 2d), e -> true);
				for (OceanDefender entity : entities) {
					if (entity.getOtherOceanDefender() != null) {
						OceanDefender defenderOne = entity;
						OceanDefender defenderTwo = entity.getOtherOceanDefender();
						if (defenderOne.isProgressTwo() && defenderTwo.isProgressTwo()) {
							incandescentOdeOfResurrection = true;
							return ModSounds.MUSIC_INCANDESCENT_ODE_OF_RESURRECTION.get();
						}
					} else {
						if (entity.isProgressTwo()) {
							incandescentOdeOfResurrection = true;
							return ModSounds.MUSIC_INCANDESCENT_ODE_OF_RESURRECTION.get();
						}
					}
				}
			}
		}

		if (Minecraft.getInstance().screen instanceof EndlessDeepSpaceCredits) {
			return ((EndlessDeepSpaceCredits)Minecraft.getInstance().screen).chapter.getMusic();
		}

		if (Minecraft.getInstance().player != null) {
			BlockPos pos = Minecraft.getInstance().gameRenderer.getMainCamera().getBlockPosition();

			if (ModList.get().isLoaded("witherstormmod")) {
				EntityType<?> witherStormEntityType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("witherstormmod:wither_storm"));
				if (witherStormEntityType != null && !Minecraft.getInstance().player.level().getEntities(witherStormEntityType, new AABB(pos, pos).inflate(8192 / 2d), e -> true).isEmpty()) {
					musicHandedMod = ModList.get().getModContainerById("witherstormmod").get().getModInfo().getDisplayName();
					return SoundEvents.EMPTY;
				}
			}
			
			if (!Minecraft.getInstance().player.level().getEntitiesOfClass(OceanDefender.class, new AABB(pos, pos).inflate(8192 / 2d), e -> true).isEmpty()) {
				List<OceanDefender> entities = Minecraft.getInstance().player.level().getEntitiesOfClass(OceanDefender.class, new AABB(pos, pos).inflate(8192 / 2d), e -> true);
				for (OceanDefender entity : entities) {
					if (entity.getOtherOceanDefender() != null) {
						OceanDefender defenderOne = entity;
						OceanDefender defenderTwo = entity.getOtherOceanDefender();
						if (!defenderOne.isProgressTwo() || !defenderTwo.isProgressTwo()) {
							return ModSounds.EMPTY.get();
						}
					} else {
						if (!entity.isProgressTwo()) {
							return ModSounds.EMPTY.get();
						}
					}
				}
			}

			if (!Minecraft.getInstance().player.level().getEntitiesOfClass(EnderLord.class, new AABB(pos, pos).inflate(8192 / 2d), e -> true).isEmpty()) {
				return ModSounds.EMPTY.get();
			}

			if (ModList.get().isLoaded("cataclysm")) {
				EntityType<?> ancientRemnantEntityType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("cataclysm:ancient_remnant"));
				if (ancientRemnantEntityType != null && !Minecraft.getInstance().player.level().getEntities(ancientRemnantEntityType, new AABB(pos, pos).inflate(8192 / 2d), e -> true).isEmpty()) {
					musicHandedMod = ModList.get().getModContainerById("cataclysm").get().getModInfo().getDisplayName();
					return SoundEvents.EMPTY;
				}

				EntityType<?> theLeviathanEntityType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("cataclysm:the_leviathan"));
				if (theLeviathanEntityType != null && !Minecraft.getInstance().player.level().getEntities(theLeviathanEntityType, new AABB(pos, pos).inflate(8192 / 2d), e -> true).isEmpty()) {
					musicHandedMod = ModList.get().getModContainerById("cataclysm").get().getModInfo().getDisplayName();
					return SoundEvents.EMPTY;
				}

				EntityType<?> enderGuardianEntityType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("cataclysm:ender_guardian"));
				if (enderGuardianEntityType != null && !Minecraft.getInstance().player.level().getEntities(enderGuardianEntityType, new AABB(pos, pos).inflate(8192 / 2d), e -> true).isEmpty()) {
					musicHandedMod = ModList.get().getModContainerById("cataclysm").get().getModInfo().getDisplayName();
					return SoundEvents.EMPTY;
				}

				EntityType<?> ignisEntityType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("cataclysm:ignis"));
				if (ignisEntityType != null && !Minecraft.getInstance().player.level().getEntities(ignisEntityType, new AABB(pos, pos).inflate(8192 / 2d), e -> true).isEmpty()) {
					musicHandedMod = ModList.get().getModContainerById("cataclysm").get().getModInfo().getDisplayName();
					return SoundEvents.EMPTY;
				}

				EntityType<?> netheriteMonstrosityEntityType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("cataclysm:netherite_monstrosity"));
				if (netheriteMonstrosityEntityType != null && !Minecraft.getInstance().player.level().getEntities(netheriteMonstrosityEntityType, new AABB(pos, pos).inflate(8192 / 2d), e -> true).isEmpty()) {
					musicHandedMod = ModList.get().getModContainerById("cataclysm").get().getModInfo().getDisplayName();
					return SoundEvents.EMPTY;
				}

				EntityType<?> maledictusEntityType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("cataclysm:maledictus"));
				if (maledictusEntityType != null && !Minecraft.getInstance().player.level().getEntities(maledictusEntityType, new AABB(pos, pos).inflate(8192 / 2d), e -> true).isEmpty()) {
					musicHandedMod = ModList.get().getModContainerById("cataclysm").get().getModInfo().getDisplayName();
					return SoundEvents.EMPTY;
				}

				EntityType<?> theHarbingerEntityType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("cataclysm:the_harbinger"));
				if (theHarbingerEntityType != null && !Minecraft.getInstance().player.level().getEntities(theHarbingerEntityType, new AABB(pos, pos).inflate(8192 / 2d), e -> true).isEmpty()) {
					musicHandedMod = ModList.get().getModContainerById("cataclysm").get().getModInfo().getDisplayName();
					return SoundEvents.EMPTY;
				}
			}

			if (!Minecraft.getInstance().player.level().getEntitiesOfClass(EnderDragon.class, new AABB(pos, pos).inflate(8192 / 2d), e -> true).isEmpty()
					|| !Minecraft.getInstance().player.level().getEntitiesOfClass(shirumengya.endless_deep_space.custom.entity.boss.enderdragon.EnderDragon.class, new AABB(pos, pos).inflate(8192 / 2d), e -> true).isEmpty()) {
				return ModSounds.MUSIC_THE_EPITOME_OF_YOUNG_MORALS.get();
			}

			if (!Minecraft.getInstance().player.level().getEntitiesOfClass(WitherBoss.class, new AABB(pos, pos).inflate(8192 / 2d), e -> true).isEmpty()) {
				return ModSounds.EMPTY.get();
			}

			if (!Minecraft.getInstance().player.level().getEntitiesOfClass(Warden.class, new AABB(pos, pos).inflate(8192 / 2d), e -> true).isEmpty()) {
				return ModSounds.EMPTY.get();
			}

			if (!Minecraft.getInstance().player.level().getEntitiesOfClass(MutationRavager.class, new AABB(pos, pos).inflate(192 / 2d), e -> true).isEmpty()) {
				return ModSounds.MUSIC_A_DUEL_OF_CONNECTING_BLADES.get();
			}

			if (raidDelay > 0) {
				return ModSounds.MUSIC_WELKIN_IS_ADVENT.get();
			}

			if (ModList.get().isLoaded("witherstormmod")) {
				if (Minecraft.getInstance().player.level().dimension() == ResourceKey.create(Registries.DIMENSION, new ResourceLocation("witherstormmod:bowels"))) {
					musicHandedMod = ModList.get().getModContainerById("witherstormmod").get().getModInfo().getDisplayName();
					return SoundEvents.EMPTY;
				}
			}

			if (ModList.get().isLoaded("aether")) {
				if (Minecraft.getInstance().player.level().dimension() == ResourceKey.create(Registries.DIMENSION, new ResourceLocation("aether:the_aether"))) {
					musicHandedMod = ModList.get().getModContainerById("aether").get().getModInfo().getDisplayName();
					return SoundEvents.EMPTY;
				}
			}

			if (ModList.get().isLoaded("blue_skies")) {
				if (Minecraft.getInstance().player.level().dimension() == ResourceKey.create(Registries.DIMENSION, new ResourceLocation("blue_skies:everbright"))
						|| Minecraft.getInstance().player.level().dimension() == ResourceKey.create(Registries.DIMENSION, new ResourceLocation("blue_skies:everdawn"))) {
					musicHandedMod = ModList.get().getModContainerById("blue_skies").get().getModInfo().getDisplayName();
					return SoundEvents.EMPTY;
				}
			}
		}
		
		return null;
	}

	@OnlyIn(Dist.CLIENT)
	public static class AllocationRateCalculator {
		private static final int UPDATE_INTERVAL_MS = 500;
		private static final List<GarbageCollectorMXBean> GC_MBEANS = ManagementFactory.getGarbageCollectorMXBeans();
		private long lastTime = 0L;
		private long lastHeapUsage = -1L;
		private long lastGcCounts = -1L;
		private long lastRate = 0L;

		public long bytesAllocatedPerSecond(long p_232517_) {
			long i = System.currentTimeMillis();
			if (i - this.lastTime < 500L) {
				return this.lastRate;
			} else {
				long j = gcCounts();
				if (this.lastTime != 0L && j == this.lastGcCounts) {
					double d0 = (double) TimeUnit.SECONDS.toMillis(1L) / (double)(i - this.lastTime);
					long k = p_232517_ - this.lastHeapUsage;
					this.lastRate = Math.round((double)k * d0);
				}

				this.lastTime = i;
				this.lastHeapUsage = p_232517_;
				this.lastGcCounts = j;
				return this.lastRate;
			}
		}

		private static long gcCounts() {
			long i = 0L;

			for(GarbageCollectorMXBean garbagecollectormxbean : GC_MBEANS) {
				i += garbagecollectormxbean.getCollectionCount();
			}

			return i;
		}
	}
}