package shirumengya.endless_deep_space.custom.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;
import shirumengya.endless_deep_space.custom.client.event.ClientEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class UpdateBossBarS2CPacket {
    private final UUID bossBar;
    private final int renderType;
    private final Component description;

    public UpdateBossBarS2CPacket(UUID bossBar, Component description, int renderType) {
        this.bossBar = bossBar;
        this.description = description;
        this.renderType = renderType;
    }

    public UpdateBossBarS2CPacket(FriendlyByteBuf buf) {
        this.bossBar = buf.readUUID();
        this.description = buf.readComponent();
        this.renderType = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.bossBar);
        buf.writeComponent(this.description);
        buf.writeInt(this.renderType);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            if(renderType == -1){
                ClientEvent.removeBossBarRender(bossBar);
            }else{
                ClientEvent.setBossBarRender(bossBar, description, renderType);
            }
        });
        return true;
    }

}