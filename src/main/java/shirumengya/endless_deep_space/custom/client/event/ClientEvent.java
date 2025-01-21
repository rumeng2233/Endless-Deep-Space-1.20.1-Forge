package shirumengya.endless_deep_space.custom.client.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import shirumengya.endless_deep_space.EndlessDeepSpaceMod;
import shirumengya.endless_deep_space.custom.client.gui.CustomBossBar;
import shirumengya.endless_deep_space.custom.client.gui.overlay.JumpStringOverlay;
import shirumengya.endless_deep_space.custom.client.gui.screens.TransformStationScreen;
import shirumengya.endless_deep_space.custom.client.renderer.ModRenderType;
import shirumengya.endless_deep_space.custom.client.renderer.ModShaderInstance;
import shirumengya.endless_deep_space.custom.config.ModClientConfig;
import shirumengya.endless_deep_space.custom.entity.ScreenShakeEntity;
import shirumengya.endless_deep_space.custom.entity.boss.oceandefenders.OceanDefender;
import shirumengya.endless_deep_space.custom.event.SwordBlockEvent;
import shirumengya.endless_deep_space.custom.init.ModMenuTypes;
import shirumengya.endless_deep_space.init.EndlessDeepSpaceModEnchantments;
import shirumengya.endless_deep_space.init.EndlessDeepSpaceModMobEffects;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = EndlessDeepSpaceMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEvent {
   public static Map<UUID, Integer> bossBarRenderTypes = new HashMap<>();
   public static Map<UUID, Component> bossBarDescription = new HashMap<>();
   public final Random random = new Random();
   public int lastHealth;
   public int displayHealth;
   public long lastHealthTime;
   public long healthBlinkTime;
   public static final ResourceLocation NORMAL_HEART = new ResourceLocation("endless_deep_space:textures/gui/hearts/normal_heart.png");
   
   public static void removeBossBarRender(UUID bossBar) {
      bossBarRenderTypes.remove(bossBar);
      bossBarDescription.remove(bossBar);
   }
   
   public static void setBossBarRender(UUID bossBar, Component description, int renderType) {
      bossBarRenderTypes.put(bossBar, renderType);
      bossBarDescription.put(bossBar, description);
   }
   
   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public void renderBossOverlay(CustomizeGuiOverlayEvent.BossEventProgress event) {
      if (ModClientConfig.CUSTOM_BOSSBAR.get()) {
         if (bossBarRenderTypes.containsKey(event.getBossEvent().getId()) && bossBarDescription.containsKey(event.getBossEvent().getId())) {
            int renderTypeFor = bossBarRenderTypes.get(event.getBossEvent().getId());
            Component description = bossBarDescription.get(event.getBossEvent().getId());
            CustomBossBar customBossBar = CustomBossBar.customBossBars.getOrDefault(renderTypeFor, null);
            if (customBossBar == null) return;
            
            event.setCanceled(true);
            customBossBar.renderBossBar(event, description, renderTypeFor);
         }
      }
   }
   
   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
      Entity entity = Minecraft.getInstance().getCameraEntity();
      if (ModClientConfig.SCREEN_SHAKE.get() && !Minecraft.getInstance().isPaused()) {
         if (entity != null) {
            float delta = Minecraft.getInstance().getFrameTime();
            float ticksExistedDelta = entity.tickCount + delta;
            BlockPos pos = Minecraft.getInstance().gameRenderer.getMainCamera().getBlockPosition();
            float shakeAmplitude = 0;
            for (ScreenShakeEntity ScreenShake : entity.level().getEntitiesOfClass(ScreenShakeEntity.class, AABB.ofSize(new Vec3(pos.getX(), pos.getY(), pos.getZ()), 1024, 1024, 1024))) {
               if (ScreenShake.distanceTo(entity) < ScreenShake.getRadius()) {
                  shakeAmplitude += ScreenShake.getShakeAmount(entity, delta);
               }
            }
            if (entity instanceof LivingEntity livingEntity && SwordBlockEvent.hasVertigoTime(livingEntity)) {
               shakeAmplitude = Math.max(shakeAmplitude, Math.min(0.04F, (float) (SwordBlockEvent.getVertigoTime(livingEntity) == MobEffectInstance.INFINITE_DURATION ? 4 : ((SwordBlockEvent.getVertigoTime(livingEntity) / 20.0F)) * 0.01)));
            }
            if (shakeAmplitude > 1.0f){
               shakeAmplitude = 1.0f;
            }
            event.setPitch((float) (event.getPitch() + shakeAmplitude * Math.cos(ticksExistedDelta * 3 + 2) * 25));
            event.setYaw((float) (event.getYaw() + shakeAmplitude * Math.cos(ticksExistedDelta * 5 + 1) * 25));
            event.setRoll((float) (event.getRoll() + shakeAmplitude * Math.cos(ticksExistedDelta * 4) * 25));
         }
      }
   }
   
   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
      event.registerAboveAll("big_jump_string", JumpStringOverlay.BIG_JUMP_STRING);
      event.registerAboveAll("jump_string", JumpStringOverlay.JUMP_STRING);
      event.registerAboveAll("small_jump_string", JumpStringOverlay.SMALL_JUMP_STRING);
   }
   
   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public void renderItemTooltip(ItemTooltipEvent event) {
      if (event.getItemStack().getEnchantmentLevel(EndlessDeepSpaceModEnchantments.DECAPITATE.get()) > 0) {
         event.getToolTip().add(1, Component.translatable("enchantment.endless_deep_space.decapitate.kill_entity_times", event.getItemStack().getOrCreateTag().getInt("KillEntityTimes")).withStyle(ChatFormatting.GRAY));
         event.getToolTip().add(2, Component.empty());
      }
   }
   
   @SubscribeEvent(priority = EventPriority.LOWEST)
   public void renderItemTooltipModName(ItemTooltipEvent event) {
      if (ModClientConfig.SHOW_ITEM_MOD_NAME.get()) {
         ResourceLocation namespace = ForgeRegistries.ITEMS.getKey(event.getItemStack().getItem());
         if (namespace != null) {
            Component component = Component.literal(ModList.get().getModContainerById(namespace.getNamespace()).get().getModInfo().getDisplayName()).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC);
            event.getToolTip().add(component);
         }
      }
   }
   
   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public void onPreRenderHUD(RenderGuiOverlayEvent.Pre event) {
      Player player = Minecraft.getInstance().player;
      if (player != null) {
         Minecraft minecraft = Minecraft.getInstance();
         ForgeGui gui = (ForgeGui) minecraft.gui;
         if (event.getOverlay() == VanillaGuiOverlay.PLAYER_HEALTH.type() && !minecraft.options.hideGui && gui.shouldDrawSurvivalElements()) {
            if (!this.hasCustomHealth(player).equals(NORMAL_HEART)) {
               this.CustomHealth(event, 25);
            }
         }
      }
   }
   
   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void onRegisterShaders(RegisterShadersEvent event) {
      event.registerShader(ModShaderInstance.create(event.getResourceProvider(), new ResourceLocation("endless_deep_space:rendertype_ender_lord_death"), DefaultVertexFormat.POSITION), shaderInstance -> {
         ModRenderType.rendertypeEnderLordDeathShader = (ModShaderInstance)shaderInstance;
         ModRenderType.rendertypeEnderLordDeathShader.onApply(() -> {
            if (ModRenderType.rendertypeEnderLordDeathShader.EDS_GAME_TIME != null) {
               ModRenderType.rendertypeEnderLordDeathShader.EDS_GAME_TIME.set(ModRenderType.getShaderGameTime());
            }
         });
      });
      
      event.registerShader(ModShaderInstance.create(event.getResourceProvider(), new ResourceLocation("endless_deep_space:rendertype_ender_lord_attack_target_beam"), DefaultVertexFormat.POSITION), shaderInstance -> {
         ModRenderType.rendertypeEnderLordAttackTargetBeamShader = (ModShaderInstance)shaderInstance;
         ModRenderType.rendertypeEnderLordAttackTargetBeamShader.onApply(() -> {
            if (ModRenderType.rendertypeEnderLordAttackTargetBeamShader.EDS_GAME_TIME != null) {
               ModRenderType.rendertypeEnderLordAttackTargetBeamShader.EDS_GAME_TIME.set(ModRenderType.getShaderGameTime());
            }
         });
      });
      
      event.registerShader(ModShaderInstance.create(event.getResourceProvider(), new ResourceLocation("endless_deep_space:rendertype_end_gateway"), DefaultVertexFormat.POSITION), shaderInstance -> {
         ModRenderType.rendertypeEndGatewayShader = (ModShaderInstance)shaderInstance;
         ModRenderType.rendertypeEndGatewayShader.onApply(() -> {
            if (ModRenderType.rendertypeEndGatewayShader.EDS_GAME_TIME != null) {
               ModRenderType.rendertypeEndGatewayShader.EDS_GAME_TIME.set(ModRenderType.getShaderGameTime());
            }
         });
      });
      
      event.registerShader(ModShaderInstance.create(event.getResourceProvider(), new ResourceLocation("endless_deep_space:rendertype_end_portal"), DefaultVertexFormat.POSITION), shaderInstance -> {
         ModRenderType.rendertypeEndPortalShader = (ModShaderInstance)shaderInstance;
         ModRenderType.rendertypeEndPortalShader.onApply(() -> {
            if (ModRenderType.rendertypeEndPortalShader.EDS_GAME_TIME != null) {
               ModRenderType.rendertypeEndPortalShader.EDS_GAME_TIME.set(ModRenderType.getShaderGameTime());
            }
         });
      });
   }
   
   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void onClientSetup(FMLClientSetupEvent event) {
      MenuScreens.register(ModMenuTypes.TRANSFORM_STATION.get(), TransformStationScreen::new);
   }
   
   public ResourceLocation hasCustomHealth(Player player) {
      if (OceanDefender.getAttrition(player) > 0.0F) {
         return new ResourceLocation("endless_deep_space:textures/gui/hearts/attrition_heart.png");
      }
      
      if (player.hasEffect(EndlessDeepSpaceModMobEffects.TOTEM_OF_UNDYING.get())) {
         return new ResourceLocation("endless_deep_space:textures/gui/hearts/totem_of_undying_effect_heart.png");
      }
      
      if (player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
         return new ResourceLocation("endless_deep_space:textures/gui/hearts/fire_resistance_effect_heart.png");
      }
      
      return NORMAL_HEART;
   }
   
   public void CustomHealth(RenderGuiOverlayEvent.Pre event,int back) {
      Player player = Minecraft.getInstance().player;
      Minecraft minecraft = Minecraft.getInstance();
      ForgeGui gui = (ForgeGui) minecraft.gui;
      GuiGraphics stack = event.getGuiGraphics();
      gui.setupOverlayRenderState(true, false);
      int width = event.getWindow().getGuiScaledWidth();
      int height = event.getWindow().getGuiScaledHeight();
      event.setCanceled(true);
      RenderSystem.setShaderTexture(0, hasCustomHealth(player));
      RenderSystem.enableBlend();
      int health = Mth.ceil(player.getHealth());
      int tickCount = gui.getGuiTicks();
      boolean highlight = this.healthBlinkTime > (long) tickCount && (this.healthBlinkTime - (long) tickCount) / 3L % 2L == 1L;
      if (health < this.lastHealth && player.invulnerableTime > 0) {
         this.lastHealthTime = Util.getMillis();
         this.healthBlinkTime = (long) (tickCount + 20);
      } else if (health > this.lastHealth && player.invulnerableTime > 0) {
         this.lastHealthTime = Util.getMillis();
         this.healthBlinkTime = (long) (tickCount + 10);
      }
      
      if (Util.getMillis() - this.lastHealthTime > 1000L) {
         this.lastHealth = health;
         this.displayHealth = health;
         this.lastHealthTime = Util.getMillis();
      }
      
      this.lastHealth = health;
      int healthLast = this.displayHealth;
      AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
      float healthMax = (float) maxHealth.getValue();
      int absorbtion = Mth.ceil(player.getAbsorptionAmount());
      int healthRows = Mth.ceil((healthMax + (float) absorbtion) / 2.0F / 10.0F);
      int rowHeight = Math.max(10 - (healthRows - 2), 3);
      this.random.setSeed((long) (tickCount * 312871L));
      int left = width / 2 - 91;
      int top = height - gui.leftHeight;
      gui.leftHeight += healthRows * rowHeight;
      if (rowHeight != 10) {
         gui.leftHeight += 10 - rowHeight;
      }
      
      int regen = -1;
      if (player.hasEffect(MobEffects.REGENERATION) || player.hasEffect(EndlessDeepSpaceModMobEffects.TOTEM_OF_UNDYING.get())) {
         regen = tickCount % Mth.ceil(healthMax + 5.0F);
      }
      
      int TOP = player.level().getLevelData().isHardcore() ? 9 : 0;
      int BACKGROUND = highlight ? back : 16;
      int margin = 34;
      float absorbtionRemaining = (float) absorbtion;
      
      for (int i = Mth.ceil((healthMax + (float) absorbtion) / 2.0F) - 1; i >= 0; --i) {
         int row = Mth.ceil((float) (i + 1) / 10.0F) - 1;
         int x = left + i % 10 * 8;
         int y = top - row * rowHeight;
         if (health <= 4 || (float) OceanDefender.getAttritionTick(player) / OceanDefender.getAttritionMaxTick(player) <= 0.5F || OceanDefender.getAttrition(player) >= 0.5F) {
            y += this.random.nextInt(OceanDefender.getAttrition(player) >= 0.5F ? 3 : 2);
         }
         
         if (i == regen) {
            y -= 2;
         }
         
         stack.blit(hasCustomHealth(player), x, y, BACKGROUND, TOP, 9, 9);
         if (highlight) {
            if (i * 2 + 1 < healthLast) {
               stack.blit(hasCustomHealth(player), x, y, margin, TOP, 9, 9);
            } else if (i * 2 + 1 == healthLast) {
               stack.blit(hasCustomHealth(player), x, y, margin + 9, TOP, 9, 9);
            }
         }
         
         if (absorbtionRemaining > 0.0F) {
            if (absorbtionRemaining == (float) absorbtion && (float) absorbtion % 2.0F == 1.0F) {
               stack.blit(hasCustomHealth(player), x, y, margin + 9, TOP, 9, 9);
               --absorbtionRemaining;
            } else {
               stack.blit(hasCustomHealth(player), x, y, margin, TOP, 9, 9);
               absorbtionRemaining -= 2.0F;
            }
         } else if (i * 2 + 1 < health) {
            stack.blit(hasCustomHealth(player), x, y, margin, TOP, 9, 9);
         } else if (i * 2 + 1 == health) {
            stack.blit(hasCustomHealth(player), x, y, margin + 9, TOP, 9, 9);
         }
      }
      
      RenderSystem.disableBlend();
      RenderSystem.setShaderTexture(0, hasCustomHealth(player));
   }
}
