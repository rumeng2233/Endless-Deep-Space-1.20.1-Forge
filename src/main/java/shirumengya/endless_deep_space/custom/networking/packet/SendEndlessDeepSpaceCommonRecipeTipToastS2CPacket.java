package shirumengya.endless_deep_space.custom.networking.packet;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import shirumengya.endless_deep_space.custom.client.gui.toasts.EndlessDeepSpaceCommonRecipeTipToast;

import java.util.function.Supplier;

public class SendEndlessDeepSpaceCommonRecipeTipToastS2CPacket {
	private final ItemStack item;
	private final ItemStack recipeitem;
	private final Component title;
	private final Component description;
	private final long time;
	private final int titlecolor;
	private final int descriptioncolor;
	private static final Logger LOGGER = LogUtils.getLogger();
	
    public SendEndlessDeepSpaceCommonRecipeTipToastS2CPacket(ItemStack item, ItemStack recipeitem, Component title, Component description, long time, int titlecolor, int descriptioncolor) {
    	this.item = item;
    	this.recipeitem = recipeitem;
    	this.title = title;
    	this.description = description;
    	this.time = time;
    	this.titlecolor = titlecolor;
    	this.descriptioncolor = descriptioncolor;
    }

    public SendEndlessDeepSpaceCommonRecipeTipToastS2CPacket(FriendlyByteBuf buf) {
    	this.item = buf.readItem();
    	this.recipeitem = buf.readItem();
    	this.title = buf.readComponent();
    	this.description = buf.readComponent();
    	this.time = buf.readLong();
    	this.titlecolor = buf.readInt();
    	this.descriptioncolor = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
    	buf.writeItem(item);
    	buf.writeItem(recipeitem);
    	buf.writeComponent(title);
    	buf.writeComponent(description);
    	buf.writeLong(time);
    	buf.writeInt(titlecolor);
    	buf.writeInt(descriptioncolor);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            LOGGER.info("Send [ItemStack Namespace:{};\nRecipeItemStack Namespace:{};\nTitle:{};\nDescription:{};\nTime:{};\nTitle Colour:{};\nDescription Colour:{}] to Client", ForgeRegistries.ITEMS.getKey(item.getItem()).toString(), ForgeRegistries.ITEMS.getKey(recipeitem.getItem()).toString(), title.toString(), description.toString(), time, titlecolor, descriptioncolor);
        	ToastComponent toastcomponent = Minecraft.getInstance().getToasts();
        	EndlessDeepSpaceCommonRecipeTipToast.add(toastcomponent, item, recipeitem, title, description, time, titlecolor, descriptioncolor);
        });
        return true;
    }

}
