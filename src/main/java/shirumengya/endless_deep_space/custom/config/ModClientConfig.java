package shirumengya.endless_deep_space.custom.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModClientConfig {
   public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
   public static final ForgeConfigSpec SPEC;
   public static final ForgeConfigSpec.ConfigValue<Boolean> BATTLE_MUSIC;
   public static final ForgeConfigSpec.ConfigValue<Boolean> CUSTOM_BOSSBAR;
   public static final ForgeConfigSpec.ConfigValue<Boolean> ENDER_LORD_RENDER_BEAM_TO_PLAYER;
   public static final ForgeConfigSpec.ConfigValue<Boolean> SCREEN_SHAKE;
   public static final ForgeConfigSpec.ConfigValue<Boolean> BIOME_JUMP_STRING;
   public static final ForgeConfigSpec.ConfigValue<Boolean> CUSTOM_HIT_OUTLINE_RENDER__ENABLE;
   public static final ForgeConfigSpec.ConfigValue<Boolean> CUSTOM_HIT_OUTLINE_RENDER__ANIMATION_COLOR;
   public static final ForgeConfigSpec.ConfigValue<Integer> CUSTOM_HIT_OUTLINE_RENDER__ANIMATION_COLOR_SETTINGS__CYCLE_SPEED;
   public static final ForgeConfigSpec.ConfigValue<Integer> CUSTOM_HIT_OUTLINE_RENDER__ANIMATION_COLOR_SETTINGS__SATURATION;
   public static final ForgeConfigSpec.ConfigValue<Integer> CUSTOM_HIT_OUTLINE_RENDER__ANIMATION_COLOR_SETTINGS__LIGHT;
   public static final ForgeConfigSpec.ConfigValue<Integer> CUSTOM_HIT_OUTLINE_RENDER__COLOR_RED;
   public static final ForgeConfigSpec.ConfigValue<Integer> CUSTOM_HIT_OUTLINE_RENDER__COLOR_GREEN;
   public static final ForgeConfigSpec.ConfigValue<Integer> CUSTOM_HIT_OUTLINE_RENDER__COLOR_BLUE;
   public static final ForgeConfigSpec.ConfigValue<Integer> CUSTOM_HIT_OUTLINE_RENDER__COLOR_ALPHA;
   public static final ForgeConfigSpec.ConfigValue<Boolean> PAUSE_SOUND;
   public static final ForgeConfigSpec.ConfigValue<Boolean> SHOW_ITEM_MOD_NAME;
   
   static {
      BUILDER.push("Endless Deep Space Mod Client Config");
      
         BATTLE_MUSIC = BUILDER.comment("Endless Deep Space mod battle music")
               .define("Endless Deep Space Mod Battle Music", true);
         
         CUSTOM_BOSSBAR = BUILDER.comment("Endless Deep Space mod custom bossbar")
               .define("Endless Deep Space Mod Custom Bossbar", true);
         
         ENDER_LORD_RENDER_BEAM_TO_PLAYER = BUILDER.comment("Ender Lord render beam to player")
               .define("Ender Lord Render Beam To Player", true);
         
         SCREEN_SHAKE = BUILDER.comment("Endless Deep Space mod screen shake")
               .define("Endless Deep Space Mod Screen Shake", true);
         
         BIOME_JUMP_STRING = BUILDER.comment("Endless Deep Space mod biome jump string")
               .define("Endless Deep Space Mod Biome Jump String", true);
         
         BUILDER.push("Custom Hit Outline Render");
            CUSTOM_HIT_OUTLINE_RENDER__ENABLE = BUILDER.comment("Enable")
                  .define("Enable", true);
            
            CUSTOM_HIT_OUTLINE_RENDER__ANIMATION_COLOR = BUILDER.comment("Animation color")
                  .define("Animation Color", true);
            
            BUILDER.push("Animation Color Settings");
               CUSTOM_HIT_OUTLINE_RENDER__ANIMATION_COLOR_SETTINGS__CYCLE_SPEED = BUILDER.comment("Cycle speed(Millisecond)")
                     .define("Cycle Speed(Millisecond)", 6000);
               
               CUSTOM_HIT_OUTLINE_RENDER__ANIMATION_COLOR_SETTINGS__SATURATION = BUILDER.comment("Saturation")
                     .define("Saturation", 50);
               
               CUSTOM_HIT_OUTLINE_RENDER__ANIMATION_COLOR_SETTINGS__LIGHT = BUILDER.comment("Light")
                     .define("Light", 100);
               
            BUILDER.pop();
         
            BUILDER.push("Color Settings");
               CUSTOM_HIT_OUTLINE_RENDER__COLOR_RED = BUILDER.comment("Red")
                  .define("Red", 0);
               
               CUSTOM_HIT_OUTLINE_RENDER__COLOR_GREEN = BUILDER.comment("Green")
                  .define("Green", 0);
               
               CUSTOM_HIT_OUTLINE_RENDER__COLOR_BLUE = BUILDER.comment("Blue")
                  .define("Blue", 0);
            BUILDER.pop();
      
            CUSTOM_HIT_OUTLINE_RENDER__COLOR_ALPHA = BUILDER.comment("Alpha")
               .define("Alpha", /*102*/255);
            
         BUILDER.pop();
      
         PAUSE_SOUND = BUILDER.comment("Endless Deep Space mod open pause screen pause sounds outside of music")
               .define("Endless Deep Space Mod Open Pause Screen Pause Sounds Outside Of Music", true);
      
         SHOW_ITEM_MOD_NAME = BUILDER.comment("Show item mod name")
               .define("Show Item Mod Name", true);
      
      BUILDER.pop();
      SPEC = BUILDER.build();
   }
}