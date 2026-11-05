package cute.bytebuffer.adorance.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class KeybindSetting extends Setting<Integer> {

    public static final int NONE = GLFW.GLFW_KEY_UNKNOWN;

    public KeybindSetting(String name, int defaultKey) {
        super(name, defaultKey);
    }

    public boolean isBound() {
        return get() != NONE;
    }

    public boolean matches(int key) {
        return isBound() && get() == key;
    }

    public void clear() {
        set(NONE);
    }

    public String keyName() {
        if (!isBound()) {
            return "None";
        }
        return InputUtil.Type.KEYSYM.createFromCode(get()).getLocalizedText().getString();
    }

    @Override
    protected Integer sanitize(Integer value) {
        return value == null ? NONE : value;
    }

    @Override
    public JsonElement serialize() {
        return new JsonPrimitive(get());
    }

    @Override
    public void deserialize(JsonElement json) {
        set(json.getAsInt());
    }
}
