package cute.bytebuffer.adorance.util.render.postprocess;

import net.minecraft.client.render.VertexConsumer;

public final class WrappedVertexConsumer implements VertexConsumer {

    private final VertexConsumer delegate;

    public WrappedVertexConsumer(VertexConsumer delegate) {
        this.delegate = delegate;
    }

    @Override
    public VertexConsumer vertex(float x, float y, float z) {
        delegate.vertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        delegate.color(red, green, blue, alpha);
        return this;
    }

    @Override
    public VertexConsumer color(int argb) {
        delegate.color(argb);
        return this;
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        delegate.texture(u, v);
        return this;
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
        delegate.overlay(u, v);
        return this;
    }

    @Override
    public VertexConsumer light(int u, int v) {
        delegate.light(u, v);
        return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        delegate.normal(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer lineWidth(float width) {
        delegate.lineWidth(width);
        return this;
    }
}
