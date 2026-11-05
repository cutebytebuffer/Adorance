package cute.bytebuffer.adorance.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class BooleanSetting extends Setting<Boolean> {

    public BooleanSetting(String name, boolean defaultValue) {
        super(name, defaultValue);
    }

    public void toggle() {
        set(!get());
    }

    @Override
    protected Boolean sanitize(Boolean value) {
        return value != null && value;
    }

    @Override
    public JsonElement serialize() {
        return new JsonPrimitive(get());
    }

    @Override
    public void deserialize(JsonElement json) {
        set(json.getAsBoolean());
    }
}
