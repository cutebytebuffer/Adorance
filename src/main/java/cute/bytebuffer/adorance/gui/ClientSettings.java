package cute.bytebuffer.adorance.gui;

import cute.bytebuffer.adorance.module.setting.Configurable;
import cute.bytebuffer.adorance.module.setting.KeybindSetting;
import org.lwjgl.glfw.GLFW;

public final class ClientSettings extends Configurable {
    private static final ClientSettings INSTANCE = new ClientSettings();

    private final KeybindSetting clickGuiKey = register(
            new KeybindSetting("Click GUI Key", GLFW.GLFW_KEY_RIGHT_SHIFT));

    private ClientSettings() {
        super("Client");
    }

    public static ClientSettings get() {
        return INSTANCE;
    }

    public KeybindSetting clickGuiKey() {
        return clickGuiKey;
    }
}
