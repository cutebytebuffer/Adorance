package cute.bytebuffer.adorance.util.render.gpu;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import cute.bytebuffer.adorance.Adorance;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.render.VertexFormats;

public final class Pipelines {

    private static final String FULLSCREEN_VERTEX = "common/fullscreen";

    private Pipelines() {
    }

    public static RenderPipeline.Builder fullscreen(String name, String fragmentShader) {
        return RenderPipeline.builder()
                .withLocation(Adorance.id("pipeline/" + name))
                .withVertexShader(Adorance.id(FULLSCREEN_VERTEX))
                .withFragmentShader(Adorance.id(fragmentShader))
                .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                .withDepthWrite(false)
                .withVertexFormat(VertexFormats.EMPTY, VertexFormat.DrawMode.TRIANGLES);
    }

    public static RenderPipeline translucent(String name, String fragmentShader, String uniformBlock, String... samplers) {
        RenderPipeline.Builder builder = fullscreen(name, fragmentShader)
                .withUniform(uniformBlock, UniformType.UNIFORM_BUFFER)
                .withBlend(BlendFunction.TRANSLUCENT);
        for (String sampler : samplers) {
            builder.withSampler(sampler);
        }
        return builder.build();
    }

    public static RenderPipeline opaque(String name, String fragmentShader, String uniformBlock, String... samplers) {
        RenderPipeline.Builder builder = fullscreen(name, fragmentShader)
                .withUniform(uniformBlock, UniformType.UNIFORM_BUFFER);
        for (String sampler : samplers) {
            builder.withSampler(sampler);
        }
        return builder.build();
    }
}
