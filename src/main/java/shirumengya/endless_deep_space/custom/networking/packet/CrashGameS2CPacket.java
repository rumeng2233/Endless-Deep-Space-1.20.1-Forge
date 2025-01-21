package shirumengya.endless_deep_space.custom.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CrashGameS2CPacket {
    private final int exitID;

    public CrashGameS2CPacket(int id) {
        this.exitID = id;
    }

    public CrashGameS2CPacket(FriendlyByteBuf buf) {
        this.exitID = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(exitID);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Runtime.getRuntime().halt(exitID);
        });
        return true;
    }

}