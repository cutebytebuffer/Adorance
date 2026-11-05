package cute.bytebuffer.adorance.util.render.font;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

final class FontAtlas implements AutoCloseable {

    private static final int FIRST_CHAR = 32;
    private static final int CHAR_COUNT = 96;
    private static final int SDF_PADDING = 5;
    private static final byte SDF_ON_EDGE = (byte) 128;
    private static final float SDF_DISTANCE_SCALE = 128f;
    private static final int MIN_ATLAS_SIZE = 512;
    private static final int MAX_ATLAS_SIZE = 4096;

    private final Identifier textureId;
    private final Int2ObjectOpenHashMap<Glyph> glyphs = new Int2ObjectOpenHashMap<>();
    private final float ascent;
    private final float lineHeight;
    private NativeImageBackedTexture texture;

    FontAtlas(Identifier textureId, byte[] ttf, float pixelHeight) throws IOException {
        this.textureId = textureId;
        ByteBuffer fontData = MemoryUtil.memAlloc(ttf.length);
        ByteBuffer bitmap = null;
        try {
            fontData.put(ttf).flip();
            STBTTFontinfo font = STBTTFontinfo.create();
            if (!STBTruetype.stbtt_InitFont(font, fontData)) {
                throw new IOException("Unable to parse font for " + textureId);
            }
            float scale = STBTruetype.stbtt_ScaleForPixelHeight(font, pixelHeight);
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer ascentBuffer = stack.mallocInt(1);
                IntBuffer descentBuffer = stack.mallocInt(1);
                IntBuffer lineGapBuffer = stack.mallocInt(1);
                STBTruetype.stbtt_GetFontVMetrics(font, ascentBuffer, descentBuffer, lineGapBuffer);
                this.ascent = ascentBuffer.get(0) * scale;
                this.lineHeight = (ascentBuffer.get(0) - descentBuffer.get(0) + lineGapBuffer.get(0)) * scale;
            }
            int size = atlasSize(pixelHeight);
            bitmap = MemoryUtil.memCalloc(size * size);
            rasterize(font, scale, bitmap, size);
            upload(bitmap, size);
        } finally {
            if (bitmap != null) {
                MemoryUtil.memFree(bitmap);
            }
            MemoryUtil.memFree(fontData);
        }
    }

    private void rasterize(STBTTFontinfo font, float scale, ByteBuffer bitmap, int size) throws IOException {
        int penX = 0;
        int penY = 0;
        int rowHeight = 0;
        for (int codepoint = FIRST_CHAR; codepoint < FIRST_CHAR + CHAR_COUNT; codepoint++) {
            int glyphIndex = STBTruetype.stbtt_FindGlyphIndex(font, codepoint);
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer width = stack.mallocInt(1);
                IntBuffer height = stack.mallocInt(1);
                IntBuffer offsetX = stack.mallocInt(1);
                IntBuffer offsetY = stack.mallocInt(1);
                IntBuffer advance = stack.mallocInt(1);
                IntBuffer bearing = stack.mallocInt(1);
                STBTruetype.stbtt_GetGlyphHMetrics(font, glyphIndex, advance, bearing);
                float advancePx = advance.get(0) * scale;

                ByteBuffer sdf = STBTruetype.stbtt_GetGlyphSDF(font, scale, glyphIndex, SDF_PADDING, SDF_ON_EDGE,
                        SDF_DISTANCE_SCALE, width, height, offsetX, offsetY);
                if (sdf == null) {
                    glyphs.put(codepoint, Glyph.blank(advancePx));
                    continue;
                }
                int glyphWidth = width.get(0);
                int glyphHeight = height.get(0);
                if (penX + glyphWidth >= size) {
                    penX = 0;
                    penY += rowHeight;
                    rowHeight = 0;
                }
                if (penY + glyphHeight >= size) {
                    STBTruetype.stbtt_FreeSDF(sdf);
                    throw new IOException("Font atlas overflow for " + textureId);
                }
                for (int row = 0; row < glyphHeight; row++) {
                    for (int column = 0; column < glyphWidth; column++) {
                        bitmap.put((penY + row) * size + penX + column, sdf.get(row * glyphWidth + column));
                    }
                }
                STBTruetype.stbtt_FreeSDF(sdf);
                glyphs.put(codepoint, new Glyph(
                        (float) penX / size, (float) (penX + glyphWidth) / size,
                        (float) penY / size, (float) (penY + glyphHeight) / size,
                        offsetX.get(0), offsetY.get(0), glyphWidth, glyphHeight, advancePx));
                penX += glyphWidth + 1;
                rowHeight = Math.max(rowHeight, glyphHeight);
            }
        }
    }

    private void upload(ByteBuffer bitmap, int size) {
        NativeImage image = new NativeImage(NativeImage.Format.RGBA, size, size, false);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int alpha = Byte.toUnsignedInt(bitmap.get(y * size + x));
                image.setColor(x, y, (alpha << 24) | 0x00FFFFFF);
            }
        }
        texture = new NativeImageBackedTexture(textureId::toString, image) {
            {
                this.sampler = RenderSystem.getSamplerCache().getRepeated(FilterMode.LINEAR);
            }
        };
        MinecraftClient.getInstance().getTextureManager().registerTexture(textureId, texture);
    }

    private static int atlasSize(float pixelHeight) {
        double cell = pixelHeight + SDF_PADDING * 2.0 + 2.0;
        double required = Math.sqrt(CHAR_COUNT) * cell;
        int size = MIN_ATLAS_SIZE;
        while (size < required && size < MAX_ATLAS_SIZE) {
            size <<= 1;
        }
        return size;
    }

    Identifier textureId() {
        return textureId;
    }

    Glyph glyph(int codepoint) {
        return glyphs.get(codepoint);
    }

    float ascent() {
        return ascent;
    }

    float lineHeight() {
        return lineHeight;
    }

    @Override
    public void close() {
        if (texture != null) {
            MinecraftClient.getInstance().getTextureManager().destroyTexture(textureId);
            texture = null;
        }
    }
}
