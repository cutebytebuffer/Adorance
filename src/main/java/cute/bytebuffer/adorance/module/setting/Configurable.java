package cute.bytebuffer.adorance.module.setting;

import cute.bytebuffer.adorance.config.ConfigManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Configurable {

    private final String name;
    private final List<Setting<?>> settings = new ArrayList<>();
    private final List<Setting<?>> view = Collections.unmodifiableList(settings);

    protected Configurable(String name) {
        this.name = name;
    }

    protected final <S extends Setting<?>> S register(S setting) {
        settings.add(setting);
        setting.onChange(value -> ConfigManager.markDirty());
        return setting;
    }

    public final String name() {
        return name;
    }

    public final List<Setting<?>> settings() {
        return view;
    }
}
