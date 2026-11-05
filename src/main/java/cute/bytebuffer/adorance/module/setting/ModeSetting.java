package cute.bytebuffer.adorance.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import cute.bytebuffer.adorance.util.Names;

import java.util.Arrays;
import java.util.List;

public final class ModeSetting<E extends Enum<E>> extends Setting<E> {

    private final E[] modes;
    private final List<String> labels;

    public ModeSetting(String name, E defaultValue) {
        super(name, defaultValue);
        this.modes = defaultValue.getDeclaringClass().getEnumConstants();
        this.labels = Arrays.stream(modes).map(Names::of).toList();
    }

    public boolean is(E mode) {
        return get() == mode;
    }

    public List<String> labels() {
        return labels;
    }

    public int index() {
        return get().ordinal();
    }

    public void select(int index) {
        set(modes[Math.floorMod(index, modes.length)]);
    }

    @Override
    protected E sanitize(E value) {
        return value == null ? defaultValue() : value;
    }

    @Override
    public JsonElement serialize() {
        return new JsonPrimitive(get().name());
    }

    @Override
    public void deserialize(JsonElement json) {
        String name = json.getAsString();
        for (E mode : modes) {
            if (mode.name().equals(name)) {
                set(mode);
                return;
            }
        }
    }
}
