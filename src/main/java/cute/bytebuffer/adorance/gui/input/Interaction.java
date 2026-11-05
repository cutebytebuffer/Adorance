package cute.bytebuffer.adorance.gui.input;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public final class Interaction {

    private static Focusable focused;
    private static Draggable dragged;
    private static boolean focusClaimed;

    private Interaction() {
    }

    public static void pointerDown() {
        focusClaimed = false;
    }

    public static void pointerHandled() {
        if (!focusClaimed) {
            clearFocus();
        }
    }

    public static void focus(Focusable target) {
        focusClaimed = true;
        if (focused == target) {
            return;
        }
        Focusable previous = focused;
        focused = target;
        if (previous != null) {
            previous.focusLost();
        }
    }

    public static void clearFocus() {
        focus(null);
    }

    public static boolean isFocused(Focusable target) {
        return target != null && focused == target;
    }

    public static boolean hasFocus() {
        return focused != null;
    }

    public static void beginDrag(Draggable target, double mouseX, double mouseY) {
        dragged = target;
        target.drag(mouseX, mouseY);
    }

    public static boolean isDragging(Draggable target) {
        return target != null && dragged == target;
    }

    public static boolean drag(double mouseX, double mouseY) {
        if (dragged == null) {
            return false;
        }
        dragged.drag(mouseX, mouseY);
        return true;
    }

    public static void release() {
        Draggable previous = dragged;
        dragged = null;
        if (previous != null) {
            previous.release();
        }
    }

    public static boolean keyPressed(int key) {
        return focused != null && focused.keyPressed(key);
    }

    public static boolean charTyped(String characters) {
        return focused != null && focused.charTyped(characters);
    }

    public static boolean isControlDown() {
        long handle = MinecraftClient.getInstance().getWindow().getHandle();
        return GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_LEFT_SUPER) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_RIGHT_SUPER) == GLFW.GLFW_PRESS;
    }

    public static void reset() {
        release();
        clearFocus();
    }
}
