package shirumengya.endless_deep_space.custom.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModCommonConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ENDER_LORD_BYPASS_NONPLAYER_DAMAGE;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ENDER_LORD_BREAK_BLOCKS;
    public static final ForgeConfigSpec.ConfigValue<Integer> ENDER_LORD_FLIGHT_HEIGHT;
    public static final ForgeConfigSpec.ConfigValue<Integer> ENDER_LORD_MAX_HEALTH;
    public static final ForgeConfigSpec.ConfigValue<Integer> ENDER_LORD_MAX_SHIELD;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ENDER_LORD_STRIKE_LIGHTNING_UPON_DEATH;
    public static final ForgeConfigSpec.ConfigValue<Integer> SWORD_BLOCK_RESPOND_TIME;
    public static final ForgeConfigSpec.ConfigValue<Boolean> SWORD_BLOCK_COST;
    public static final ForgeConfigSpec.ConfigValue<Integer> SWORD_BLOCK_MAX_RESPOND_TIME;

    static {
        BUILDER.push("Endless Deep Space Mod Common Config");

        ENDER_LORD_BYPASS_NONPLAYER_DAMAGE = BUILDER.comment("Ender Lord bypass Non-Player damage")
                .define("Ender Lord Bypass Non-Player Damage", true);

        ENDER_LORD_BREAK_BLOCKS = BUILDER.comment("Ender Lord can break any blocks")
                .define("Ender Lord Break Any Blocks", true);

        ENDER_LORD_FLIGHT_HEIGHT = BUILDER.comment("Ender Lord flight height(The highest block pos add this value)")
                .define("Ender Lord Flight Height", 15);

        ENDER_LORD_MAX_HEALTH = BUILDER.comment("Ender Lord max health")
                .define("Ender Lord Max Health", 600);

        ENDER_LORD_MAX_SHIELD = BUILDER.comment("Ender Lord max shield")
                .define("Ender Lord Max Shield", 200);

        ENDER_LORD_STRIKE_LIGHTNING_UPON_DEATH = BUILDER.comment("Ender Lord strike lightning upon death")
                .define("Ender Lord Strike Lightning Upon Death", false);

        SWORD_BLOCK_RESPOND_TIME = BUILDER.comment("Sword block respond time(Tick)")
                .defineInRange("Sword Block Respond Time(Tick)", 5, 0, Integer.MAX_VALUE);

        SWORD_BLOCK_COST = BUILDER.comment("Sword block cost")
                .define("Sword Block Cost", false);

        SWORD_BLOCK_MAX_RESPOND_TIME = BUILDER.comment("Sword block max respond time(Tick)")
                .defineInRange("Sword Block Max Respond Time(Tick)", 20, 0, Integer.MAX_VALUE);
        
        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
