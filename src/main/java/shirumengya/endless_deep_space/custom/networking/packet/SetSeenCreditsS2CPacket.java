package shirumengya.endless_deep_space.custom.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import shirumengya.endless_deep_space.custom.client.sounds.ModMusicManager;

import java.util.function.Supplier;

public class SetSeenCreditsS2CPacket {
    private final boolean seenCredits;

    public SetSeenCreditsS2CPacket(boolean seenCredits) {
        this.seenCredits = seenCredits;
    }

    public SetSeenCreditsS2CPacket(FriendlyByteBuf buf) {
        this.seenCredits = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(seenCredits);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ModMusicManager.seenCredits = this.seenCredits;
        });
        return true;
    }

}