package shirumengya.endless_deep_space.custom.init;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import shirumengya.endless_deep_space.EndlessDeepSpaceMod;
import shirumengya.endless_deep_space.custom.item.NormalBowItem;
import shirumengya.endless_deep_space.custom.item.PocketKnifeItem;
import shirumengya.endless_deep_space.custom.item.TrackingBowItem;

public class ModItems {
	public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, EndlessDeepSpaceMod.MODID);

	public static final RegistryObject<Item> ENDER_LORD_SPAWN_EGG = REGISTRY.register("ender_lord_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.ENDER_LORD, -15517642, -11141121, new Item.Properties()));
	public static final RegistryObject<Item> MUTATION_RAVAGER_SPAWN_EGG = REGISTRY.register("mutation_ravager_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.MUTATION_RAVAGER, -6710887, -10066330, new Item.Properties()));
	public static final RegistryObject<Item> OCEAN_DEFENDER_SPAWN_EGG = REGISTRY.register("ocean_defender_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.OCEAN_DEFENDER, -3355444, -10066330, new Item.Properties()));
	
	public static final RegistryObject<Item> WOODEN_POCKET_KNIFE = REGISTRY.register("wooden_pocket_knife", () -> new PocketKnifeItem(Tiers.WOOD, 3, -2.4F, new Item.Properties()));
	public static final RegistryObject<Item> STONE_POCKET_KNIFE = REGISTRY.register("stone_pocket_knife", () -> new PocketKnifeItem(Tiers.STONE, 3, -2.4F, new Item.Properties()));
	public static final RegistryObject<Item> IRON_POCKET_KNIFE = REGISTRY.register("iron_pocket_knife", () -> new PocketKnifeItem(Tiers.IRON, 3, -2.4F, new Item.Properties()));
	public static final RegistryObject<Item> GOLDEN_POCKET_KNIFE = REGISTRY.register("golden_pocket_knife", () -> new PocketKnifeItem(Tiers.GOLD, 3, -2.4F, new Item.Properties()));
	public static final RegistryObject<Item> DIAMOND_POCKET_KNIFE = REGISTRY.register("diamond_pocket_knife", () -> new PocketKnifeItem(Tiers.DIAMOND, 3, -2.4F, new Item.Properties()));
	public static final RegistryObject<Item> NETHERITE_POCKET_KNIFE = REGISTRY.register("netherite_pocket_knife", () -> new PocketKnifeItem(Tiers.NETHERITE, 3, -2.4F, new Item.Properties()));
	
	public static final RegistryObject<Item> TRACKING_BOW = REGISTRY.register("tracking_bow", () -> new TrackingBowItem((new Item.Properties()).durability(384)));
	public static final RegistryObject<Item> TRACKING_ARROW = REGISTRY.register("tracking_arrow", () -> new Item(new Item.Properties()));
	public static final RegistryObject<Item> NORMAL_BOW = REGISTRY.register("bow", () -> new NormalBowItem((new Item.Properties()).durability(384)));
	
	public static final RegistryObject<Item> TRANSFORM_STATION = block(ModBlocks.TRANSFORM_STATION);
	public static final RegistryObject<Item> WHITE_GUIDING_STONE = block(ModBlocks.WHITE_GUIDING_STONE);
	public static final RegistryObject<Item> ORANGE_GUIDING_STONE = block(ModBlocks.ORANGE_GUIDING_STONE);
	public static final RegistryObject<Item> MAGENTA_GUIDING_STONE = block(ModBlocks.MAGENTA_GUIDING_STONE);
	public static final RegistryObject<Item> LIGHT_BLUE_GUIDING_STONE = block(ModBlocks.LIGHT_BLUE_GUIDING_STONE);
	public static final RegistryObject<Item> YELLOW_GUIDING_STONE = block(ModBlocks.YELLOW_GUIDING_STONE);
	public static final RegistryObject<Item> LIME_GUIDING_STONE = block(ModBlocks.LIME_GUIDING_STONE);
	public static final RegistryObject<Item> PINK_GUIDING_STONE = block(ModBlocks.PINK_GUIDING_STONE);
	public static final RegistryObject<Item> GRAY_GUIDING_STONE = block(ModBlocks.GRAY_GUIDING_STONE);
	public static final RegistryObject<Item> LIGHT_GRAY_GUIDING_STONE = block(ModBlocks.LIGHT_GRAY_GUIDING_STONE);
	public static final RegistryObject<Item> CYAN_GUIDING_STONE = block(ModBlocks.CYAN_GUIDING_STONE);
	public static final RegistryObject<Item> PURPLE_GUIDING_STONE = block(ModBlocks.PURPLE_GUIDING_STONE);
	public static final RegistryObject<Item> BLUE_GUIDING_STONE = block(ModBlocks.BLUE_GUIDING_STONE);
	public static final RegistryObject<Item> BROWN_GUIDING_STONE = block(ModBlocks.BROWN_GUIDING_STONE);
	public static final RegistryObject<Item> GREEN_GUIDING_STONE = block(ModBlocks.GREEN_GUIDING_STONE);
	public static final RegistryObject<Item> RED_GUIDING_STONE = block(ModBlocks.RED_GUIDING_STONE);
	public static final RegistryObject<Item> BLACK_GUIDING_STONE = block(ModBlocks.BLACK_GUIDING_STONE);
	public static final RegistryObject<Item> AQUA_GUIDING_STONE = block(ModBlocks.AQUA_GUIDING_STONE);
	public static final RegistryObject<Item> PURE_BLACK_GUIDING_STONE = block(ModBlocks.PURE_BLACK_GUIDING_STONE);
	public static final RegistryObject<Item> SKY_BLUE_GUIDING_STONE = block(ModBlocks.SKY_BLUE_GUIDING_STONE);
	public static final RegistryObject<Item> BLUE_GREEN_GUIDING_STONE = block(ModBlocks.BLUE_GREEN_GUIDING_STONE);
	public static final RegistryObject<Item> WARNING_GUIDING_STONE = block(ModBlocks.WARNING_GUIDING_STONE);
	public static final RegistryObject<Item> PURE_GREEN_GUIDING_STONE = block(ModBlocks.PURE_GREEN_GUIDING_STONE);
	public static final RegistryObject<Item> PURE_RED_GUIDING_STONE = block(ModBlocks.PURE_RED_GUIDING_STONE);
	public static final RegistryObject<Item> PURE_BLUE_GUIDING_STONE = block(ModBlocks.PURE_BLUE_GUIDING_STONE);
	public static final RegistryObject<Item> PURE_MAGENTA_GUIDING_STONE = block(ModBlocks.PURE_MAGENTA_GUIDING_STONE);
	

	private static RegistryObject<Item> block(RegistryObject<Block> block) {
		return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
	}
}
