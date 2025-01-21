
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package shirumengya.endless_deep_space.custom.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import shirumengya.endless_deep_space.EndlessDeepSpaceMod;
import shirumengya.endless_deep_space.init.EndlessDeepSpaceModItems;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModTabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EndlessDeepSpaceMod.MODID);
	public static final RegistryObject<CreativeModeTab> ENDLESS_DEEP_SPACE = REGISTRY.register("endless_deep_space",
			() -> CreativeModeTab.builder().title(Component.translatable("item_group.endless_deep_space.endless_deep_space")).icon(() -> new ItemStack(EndlessDeepSpaceModItems.LOGO.get())).displayItems((parameters, tabData) -> {
				tabData.accept(ModItems.TRANSFORM_STATION.get());
				
				tabData.accept(ModItems.WHITE_GUIDING_STONE.get());
				tabData.accept(ModItems.ORANGE_GUIDING_STONE.get());
				tabData.accept(ModItems.MAGENTA_GUIDING_STONE.get());
				tabData.accept(ModItems.LIGHT_BLUE_GUIDING_STONE.get());
				tabData.accept(ModItems.YELLOW_GUIDING_STONE.get());
				tabData.accept(ModItems.LIME_GUIDING_STONE.get());
				tabData.accept(ModItems.PINK_GUIDING_STONE.get());
				tabData.accept(ModItems.GRAY_GUIDING_STONE.get());
				tabData.accept(ModItems.LIGHT_GRAY_GUIDING_STONE.get());
				tabData.accept(ModItems.CYAN_GUIDING_STONE.get());
				tabData.accept(ModItems.PURPLE_GUIDING_STONE.get());
				tabData.accept(ModItems.BLUE_GUIDING_STONE.get());
				tabData.accept(ModItems.BROWN_GUIDING_STONE.get());
				tabData.accept(ModItems.GREEN_GUIDING_STONE.get());
				tabData.accept(ModItems.RED_GUIDING_STONE.get());
				tabData.accept(ModItems.BLACK_GUIDING_STONE.get());
				tabData.accept(ModItems.AQUA_GUIDING_STONE.get());
				tabData.accept(ModItems.PURE_BLACK_GUIDING_STONE.get());
				tabData.accept(ModItems.SKY_BLUE_GUIDING_STONE.get());
				tabData.accept(ModItems.BLUE_GREEN_GUIDING_STONE.get());
				tabData.accept(ModItems.WARNING_GUIDING_STONE.get());
				tabData.accept(ModItems.PURE_RED_GUIDING_STONE.get());
				tabData.accept(ModItems.PURE_GREEN_GUIDING_STONE.get());
				tabData.accept(ModItems.PURE_BLUE_GUIDING_STONE.get());
				tabData.accept(ModItems.PURE_MAGENTA_GUIDING_STONE.get());
				

				tabData.accept(EndlessDeepSpaceModItems.TOTEM_SWORD.get());

				tabData.accept(ModItems.WOODEN_POCKET_KNIFE.get());
				tabData.accept(ModItems.STONE_POCKET_KNIFE.get());
				tabData.accept(ModItems.IRON_POCKET_KNIFE.get());
				tabData.accept(ModItems.GOLDEN_POCKET_KNIFE.get());
				tabData.accept(ModItems.DIAMOND_POCKET_KNIFE.get());
				tabData.accept(ModItems.NETHERITE_POCKET_KNIFE.get());

				tabData.accept(ModItems.NORMAL_BOW.get());
				tabData.accept(Items.ARROW);
				tabData.accept(ModItems.TRACKING_BOW.get());
				tabData.accept(ModItems.TRACKING_ARROW.get());

				tabData.accept(ModItems.ENDER_LORD_SPAWN_EGG.get());
				tabData.accept(ModItems.MUTATION_RAVAGER_SPAWN_EGG.get());
				tabData.accept(ModItems.OCEAN_DEFENDER_SPAWN_EGG.get());
			}).build());

	@SubscribeEvent
	public static void buildTabContentsVanilla(BuildCreativeModeTabContentsEvent tabData) {
		if (tabData.getTabKey() == CreativeModeTabs.OP_BLOCKS) {
			if (tabData.hasPermissions()) {
				tabData.accept(EndlessDeepSpaceModItems.TOTEM_SWORD.get());
			}
		} else if (tabData.getTabKey() == CreativeModeTabs.COMBAT) {
			tabData.accept(ModItems.WOODEN_POCKET_KNIFE.get());
			tabData.accept(ModItems.STONE_POCKET_KNIFE.get());
			tabData.accept(ModItems.IRON_POCKET_KNIFE.get());
			tabData.accept(ModItems.GOLDEN_POCKET_KNIFE.get());
			tabData.accept(ModItems.DIAMOND_POCKET_KNIFE.get());
			tabData.accept(ModItems.NETHERITE_POCKET_KNIFE.get());

			tabData.accept(ModItems.NORMAL_BOW.get());
			tabData.accept(Items.ARROW);
			tabData.accept(ModItems.TRACKING_BOW.get());
			tabData.accept(ModItems.TRACKING_ARROW.get());

			tabData.accept(EndlessDeepSpaceModItems.TOTEM_SWORD.get());
		} else if (tabData.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
			tabData.accept(ModItems.ENDER_LORD_SPAWN_EGG.get());
			tabData.accept(ModItems.MUTATION_RAVAGER_SPAWN_EGG.get());
			tabData.accept(ModItems.OCEAN_DEFENDER_SPAWN_EGG.get());
		} else if (tabData.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
		} else if (tabData.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
			tabData.accept(ModItems.TRANSFORM_STATION.get());
			
			tabData.accept(ModItems.WHITE_GUIDING_STONE.get());
			tabData.accept(ModItems.ORANGE_GUIDING_STONE.get());
			tabData.accept(ModItems.MAGENTA_GUIDING_STONE.get());
			tabData.accept(ModItems.LIGHT_BLUE_GUIDING_STONE.get());
			tabData.accept(ModItems.YELLOW_GUIDING_STONE.get());
			tabData.accept(ModItems.LIME_GUIDING_STONE.get());
			tabData.accept(ModItems.PINK_GUIDING_STONE.get());
			tabData.accept(ModItems.GRAY_GUIDING_STONE.get());
			tabData.accept(ModItems.LIGHT_GRAY_GUIDING_STONE.get());
			tabData.accept(ModItems.CYAN_GUIDING_STONE.get());
			tabData.accept(ModItems.PURPLE_GUIDING_STONE.get());
			tabData.accept(ModItems.BLUE_GUIDING_STONE.get());
			tabData.accept(ModItems.BROWN_GUIDING_STONE.get());
			tabData.accept(ModItems.GREEN_GUIDING_STONE.get());
			tabData.accept(ModItems.RED_GUIDING_STONE.get());
			tabData.accept(ModItems.BLACK_GUIDING_STONE.get());
			tabData.accept(ModItems.AQUA_GUIDING_STONE.get());
			tabData.accept(ModItems.PURE_BLACK_GUIDING_STONE.get());
			tabData.accept(ModItems.SKY_BLUE_GUIDING_STONE.get());
			tabData.accept(ModItems.BLUE_GREEN_GUIDING_STONE.get());
			tabData.accept(ModItems.WARNING_GUIDING_STONE.get());
			tabData.accept(ModItems.PURE_GREEN_GUIDING_STONE.get());
			tabData.accept(ModItems.PURE_RED_GUIDING_STONE.get());
			tabData.accept(ModItems.PURE_GREEN_GUIDING_STONE.get());
			tabData.accept(ModItems.PURE_BLUE_GUIDING_STONE.get());
			tabData.accept(ModItems.PURE_MAGENTA_GUIDING_STONE.get());
		}
	}
}
