package shirumengya.endless_deep_space.custom.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import shirumengya.endless_deep_space.custom.client.sounds.ModMusicManager;

import java.util.function.Supplier;

public class SetRecordDelayS2CPacket {
	private final int delay;
	
    public SetRecordDelayS2CPacket(int delay) {
		this.delay = delay;
    }

    public SetRecordDelayS2CPacket(FriendlyByteBuf buf) {
		this.delay = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
		buf.writeInt(delay);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
			ModMusicManager.recordDelay = this.delay;
        });
        return true;
    }

}
