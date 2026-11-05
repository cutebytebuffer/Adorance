package cute.bytebuffer.adorance.util.render.font;

import com.mojang.blaze3d.systems.RenderSystem;
import cute.bytebuffer.adorance.util.render.ColorUtil;
import cute.bytebuffer.adorance.util.render.RenderState;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.BufferAllocator;
import org.joml.Matrix3x2fc;

public final class FontRenderer implements AutoCloseable {

    private static final int FULL_BRIGHT = LightmapTextureManager.MAX_LIGHT_COORDINATE;
    private static final int BUFFER_SIZE = 1 << 18;

    private final FontAtlas atlas;
    private final float renderScale;
    private final BufferAllocator allocator = new BufferAllocator(BUFFER_SIZE);
    private final VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(allocator);

    FontRenderer(FontAtlas atlas, float renderScale) {
        this.atlas = atlas;
        this.renderScale = renderScale;
    }

    public void draw(Matrix3x2fc matrix, String text, float x, float y, int argb) {
        RenderLayer layer = RenderLayers.textSeeThrough(atlas.textureId());
        emit(immediate.getBuffer(layer), matrix, text, x, y, argb);
        RenderState.ScissorRect scissor = RenderState.activeScissor();
        if (scissor != null) {
            RenderSystem.enableScissorForRenderTypeDraws(scissor.x(), scissor.y(), scissor.width(), scissor.height());
        }
        immediate.draw();
        if (scissor != null) {
            RenderSystem.disableScissorForRenderTypeDraws();
        }
    }

    public float width(String text) {
        float width = 0f;
        for (int i = 0; i < text.length(); ) {
            int codepoint = text.codePointAt(i);
            Glyph glyph = atlas.glyph(codepoint);
            if (glyph != null) {
                width += glyph.advance() / renderScale;
            }
            i += Character.charCount(codepoint);
        }
        return width;
    }

    public float lineHeight() {
        return atlas.lineHeight() / renderScale;
    }

    private void emit(VertexConsumer consumer, Matrix3x2fc matrix, String text, float x, float y, int argb) {
        int red = ColorUtil.red(argb);
        int green = ColorUtil.green(argb);
        int blue = ColorUtil.blue(argb);
        int alpha = ColorUtil.alpha(argb);
        float cursor = x;
        for (int i = 0; i < text.length(); ) {
            int codepoint = text.codePointAt(i);
            i += Character.charCount(codepoint);
            Glyph glyph = atlas.glyph(codepoint);
            if (glyph == null) {
                continue;
            }
            if (glyph.visible()) {
                float x0 = cursor + glyph.bearingX() / renderScale;
                float y0 = y + (glyph.bearingY() + atlas.ascent()) / renderScale;
                float x1 = x0 + glyph.width() / renderScale;
                float y1 = y0 + glyph.height() / renderScale;
                consumer.vertex(matrix, x0, y0).color(red, green, blue, alpha).texture(glyph.minU(), glyph.minV()).light(FULL_BRIGHT);
                consumer.vertex(matrix, x0, y1).color(red, green, blue, alpha).texture(glyph.minU(), glyph.maxV()).light(FULL_BRIGHT);
                consumer.vertex(matrix, x1, y1).color(red, green, blue, alpha).texture(glyph.maxU(), glyph.maxV()).light(FULL_BRIGHT);
                consumer.vertex(matrix, x1, y0).color(red, green, blue, alpha).texture(glyph.maxU(), glyph.minV()).light(FULL_BRIGHT);
            }
            cursor += glyph.advance() / renderScale;
        }
    }

    @Override
    public void close() {
        allocator.close();
        atlas.close();
    }
}
