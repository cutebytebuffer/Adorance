package cute.bytebuffer.adorance.module.setting;

import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public abstract sealed class Setting<T> permits BooleanSetting, NumberSetting, ModeSetting, MultiModeSetting,
        ColorSetting, StringSetting, KeybindSetting {

    private final String name;
    private final T defaultValue;
    private final List<Consumer<T>> listeners = new ArrayList<>();
    private BooleanSupplier visibility = () -> true;
    private T value;

    protected Setting(String name, T defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public final String name() {
        return name;
    }

    public final T get() {
        return value;
    }

    public final T defaultValue() {
        return defaultValue;
    }

    public final void set(T value) {
        T next = sanitize(value);
        if (Objects.equals(this.value, next)) {
            return;
        }
        this.value = next;
        for (Consumer<T> listener : listeners) {
            listener.accept(next);
        }
    }

    public final void reset() {
        set(defaultValue);
    }

    public final void onChange(Consumer<T> listener) {
        listeners.add(listener);
    }

    @SuppressWarnings("unchecked")
    public final <S extends Setting<T>> S visibleWhen(BooleanSupplier condition) {
        BooleanSupplier previous = visibility;
        visibility = () -> previous.getAsBoolean() && condition.getAsBoolean();
        return (S) this;
    }

    public final boolean isVisible() {
        return visibility.getAsBoolean();
    }

    protected T sanitize(T value) {
        return value;
    }

    public abstract JsonElement serialize();

    public abstract void deserialize(JsonElement json);
}
