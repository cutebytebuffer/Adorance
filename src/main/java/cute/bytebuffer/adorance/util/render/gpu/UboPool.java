package cute.bytebuffer.adorance.util.render.gpu;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class UboPool {

    private static final class Slots {
        private final List<GpuBuffer> buffers = new ArrayList<>();
        private long frame = -1L;
        private int used;
    }

    private static final Map<String, Slots> POOL = new HashMap<>();
    private static long frame;

    private UboPool() {
    }

    public static void nextFrame() {
        frame++;
    }

    public static GpuBuffer build(String label, int size, Consumer<Std140Builder> writer) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            Std140Builder builder = Std140Builder.onStack(stack, size);
            writer.accept(builder);
            return upload(label, builder.get());
        }
    }

    public static GpuBuffer upload(String label, ByteBuffer data) {
        GpuDevice device = RenderSystem.getDevice();
        int size = data.remaining();
        Slots slots = POOL.computeIfAbsent(label + "#" + size, key -> new Slots());
        if (slots.frame != frame) {
            slots.frame = frame;
            slots.used = 0;
        }
        GpuBuffer buffer = slots.used < slots.buffers.size() ? slots.buffers.get(slots.used) : null;
        if (buffer == null || buffer.isClosed()) {
            buffer = device.createBuffer(() -> label, GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, size);
            if (slots.used < slots.buffers.size()) {
                slots.buffers.set(slots.used, buffer);
            } else {
                slots.buffers.add(buffer);
            }
        }
        slots.used++;
        device.createCommandEncoder().writeToBuffer(buffer.slice(), data);
        return buffer;
    }

    public static void clear() {
        for (Slots slots : POOL.values()) {
            for (GpuBuffer buffer : slots.buffers) {
                if (!buffer.isClosed()) {
                    buffer.close();
                }
            }
        }
        POOL.clear();
    }
}
