
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package shirumengya.endless_deep_space.init;

import net.minecraftforge.fml.common.Mod;

import net.minecraft.world.level.GameRules;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class EndlessDeepSpaceModGameRules {
	public static final GameRules.Key<GameRules.BooleanValue> ARROWS_EXPLOSION_DROP_BLOCKS = GameRules.register("arrowsExplosionDropBlocks", GameRules.Category.DROPS, GameRules.BooleanValue.create(true));
	public static final GameRules.Key<GameRules.IntegerValue> GUIDING_STONE_LOAD_TICKET_LEVEL = GameRules.register("guidingStoneLoadTicketLevel", GameRules.Category.UPDATES, GameRules.IntegerValue.create(31));
	public static final GameRules.Key<GameRules.IntegerValue> BOSS_LOAD_TICKET_LEVEL = GameRules.register("bossLoadTicketLevel", GameRules.Category.UPDATES, GameRules.IntegerValue.create(21));
}
