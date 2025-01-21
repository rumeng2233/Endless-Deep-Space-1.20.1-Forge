package shirumengya.endless_deep_space.custom.networking.packet;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;
import org.slf4j.Logger;
import shirumengya.endless_deep_space.custom.client.gui.toasts.EndlessDeepSpaceCommonToast;

import java.util.function.Supplier;

public class SendEndlessDeepSpaceCommonMessageToastS2CPacket {
	private final Component title;
	private final Component description;
	private final long time;
	private final int titlecolor;
	private final int descriptioncolor;
	private static final Logger LOGGER = LogUtils.getLogger();
	
    public SendEndlessDeepSpaceCommonMessageToastS2CPacket(Component title, Component description, long time, int titlecolor, int descriptioncolor) {
    	this.title = title;
    	this.description = description;
    	this.time = time;
    	this.titlecolor = titlecolor;
    	this.descriptioncolor = descriptioncolor;
    }

    public SendEndlessDeepSpaceCommonMessageToastS2CPacket(FriendlyByteBuf buf) {
    	this.title = buf.readComponent();
    	this.description = buf.readComponent();
    	this.time = buf.readLong();
    	this.titlecolor = buf.readInt();
    	this.descriptioncolor = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
    	buf.writeComponent(title);
    	buf.writeComponent(description);
    	buf.writeLong(time);
    	buf.writeInt(titlecolor);
    	buf.writeInt(descriptioncolor);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            LOGGER.info("Send [Title:{};\nDescription:{};\nTime:{};\nTitle Colour:{};\nDescription Colour:{}] to Client", title.toString(), description.toString(), time, titlecolor, descriptioncolor);
        	ToastComponent toastcomponent = Minecraft.getInstance().getToasts();
        	EndlessDeepSpaceCommonToast.addOrUpdate(toastcomponent, null, title, description, time, titlecolor, descriptioncolor);
        });
        return true;
    }

}
