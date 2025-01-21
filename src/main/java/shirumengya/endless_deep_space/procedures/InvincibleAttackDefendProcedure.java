package shirumengya.endless_deep_space.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import shirumengya.endless_deep_space.init.EndlessDeepSpaceModMobEffects;
import shirumengya.endless_deep_space.network.EndlessDeepSpaceModVariables;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class InvincibleAttackDefendProcedure {
	@SubscribeEvent
	public static void onEntityAttacked(LivingAttackEvent event) {
		if (event != null && event.getEntity() != null) {
			execute(event, event.getEntity());
		}
	}

	public static void execute(Entity entity) {
		execute(null, entity);
	}

	private static void execute(@Nullable Event event, Entity entity) {
		if (entity == null)
			return;
		if ((entity.getCapability(EndlessDeepSpaceModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new EndlessDeepSpaceModVariables.PlayerVariables())).Invincible
				|| entity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(EndlessDeepSpaceModMobEffects.INVINCIBLE.get())) {
			if (event != null && event.isCancelable()) {
				event.setCanceled(true);
			} else if (event != null && event.hasResult()) {
				event.setResult(Event.Result.DENY);
			}
		}
	}
}
