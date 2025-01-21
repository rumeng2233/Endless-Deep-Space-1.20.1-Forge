package shirumengya.endless_deep_space.custom.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import shirumengya.endless_deep_space.custom.world.explosion.CustomExplosion;

import java.util.function.Supplier;

public class MushroomCloudS2CPacket {
	private final double x;
	private final double y;
	private final double z;
	private final double radius;
	
    public MushroomCloudS2CPacket(double x, double y, double z, double radius) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.radius = radius;
    }

    public MushroomCloudS2CPacket(FriendlyByteBuf buf) {
		this.x = buf.readDouble();
		this.y = buf.readDouble();
		this.z = buf.readDouble();
		this.radius = buf.readDouble();
    }

    public void toBytes(FriendlyByteBuf buf) {
		buf.writeDouble(x);
		buf.writeDouble(y);
		buf.writeDouble(z);
		buf.writeDouble(radius);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
        	CustomExplosion.mushroomCloud(x, y, z, radius);
        });
        return true;
    }

}
