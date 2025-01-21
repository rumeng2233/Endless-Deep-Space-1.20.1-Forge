package shirumengya.endless_deep_space.custom.client.renderer;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.Timer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import shirumengya.endless_deep_space.EndlessDeepSpaceMod;
import shirumengya.endless_deep_space.custom.init.ModItems;
import shirumengya.endless_deep_space.init.EndlessDeepSpaceModEnchantments;
import shirumengya.endless_deep_space.init.EndlessDeepSpaceModItems;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class ModRenderType extends RenderType {
   public static final Logger LOGGER = LogUtils.getLogger();
   public static float shaderGameTime;
   public static long gameTime;
   public static final Timer timer = new Timer(20.0F, 0L);
   
   
   public static ModShaderInstance rendertypeEnderLordDeathShader;
   public static ModShaderInstance rendertypeEnderLordAttackTargetBeamShader;
   public static ModShaderInstance rendertypeEndGatewayShader;
   public static ModShaderInstance rendertypeEndPortalShader;
   
   
   public ModRenderType(String p_173178_, VertexFormat p_173179_, VertexFormat.Mode p_173180_, int p_173181_, boolean p_173182_, boolean p_173183_, Runnable p_173184_, Runnable p_173185_) {
      super(p_173178_, p_173179_, p_173180_, p_173181_, p_173182_, p_173183_, p_173184_, p_173185_);
      throw new UnsupportedOperationException("Don't instantiate this");
   }
   
   public static void tick() {
      setShaderGameTime(gameTime, timer.partialTick);
   }
   
   public static void setShaderGameTime(long p_157448_, float p_157449_) {
      float f = ((float)(p_157448_ % 24000L) + p_157449_) / 24000.0F;
      if (!RenderSystem.isOnRenderThread()) {
         RenderSystem.recordRenderCall(() -> {
            shaderGameTime = f;
         });
      } else {
         shaderGameTime = f;
      }
      
   }
   
   public static float getShaderGameTime() {
      RenderSystem.assertOnRenderThread();
      return shaderGameTime;
   }
   
   
   public static RenderType EnderLordDeath(ResourceLocation textureOne, ResourceLocation textureTwo, boolean cull) {
      return create("ender_lord_death",
            DefaultVertexFormat.POSITION,
            VertexFormat.Mode.QUADS,
            256,
            false,
            false,
            CompositeState.builder()
                  .setShaderState(new RenderStateShard.ShaderStateShard(() -> rendertypeEnderLordDeathShader))
                  .setTextureState(MultiTextureStateShard.builder().add(textureOne, false, false).add(textureTwo, false, false).build())
                  .setCullState(cull ? CULL : NO_CULL)
                  .createCompositeState(false));
   }
   
   public static RenderType EnderLordAttackTargetBeam(ResourceLocation textureOne, ResourceLocation textureTwo, boolean cull) {
      return create("ender_lord_attack_target_beam",
            DefaultVertexFormat.POSITION,
            VertexFormat.Mode.QUADS,
            256,
            false,
            false,
            CompositeState.builder()
                  .setShaderState(new RenderStateShard.ShaderStateShard(() -> rendertypeEnderLordAttackTargetBeamShader))
                  .setTextureState(MultiTextureStateShard.builder().add(textureOne, false, false).add(textureTwo, false, false).build())
                  .setCullState(cull ? CULL : NO_CULL)
                  .createCompositeState(false));
   }
   
   public static RenderType EndGateway(ResourceLocation textureOne, ResourceLocation textureTwo, boolean cull) {
      return create("end_gateway",
            DefaultVertexFormat.POSITION,
            VertexFormat.Mode.QUADS,
            256,
            false,
            false,
            CompositeState.builder()
                  .setShaderState(new RenderStateShard.ShaderStateShard(() -> rendertypeEndGatewayShader))
                  .setTextureState(MultiTextureStateShard.builder().add(textureOne, false, false).add(textureTwo, false, false).build())
                  .setCullState(cull ? CULL : NO_CULL)
                  .createCompositeState(false));
   }
   
   public static RenderType EndPortal(ResourceLocation textureOne, ResourceLocation textureTwo, boolean cull) {
      return create("end_portal",
            DefaultVertexFormat.POSITION,
            VertexFormat.Mode.QUADS,
            256,
            false,
            false,
            CompositeState.builder()
                  .setShaderState(new RenderStateShard.ShaderStateShard(() -> rendertypeEndPortalShader))
                  .setTextureState(MultiTextureStateShard.builder().add(textureOne, false, false).add(textureTwo, false, false).build())
                  .setCullState(cull ? CULL : NO_CULL)
                  .createCompositeState(false));
   }
   
   
   public static List<RenderType> glint = newRenderList(ModRenderType::buildGlintRenderType);
   public static List<RenderType> glintTranslucent = newRenderList(ModRenderType::buildGlintTranslucentRenderType);
   public static List<RenderType> entityGlint = newRenderList(ModRenderType::buildEntityGlintRenderType);
   public static List<RenderType> glintDirect = newRenderList(ModRenderType::buildGlintDirectRenderType);
   public static List<RenderType> entityGlintDirect = newRenderList(ModRenderType::buildEntityGlintDriectRenderType);
   public static List<RenderType> armorGlint = newRenderList(ModRenderType::buildArmorGlintRenderType);
   public static List<RenderType> armorEntityGlint = newRenderList(ModRenderType::buildArmorEntityGlintRenderType);
   
   public static void addGlintTypes(Object2ObjectLinkedOpenHashMap<RenderType, BufferBuilder> map) {
      addGlintTypes(map, glint);
      addGlintTypes(map, glintTranslucent);
      addGlintTypes(map, entityGlint);
      addGlintTypes(map, glintDirect);
      addGlintTypes(map, entityGlintDirect);
      addGlintTypes(map, armorGlint);
      addGlintTypes(map, armorEntityGlint);
   }
   
   public static List<String> getGlintType() {
      ArrayList<String> type = Lists.newArrayList();
      type.add(0, "black");
      type.add(1, "blue");
      type.add(2, "brown");
      type.add(3, "cyan");
      type.add(4, "gray");
      type.add(5, "green");
      type.add(6, "light_blue");
      type.add(7, "light_gray");
      type.add(8, "lime");
      type.add(9, "magenta");
      type.add(10, "orange");
      type.add(11, "pink");
      type.add(12, "purple");
      type.add(13, "rainbow");
      type.add(14, "red");
      type.add(15, "white");
      type.add(16, "yellow");
      type.add(17, "broken");
      type.add(18, "stars");
      return type;
   }
   
   private static List<RenderType> newRenderList(Function<String, RenderType> func) {
      ArrayList<RenderType> list = new ArrayList<>(getGlintType().size() + 1);
      
      for (int i = 0;i < getGlintType().size();i++) {
         list.add(func.apply(getGlintType().get(i)));
      }
      
      return list;
   }
   
   private static void addGlintTypes(Object2ObjectLinkedOpenHashMap<RenderType, BufferBuilder> map, List<RenderType> typeList) {
      for(RenderType renderType : typeList) {
         if (!map.containsKey(renderType)) {
            map.put(renderType, new BufferBuilder(renderType.bufferSize()));
         }
      }
   }
   
   private static RenderType buildGlintRenderType(String name) {
      return RenderType.create("glint_" + name, DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, false, false, CompositeState.builder()
            .setShaderState(RenderStateShard.RENDERTYPE_GLINT_SHADER)
            .setTextureState(new TextureStateShard(texture(name), true, false))
            .setWriteMaskState(RenderStateShard.COLOR_WRITE)
            .setCullState(RenderStateShard.NO_CULL)
            .setDepthTestState(RenderStateShard.EQUAL_DEPTH_TEST)
            .setTransparencyState(RenderStateShard.GLINT_TRANSPARENCY)
            .setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
            .setTexturingState(RenderStateShard.GLINT_TEXTURING)
            .createCompositeState(false));
   }
   
   private static RenderType buildGlintTranslucentRenderType(String name) {
      return RenderType.create("glint_translucent_" + name, DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, false, false, CompositeState.builder()
            .setShaderState(RenderStateShard.RENDERTYPE_GLINT_TRANSLUCENT_SHADER)
            .setTextureState(new TextureStateShard(texture(name), true, false))
            .setWriteMaskState(RenderStateShard.COLOR_WRITE)
            .setCullState(RenderStateShard.NO_CULL)
            .setDepthTestState(RenderStateShard.EQUAL_DEPTH_TEST)
            .setTransparencyState(RenderStateShard.GLINT_TRANSPARENCY)
            .setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
            .setTexturingState(RenderStateShard.GLINT_TEXTURING)
            .createCompositeState(false));
   }
   
   private static RenderType buildEntityGlintRenderType(String name) {
      return RenderType.create("entity_glint_" + name, DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, false, false, CompositeState.builder()
            .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_GLINT_SHADER)
            .setTextureState(new TextureStateShard(texture(name), true, false))
            .setWriteMaskState(RenderStateShard.COLOR_WRITE)
            .setCullState(RenderStateShard.NO_CULL)
            .setDepthTestState(RenderStateShard.EQUAL_DEPTH_TEST)
            .setTransparencyState(RenderStateShard.GLINT_TRANSPARENCY)
            .setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
            .setTexturingState(RenderStateShard.ENTITY_GLINT_TEXTURING)
            .createCompositeState(false));
   }
   
   
   private static RenderType buildGlintDirectRenderType(String name) {
      return RenderType.create("glint_direct_" + name, DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, false, false, CompositeState.builder()
            .setShaderState(RenderStateShard.RENDERTYPE_GLINT_DIRECT_SHADER)
            .setTextureState(new TextureStateShard(texture(name), true, false))
            .setWriteMaskState(RenderStateShard.COLOR_WRITE)
            .setCullState(RenderStateShard.NO_CULL)
            .setDepthTestState(RenderStateShard.EQUAL_DEPTH_TEST)
            .setTransparencyState(RenderStateShard.GLINT_TRANSPARENCY)
            .setTexturingState(RenderStateShard.GLINT_TEXTURING)
            .createCompositeState(false));
   }
   
   
   private static RenderType buildEntityGlintDriectRenderType(String name) {
      return RenderType.create("entity_glint_direct_" + name, DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, false, false, CompositeState.builder()
            .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_GLINT_DIRECT_SHADER)
            .setTextureState(new TextureStateShard(texture(name), true, false))
            .setWriteMaskState(RenderStateShard.COLOR_WRITE)
            .setCullState(RenderStateShard.NO_CULL)
            .setDepthTestState(RenderStateShard.EQUAL_DEPTH_TEST)
            .setTransparencyState(RenderStateShard.GLINT_TRANSPARENCY)
            .setTexturingState(RenderStateShard.ENTITY_GLINT_TEXTURING)
            .createCompositeState(false));
   }
   
   private static RenderType buildArmorGlintRenderType(String name) {
      return RenderType.create("armor_glint_" + name, DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, false, false, CompositeState.builder()
            .setShaderState(RenderStateShard.RENDERTYPE_ARMOR_GLINT_SHADER)
            .setTextureState(new TextureStateShard(texture(name), true, false))
            .setWriteMaskState(RenderStateShard.COLOR_WRITE)
            .setCullState(RenderStateShard.NO_CULL)
            .setDepthTestState(RenderStateShard.EQUAL_DEPTH_TEST)
            .setTransparencyState(RenderStateShard.GLINT_TRANSPARENCY)
            .setTexturingState(RenderStateShard.ENTITY_GLINT_TEXTURING)
            .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
            .createCompositeState(false));
   }
   
   private static RenderType buildArmorEntityGlintRenderType(String name) {
      return RenderType.create("armor_entity_glint_" + name, DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, false, false, CompositeState.builder()
            .setShaderState(RenderStateShard.RENDERTYPE_ARMOR_ENTITY_GLINT_SHADER)
            .setTextureState(new TextureStateShard(texture(name), true, false))
            .setWriteMaskState(RenderStateShard.COLOR_WRITE)
            .setCullState(RenderStateShard.NO_CULL)
            .setDepthTestState(RenderStateShard.EQUAL_DEPTH_TEST)
            .setTransparencyState(RenderStateShard.GLINT_TRANSPARENCY)
            .setTexturingState(RenderStateShard.ENTITY_GLINT_TEXTURING)
            .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
            .createCompositeState(false));
   }
   
   private static ResourceLocation texture(String name) {
      return new ResourceLocation(EndlessDeepSpaceMod.MODID, "textures/glint/enchanted_item_glint_" + name + ".png");
   }
   
   private static final ThreadLocal<ItemStack> targetStack = new ThreadLocal<>();
   
   public static void setTargetStack(ItemStack stack) {
      targetStack.set(stack);
   }
   
   public static RenderType getGlint() {
      return renderType(glint, RenderType::glint);
   }
   
   public static RenderType getGlintTranslucent() {
      return renderType(glintTranslucent, RenderType::glintTranslucent);
   }
   
   public static RenderType getEntityGlint() {
      return renderType(entityGlint, RenderType::entityGlint);
   }
   
   public static RenderType getGlintDirect() {
      return renderType(glintDirect, RenderType::glintDirect);
   }
   
   public static RenderType getEntityGlintDirect() {
      return renderType(entityGlintDirect, RenderType::entityGlintDirect);
   }
   
   public static RenderType getArmorGlint() {
      return renderType(armorGlint, RenderType::armorGlint);
   }
   
   public static RenderType getArmorEntityGlint() {
      return renderType(armorEntityGlint, RenderType::armorEntityGlint);
   }
   
   private static RenderType renderType(List<RenderType> list, Supplier<RenderType> vanilla) {
      ItemStack target = targetStack.get();
      if (target != null) {
         if (target.getItem() == EndlessDeepSpaceModItems.TOTEM_SWORD.get()) {
            return list.get(16);
         }
         
         if (target.getItem() == ModItems.OCEAN_DEFENDER_SPAWN_EGG.get()) {
            return list.get(6);
         }
         
         if (target.getItem() == ModItems.ENDER_LORD_SPAWN_EGG.get()) {
            return list.get(6);
         }
         
         if (target.getItem() == ModItems.MUTATION_RAVAGER_SPAWN_EGG.get()) {
            return list.get(9);
         }
         
         if (target.getEnchantmentLevel(EndlessDeepSpaceModEnchantments.DECAPITATE.get()) > 0) {
            return list.get(14);
         }
      }
      
      return vanilla.get();
   }
}
