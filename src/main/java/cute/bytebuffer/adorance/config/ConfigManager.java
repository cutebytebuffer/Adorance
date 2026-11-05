package cute.bytebuffer.adorance.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cute.bytebuffer.adorance.gui.theme.Theme;
import cute.bytebuffer.adorance.gui.ClientSettings;
import cute.bytebuffer.adorance.module.Module;
import cute.bytebuffer.adorance.module.ModuleManager;
import cute.bytebuffer.adorance.module.setting.Configurable;
import cute.bytebuffer.adorance.module.setting.Setting;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class ConfigManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("Adorance/Config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("adorance.json");
    private static final long SAVE_DELAY_MS = 750L;

    private static boolean ready;
    private static boolean applying;
    private static long saveDueAt = -1L;

    private ConfigManager() {
    }

    public static void load() {
        if (Files.exists(FILE)) {
            apply(read());
        }
        ready = true;
        save();
    }

    public static void markDirty() {
        if (ready && !applying) {
            saveDueAt = System.currentTimeMillis() + SAVE_DELAY_MS;
        }
    }

    public static void flush(boolean force) {
        if (saveDueAt >= 0L && (force || System.currentTimeMillis() >= saveDueAt)) {
            save();
        }
    }

    public static void save() {
        if (!ready) {
            return;
        }
        saveDueAt = -1L;
        write(GSON.toJson(snapshot()));
    }

    private static JsonObject snapshot() {
        JsonObject modules = new JsonObject();
        for (Module module : ModuleManager.get().modules()) {
            JsonObject entry = new JsonObject();
            entry.addProperty("enabled", module.isEnabled());
            entry.add("settings", settingsOf(module));
            modules.add(module.name(), entry);
        }
        JsonObject root = new JsonObject();
        root.add("modules", modules);
        root.add("theme", settingsOf(Theme.get()));
        root.add("client", settingsOf(ClientSettings.get()));
        return root;
    }

    private static JsonObject settingsOf(Configurable configurable) {
        JsonObject settings = new JsonObject();
        for (Setting<?> setting : configurable.settings()) {
            settings.add(setting.name(), setting.serialize());
        }
        return settings;
    }

    private static void apply(JsonObject root) {
        applying = true;
        try {
            if (root.has("theme") && root.get("theme").isJsonObject()) {
                applySettings(Theme.get(), root.getAsJsonObject("theme"));
            }
            if (root.has("client") && root.get("client").isJsonObject()) {
                applySettings(ClientSettings.get(), root.getAsJsonObject("client"));
            }
            JsonObject modules = root.has("modules") ? root.getAsJsonObject("modules") : new JsonObject();
            for (Module module : ModuleManager.get().modules()) {
                JsonObject entry = modules.has(module.name()) ? modules.getAsJsonObject(module.name()) : null;
                if (entry == null) {
                    continue;
                }
                if (entry.has("settings") && entry.get("settings").isJsonObject()) {
                    applySettings(module, entry.getAsJsonObject("settings"));
                }
                module.setEnabled(entry.has("enabled") && entry.get("enabled").getAsBoolean());
            }
        } catch (RuntimeException exception) {
            LOGGER.error("Malformed module config {}", FILE, exception);
        } finally {
            applying = false;
        }
    }

    private static void applySettings(Configurable configurable, JsonObject values) {
        for (Setting<?> setting : configurable.settings()) {
            JsonElement value = values.get(setting.name());
            if (value == null) {
                continue;
            }
            try {
                setting.deserialize(value);
            } catch (RuntimeException exception) {
                LOGGER.warn("Ignoring invalid value for {}/{}", configurable.name(), setting.name());
            }
        }
    }

    private static JsonObject read() {
        try {
            return JsonParser.parseString(Files.readString(FILE, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (IOException | RuntimeException exception) {
            LOGGER.error("Unable to read module config {}", FILE, exception);
            return new JsonObject();
        }
    }

    private static void write(String content) {
        try {
            Files.createDirectories(FILE.getParent());
            Path temp = FILE.resolveSibling(FILE.getFileName() + ".tmp");
            Files.writeString(temp, content, StandardCharsets.UTF_8);
            try {
                Files.move(temp, FILE, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException unsupportedAtomicMove) {
                Files.move(temp, FILE, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            LOGGER.error("Unable to write module config {}", FILE, exception);
        }
    }
}
