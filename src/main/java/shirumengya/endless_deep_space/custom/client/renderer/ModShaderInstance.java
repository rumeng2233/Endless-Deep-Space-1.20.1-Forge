package shirumengya.endless_deep_space.custom.client.renderer;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class ModShaderInstance extends ShaderInstance {
    @Nullable
    public final Uniform EDS_GAME_TIME;
    private final List<Runnable> applyCallbacks = new LinkedList<>();

    public ModShaderInstance(ResourceProvider p_173336_, String p_173337_, VertexFormat p_173338_) throws IOException {
        super(p_173336_, p_173337_, p_173338_);
        this.EDS_GAME_TIME = this.getUniform("EDSGameTime");
    }

    public ModShaderInstance(ResourceProvider p_173336_, ResourceLocation shaderLocation, VertexFormat p_173338_) throws IOException {
        super(p_173336_, shaderLocation, p_173338_);
        this.EDS_GAME_TIME = this.getUniform("EDSGameTime");
    }

    public static ModShaderInstance create(ResourceProvider resourceProvider, ResourceLocation loc, VertexFormat format) {
        try {
            return new ModShaderInstance(resourceProvider, loc, format);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to initialize shader.", ex);
        }
    }

    public void onApply(Runnable callback) {
        applyCallbacks.add(callback);
    }

    @Override
    public void apply() {
        for (Runnable callback : applyCallbacks) {
            callback.run();
        }
        super.apply();
    }
}
