package cute.bytebuffer.adorance.util.render.gpu;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import cute.bytebuffer.adorance.util.render.RenderState;
import net.minecraft.client.gl.Framebuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public final class PassBuilder {

    private record TextureBinding(String name, GpuTextureView view, FilterMode filter) {
    }

    private record UniformBinding(String name, GpuBuffer buffer) {
    }

    private final String label;
    private final List<TextureBinding> textures = new ArrayList<>();
    private final List<UniformBinding> uniforms = new ArrayList<>();
    private RenderPipeline pipeline;
    private Framebuffer target;
    private Framebuffer depthTarget;
    private RenderState.ScissorRect scissor;

    private PassBuilder(String label) {
        this.label = label;
    }

    public static PassBuilder create(String label) {
        return new PassBuilder(label);
    }

    public PassBuilder pipeline(RenderPipeline pipeline) {
        this.pipeline = pipeline;
        return this;
    }

    public PassBuilder target(Framebuffer target) {
        this.target = target;
        return this;
    }

    public PassBuilder depthTarget(Framebuffer depthTarget) {
        this.depthTarget = depthTarget;
        return this;
    }

    public PassBuilder scissor(RenderState.ScissorRect scissor) {
        this.scissor = scissor;
        return this;
    }

    public PassBuilder texture(String name, GpuTextureView view, FilterMode filter) {
        textures.add(new TextureBinding(name, view, filter));
        return this;
    }

    public PassBuilder sampler(String name, Framebuffer source) {
        return texture(name, source.getColorAttachmentView(), FilterMode.LINEAR);
    }

    public PassBuilder depthSampler(String name, Framebuffer source) {
        return texture(name, source.getDepthAttachmentView(), FilterMode.NEAREST);
    }

    public PassBuilder uniform(String name, GpuBuffer buffer) {
        uniforms.add(new UniformBinding(name, buffer));
        return this;
    }

    public void draw() {
        if (!isComplete()) {
            return;
        }
        try (RenderPass pass = open()) {
            pass.setPipeline(pipeline);
            if (scissor != null) {
                pass.enableScissor(scissor.x(), scissor.y(), scissor.width(), scissor.height());
            }
            for (TextureBinding binding : textures) {
                pass.bindTexture(binding.name(), binding.view(), RenderSystem.getSamplerCache().get(binding.filter()));
            }
            for (UniformBinding binding : uniforms) {
                pass.setUniform(binding.name(), binding.buffer());
            }
            pass.draw(0, 3);
        } catch (RuntimeException exception) {
            RenderErrors.report("pass " + label, exception);
        }
    }

    private boolean isComplete() {
        if (pipeline == null || target == null || target.getColorAttachmentView() == null) {
            RenderErrors.warnOnce("pass-target:" + label, "Pass {} skipped: missing pipeline or color target", label);
            return false;
        }
        for (TextureBinding binding : textures) {
            if (binding.view() == null) {
                RenderErrors.warnOnce("pass-sampler:" + label + binding.name(), "Pass {} skipped: sampler {} has no texture", label, binding.name());
                return false;
            }
        }
        if (depthTarget != null && depthTarget.getDepthAttachmentView() == null) {
            RenderErrors.warnOnce("pass-depth:" + label, "Pass {} skipped: depth target has no depth attachment", label);
            return false;
        }
        return scissor == null || scissor.width() > 0 && scissor.height() > 0;
    }

    private RenderPass open() {
        var encoder = RenderSystem.getDevice().createCommandEncoder();
        if (depthTarget == null) {
            return encoder.createRenderPass(() -> label, target.getColorAttachmentView(), OptionalInt.empty());
        }
        return encoder.createRenderPass(() -> label, target.getColorAttachmentView(), OptionalInt.empty(),
                depthTarget.getDepthAttachmentView(), OptionalDouble.empty());
    }
}
