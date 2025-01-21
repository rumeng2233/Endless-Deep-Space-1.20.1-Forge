package shirumengya.endless_deep_space.mixins;

import com.mojang.blaze3d.audio.Channel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.world.entity.Entity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import shirumengya.endless_deep_space.custom.client.sounds.LoopTickableSoundInstance;
import shirumengya.endless_deep_space.custom.client.sounds.ModMusicManager;
import shirumengya.endless_deep_space.custom.config.ModClientConfig;
import shirumengya.endless_deep_space.custom.entity.boss.oceandefenders.OceanDefender;

import javax.annotation.Nullable;

@Mixin({Minecraft.class})
public abstract class MinecraftMixin {
   @Shadow @Nullable public ClientLevel level;
   @Shadow @Nullable public LocalPlayer player;
   
   @Unique
   private static final Logger LOGGER = LogManager.getLogger(Minecraft.class);
   
   public MinecraftMixin() {
   
   }
   
   @Inject(method = {"pauseGame"}, at = {@At("HEAD")}, cancellable = true)
   public void pauseGame(boolean p_91359_, CallbackInfo ci) {
      if (Minecraft.getInstance().screen == null) {
         boolean flag = Minecraft.getInstance().hasSingleplayerServer() && !Minecraft.getInstance().getSingleplayerServer().isPublished();
         if (flag) {
            Minecraft.getInstance().setScreen(new PauseScreen(!p_91359_));
            if (ModClientConfig.PAUSE_SOUND.get()) {
               SoundEngine engine = ((SoundManagerAccessor) Minecraft.getInstance().getSoundManager()).getSoundEngine();
               if (((SoundEngineAccessor) engine).getLoaded()) {
                  ((SoundEngineAccessor) engine).getInstanceToChannel().forEach((instance, channelHandle) -> {
                     if (!(instance instanceof LoopTickableSoundInstance)) {
                        channelHandle.execute(Channel::pause);
                     }
                  });
               }
            }
         } else {
            Minecraft.getInstance().setScreen(new PauseScreen(true));
         }
         
      }
      ci.cancel();
   }
   
   @Inject(method = {"runTick"}, at = {@At("HEAD")})
   public void tick(boolean p_91384_, CallbackInfo ci) {
      Minecraft.getInstance().getProfiler().push("EndlessDeepSpaceModClientSideTick");
      ModMusicManager.EndlessDeepSpaceModClientSideTick();
      Minecraft.getInstance().getProfiler().pop();
   }
   
   @Inject(method = {"shouldEntityAppearGlowing"}, at = {@At("HEAD")}, cancellable = true)
   public void shouldEntityAppearGlowing(Entity p_91315_, CallbackInfoReturnable<Boolean> ci) {
      if (p_91315_ instanceof OceanDefender defender && defender.getPhase() == OceanDefender.PHASE_ATTRITION_SPRECHSTIMME) {
         ci.setReturnValue(true);
      }
   }
   
   @Inject(method = {"setScreen"}, at = {@At("TAIL")})
   public void setScreen(Screen p_91153_, CallbackInfo ci) {
      if (Minecraft.getInstance().screen != null) {
         LOGGER.info("Opening Screen:[" + Minecraft.getInstance().screen.getClass().getCanonicalName() + "]");
      } else {
         LOGGER.info("Opening Screen:[null]");
      }
   }
}
