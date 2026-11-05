package cute.bytebuffer.adorance.module;

import cute.bytebuffer.adorance.config.ConfigManager;
import cute.bytebuffer.adorance.event.EventBus;
import cute.bytebuffer.adorance.module.setting.Configurable;
import cute.bytebuffer.adorance.module.setting.KeybindSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public abstract class Module extends Configurable {

    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    private final String description;
    private final Category category;
    private final KeybindSetting keybind;
    private boolean enabled;

    protected Module(String name, String description, Category category) {
        this(name, description, category, KeybindSetting.NONE);
    }

    protected Module(String name, String description, Category category, int defaultKey) {
        super(name);
        this.description = description;
        this.category = category;
        this.keybind = register(new KeybindSetting("Keybind", defaultKey));
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    public void info(String message){
       mc.player.sendMessage(Text.of("[" + this.name() + "] " + message), false);
    }

    public final void toggle() {
        setEnabled(!enabled);
    }

    public final void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }
        this.enabled = enabled;
        if (enabled) {
            EventBus.get().subscribe(this);
            onEnable();
        } else {
            onDisable();
            EventBus.get().unsubscribe(this);
        }
        ConfigManager.markDirty();
    }

    public final boolean isEnabled() {
        return enabled;
    }

    public final String description() {
        return description;
    }

    public final Category category() {
        return category;
    }

    public final KeybindSetting keybind() {
        return keybind;
    }
}
