package cute.bytebuffer.adorance.util.render.font;

import cute.bytebuffer.adorance.Adorance;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class Fonts {

    private static final Map<FontWeight, byte[]> SOURCES = new EnumMap<>(FontWeight.class);
    private static final Map<String, FontRenderer> RENDERERS = new HashMap<>();
    private static int renderScale = -1;

    private Fonts() {
    }

    public static FontRenderer get(FontWeight weight, float size) {
        int scale = currentRenderScale();
        if (scale != renderScale) {
            close();
            renderScale = scale;
        }
        String key = weight.key() + "_" + String.format(Locale.ROOT, "%.3f", size);
        return RENDERERS.computeIfAbsent(key, ignored -> load(weight, size, key));
    }

    public static float width(FontWeight weight, float size, String text) {
        return get(weight, size).width(text);
    }

    public static float lineHeight(FontWeight weight, float size) {
        return get(weight, size).lineHeight();
    }

    public static float centeredY(FontWeight weight, float size, float top, float height) {
        return top + (height - lineHeight(weight, size)) * 0.5f;
    }

    public static void close() {
        RENDERERS.values().forEach(FontRenderer::close);
        RENDERERS.clear();
    }

    private static int currentRenderScale() {
        return Math.max(MinecraftClient.getInstance().getWindow().getScaleFactor() * 2, 4);
    }

    private static FontRenderer load(FontWeight weight, float size, String key) {
        try {
            Identifier textureId = Adorance.id("fonts/" + key.replace('.', '_'));
            FontAtlas atlas = new FontAtlas(textureId, source(weight), size * renderScale);
            return new FontRenderer(atlas, renderScale);
        } catch (IOException exception) {
            throw new UncheckedIOException("Unable to load font " + weight, exception);
        }
    }

    private static byte[] source(FontWeight weight) throws IOException {
        byte[] cached = SOURCES.get(weight);
        if (cached != null) {
            return cached;
        }
        try (InputStream stream = MinecraftClient.getInstance().getResourceManager().open(Adorance.id(weight.path()))) {
            byte[] bytes = stream.readAllBytes();
            SOURCES.put(weight, bytes);
            return bytes;
        }
    }
}
