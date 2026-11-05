package cute.bytebuffer.adorance.module;

import cute.bytebuffer.adorance.event.EventHandler;
import cute.bytebuffer.adorance.event.impl.KeyPressEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ModuleManager {

    private static final ModuleManager INSTANCE = new ModuleManager();

    private final List<Module> modules = new ArrayList<>();
    private final Map<Category, List<Module>> byCategory = new EnumMap<>(Category.class);

    private ModuleManager() {
        for (Category category : Category.values()) {
            byCategory.put(category, new ArrayList<>());
        }
    }

    public static ModuleManager get() {
        return INSTANCE;
    }

    public void register(Module... toRegister) {
        for (Module module : toRegister) {
            if (find(module.name()).isPresent()) {
                throw new IllegalStateException("Duplicate module name: " + module.name());
            }
            modules.add(module);
            byCategory.get(module.category()).add(module);
        }
    }

    public List<Module> modules() {
        return Collections.unmodifiableList(modules);
    }

    public List<Module> modules(Category category) {
        return Collections.unmodifiableList(byCategory.get(category));
    }

    public Optional<Module> find(String name) {
        return modules.stream().filter(module -> module.name().equalsIgnoreCase(name)).findFirst();
    }

    public <M extends Module> Optional<M> find(Class<M> type) {
        return modules.stream().filter(type::isInstance).map(type::cast).findFirst();
    }

    @EventHandler
    private void onKeyPress(KeyPressEvent event) {
        for (Module module : modules) {
            if (module.keybind().matches(event.key())) {
                module.toggle();
            }
        }
    }
}
