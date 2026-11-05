package cute.bytebuffer.adorance.util.render.postprocess;

import cute.bytebuffer.adorance.util.render.gpu.ManagedFramebuffer;
import cute.bytebuffer.adorance.util.render.gpu.RenderErrors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;

public abstract class PostProcessShader {

    private final String id;
    private final ManagedFramebuffer capture;

    protected PostProcessShader(String id) {
        this.id = id;
        this.capture = new ManagedFramebuffer(id + ":capture", true);
    }

    public final String id() {
        return id;
    }

    public final Capture capture() {
        return Capture.begin(capture);
    }

    public final SimpleFramebuffer captured() {
        return capture.get();
    }

    final void beginFrame() {
        if (isActive()) {
            capture.clear();
        }
    }

    final void composite() {
        if (!isActive()) {
            return;
        }
        try {
            Framebuffer main = MinecraftClient.getInstance().getFramebuffer();
            SimpleFramebuffer source = capture.get();
            if (main.getColorAttachmentView() != null && source.getColorAttachmentView() != null) {
                render(main, source);
            }
        } catch (RuntimeException exception) {
            RenderErrors.report("post-process " + id, exception);
        }
    }

    void resize(int width, int height) {
        capture.resize(width, height);
        onResize(width, height);
    }

    void destroy() {
        capture.destroy();
        onDestroy();
    }

    protected boolean isActive() {
        return true;
    }

    protected void onResize(int width, int height) {
    }

    protected void onDestroy() {
    }

    protected abstract void render(Framebuffer main, SimpleFramebuffer captured);
}
