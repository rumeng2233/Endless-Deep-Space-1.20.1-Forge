package shirumengya.endless_deep_space.mixins;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import shirumengya.endless_deep_space.EndlessDeepSpaceMod;
import shirumengya.endless_deep_space.custom.client.sounds.ModMusicManager;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Mixin({DebugScreenOverlay.class})
public abstract class DebugScreenOverlayMixin {

	@Shadow @Final private Minecraft minecraft;

	@Shadow protected HitResult block;

	@Shadow protected abstract String getPropertyValueString(Map.Entry<Property<?>, Comparable<?>> p_94072_);

	@Shadow protected HitResult liquid;

    @Shadow
    private static long bytesToMegabytes(long p_94051_) {
        return 0;
    }

    private final ModMusicManager.AllocationRateCalculator allocationRateCalculator = new ModMusicManager.AllocationRateCalculator();

	public DebugScreenOverlayMixin() {
		
	}

	@Inject(method = {"getSystemInformation"}, at = {@At("HEAD")}, cancellable = true)
	public void getSystemInformation(CallbackInfoReturnable<List<String>> ci) {
		long i = Runtime.getRuntime().maxMemory();
		long j = Runtime.getRuntime().totalMemory();
		long k = Runtime.getRuntime().freeMemory();
		long l = j - k;
		List<String> list = Lists.newArrayList(String.format(Locale.ROOT, "Java: %s %dbit", System.getProperty("java.version"), this.minecraft.is64Bit() ? 64 : 32), String.format(Locale.ROOT, "Mem: % 2d%% %03d/%03dMB", l * 100L / i, bytesToMegabytes(l), bytesToMegabytes(i)), String.format(Locale.ROOT, "Allocation rate: %03dMB /s", bytesToMegabytes(this.allocationRateCalculator.bytesAllocatedPerSecond(l))), String.format(Locale.ROOT, "Allocated: % 2d%% %03dMB", j * 100L / i, bytesToMegabytes(j)), "", String.format(Locale.ROOT, "CPU: %s", GlUtil.getCpuInfo()), "", String.format(Locale.ROOT, "Display: %dx%d (%s)", Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight(), GlUtil.getVendor()), GlUtil.getRenderer(), GlUtil.getOpenGLVersion());
		list.add("");
		list.add("Endless Deep Space " + EndlessDeepSpaceMod.MOD_VERSION);
		list.add("RecordDelay: " + ModMusicManager.recordDelay);
		list.add("RaidDelay: " + ModMusicManager.raidDelay);
		list.add("PauseDelay: " + ModMusicManager.pauseDelay);
		list.add("InteractiveMusic: " + ModMusicManager.interactiveMusic);
		if (ModMusicManager.interactiveMusic) {
			list.add("InteractiveMusicPlayOne: " + ModMusicManager.interactiveMusicPlayOne);
			if (this.minecraft.showOnlyReducedInfo()) {
				list.add("InteractiveMusicOne: " + (ModMusicManager.musicOne == null ? "" : ModMusicManager.musicOne.getLocation().getPath()));
				list.add("InteractiveMusicTwo: " + (ModMusicManager.musicTwo == null ? "" : ModMusicManager.musicTwo.getLocation().getPath()));
			} else {
				list.add("InteractiveMusicOne: " + (ModMusicManager.musicOne == null ? "" : ModMusicManager.musicOne.getLocation()));
				if (ModMusicManager.musicOne != null && ModMusicManager.musicOne.fade > 0.0F) {
					list.add("Fade: " + ModMusicManager.musicOne.fade);
					list.add("IsStopping: " + ModMusicManager.musicOne.isStopping());
				}

				list.add("");

				list.add("InteractiveMusicTwo: " + (ModMusicManager.musicTwo == null ? "" : ModMusicManager.musicTwo.getLocation()));
				if (ModMusicManager.musicTwo != null && ModMusicManager.musicTwo.fade > 0.0F) {
					list.add("Fade: " + ModMusicManager.musicTwo.fade);
					list.add("IsStopping: " + ModMusicManager.musicTwo.isStopping());
				}
			}
		} else {
			if (this.minecraft.showOnlyReducedInfo()) {
				list.add("CurrentMusic: " + (ModMusicManager.currentMusic == null ? "" : (ModMusicManager.currentMusic.getLocation().equals(SoundEvents.EMPTY.getLocation()) ? ChatFormatting.UNDERLINE + (ModMusicManager.musicHandedMod == null ? ChatFormatting.YELLOW + Component.translatable("message.endless_deep_space.music_handed.unknown_mod").getString() : ChatFormatting.YELLOW + Component.translatable("message.endless_deep_space.music_handed.known_mod", ModMusicManager.musicHandedMod).getString()) : ModMusicManager.currentMusic.getLocation().getPath())));
			} else {
				list.add("CurrentMusic: " + (ModMusicManager.currentMusic == null ? "" : (ModMusicManager.currentMusic.getLocation().equals(SoundEvents.EMPTY.getLocation()) ? ChatFormatting.UNDERLINE + (ModMusicManager.musicHandedMod == null ? ChatFormatting.YELLOW + Component.translatable("message.endless_deep_space.music_handed.unknown_mod").getString() : ChatFormatting.YELLOW + Component.translatable("message.endless_deep_space.music_handed.known_mod", ModMusicManager.musicHandedMod).getString()) : ModMusicManager.currentMusic.getLocation())));
				if (ModMusicManager.currentMusic != null && ModMusicManager.currentMusic.fade > 0.0F) {
					list.add("Fade: " + ModMusicManager.currentMusic.fade);
					list.add("IsStopping: " + ModMusicManager.currentMusic.isStopping());
				}
			}
		}

		if (this.minecraft.showOnlyReducedInfo()) {
			ci.setReturnValue(list);
		} else {
			if (this.block.getType() == HitResult.Type.BLOCK) {
				BlockPos blockpos = ((BlockHitResult)this.block).getBlockPos();
				BlockState blockstate = this.minecraft.level.getBlockState(blockpos);
				list.add("");
				list.add(ChatFormatting.UNDERLINE + "Targeted Block: " + blockpos.getX() + ", " + blockpos.getY() + ", " + blockpos.getZ());
				list.add(String.valueOf(ForgeRegistries.BLOCKS.getKey(blockstate.getBlock())));

				for(Map.Entry<Property<?>, Comparable<?>> entry : blockstate.getValues().entrySet()) {
					list.add(this.getPropertyValueString(entry));
				}

				blockstate.getTags().map((p_205365_) -> {
					return "#" + p_205365_.location();
				}).forEach(list::add);
			}

			if (this.liquid.getType() == HitResult.Type.BLOCK) {
				BlockPos blockpos1 = ((BlockHitResult)this.liquid).getBlockPos();
				FluidState fluidstate = this.minecraft.level.getFluidState(blockpos1);
				list.add("");
				list.add(ChatFormatting.UNDERLINE + "Targeted Fluid: " + blockpos1.getX() + ", " + blockpos1.getY() + ", " + blockpos1.getZ());
				list.add(String.valueOf(ForgeRegistries.FLUIDS.getKey(fluidstate.getType())));

				for(Map.Entry<Property<?>, Comparable<?>> entry1 : fluidstate.getValues().entrySet()) {
					list.add(this.getPropertyValueString(entry1));
				}

				fluidstate.getTags().map((p_205379_) -> {
					return "#" + p_205379_.location();
				}).forEach(list::add);
			}

			Entity entity = this.minecraft.crosshairPickEntity;
			if (entity != null) {
				list.add("");
				list.add(ChatFormatting.UNDERLINE + "Targeted Entity");
				list.add(String.valueOf(ForgeRegistries.ENTITY_TYPES.getKey(entity.getType())));
				entity.getType().builtInRegistryHolder().tags().forEach(t -> list.add("#" + t.location()));
			}

			ci.setReturnValue(list);
		}
	}
}
