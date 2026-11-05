package cute.bytebuffer.adorance.module.setting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import cute.bytebuffer.adorance.util.Names;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class MultiModeSetting<E extends Enum<E>> extends Setting<Set<E>> {

    private final Class<E> type;
    private final E[] modes;
    private final List<String> labels;

    @SafeVarargs
    public MultiModeSetting(String name, Class<E> type, E... defaults) {
        super(name, frozen(type, Arrays.asList(defaults)));
        this.type = type;
        this.modes = type.getEnumConstants();
        this.labels = Arrays.stream(modes).map(Names::of).toList();
    }

    private static <E extends Enum<E>> Set<E> frozen(Class<E> type, Iterable<E> values) {
        EnumSet<E> set = EnumSet.noneOf(type);
        for (E value : values) {
            if (value != null) {
                set.add(value);
            }
        }
        return Collections.unmodifiableSet(set);
    }

    public boolean isEnabled(E mode) {
        return get().contains(mode);
    }

    public boolean isEnabled(int index) {
        return get().contains(modes[index]);
    }

    public void toggle(int index) {
        EnumSet<E> next = EnumSet.noneOf(type);
        next.addAll(get());
        E mode = modes[index];
        if (!next.remove(mode)) {
            next.add(mode);
        }
        set(next);
    }

    public List<String> labels() {
        return labels;
    }

    @Override
    protected Set<E> sanitize(Set<E> value) {
        return value == null ? defaultValue() : frozen(type, value);
    }

    @Override
    public JsonElement serialize() {
        JsonArray array = new JsonArray();
        get().forEach(mode -> array.add(mode.name()));
        return array;
    }

    @Override
    public void deserialize(JsonElement json) {
        EnumSet<E> next = EnumSet.noneOf(type);
        for (JsonElement element : json.getAsJsonArray()) {
            String name = element.getAsString();
            for (E mode : modes) {
                if (mode.name().equals(name)) {
                    next.add(mode);
                }
            }
        }
        set(next);
    }
}
