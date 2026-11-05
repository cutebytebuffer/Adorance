package cute.bytebuffer.adorance.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class StringSetting extends Setting<String> {

    private final int maxLength;
    private final String placeholder;

    public StringSetting(String name, String defaultValue, int maxLength) {
        this(name, defaultValue, maxLength, "");
    }

    public StringSetting(String name, String defaultValue, int maxLength, String placeholder) {
        super(name, truncate(defaultValue, maxLength));
        this.maxLength = maxLength;
        this.placeholder = placeholder;
    }

    private static String truncate(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }

    public int maxLength() {
        return maxLength;
    }

    public String placeholder() {
        return placeholder;
    }

    public boolean isEmpty() {
        return get().isEmpty();
    }

    @Override
    protected String sanitize(String value) {
        return truncate(value, maxLength);
    }

    @Override
    public JsonElement serialize() {
        return new JsonPrimitive(get());
    }

    @Override
    public void deserialize(JsonElement json) {
        set(json.getAsString());
    }
}
