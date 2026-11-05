package cute.bytebuffer.adorance.util.render.postprocess;

import cute.bytebuffer.adorance.util.render.gpu.ManagedFramebuffer;
import net.minecraft.client.render.RenderLayer;

import java.util.ArrayDeque;
import java.util.Deque;

public final class Capture implements AutoCloseable {

    private static final Deque<Capture> ACTIVE = new ArrayDeque<>();

    private final ManagedFramebuffer target;
    private boolean closed;

    private Capture(ManagedFramebuffer target) {
        this.target = target;
    }

    static Capture begin(ManagedFramebuffer target) {
        Capture capture = new Capture(target);
        ACTIVE.push(capture);
        return capture;
    }

    public static boolean isActive() {
        return !ACTIVE.isEmpty();
    }

    public static RenderLayer retarget(RenderLayer layer) {
        Capture capture = ACTIVE.peek();
        return capture == null ? layer : capture.target.retarget(layer);
    }

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            ACTIVE.remove(this);
        }
    }
}
