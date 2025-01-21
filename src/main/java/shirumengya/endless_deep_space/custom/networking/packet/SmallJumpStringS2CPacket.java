package shirumengya.endless_deep_space.custom.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;
import shirumengya.endless_deep_space.custom.client.gui.overlay.JumpStringOverlay;

import java.util.function.Supplier;

public class SmallJumpStringS2CPacket {
	private final Component jumpString;
	private final int jumpTime;
	private final boolean animateJumpMessageColor;
	private final boolean jumpStringCanUpdate;
	
    public SmallJumpStringS2CPacket(Component string, int time, boolean animateColor, boolean canUpdate) {
		this.jumpString = string;
		this.jumpTime = time;
		this.animateJumpMessageColor = animateColor;
		this.jumpStringCanUpdate = canUpdate;
    }

    public SmallJumpStringS2CPacket(FriendlyByteBuf buf) {
    	this.jumpString = buf.readComponent();
		this.jumpTime = buf.readInt();
		this.animateJumpMessageColor = buf.readBoolean();
		this.jumpStringCanUpdate = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
		buf.writeComponent(jumpString);
		buf.writeInt(jumpTime);
		buf.writeBoolean(animateJumpMessageColor);
		buf.writeBoolean(jumpStringCanUpdate);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            JumpStringOverlay.UpdateSmallJumpString(jumpString, jumpTime, animateJumpMessageColor, jumpStringCanUpdate);
        });
        return true;
    }

}
