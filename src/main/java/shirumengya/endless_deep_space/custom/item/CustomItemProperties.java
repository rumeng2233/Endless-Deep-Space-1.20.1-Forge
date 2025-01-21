package shirumengya.endless_deep_space.custom.item;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import shirumengya.endless_deep_space.custom.init.ModItems;

public class CustomItemProperties {
    public static void addCustomItemProperties() {
		makeBow(ModItems.TRACKING_BOW.get());
		makeBow(ModItems.NORMAL_BOW.get());
    }

    public static void makeBow(Item item) {
        ItemProperties.register(item, new ResourceLocation("pull"), (p_174635_, p_174636_, p_174637_, p_174638_) -> {
            if (p_174637_ == null) {
                return 0.0F;
            } else {
                return p_174637_.getUseItem() != p_174635_ ? 0.0F : (float)(p_174635_.getUseDuration() - p_174637_.getUseItemRemainingTicks()) / 20.0F;
            }
        });

        ItemProperties.register(item, new ResourceLocation("pulling"), (p_174630_, p_174631_, p_174632_, p_174633_) -> {
            return p_174632_ != null && p_174632_.isUsingItem() && p_174632_.getUseItem() == p_174630_ ? 1.0F : 0.0F;
        });
    }

    public static void makeFishingRod(Item item) {
        ItemProperties.register(item, new ResourceLocation("cast"), (p_174585_, p_174586_, p_174587_, p_174588_) -> {
	      	if (p_174587_ == null) {
	    		return 0.0F;
	      	} else {
	    		boolean flag = p_174587_.getMainHandItem() == p_174585_;
	  			boolean flag1 = p_174587_.getOffhandItem() == p_174585_;
	            
	   			return (flag || flag1) && p_174587_ instanceof Player && ((Player)p_174587_).fishing != null ? 1.0F : 0.0F;
	 		}
   		});
   	}

    public static void makeCrossbow(Item item) {
        ItemProperties.register(item, new ResourceLocation("pull"), (p_174610_, p_174611_, p_174612_, p_174613_) -> {
	       	if (p_174612_ == null) {
	      		return 0.0F;
	      	} else {
	     		return CrossbowItem.isCharged(p_174610_) ? 0.0F : (float)(p_174610_.getUseDuration() - p_174612_.getUseItemRemainingTicks()) / (float)CrossbowItem.getChargeDuration(p_174610_);
	      	}
  		});

    	ItemProperties.register(item, new ResourceLocation("pulling"), (p_174605_, p_174606_, p_174607_, p_174608_) -> {
    		return p_174607_ != null && p_174607_.isUsingItem() && p_174607_.getUseItem() == p_174605_ && !CrossbowItem.isCharged(p_174605_) ? 1.0F : 0.0F;
   		});

   		ItemProperties.register(item, new ResourceLocation("charged"), (p_174600_, p_174601_, p_174602_, p_174603_) -> {
    		return p_174602_ != null && CrossbowItem.isCharged(p_174600_) ? 1.0F : 0.0F;
   		});

   		ItemProperties.register(item, new ResourceLocation("firework"), (p_174595_, p_174596_, p_174597_, p_174598_) -> {
     		return p_174597_ != null && CrossbowItem.isCharged(p_174595_) && CrossbowItem.containsChargedProjectile(p_174595_, Items.FIREWORK_ROCKET) ? 1.0F : 0.0F;
   		});
 	}
}
