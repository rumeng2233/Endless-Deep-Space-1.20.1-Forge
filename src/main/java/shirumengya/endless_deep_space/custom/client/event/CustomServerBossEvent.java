package shirumengya.endless_deep_space.custom.client.event;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import shirumengya.endless_deep_space.custom.networking.ModMessages;
import shirumengya.endless_deep_space.custom.networking.packet.UpdateBossBarS2CPacket;

public class CustomServerBossEvent extends ServerBossEvent {

    private int renderType;
    private Component bossDescription;

    public CustomServerBossEvent(Component description, Component component, BossEvent.BossBarColor bossBarColor, boolean dark, int renderType) {
        super(component, bossBarColor, BossBarOverlay.PROGRESS);
        this.setDarkenScreen(dark);
        this.renderType = renderType;
        this.bossDescription = description;
    }

    public CustomServerBossEvent(Entity entity, Component component, BossEvent.BossBarColor bossBarColor, boolean dark, int renderType) {
        this(Component.translatable(entity.getType().getDescriptionId() + ".description"), component, bossBarColor, dark, renderType);
    }

    public void setRenderType(int renderType) {
        if (renderType != this.renderType) {
            this.renderType = renderType;
            ModMessages.sendToAllPlayers(new UpdateBossBarS2CPacket(this.getId(), bossDescription, renderType));
        }
    }

    public void setDescription(Component description) {
        if (!description.equals(this.bossDescription)) {
            this.bossDescription = description;
            ModMessages.sendToAllPlayers(new UpdateBossBarS2CPacket(this.getId(), description, this.renderType));
        }
    }

    public int getRenderType() {
        return this.renderType;
    }


    public void addPlayer(ServerPlayer serverPlayer) {
        ModMessages.sendToPlayer(new UpdateBossBarS2CPacket(this.getId(), bossDescription, renderType), serverPlayer);
        super.addPlayer(serverPlayer);
    }

    public void removePlayer(ServerPlayer serverPlayer) {
        ModMessages.sendToPlayer(new UpdateBossBarS2CPacket(this.getId(), bossDescription, -1), serverPlayer);
        super.removePlayer(serverPlayer);
    }

}
