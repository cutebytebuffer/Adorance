package cute.bytebuffer.adorance.util.render.postprocess;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;

import java.util.ArrayList;
import java.util.List;

public final class PostProcessing {

    private static final List<PostProcessShader> SHADERS = new ArrayList<>();

    private PostProcessing() {
    }

    public static <S extends PostProcessShader> S register(S shader) {
        SHADERS.add(shader);
        return shader;
    }

    public static void beginFrame() {
        for (PostProcessShader shader : SHADERS) {
            shader.beginFrame();
        }
    }

    public static void composite() {
        for (PostProcessShader shader : SHADERS) {
            shader.composite();
        }
    }

    public static void resize() {
        Framebuffer main = MinecraftClient.getInstance().getFramebuffer();
        for (PostProcessShader shader : SHADERS) {
            shader.resize(main.textureWidth, main.textureHeight);
        }
    }

    public static void destroy() {
        for (PostProcessShader shader : SHADERS) {
            shader.destroy();
        }
    }
}
