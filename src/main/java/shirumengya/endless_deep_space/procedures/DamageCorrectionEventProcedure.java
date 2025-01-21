package shirumengya.endless_deep_space.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import shirumengya.endless_deep_space.custom.init.ModAttributes;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class DamageCorrectionEventProcedure {
	@SubscribeEvent
	public static void onEntityAttacked(LivingHurtEvent event) {
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
		if (entity instanceof LivingEntity _livingEntity0 && _livingEntity0.getAttributes().hasAttribute(ModAttributes.DAMAGE_REDUCTION.get())) {
			if (event instanceof LivingHurtEvent _hurt) {
				_hurt.setAmount((float) (_hurt.getAmount() - (entity instanceof LivingEntity _livingEntity1 && _livingEntity1.getAttributes().hasAttribute(ModAttributes.DAMAGE_REDUCTION.get())
						? _livingEntity1.getAttribute(ModAttributes.DAMAGE_REDUCTION.get()).getValue()
						: 0)));
			}
		}
	}
}
