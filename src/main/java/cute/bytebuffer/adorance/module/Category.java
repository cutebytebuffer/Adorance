package cute.bytebuffer.adorance.module;

import cute.bytebuffer.adorance.Adorance;
import net.minecraft.util.Identifier;

public enum Category {
    OPTIMIZERS("Optimizers", "optimizers"),
    RENDER("Render", "render"),
    MISC("Misc", "misc");

    private final String displayName;
    private final Identifier icon;

    Category(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = Adorance.id("icons/" + icon + ".png");
    }

    public String displayName() {
        return displayName;
    }

    public Identifier icon() {
        return icon;
    }
}
