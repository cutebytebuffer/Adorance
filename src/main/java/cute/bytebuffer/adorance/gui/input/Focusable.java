package cute.bytebuffer.adorance.gui.input;

public interface Focusable {

    boolean keyPressed(int key);

    boolean charTyped(String characters);

    default void focusLost() {
    }
}
