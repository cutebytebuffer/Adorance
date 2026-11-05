package cute.bytebuffer.adorance.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.Locale;

public final class ColorSetting extends Setting<Integer> {

    public ColorSetting(String name, int defaultArgb) {
        super(name, defaultArgb);
    }

    public int argb() {
        return get();
    }

    public int alpha() {
        return get() >>> 24;
    }

    public int rgb() {
        return get() & 0x00FFFFFF;
    }

    @Override
    protected Integer sanitize(Integer value) {
        return value == null ? defaultValue() : value;
    }

    @Override
    public JsonElement serialize() {
        return new JsonPrimitive(String.format(Locale.ROOT, "#%08X", get()));
    }

    @Override
    public void deserialize(JsonElement json) {
        JsonPrimitive primitive = json.getAsJsonPrimitive();
        if (primitive.isNumber()) {
            set(primitive.getAsInt());
            return;
        }
        String hex = primitive.getAsString().replace("#", "");
        int parsed = (int) Long.parseLong(hex, 16);
        set(hex.length() <= 6 ? 0xFF000000 | parsed : parsed);
    }
}
