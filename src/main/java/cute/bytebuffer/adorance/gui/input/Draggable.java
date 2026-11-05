package cute.bytebuffer.adorance.gui.input;

public interface Draggable {

    void drag(double mouseX, double mouseY);

    default void release() {
    }
}
