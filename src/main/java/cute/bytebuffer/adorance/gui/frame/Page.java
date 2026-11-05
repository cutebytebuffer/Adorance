package cute.bytebuffer.adorance.gui.frame;

import cute.bytebuffer.adorance.util.Rect;
import net.minecraft.util.Identifier;

interface Page {

    String title();

    Identifier icon();

    default boolean pinned() {
        return false;
    }

    default boolean searchable() {
        return false;
    }

    default void applyQuery(String query) {
    }

    void show(float delaySeconds);

    void render(Rect area, double mouseX, double mouseY, float dt);

    boolean mouseClicked(double mouseX, double mouseY, int button);

    boolean mouseScrolled(double mouseX, double mouseY, double amount);
}
