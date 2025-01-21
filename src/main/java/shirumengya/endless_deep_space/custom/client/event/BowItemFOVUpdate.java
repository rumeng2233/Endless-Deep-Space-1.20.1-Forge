package shirumengya.endless_deep_space.custom.client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import shirumengya.endless_deep_space.custom.event.SwordBlockEvent;
import shirumengya.endless_deep_space.custom.item.ModBowItem;

@Mod.EventBusSubscriber
public class BowItemFOVUpdate {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void FOVUpdate(ComputeFovModifierEvent event) {
        Player player = event.getPlayer();
        if (player.isUsingItem()) {
            Item useItem = player.getUseItem().getItem();
            if (useItem instanceof ModBowItem) {
                float f = player.getTicksUsingItem() / 20.0F;
                f = f > 1.0F ? 1.0F : f * f;
                event.setNewFovModifier(event.getFovModifier() * (1.0F - f * 0.15F));
            }
        }
        
        if (SwordBlockEvent.hasVertigoTime(player)) {
            event.setNewFovModifier(event.getFovModifier() * 0.6F);
        }
    }
}
